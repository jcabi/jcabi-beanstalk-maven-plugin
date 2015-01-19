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
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.maven.plugin.MojoFailureException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for the AbstractBeanstalkMojo class.
 * @author Dmitri Pisarenko (dp@altruix.co)
 * @version $Id$
 * @since 1.0
 */
public final class AbstractBeanstalkMojoTest {
    private static class BeanstalkMojoForTesting extends AbstractBeanstalkMojo
    {
        @Override
        protected void exec(final Application app, final Version version,
            final String tmpl) {
        }
    }

    /**
     * Verifies that execute calls checkEbextensionsValidity method.
     */
    @Test
    @Ignore
    public void executeCallscheckEbextensionsValidity()
        throws MojoFailureException {
        final AbstractBeanstalkMojo mojo = Mockito.spy(
            new AbstractBeanstalkMojoTest.BeanstalkMojoForTesting()
        );
        mojo.setSkip(false);
        Mockito.doNothing().when(mojo).checkEbextensionsValidity();
        mojo.execute();
        Mockito.verify(mojo).checkEbextensionsValidity();
    }

    /**
     * Verifies that checkEbextensionsValidity throws no exception, if a config
     * file is a valid JSON file.
     * @throws IOException Thrown in case of error.
     * @throws MojoFailureException Thrown in case of error.
     */
    @Test
    public void checkEbextensionsValidityNoExceptionOnValidJson()
        throws IOException, MojoFailureException {
        ebextensionsValidationTestLogic(true, false);
    }

    /**
     * Verifies that checkEbextensionsValidity throws no exception, if a config
     * file is a valid YAML file.
     * @throws IOException Thrown in case of error.
     * @throws MojoFailureException Thrown in case of error.
     */
    @Test
    public void checkEbextensionsValidityNoExceptionOnValidYaml()
        throws IOException, MojoFailureException {
        ebextensionsValidationTestLogic(false, true);
    }

    /**
     * Verifies that checkEbextensionsValidity throws an exception, if a config
     * file is neither valid JSON, nor valid YAML file.
     * @throws IOException Thrown in case of error.
     */
    @Test
    public void checkEbextensionsValidityInvalidJsonInvalidYaml()
        throws IOException {
        try {
            ebextensionsValidationTestLogic(false, false);
        } catch (final MojoFailureException exception) {
            MatcherAssert.assertThat(
                exception.getMessage(),
                Matchers.equalTo(
                    Joiner.on("").join(
                        "File '.ebextensions/01run.config' in ",
                        ".ebextensions is neither valid ",
                        "JSON, nor valid YAML"))
            );
        }
    }

    /**
     * Verifies that checkEbextensionsValidity throws an exception, if there is
     * no .ebextensions directory in the WAR file.
     * @throws IOException Thrown in case of error.
     */
    @Test
    public void checkEbextensionsValidityThrowsExceptionNoDir()
        throws IOException {
        final AbstractBeanstalkMojo mojo = Mockito.spy(
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
        final AbstractBeanstalkMojo mojo = Mockito.spy(
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
        } catch (MojoFailureException exception) {
            MatcherAssert.assertThat(
                exception.getMessage(),
                Matchers.equalTo(
                    ".ebextensions contains no config files."
                )
            );
        }
    }
    private void ebextensionsValidationTestLogic(final boolean jsonValid,
        final boolean yamlValid)
        throws IOException, MojoFailureException {
        final AbstractBeanstalkMojo mojo = Mockito.spy(
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
            ".ebextensions/01run.config");
        Mockito.when(configfile.isDirectory()).thenReturn(false);
        Mockito.when(entries.nextElement()).thenReturn(configfile);
        final String text = "01run.config contents";
        Mockito.doReturn(text).when(mojo).readFile(warfile, configfile);
        Mockito.doReturn(jsonValid).when(mojo).validJson(text);
        Mockito.doReturn(yamlValid).when(mojo).validYaml(text);
        mojo.checkEbextensionsValidity();
    }
}
