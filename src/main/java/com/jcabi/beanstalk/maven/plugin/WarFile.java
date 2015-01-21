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
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.jcabi.log.Logger;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @author Dmitri Pisarenko (dp@altruix.co)
 * @version $Id$
 * @since 1.0
 */
public class WarFile {
    private final File war;

    public WarFile(final File file) {
        this.war = file;
    }
    /**
     * Verifies that the .ebextensions contains valid configuration file or
     * files.
     * @throws org.apache.maven.plugin.MojoFailureException Thrown, if the .ebextensions does not exist
     *  in the WAR file, is empty or one of its files is neither valid JSON,
     *  nor valid YAML.
     */
    public void checkEbextensionsValidity() throws MojoFailureException {
        try {
            final ZipFile warfile = this.createZipFile();
            final ZipEntry ebextdir = warfile.getEntry(".ebextensions");
            if (ebextdir == null) {
                throw new MojoFailureException(
                    ".ebextensions directory does not exist in the WAR file"
                );
            }
            final Enumeration<? extends ZipEntry> entries = warfile.entries();
            int files = 0;
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                if (entry.getName().startsWith(".ebextensions/")
                    && !entry.isDirectory()) {
                    files += 1;
                    final String text = this.readFile(warfile, entry);
                    if (!(this.validJson(text) || this.validYaml(text))) {
                        throw new MojoFailureException(
                            Joiner.on("").join(
                                "File '",
                                entry.getName(),
                                "' in .ebextensions is neither valid JSON,",
                                " nor valid YAML"
                            )
                        );
                    }
                }
            }
            if (files < 1) {
                throw new MojoFailureException(
                    ".ebextensions contains no config files."
                );
            }
        } catch (final IOException exception) {
            Logger.error(this, exception.getMessage());
            throw new MojoFailureException(
                ".ebextensions validation failed"
            );
        }
    }

    /**
     * Creates a ZipFile from war.
     * @return ZipFile, which contains the war file.
     * @throws IOException Thrown in case of error.
     */
    protected ZipFile createZipFile() throws IOException {
        return new ZipFile(this.war);
    }

    /**
     * Reads text from a ZIP file.
     * @param warfile ZIP file, which contains entry.
     * @param entry ZIP entry (compressed file) to read from.
     * @return Text content of entry.
     */
    protected String readFile(final ZipFile warfile, final ZipEntry entry) {
        String text = null;
        InputStream inputStream = null;
        InputStreamReader reader = null;
        try {
            inputStream = warfile.getInputStream(entry);
            reader = new InputStreamReader(inputStream);
            text = CharStreams.toString(reader);
        } catch (final IOException exception) {
            Logger.error(this, exception.getMessage());
        } finally {
            Closeables.closeQuietly(inputStream);
            Closeables.closeQuietly(reader);
        }
        return text;
    }

    /**
     * Validates a YAML string.
     * @param text Text to validate
     * @return True, if text is a valid YAML string.
     * @todo #2:30min Implement validation of YAML inside the method
     *  AbstractBeanstalkMojo.validYaml. Remember to unit test your solution.
     */
    protected boolean validYaml(final String text) {
        throw new NotImplementedException(
            "com.jcabi.beanstalk.maven.plugin.AbstractBeanstalkMojo.validYaml"
        );
    }

    /**
     * Validates a JSON string.
     * @param text Text to validate
     * @return True, if text is a valid JSON string.
     * @todo #2:30min Implement validation of JSON inside the method
     *  AbstractBeanstalkMojo.validJson(). Remember to unit test your solution.
     */
    protected boolean validJson(final String text) {
        throw new NotImplementedException(
            "com.jcabi.beanstalk.maven.plugin.AbstractBeanstalkMojo.validJson"
        );
    }

}
