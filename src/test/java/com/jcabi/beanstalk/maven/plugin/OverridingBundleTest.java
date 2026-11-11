/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;
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
    void uploadsWhenOnlyLongerKeyExists(@TempDir final Path temp) throws Exception {
        final String bucket = "prefix-bucket";
        final String key = "app.war";
        final File war = Files.write(
            temp.resolve(key), "new bundle".getBytes(StandardCharsets.UTF_8)
        ).toFile();
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        final S3ObjectSummary summary = new S3ObjectSummary();
        summary.setKey("app.war.backup");
        final ObjectListing listing = new ObjectListing();
        listing.getObjectSummaries().add(summary);
        Mockito.doReturn(listing)
            .when(client).listObjects(Mockito.any(ListObjectsRequest.class));
        final AmazonS3Exception missing = new AmazonS3Exception("No such key");
        missing.setStatusCode(404);
        Mockito.doThrow(missing).when(client).getObjectMetadata(bucket, key);
        Mockito.doReturn(new PutObjectResult())
            .when(client).putObject(bucket, key, war);
        MatcherAssert.assertThat(
            "the absent exact key must be uploaded",
            new OverridingBundle(client, bucket, key, war).location().getS3Key(),
            Matchers.equalTo(key)
        );
        Mockito.verify(client).putObject(bucket, key, war);
        Mockito.verify(client, Mockito.never()).getObjectMetadata(bucket, key);
    }

    @Test
    void reusesExactKeyWithMatchingEtag(@TempDir final Path temp) throws Exception {
        final String bucket = "reuse-bucket";
        final String key = "existing.war";
        final File war = Files.write(
            temp.resolve(key), "same bundle".getBytes(StandardCharsets.UTF_8)
        ).toFile();
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        final Bundle bundle = new OverridingBundle(client, bucket, key, war);
        final S3ObjectSummary summary = new S3ObjectSummary();
        summary.setKey(key);
        final ObjectListing listing = new ObjectListing();
        listing.getObjectSummaries().add(summary);
        Mockito.doReturn(listing)
            .when(client).listObjects(Mockito.any(ListObjectsRequest.class));
        final ObjectMetadata metadata = Mockito.mock(ObjectMetadata.class);
        Mockito.doReturn(bundle.etag()).when(metadata).getETag();
        Mockito.doReturn(metadata).when(client).getObjectMetadata(bucket, key);
        MatcherAssert.assertThat(
            "the matching exact key must be reused",
            bundle.location().getS3Key(), Matchers.equalTo(key)
        );
        Mockito.verify(client).getObjectMetadata(bucket, key);
        Mockito.verify(client, Mockito.never()).putObject(bucket, key, war);
    }

    @Test
    void replacesExactKeyWithDifferentEtag(@TempDir final Path temp) throws Exception {
        final String bucket = "replace-bucket";
        final String key = "outdated.war";
        final File war = Files.write(
            temp.resolve(key), "updated bundle".getBytes(StandardCharsets.UTF_8)
        ).toFile();
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        final S3ObjectSummary summary = new S3ObjectSummary();
        summary.setKey(key);
        final ObjectListing listing = new ObjectListing();
        listing.getObjectSummaries().add(summary);
        Mockito.doReturn(listing)
            .when(client).listObjects(Mockito.any(ListObjectsRequest.class));
        final ObjectMetadata metadata = Mockito.mock(ObjectMetadata.class);
        Mockito.doReturn("old-etag").when(metadata).getETag();
        Mockito.doReturn(metadata).when(client).getObjectMetadata(bucket, key);
        Mockito.doReturn(new PutObjectResult())
            .when(client).putObject(bucket, key, war);
        MatcherAssert.assertThat(
            "the updated file must retain the exact deployment key",
            new OverridingBundle(client, bucket, key, war).location().getS3Key(),
            Matchers.equalTo(key)
        );
        Mockito.verify(client).putObject(bucket, key, war);
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
