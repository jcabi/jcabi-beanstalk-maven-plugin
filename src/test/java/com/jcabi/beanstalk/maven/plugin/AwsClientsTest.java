/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import com.amazonaws.Request;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.handlers.HandlerContextKey;
import com.amazonaws.handlers.RequestHandler2;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.s3.AmazonS3;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link AwsClients} using real SDK request marshalling.
 *
 * @since 1.0
 */
final class AwsClientsTest {

    @Test
    void keepsBeanstalkEndpointAndCredentials() {
        final AWSCredentials credentials = new BasicAWSCredentials("key", "secret");
        final AWSElasticBeanstalk client = new AwsClients(credentials).beanstalk(
            AwsClientsTest.capture(
                credentials, "https://elasticbeanstalk.us-east-1.amazonaws.com"
            )
        );
        try {
            MatcherAssert.assertThat(
                "the Beanstalk request must reach the observer",
                Assertions.assertThrows(
                    SdkClientException.class, client::describeApplications,
                    "the request must stop before contacting AWS"
                ).getMessage(),
                Matchers.equalTo("Request inspected; network disabled")
            );
        } finally {
            client.shutdown();
        }
    }

    @Test
    void keepsGlobalStorageEndpointAndCredentials() {
        final AWSCredentials credentials = new BasicAWSCredentials("s3key", "s3secret");
        final AmazonS3 client = new AwsClients(credentials).storage(
            AwsClientsTest.capture(
                credentials, "https://artifacts.example.com.s3.amazonaws.com"
            )
        );
        try {
            MatcherAssert.assertThat(
                "the S3 request must reach the observer",
                Assertions.assertThrows(
                    SdkClientException.class,
                    () -> client.listObjects("artifacts.example.com"),
                    "the request must stop before contacting S3"
                ).getMessage(),
                Matchers.equalTo("Request inspected; network disabled")
            );
        } finally {
            client.shutdown();
        }
    }

    private static RequestHandler2 capture(final AWSCredentials credentials,
        final String endpoint) {
        return new RequestHandler2() {
            @Override
            public void beforeRequest(final Request<?> request) {
                MatcherAssert.assertThat(
                    "the SDK must retain the legacy endpoint",
                    request.getEndpoint().toString(), Matchers.equalTo(endpoint)
                );
                MatcherAssert.assertThat(
                    "the SDK must use the supplied Maven credentials",
                    request.getHandlerContext(HandlerContextKey.AWS_CREDENTIALS),
                    Matchers.sameInstance(credentials)
                );
                throw new SdkClientException("Request inspected; network disabled");
            }
        };
    }
}
