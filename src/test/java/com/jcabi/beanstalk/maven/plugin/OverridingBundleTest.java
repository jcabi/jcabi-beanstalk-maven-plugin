/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectResult;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

/**
 * Test case for {@link OverridingBundle}.
 *
 * @since 0.3
 */
final class OverridingBundleTest {

    @Test
    void overridesFileInAws(@TempDir final Path temp) throws Exception {
        final String bucket = "some-bucket";
        final String key = "some-key";
        final File war = Files.write(
            temp.resolve("temp.war"),
            "broken JAR file content".getBytes(StandardCharsets.UTF_8)
        ).toFile();
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        Mockito.doReturn(new PutObjectResult())
            .when(client).putObject(bucket, key, war);
        Mockito.doReturn(new ObjectListing())
            .when(client).listObjects(Mockito.any(ListObjectsRequest.class));
        final Bundle bundle = new OverridingBundle(client, bucket, key, war);
        Assertions.assertAll(
            () -> MatcherAssert.assertThat(
                "the name cannot differ from the S3 key",
                bundle.name(),
                Matchers.equalTo(key)
            ),
            () -> MatcherAssert.assertThat(
                "the location cannot point to another S3 key",
                bundle.location().getS3Key(),
                Matchers.equalTo(key)
            )
        );
    }

    @Test
    void cachesResultOfLocation(@TempDir final Path temp) throws Exception {
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        final String bucket = "some-bucket-for-cache";
        final String key = "some-key-for-cache";
        final File war = Files.write(
            temp.resolve("temp1.war"),
            "some JAR file content".getBytes(StandardCharsets.UTF_8)
        ).toFile();
        Mockito.doReturn(new PutObjectResult())
            .when(client).putObject(bucket, key, war);
        Mockito.doReturn(new ObjectListing())
            .when(client).listObjects(Mockito.any(ListObjectsRequest.class));
        final Bundle bundle = new OverridingBundle(client, bucket, key, war);
        bundle.location();
        bundle.location();
        Mockito.verify(client, Mockito.times(1)).putObject(bucket, key, war);
    }

}
