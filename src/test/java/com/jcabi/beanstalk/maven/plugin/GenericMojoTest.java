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

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.maven.plugin.MojoFailureException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link AbstractBeanstalkMojo}.
 * @author Neo Matrix (Neo.matrix@gmail.com)
 * @version $Id$
 */
public final class GenericMojoTest {
    /**
     * {@link AbstractBeanstalkMojo} can execute successfully.
     * @throws Exception If something is wrong.
     * @checkstyle ExecutableStatementCountCheck (40 lines)
     */
    @Test
    public void executesSuccessfully() throws Exception {
        // @checkstyle IllegalTypeCheck (2 lines)
        final AbstractBeanstalkMojo mojo =
            Mockito.mock(AbstractBeanstalkMojo.class);
        final File mockFile = Mockito.mock(File.class);
        final ZipFile mockZipFile = Mockito.mock(ZipFile.class);
        Mockito.when(mockFile.exists()).thenReturn(true);
        Mockito.when(mojo.createZipFile()).thenReturn(mockZipFile);
        Mockito.doCallRealMethod().when(mojo).execute();
        Mockito.doCallRealMethod().when(mojo).getLog();
        Mockito.doCallRealMethod().when(mojo)
            .setWar(Mockito.any(File.class));
        Mockito.doCallRealMethod().when(mojo)
            .setName(Mockito.any(String.class));
        Mockito.doCallRealMethod().when(mojo)
            .setBucket(Mockito.any(String.class));
        Mockito.doCallRealMethod().when(mojo)
            .setKey(Mockito.any(String.class));
        mojo.setWar(mockFile);
        mojo.setName("name");
        mojo.setBucket("bucket");
        mojo.setKey("key");
        mojo.execute();
        Mockito.verify(mojo).createZipFile();
        Mockito.verify(mojo).validate(mockZipFile);
        Mockito.verify(mojo).createServerCredentials();
        Mockito.verify(mockZipFile).close();
        Mockito.verify(mockFile, Mockito.times(2)).exists();
    }

    /**
     * {@link AbstractBeanstalkMojo} can validate bad json.
     * @throws Exception If something is wrong
     */
    @Test
    public void validatesBadJson() throws Exception {
        Assert.assertFalse(
            new DeployMojo().validJson(new StringBuilder()
                .append("[\n")
                .append("id: 102\n")
                .append("name: \"Rudy\"\n")
                .append("colors: [green, yellow]\n")
                .append("]\n").toString()
            )
        );
    }

    /**
     * {@link AbstractBeanstalkMojo} can validate good json.
     * @throws Exception If something is wrong
     */
    @Test
    public void validatesGoodJson() throws Exception {
        Assert.assertTrue(
            new DeployMojo().validJson(new StringBuilder()
                .append("{\n")
                .append("\"id'\": 102,\n")
                .append("\"name\": \"Rudy\",\n")
                .append("\"colors\": [\"green\", \"yellow\"],\n")
                .append("}\n").toString()
            )
        );
    }

    /**
     * {@link AbstractBeanstalkMojo} can validate bad yaml.
     * @throws Exception If something is wrong
     */
    @Test
    public void validatesBadYaml() throws Exception {
        Assert.assertFalse(
            new DeployMojo().validYaml(new StringBuilder()
                .append("Some illegal Prefix\n")
                .append("Time: 2005-11-23 10:01:42 -5\n")
                .append("Admin: ed\n")
                .append("Messages:\n")
                .append(" Hello is an error information\n")
                .append(" for the configuration file\n").toString()
            )
        );
    }

    /**
     * {@link AbstractBeanstalkMojo} can validate good yaml.
     * @throws Exception If something is wrong
     */
    @Test
    public void validatesGoodYaml() throws Exception {
        Assert.assertTrue(
            new DeployMojo().validYaml(new StringBuilder()
                .append("Time: 2001-11-23 15:01:42 -5\n")
                .append("User: ed\n")
                .append("Warning:\n")
                .append("  This is an error message\n")
                .append("  for the log file\n").toString()
            )
        );
    }

    /**
     * {@link AbstractBeanstalkMojo} can throw an exception on execute, if there
     * is no .ebextensions directory in the WAR file.
     * @throws Exception Thrown in case of error.
     */
    @Test
    public void throwsExceptionWhenNoEbextensionsDirInZip() throws Exception {
        // @checkstyle IllegalTypeCheck (2 lines)
        final AbstractBeanstalkMojo mojo =
            Mockito.mock(AbstractBeanstalkMojo.class);
        final File mockFile = Mockito.mock(File.class);
        final ZipFile mockZipFile = Mockito.mock(ZipFile.class);
        Mockito.when(mockFile.exists()).thenReturn(true);
        Mockito.when(mojo.createZipFile()).thenReturn(mockZipFile);
        Mockito.doCallRealMethod().when(mojo).execute();
        Mockito.doCallRealMethod().when(mojo)
            .setWar(Mockito.any(File.class));
        Mockito.doCallRealMethod().when(mojo)
            .validate(Mockito.any(ZipFile.class));
        mojo.setWar(mockFile);
        Mockito.when(mockZipFile.getEntry(".ebextensions")).thenReturn(null);
        try {
            mojo.execute();
            Assert.fail("Expect MojoFailureException to be thrown");
        } catch (final MojoFailureException exception) {
            MatcherAssert.assertThat(
                exception.getMessage(),
                Matchers.equalTo(
                    ".ebextensions directory does not exist in the WAR file"
                )
            );
        }
    }

    /**
     * {@link AbstractBeanstalkMojo} can throw an exception on execute, if the
     * .ebextensions directory is empty.
     * @throws Exception Thrown in case of error.
     */
    @Test
    public void throwsExceptionWhenNoConfigFilesInEbextensionsDir()
        throws Exception {
        // @checkstyle IllegalTypeCheck (2 lines)
        final AbstractBeanstalkMojo mojo =
            Mockito.mock(AbstractBeanstalkMojo.class);
        Mockito.doCallRealMethod().when(mojo).execute();
        Mockito.doCallRealMethod().when(mojo).createZipFile();
        Mockito.doCallRealMethod().when(mojo)
            .setWar(Mockito.any(File.class));
        Mockito.doCallRealMethod().when(mojo)
            .validate(Mockito.any(ZipFile.class));
        final File temp = File.createTempFile("test", ".zip");
        final FileOutputStream fos = new FileOutputStream(temp);
        final ZipOutputStream out = new ZipOutputStream(fos);
        out.putNextEntry(new ZipEntry(".ebextensions/"));
        out.flush();
        out.close();
        fos.flush();
        fos.close();
        mojo.setWar(temp);
        try {
            mojo.execute();
        } catch (final MojoFailureException exception) {
            MatcherAssert.assertThat(
                exception.getMessage(),
                Matchers.equalTo(
                    ".ebextensions contains no config files."
                )
            );
        }
    }
}
