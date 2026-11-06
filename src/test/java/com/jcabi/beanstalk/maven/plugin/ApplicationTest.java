/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest;
import com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult;
import com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest;
import com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;
import com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest;
import com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Test case for {@link Application}.
 *
 * @since 0.3
 */
final class ApplicationTest {

    @Test
    void createsNewEnvironment() {
        final String name = "some-app-name";
        final AWSElasticBeanstalk ebt = ApplicationTest.beanstalk(name);
        final Application app = new Application(ebt, name);
        final Version version = Mockito.mock(Version.class);
        Mockito.doReturn("uploaded-version").when(version).label();
        app.clean(false);
        MatcherAssert.assertThat(
            "candidate environment cannot be absent",
            app.candidate(version, "some-template"),
            Matchers.notNullValue()
        );
        Mockito.verify(ebt).createEnvironment(
            Mockito.argThat(
                request -> name.equals(request.getApplicationName())
                    && "uploaded-version".equals(request.getVersionLabel())
                    && "some-template".equals(request.getTemplateName())
                    && name.equals(request.getCNAMEPrefix())
            )
        );
    }

    @Test
    void pausesBeforeRetryingOccupiedCname() {
        final String name = "released-app";
        final AWSElasticBeanstalk ebt = ApplicationTest.beanstalk(name);
        Mockito.when(
            ebt.checkDNSAvailability(Mockito.any(CheckDNSAvailabilityRequest.class))
        ).thenReturn(
            new CheckDNSAvailabilityResult().withAvailable(false),
            new CheckDNSAvailabilityResult().withAvailable(true)
        );
        final long started = System.nanoTime();
        new Application(ebt, name).candidate(Mockito.mock(Version.class), "template");
        MatcherAssert.assertThat(
            "occupied CNAME must be polled no sooner than five seconds later",
            System.nanoTime() - started,
            Matchers.greaterThanOrEqualTo(TimeUnit.SECONDS.toNanos(5L))
        );
        Mockito.verify(ebt, Mockito.times(2)).checkDNSAvailability(
            Mockito.any(CheckDNSAvailabilityRequest.class)
        );
        Mockito.verify(ebt).createEnvironment(
            Mockito.argThat(request -> name.equals(request.getCNAMEPrefix()))
        );
    }

    @Test
    void timesOutWaitingForOccupiedCname() {
        final String name = "occupied-app";
        final AWSElasticBeanstalk ebt = ApplicationTest.beanstalk(name);
        Mockito.when(
            ebt.checkDNSAvailability(Mockito.any(CheckDNSAvailabilityRequest.class))
        ).thenReturn(new CheckDNSAvailabilityResult().withAvailable(false))
            .thenThrow(new IllegalStateException("Unexpected extra DNS poll"));
        final Clock clock = Mockito.mock(Clock.class);
        Mockito.when(clock.millis()).thenReturn(0L, TimeUnit.MINUTES.toMillis(10L));
        final MockedStatic<Clock> clocks = Mockito.mockStatic(
            Clock.class, invocation -> clock
        );
        try (clocks) {
            MatcherAssert.assertThat(
                "timeout must identify the occupied CNAME",
                Assertions.assertThrows(
                    DeploymentException.class,
                    () -> new Application(ebt, name).candidate(
                        Mockito.mock(Version.class), "template"
                    )
                ).getMessage(),
                Matchers.containsString(String.format("Timed out waiting for '%s'", name))
            );
        }
        Mockito.verify(ebt, Mockito.never()).createEnvironment(
            Mockito.any(CreateEnvironmentRequest.class)
        );
    }

    @Test
    void preservesInterruptionWhileWaitingForCname() {
        final String name = "interrupted-app";
        final AWSElasticBeanstalk ebt = ApplicationTest.beanstalk(name);
        Mockito.when(
            ebt.checkDNSAvailability(Mockito.any(CheckDNSAvailabilityRequest.class))
        ).thenReturn(new CheckDNSAvailabilityResult().withAvailable(false))
            .thenThrow(new IllegalStateException("Retry continued after interrupt"));
        Thread.currentThread().interrupt();
        try {
            MatcherAssert.assertThat(
                "interruption must retain its cause and remain visible to the caller",
                Assertions.assertThrows(
                    DeploymentException.class,
                    () -> new Application(ebt, name).candidate(
                        Mockito.mock(Version.class), "template"
                    )
                ).getCause() instanceof InterruptedException
                    && Thread.currentThread().isInterrupted(),
                Matchers.is(true)
            );
        } finally {
            Thread.interrupted();
        }
        Mockito.verify(ebt, Mockito.never()).createEnvironment(
            Mockito.any(CreateEnvironmentRequest.class)
        );
    }

    @Test
    void usesAlternateCnameForGreenPrimary() {
        final String name = "green-app";
        final AWSElasticBeanstalk ebt = ApplicationTest.beanstalk(name);
        Mockito.when(
            ebt.checkDNSAvailability(Mockito.any(CheckDNSAvailabilityRequest.class))
        ).thenAnswer(
            invocation -> new CheckDNSAvailabilityResult().withAvailable(
                !name.equals(
                    invocation.<CheckDNSAvailabilityRequest>getArgument(0)
                        .getCNAMEPrefix()
                )
            )
        );
        Mockito.when(
            ebt.describeEnvironments(Mockito.any(DescribeEnvironmentsRequest.class))
        ).thenReturn(
            new DescribeEnvironmentsResult().withEnvironments(
                new EnvironmentDescription()
                    .withApplicationName(name)
                    .withCNAME(String.format("%s.elasticbeanstalk.com", name))
                    .withEnvironmentName("primary-env")
                    .withEnvironmentId("primary-id")
                    .withStatus("Ready")
                    .withHealth("Green")
            )
        );
        new Application(ebt, name).candidate(Mockito.mock(Version.class), "template");
        Mockito.verify(ebt).createEnvironment(
            Mockito.argThat(request -> !name.equals(request.getCNAMEPrefix()))
        );
    }

    @Test
    void deploysAndReversesWithLiveAccount(@TempDir final Path temp)
        throws Exception {
        Assumptions.assumeTrue(System.getProperty("aws.key") != null);
        final AWSCredentials creds = new BasicAWSCredentials(
            System.getProperty("aws.key"),
            System.getProperty("aws.secret")
        );
        final AWSElasticBeanstalk ebt = new AwsClients(creds).beanstalk();
        final String name = "netbout";
        final File war = Files.write(
            temp.resolve("temp.war"),
            "broken JAR file content".getBytes(StandardCharsets.UTF_8)
        ).toFile();
        final Environment candidate = new Application(ebt, name).candidate(
            new OverridingVersion(
                ebt,
                name,
                new OverridingBundle(
                    new AwsClients(creds).storage(),
                    "webapps.netbout.com",
                    war.getName(),
                    war
                )
            ),
            name
        );
        candidate.terminate();
        MatcherAssert.assertThat(
            String.format(
                "environment %s cannot survive termination", candidate
            ),
            candidate.terminated(),
            Matchers.is(true)
        );
    }

    private static AWSElasticBeanstalk beanstalk(final String name) {
        final AWSElasticBeanstalk ebt = Mockito.mock(AWSElasticBeanstalk.class);
        Mockito.when(
            ebt.checkDNSAvailability(
                Mockito.any(CheckDNSAvailabilityRequest.class)
            )
        ).thenReturn(new CheckDNSAvailabilityResult().withAvailable(true));
        Mockito.when(
            ebt.createEnvironment(
                Mockito.any(CreateEnvironmentRequest.class)
            )
        ).thenReturn(
            new CreateEnvironmentResult()
                .withApplicationName(name)
                .withEnvironmentId("f4g5h6j7")
                .withEnvironmentName(name)
        );
        Mockito.when(
            ebt.describeEnvironments(
                Mockito.any(DescribeEnvironmentsRequest.class)
            )
        ).thenReturn(
            new DescribeEnvironmentsResult().withEnvironments(
                Collections.singletonList(
                    new EnvironmentDescription()
                        .withCNAME("")
                        .withEnvironmentName("some-env")
                        .withEnvironmentId("a1b2c3d4")
                        .withStatus("Ready")
                )
            )
        );
        Mockito.when(
            ebt.terminateEnvironment(
                Mockito.any(TerminateEnvironmentRequest.class)
            )
        ).thenReturn(new TerminateEnvironmentResult());
        return ebt;
    }
}
