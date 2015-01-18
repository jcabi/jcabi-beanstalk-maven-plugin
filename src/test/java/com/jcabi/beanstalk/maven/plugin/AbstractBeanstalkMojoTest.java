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

    @Test
    public void checkEbextensionsValidityNoExceptionOnValidJson()
        throws IOException, MojoFailureException {
        ebextensionsValidationTestLogic(true, false);
    }

    @Test
    public void checkEbextensionsValidityNoExceptionOnValidYaml()
        throws IOException, MojoFailureException {
        ebextensionsValidationTestLogic(false, true);
    }

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
                    ""
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
