/**
 * Copyright (c) 2012-2017, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
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
