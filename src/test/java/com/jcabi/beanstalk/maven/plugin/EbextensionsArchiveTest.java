/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Archive layout tests for {@link Ebextensions}.
 *
 * @since 0.14
 */
final class EbextensionsArchiveTest {

    @Test
    void validatesConfigWithoutDirectoryEntry(@TempDir final Path temp)
        throws IOException {
        final Path war = temp.resolve("deployment.war");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(war))) {
            zip.putNextEntry(new ZipEntry(".ebextensions/01.config"));
            zip.write("option_settings: {}".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        Assertions.assertDoesNotThrow(new Ebextensions(war.toFile())::validate);
    }
}
