/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;
import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link Environment}.
 * @since 0.3
 */
final class EnvironmentTest {

    @Test
    void checksReadinessOfEnvironment() {
        final AWSElasticBeanstalk ebt = Mockito.mock(AWSElasticBeanstalk.class);
        Mockito.when(
            ebt.describeEnvironments(
                Mockito.any(DescribeEnvironmentsRequest.class)
            )
        ).thenReturn(
            new DescribeEnvironmentsResult().withEnvironments(
                Collections.singletonList(
                    new EnvironmentDescription()
                        .withStatus("Ready")
                        .withHealth("Red")
                )
            )
        );
        MatcherAssert.assertThat(
            "red environment cannot be green",
            new Environment(ebt, "some-env-id").green(),
            Matchers.is(false)
        );
    }

    @Test
    void fetchesTailReportFromLiveEnvironment() {
        Assumptions.assumeTrue(System.getProperty("aws.key") != null);
        MatcherAssert.assertThat(
            "live environment cannot report an empty TAIL",
            new Environment(
                new AWSElasticBeanstalkClient(
                    new BasicAWSCredentials(
                        System.getProperty("aws.key"),
                        System.getProperty("aws.secret")
                    )
                ),
                "e-2n2mqauqae"
            ).tail(),
            Matchers.not(Matchers.emptyString())
        );
    }

    @Test
    void collectsEventsFromLiveEnvironment() {
        Assumptions.assumeTrue(System.getProperty("aws.key") != null);
        MatcherAssert.assertThat(
            "live environment cannot report no events",
            new Environment(
                new AWSElasticBeanstalkClient(
                    new BasicAWSCredentials(
                        System.getProperty("aws.key"),
                        System.getProperty("aws.secret")
                    )
                ),
                "e-nxmcbf3pvk"
            ).events(),
            Matchers.not(Matchers.emptyArray())
        );
    }
}
