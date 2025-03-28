/**
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
import com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription;
import com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest;
import com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult;
import com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;
import com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest;
import com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult;
import com.amazonaws.services.s3.AmazonS3Client;
import com.jcabi.log.Logger;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

/**
 * Test case for {@link Application}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.ExcessiveImports")
public final class ApplicationTest {

    /**
     * AWS key, if provided in command line.
     */
    private static final String AWS_KEY = System.getProperty("aws.key");

    /**
     * AWS secret, if provided in command line.
     */
    private static final String AWS_SECRET = System.getProperty("aws.secret");

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * Application can create a new environment.
     * @throws Exception If something is wrong
     */
    @Test
    public void createsNewEnvironment() throws Exception {
        final String name = "some-app-name";
        final String template = "some-template";
        final Version version = Mockito.mock(Version.class);
        final AWSElasticBeanstalk ebt = Mockito.mock(AWSElasticBeanstalk.class);
        Mockito.doReturn(
            new CheckDNSAvailabilityResult().withAvailable(true)
        ).when(ebt)
            .checkDNSAvailability(
                Mockito.any(CheckDNSAvailabilityRequest.class)
            );
        Mockito.doReturn(
            new CreateEnvironmentResult()
                .withApplicationName(name)
                .withEnvironmentId("f4g5h6j7")
                .withEnvironmentName(name)
        ).when(ebt)
            .createEnvironment(
                Mockito.any(CreateEnvironmentRequest.class)
            );
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
                        .withCNAME("")
                        .withEnvironmentName("some-env")
                        .withEnvironmentId("a1b2c3d4")
                        .withStatus("Ready")
                )
            )
        ).when(ebt)
            .describeEnvironments(
                Mockito.any(DescribeEnvironmentsRequest.class)
            );
        Mockito.doReturn(new TerminateEnvironmentResult())
            .when(ebt)
            .terminateEnvironment(
                Mockito.any(TerminateEnvironmentRequest.class)
            );
        final Application app = new Application(ebt, name);
        app.clean(false);
        MatcherAssert.assertThat(
            app.candidate(version, template),
            Matchers.notNullValue()
        );
    }

    /**
     * Environment can deploy and reverse with a broken WAR file. This test
     * has to be executed only if you have full access to AWS S3 bucket, and
     * AWS EBT for deployment. The test runs full cycle of deployment and then
     * destroying of a new environment. It won't hurt anything, but will
     * consume some EBT resources. Be careful.
     *
     * @throws Exception If something is wrong
     */
    @Test
    public void deploysAndReversesWithLiveAccount() throws Exception {
        Assume.assumeThat(ApplicationTest.AWS_KEY, Matchers.notNullValue());
        final AWSCredentials creds = new BasicAWSCredentials(
            ApplicationTest.AWS_KEY,
            ApplicationTest.AWS_SECRET
        );
        final AWSElasticBeanstalk ebt = new AWSElasticBeanstalkClient(creds);
        final String name = "netbout";
        final Application app = new Application(ebt, name);
        final File war = this.temp.newFile("temp.war");
        FileUtils.writeStringToFile(war, "broken JAR file content");
        final Environment candidate = app.candidate(
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
        MatcherAssert.assertThat(candidate.green(), Matchers.equalTo(false));
        Logger.info(this, "tail report:\n%s", candidate.tail());
        candidate.terminate();
    }

}
