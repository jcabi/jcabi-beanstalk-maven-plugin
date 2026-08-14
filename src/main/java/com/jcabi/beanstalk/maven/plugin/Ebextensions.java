/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.maven.plugin.MojoFailureException;

/**
 * The .ebextensions directory inside a WAR file.
 *
 * @since 0.14
 */
final class Ebextensions {

    /**
     * WAR file.
     */
    private final transient File war;

    /**
     * Public ctor.
     * @param file WAR file
     */
    Ebextensions(final File file) {
        this.war = file;
    }

    /**
     * Verify that the directory exists and contains valid config files.
     * @throws MojoFailureException Thrown, if the .ebextensions does not
     *  exist in the WAR file, is empty or one of its files is neither valid
     *  JSON, nor valid YAML
     */
    void validate() throws MojoFailureException {
        try (ZipFile zip = new ZipFile(this.war)) {
            if (zip.getEntry(".ebextensions") == null) {
                throw new MojoFailureException(
                    ".ebextensions directory does not exist in the WAR file"
                );
            }
            final Collection<ZipEntry> configs = Ebextensions.configs(zip);
            if (configs.isEmpty()) {
                throw new MojoFailureException(
                    ".ebextensions contains no config files."
                );
            }
            for (final ZipEntry config : configs) {
                Ebextensions.validate(zip, config);
            }
        } catch (final IOException ex) {
            throw new MojoFailureException(
                String.format("%s is not a valid WAR file", this.war),
                ex
            );
        }
    }

    /**
     * Verify that this entry is a valid config file.
     * @param zip WAR file
     * @param entry ZIP entry to validate
     * @throws MojoFailureException Thrown, if it is neither valid JSON,
     *  nor valid YAML
     */
    private static void validate(final ZipFile zip, final ZipEntry entry)
        throws MojoFailureException {
        if (!new ConfigFile(Ebextensions.text(zip, entry)).valid()) {
            throw new MojoFailureException(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "File '%s' in .ebextensions is neither valid JSON, nor valid YAML",
                    entry.getName()
                )
            );
        }
    }

    /**
     * Find all config files in the .ebextensions directory.
     * @param zip WAR file
     * @return Collection of entries, may be empty
     */
    private static Collection<ZipEntry> configs(final ZipFile zip) {
        final Collection<ZipEntry> configs = new LinkedList<>();
        final Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();
            if (entry.getName().startsWith(".ebextensions/")
                && !entry.isDirectory()) {
                configs.add(entry);
            }
        }
        return configs;
    }

    /**
     * Read the text of one entry.
     * @param zip WAR file
     * @param entry ZIP entry (compressed file) to read from
     * @return Text content of the entry
     * @throws MojoFailureException Thrown, if it cannot be read
     */
    private static String text(final ZipFile zip, final ZipEntry entry)
        throws MojoFailureException {
        final StringWriter writer = new StringWriter();
        try (Reader reader = new InputStreamReader(
            zip.getInputStream(entry), StandardCharsets.UTF_8
        )) {
            reader.transferTo(writer);
        } catch (final IOException ex) {
            throw new MojoFailureException(
                String.format(
                    "Failed to read %s in %s",
                    entry.getName(),
                    zip.getName()
                ),
                ex
            );
        }
        return writer.toString();
    }

}
