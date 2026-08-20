/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest;
import com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult;
import com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest;
import com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;
import com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest;
import com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult;
import com.amazonaws.services.s3.AmazonS3Client;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

/**
 * Test case for {@link Application}.
 * @since 0.3
 */
final class ApplicationTest {

    @Test
    void createsNewEnvironment() {
        final String name = "some-app-name";
        final Application app = new Application(
            ApplicationTest.beanstalk(name), name
        );
        app.clean(false);
        MatcherAssert.assertThat(
            "candidate environment cannot be absent",
            app.candidate(Mockito.mock(Version.class), "some-template"),
            Matchers.notNullValue()
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
        final AWSElasticBeanstalk ebt = new AWSElasticBeanstalkClient(creds);
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
                    new AmazonS3Client(creds),
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
