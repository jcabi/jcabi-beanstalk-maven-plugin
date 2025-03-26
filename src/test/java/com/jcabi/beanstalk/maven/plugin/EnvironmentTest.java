/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription;
import com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;
import com.jcabi.log.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * Test case for {@link Environment}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class EnvironmentTest {

    /**
     * AWS key, if provided in command line.
     */
    private static final String AWS_KEY = System.getProperty("aws.key");

    /**
     * AWS secret, if provided in command line.
     */
    private static final String AWS_SECRET = System.getProperty("aws.secret");

    /**
     * Configure logging.
     */
    @BeforeClass
    public static void initLog() {
        StaticLoggerBinder.getSingleton().setMavenLog(new SystemStreamLog());
    }

    /**
     * Environment can check readiness of environment.
     * @throws Exception If something is wrong
     */
    @Test
    public void checksReadinessOfEnvironment() throws Exception {
        final String eid = "some-env-id";
        final AWSElasticBeanstalk ebt = Mockito.mock(AWSElasticBeanstalk.class);
        Mockito.doReturn(
            new DescribeConfigurationSettingsResult().withConfigurationSettings(
                new ArrayList<ConfigurationSettingsDescription>(0)
            )
        ).when(ebt)
            .describeConfigurationSettings(
                Mockito.any(DescribeConfigurationSettingsRequest.class)
            );
        Mockito.doReturn(
            new DescribeEnvironmentsResult().withEnvironments(
                Arrays.asList(
                    new EnvironmentDescription()
                        .withStatus("Ready")
                        .withHealth("Red")
                )
            )
        ).when(ebt)
            .describeEnvironments(
                Mockito.any(DescribeEnvironmentsRequest.class)
            );
        final Environment env = new Environment(ebt, eid);
        MatcherAssert.assertThat(
            env.green(),
            Matchers.equalTo(false)
        );
    }

    /**
     * Environment can fetch TAIL report from live environment.
     * @throws Exception If something is wrong
     */
    @Test
    public void fetchesTailReportFromLiveEnvironment() throws Exception {
        Assume.assumeThat(EnvironmentTest.AWS_KEY, Matchers.notNullValue());
        final AWSCredentials creds = new BasicAWSCredentials(
            EnvironmentTest.AWS_KEY,
            EnvironmentTest.AWS_SECRET
        );
        final AWSElasticBeanstalk ebt = new AWSElasticBeanstalkClient(creds);
        final Environment env = new Environment(ebt, "e-2n2mqauqae");
        Logger.info(this, "tail report:\n%s", env.tail());
    }

    /**
     * Environment can collect events from running environment.
     * @throws Exception If something is wrong
     */
    @Test
    public void collectsEventsFromLiveEnvironment() throws Exception {
        Assume.assumeThat(EnvironmentTest.AWS_KEY, Matchers.notNullValue());
        final AWSCredentials creds = new BasicAWSCredentials(
            EnvironmentTest.AWS_KEY,
            EnvironmentTest.AWS_SECRET
        );
        final AWSElasticBeanstalk ebt = new AWSElasticBeanstalkClient(creds);
        final Environment env = new Environment(ebt, "e-nxmcbf3pvk");
        Logger.info(
            this,
            "events: %[list]s",
            Arrays.asList(env.events())
        );
    }

}
