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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.maven.plugin.MojoFailureException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit tests for the WarFile class.
 * @author Dmitri Pisarenko (dp@altruix.co)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
public final class WarFileTest {
    /**
     * Verifies that checkEbextensionsValidity throws an exception, if there is
     * no .ebextensions directory in the WAR file.
     * @throws java.io.IOException Thrown in case of error.
     */
    @Test
    public void checkEbextensionsValidityThrowsExceptionNoDir()
        throws IOException {
        final ZipFile zip = Mockito.mock(ZipFile.class);
        Mockito.when(zip.getEntry(".ebextensions")).thenReturn(null);
        final WarFile war = new WarFile(zip);
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
    @SuppressWarnings("unchecked")
    public void checkEbextensionsValidityThrowsExceptionNoConfigFiles()
        throws IOException {
        final File temp = File.createTempFile("test", ".zip");
        final FileOutputStream fos = new FileOutputStream(temp);
        final ZipOutputStream out = new ZipOutputStream(fos);
        out.putNextEntry(new ZipEntry(".ebextensions/"));
        out.flush();
        out.close();
        fos.flush();
        fos.close();
        final WarFile war = new WarFile(new ZipFile(temp));
        try {
            war.checkEbextensionsValidity();
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
     * Verifies that WarFile can use a .ebextensions with valid json object
     * string. Test passes when WarFile.checkEbextensionsValidity method
     * doesn't throw exception.
     * @throws Exception Thrown in case of error.
     */
    @Test
    public void usesEbextensionsWithValidJsonObject() throws Exception {
        final ZipFile zip = this.zipWithEbextensionsWithText(
            "{\"blah\":\"blah\"}"
        );
        final WarFile war = new WarFile(zip);
        war.checkEbextensionsValidity();
    }

    /**
     * Verifies that WarFile can use a .ebextensions with valid json array
     * string. Test passes when WarFile.checkEbextensionsValidity method
     * doesn't throw exception.
     * @throws Exception Thrown in case of error.
     */
    @Test
    public void usesEbextensionsWithValidJsonArray() throws Exception {
        final ZipFile zip = this.zipWithEbextensionsWithText(
            "[{\"blah\":\"blah\"},{\"blah\":\"blah\"}]"
        );
        final WarFile war = new WarFile(zip);
        war.checkEbextensionsValidity();
    }

    /**
     * Verifies that WarFile throws exception when .ebextensions
     * contains invalid json.
     * @todo #16 enable test when WarFile.validYaml implemented
     * @throws Exception Thrown in case of error.
     */
    @Test
    @Ignore
    @SuppressWarnings("StringLiteralsConcatenationCheck")
    public void throwsExceptionWhenUsesEbextensionsWithInvalidJson()
        throws Exception {
        final ZipFile zip = this.zipWithEbextensionsWithText(
            "{\"blah\":\"blah\"]"
        );
        final WarFile war = new WarFile(zip);
        try {
            war.checkEbextensionsValidity();
            Assert.fail();
        } catch (final MojoFailureException exception) {
            MatcherAssert.assertThat(
                exception.getMessage(),
                Matchers.equalTo(
                    new StringBuilder()
                        .append("File '.ebextensions/' in .ebextensions is")
                        .append(" neither valid JSON, nor valid YAML")
                        .toString()
                )
            );
        }
    }

    /**
     * Prepares ZipFile mock containing .ebextensions entry with text
     * provided.
     * @param text Text .ebextensions entry contains.
     * @return ZipFile mock.
     * @throws IOException Thrown in case of error.
     */
    private ZipFile zipWithEbextensionsWithText(final String text)
        throws IOException {
        final ZipFile zip = Mockito.mock(ZipFile.class);
        final ZipEntry entry = Mockito.mock(ZipEntry.class);
        Mockito.when(zip.getEntry(".ebextensions")).thenReturn(entry);
        @SuppressWarnings("unchecked")
        final Enumeration<ZipEntry> enumeration =
            Mockito.mock(Enumeration.class);
        Mockito.doReturn(enumeration).when(zip).entries();
        Mockito.when(enumeration.hasMoreElements()).thenReturn(true)
            .thenReturn(false);
        Mockito.when(enumeration.nextElement()).thenReturn(entry)
            .thenReturn(null);
        Mockito.when(entry.getName()).thenReturn(".ebextensions/");
        Mockito.when(zip.getInputStream(entry)).thenReturn(
            new ByteArrayInputStream(
                text.getBytes(StandardCharsets.UTF_8)
            )
        );
        return zip;
    }

}
