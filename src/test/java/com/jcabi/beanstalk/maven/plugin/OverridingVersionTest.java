/*
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
import com.amazonaws.services.elasticbeanstalk.model.S3Location;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link OverridingVersion}.
 *
 * @since 0.3
 */
final class OverridingVersionTest {

    @Test
    void overridesVersionInEbt() {
        final String key = "some-bundle-key";
        final Bundle bundle = Mockito.mock(Bundle.class);
        Mockito.doReturn(key).when(bundle).name();
        final S3Location location = new S3Location("deploy-bucket", key);
        Mockito.doReturn(location).when(bundle).location();
        Mockito.doReturn("bundle-etag").when(bundle).etag();
        final AWSElasticBeanstalk ebt = Mockito.mock(AWSElasticBeanstalk.class);
        Mockito.when(
            ebt.describeApplicationVersions(
                Mockito.any(DescribeApplicationVersionsRequest.class)
            )
        ).thenReturn(new DescribeApplicationVersionsResult());
        Mockito.when(
            ebt.createApplicationVersion(
                Mockito.any(CreateApplicationVersionRequest.class)
            )
        ).thenReturn(
            new CreateApplicationVersionResult().withApplicationVersion(
                new ApplicationVersionDescription()
                    .withVersionLabel(key)
            )
        );
        MatcherAssert.assertThat(
            "the label cannot differ from the name of the bundle",
            new OverridingVersion(ebt, "some-app", bundle).label(),
            Matchers.equalTo(key)
        );
        Mockito.verify(ebt).createApplicationVersion(
            new CreateApplicationVersionRequest()
                .withApplicationName("some-app")
                .withVersionLabel(key)
                .withSourceBundle(location)
                .withDescription("bundle-etag")
        );
    }
}
