/**
 * Copyright (c) 2012-2014, jcabi.com
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

import com.google.common.base.Joiner;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link DeployMojo} (more detailed test is in maven invoker).
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class DeployMojoTest {

    /**
     * DeployMojo can skip execution when flag is set.
     * @throws Exception If something is wrong
     */
    @Test
    public void skipsExecutionWhenRequired() throws Exception {
        final DeployMojo mojo = new DeployMojo();
        mojo.setSkip(true);
        mojo.execute();
    }
    /**
     * Verifies that execute calls checkEbextensionsValidity method.
     * @throws org.apache.maven.plugin.MojoFailureException Thrown in case
     * of error.
     */
    @Test
    public void executeCallscheckEbextensionsValidity()
        throws MojoFailureException {
        final BeanstalkMojoForTesting mojo = Mockito.spy(
            new BeanstalkMojoForTesting()
        );
        mojo.setSkip(false);
        final File war = Mockito.mock(File.class);
        Mockito.when(war.exists()).thenReturn(true);
        mojo.setWar(war);
        Mockito.doNothing().when(mojo).checkEbextensionsValidity();
        final Server server = Mockito.mock(Server.class);
        Mockito.when(server.getUsername()).thenReturn("A1234567890123456789");
        Mockito.when(server.getPassword()).thenReturn(
            "1234567890123456789012345678901234567890");
        final Settings settings = Mockito.mock(Settings.class);
        Mockito.when(settings.getServer("name")).thenReturn(server);
        Mockito.doReturn(new ServerCredentials(settings,
            "name")).when(mojo)
            .createServerCredentials();
        Mockito.doNothing().when(mojo).exec(
            org.mockito.Matchers.isA(Application.class),
            org.mockito.Matchers.isA(OverridingVersion.class),
            org.mockito.Matchers.anyString());
        mojo.execute();
        Mockito.verify(mojo).checkEbextensionsValidity();
    }

    /**
     * Verifies that checkEbextensionsValidity throws no exception, if a config
     * file is a valid JSON file.
     * @throws Exception Thrown in case of error.
     */
    @Test
    public void checkEbextensionsValidityNoExceptionOnValidJson()
        throws Exception {
        this.ebextensionsValidationTestLogic(true, false);
    }

    /**
     * Verifies that checkEbextensionsValidity throws no exception, if a config
     * file is a valid YAML file.
     * @throws Exception Thrown in case of error.
     */
    @Test
    public void checkEbextensionsValidityNoExceptionOnValidYaml()
        throws Exception {
        this.ebextensionsValidationTestLogic(false, true);
    }

    /**
     * Verifies that checkEbextensionsValidity throws an exception, if a config
     * file is neither valid JSON, nor valid YAML file.
     * @throws Exception Thrown in case of error.
     */
    @Test
    public void checkEbextensionsValidityInvalidJsonInvalidYaml()
        throws Exception {
        try {
            this.ebextensionsValidationTestLogic(false, false);
        } catch (final MojoFailureException exception) {
            MatcherAssert.assertThat(
                exception.getMessage(),
                Matchers.equalTo(
                    Joiner.on("").join(
                        "File '.ebextensions/01run.config' in ",
                        ".ebextensions is neither valid ",
                        "JSON, nor valid YAML"
                    )
                )
            );
        }
    }

    /**
     * Verifies that checkEbextensionsValidity throws an exception, if there is
     * no .ebextensions directory in the WAR file.
     * @throws java.io.IOException Thrown in case of error.
     */
    @Test
    public void checkEbextensionsValidityThrowsExceptionNoDir()
        throws IOException {
        final BeanstalkMojoForTesting mojo = Mockito.spy(
            new BeanstalkMojoForTesting()
        );
        final ZipFile warfile = Mockito.mock(ZipFile.class);
        Mockito.doReturn(warfile).when(mojo).createZipFile();
        Mockito.when(warfile.getEntry(".ebextensions")).thenReturn(null);
        try {
            mojo.checkEbextensionsValidity();
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
     * Verifies that checkEbextensionsValidity throws an exception, if the
     * .ebextensions is empty.
     * @throws IOException Thrown in case of error.
     */
    @Test
    public void checkEbextensionsValidityThrowsExceptionNoConfigFiles()
        throws IOException {
        final BeanstalkMojoForTesting mojo = Mockito.spy(
            new BeanstalkMojoForTesting()
        );
        final ZipFile warfile = Mockito.mock(ZipFile.class);
        Mockito.doReturn(warfile).when(mojo).createZipFile();
        final ZipEntry ebextdir = Mockito.mock(ZipEntry.class);
        Mockito.when(warfile.getEntry(".ebextensions")).thenReturn(ebextdir);
        final Enumeration entries =
            Mockito.mock(Enumeration.class);
        Mockito.when(warfile.entries()).thenReturn(entries);
        Mockito.when(entries.hasMoreElements()).thenReturn(false);
        try {
            mojo.checkEbextensionsValidity();
        } catch (final MojoFailureException exception) {
            MatcherAssert.assertThat(
                exception.getMessage(),
                Matchers.equalTo(
                    ".ebextensions contains no config files."
                )
            );
        }
    }

    /**
     * Encapsulates the test logic for several tests.
     * @param jsonvalid Specifies the return value of mocked validJson method.
     * @param yamlvalid Specifies the return value of mocked validYaml method.
     * @throws Exception Thrown in case of error.
     */
    private void ebextensionsValidationTestLogic(final boolean jsonvalid,
        final boolean yamlvalid) throws Exception {
        final BeanstalkMojoForTesting mojo = Mockito.spy(
            new BeanstalkMojoForTesting()
        );
        final ZipFile warfile = Mockito.mock(ZipFile.class);
        Mockito.doReturn(warfile).when(mojo).createZipFile();
        final ZipEntry ebextdir = Mockito.mock(ZipEntry.class);
        Mockito.when(warfile.getEntry(".ebextensions")).thenReturn(ebextdir);
        final Enumeration entries =
            Mockito.mock(Enumeration.class);
        Mockito.when(warfile.entries()).thenReturn(entries);
        Mockito.when(entries.hasMoreElements())
            .thenReturn(true)
            .thenReturn(false);
        final ZipEntry configfile = Mockito.mock(ZipEntry.class);
        Mockito.when(configfile.getName()).thenReturn(
            ".ebextensions/01run.config"
        );
        Mockito.when(configfile.isDirectory()).thenReturn(false);
        Mockito.when(entries.nextElement()).thenReturn(configfile);
        final String text = "01run.config contents";
        Mockito.doReturn(text).when(mojo).readFile(warfile, configfile);
        Mockito.doReturn(jsonvalid).when(mojo).validJson(text);
        Mockito.doReturn(yamlvalid).when(mojo).validYaml(text);
        mojo.checkEbextensionsValidity();
    }
    private static class BeanstalkMojoForTesting extends AbstractBeanstalkMojo
    {
        @Override
        protected void exec(final Application app, final Version version,
            final String tmpl) {
        }
    }
}
