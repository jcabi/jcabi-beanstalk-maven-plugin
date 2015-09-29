/**
 * Copyright (c) 2012-2015, jcabi.com
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
import java.util.zip.ZipFile;
import org.junit.Test;
import org.mockito.Mockito;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * Test case for {@link AbstractBeanstalkMojo}.
 * @author Neo Matrix (Neo.matrix@gmail.com)
 * @version $Id$
 * @checkstyle MultipleStringLiteralsCheck (10 lines)
 */
public final class GenericMojoTest {
    /**
     * Test {@link AbstractBeanstalkMojo#execute()} method.
     * @throws Exception If something is wrong.
     * @checkstyle ExecutableStatementCountCheck (40 lines)
     */
    @Test
    public void testExecute() throws Exception {
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
        Mockito.verify(mojo).createWarFile(mockZipFile);
        Mockito.verify(mojo).validateWarFile(Mockito.any(WarFile.class));
        Mockito.verify(mojo).createServerCredentials();
        Mockito.verify(mockZipFile).close();
        Mockito.verify(mockFile, Mockito.times(2)).exists();
    }

    /**
     * Test validating an incorrect yaml format.
     * @throws Exception If something is wrong
     */
    @Test(expected = YAMLException.class)
    public void testInvalidYaml() throws Exception {
        // @checkstyle StringLiteralsConcatenationCheck (6 lines)
        final String invalid = "Some illegal Prefix\n"
                + "Time: 2005-11-23 10:01:42 -5\n"
                + "Admin: ed\n"
                + "Messages:\n"
                + "  Hello is an error information\n"
                + "  for the configuration file\n";
        final DeployMojo mojo = new DeployMojo();
        mojo.validYaml(invalid);
    }

    /**
     * Test validating an correct yaml format.
     * @throws Exception If something is wrong
     */
    @Test
    public void testValidYaml() throws Exception {
        // @checkstyle StringLiteralsConcatenationCheck (6 lines)
        final String valid =
                "Time: 2001-11-23 15:01:42 -5\n"
                        + "User: ed\n"
                        + "Warning:\n"
                        + "  This is an error message\n"
                        + "  for the log file\n";
        final DeployMojo mojo = new DeployMojo();
        mojo.validYaml(valid);
    }
}
