/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription;
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest;
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult;
import com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link OverridingVersion}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 */
public final class OverridingVersionTest {

    /**
     * OverridingVersion can override a version in AWS EBT.
     * @throws Exception If something is wrong
     */
    @Test
    public void overridesVersionInEbt() throws Exception {
        final String app = "some-app";
        final String key = "some-bundle-key";
        final Bundle bundle = Mockito.mock(Bundle.class);
        Mockito.doReturn(key).when(bundle).name();
        final AWSElasticBeanstalk ebt = Mockito.mock(AWSElasticBeanstalk.class);
        Mockito.doReturn(new DescribeApplicationVersionsResult())
            .when(ebt).describeApplicationVersions(
                Mockito.any(DescribeApplicationVersionsRequest.class)
            );
        Mockito.doReturn(
            new CreateApplicationVersionResult()
                .withApplicationVersion(
                    new ApplicationVersionDescription()
                        .withVersionLabel(key)
            )
        ).when(ebt)
            .createApplicationVersion(
                Mockito.any(CreateApplicationVersionRequest.class)
            );
        final Version version = new OverridingVersion(ebt, app, bundle);
        MatcherAssert.assertThat(
            version.label(),
            Matchers.equalTo(key)
        );
    }

}
