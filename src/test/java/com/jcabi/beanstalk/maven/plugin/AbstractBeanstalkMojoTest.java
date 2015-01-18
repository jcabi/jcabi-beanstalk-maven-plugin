package com.jcabi.beanstalk.maven.plugin;

import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.maven.plugin.MojoFailureException;
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
    public void checkEbextensionsValidityThrowsExceptionJsonViolation()
        throws IOException, MojoFailureException {
        final AbstractBeanstalkMojo mojo = Mockito.spy(
            new AbstractBeanstalkMojoTest.BeanstalkMojoForTesting()
        );
        final ZipFile warfile = Mockito.mock(ZipFile.class);
        Mockito.doReturn(warfile).when(mojo).createZipFile();
        final ZipEntry ebextdir = Mockito.mock(ZipEntry.class);
        Mockito.when(warfile.getEntry(".ebextensions")).thenReturn(ebextdir);
            warfile.getEntry(".ebextensions");
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
            entries.nextElement();
        final String text = "01run.config contents";
        Mockito.doReturn(text).when(mojo).readFile(warfile, configfile);
        Mockito.doReturn(true).when(mojo).validJson(text);
        Mockito.doReturn(true).when(mojo).validYaml(text);
        mojo.checkEbextensionsValidity();
    }
    @Test
    public void checkEbextensionsValidityThrowsExceptionYamlViolation() {

    }
    @Test
    public void checkEbextensionsValidityThrowsNoException() {

    }
    @Test
    public void checkEbextensionsValidityThrowsExceptionNoDir() {

    }
    @Test
    public void checkEbextensionsValidityThrowsExceptionNoConfigFiles() {

    }

}
