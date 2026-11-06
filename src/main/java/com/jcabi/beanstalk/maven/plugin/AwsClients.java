/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.handlers.RequestHandler2;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

/**
 * SDK clients with the plugin's original credentials and endpoints.
 *
 * @since 1.0
 */
final class AwsClients {

    /**
     * Maven server credentials.
     */
    private final AWSCredentials credentials;

    /**
     * New clients for these credentials.
     *
     * @param creds Maven server credentials
     */
    AwsClients(final AWSCredentials creds) {
        this.credentials = creds;
    }

    /**
     * Beanstalk client at the original US East endpoint.
     *
     * @param handlers Request observers
     * @return Beanstalk client
     */
    AWSElasticBeanstalk beanstalk(final RequestHandler2... handlers) {
        return AWSElasticBeanstalkClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(this.credentials))
            .withRequestHandlers(handlers).withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(
                    "https://elasticbeanstalk.us-east-1.amazonaws.com", "us-east-1"
                )
            ).build();
    }

    /**
     * S3 client at the original global endpoint.
     *
     * @param handlers Request observers
     * @return S3 client
     */
    AmazonS3 storage(final RequestHandler2... handlers) {
        return AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(this.credentials))
            .withRequestHandlers(handlers).withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(
                    "https://s3.amazonaws.com", "us-east-1"
                )
            ).build();
    }
}
