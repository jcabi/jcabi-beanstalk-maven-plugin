/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectResult;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

/**
 * Test case for {@link OverridingBundle}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 */
public final class OverridingBundleTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * OverridingBundle can override a file in AWS S3.
     * @throws Exception If something is wrong
     */
    @Test
    public void overridesFileInAws() throws Exception {
        final String bucket = "some-bucket";
        final String key = "some-key";
        final File war = this.temp.newFile("temp.war");
        FileUtils.writeStringToFile(war, "broken JAR file content");
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        Mockito.doReturn(new PutObjectResult())
            .when(client).putObject(bucket, key, war);
        Mockito.doReturn(new ObjectListing())
            .when(client).listObjects(Mockito.any(ListObjectsRequest.class));
        final Bundle bundle = new OverridingBundle(client, bucket, key, war);
        MatcherAssert.assertThat(
            bundle.name(),
            Matchers.equalTo(key)
        );
        MatcherAssert.assertThat(
            bundle.location().getS3Key(),
            Matchers.equalTo(key)
        );
    }

    /**
     * OverridingBundle caches result of location() method.
     * @throws Exception If something is wrong
     */
    @Test
    public void cachesResultOfLocation() throws Exception {
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        final File war = this.temp.newFile("temp1.war");
        FileUtils.writeStringToFile(war, "some JAR file content");
        final String bucket = "some-bucket-for-cache";
        final String key = "some-key-for-cache";
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
