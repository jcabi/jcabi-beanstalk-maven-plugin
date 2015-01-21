package com.jcabi.beanstalk.maven.plugin;

import com.google.common.base.Joiner;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.maven.plugin.MojoFailureException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Dmitri Pisarenko (dp@altruix.co)
 * @version $Id$
 * @since 1.0
 */
public class WarFileTest {
    /**
     * Verifies that checkEbextensionsValidity throws an exception, if there is
     * no .ebextensions directory in the WAR file.
     * @throws java.io.IOException Thrown in case of error.
     */
    @Test
    public void checkEbextensionsValidityThrowsExceptionNoDir()
        throws IOException {
        final File file = Mockito.mock(File.class);
        final WarFile war = Mockito.spy(new WarFile(file));
        final ZipFile zip = Mockito.mock(ZipFile.class);
        Mockito.doReturn(zip).when(war).createZipFile();
        Mockito.when(zip.getEntry(".ebextensions")).thenReturn(null);
        try {
            war.checkEbextensionsValidity();
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

    /**
     * Encapsulates the test logic for several tests.
     * @param jsonvalid Specifies the return value of mocked validJson method.
     * @param yamlvalid Specifies the return value of mocked validYaml method.
     * @throws Exception Thrown in case of error.
     */
    private void ebextensionsValidationTestLogic2(final boolean jsonvalid,
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

}
