/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.maven.plugin.MojoFailureException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

/**
 * Test case for {@link AbstractBeanstalkMojo}.
 * @since 0.3
 */
final class GenericMojoTest {

    @Test
    void executesSuccessfully(@TempDir final Path temp) throws Exception {
        final AbstractBeanstalkMojo mojo =
            GenericMojoTest.mojo();
        Mockito.doCallRealMethod().when(mojo)
            .setName(Mockito.any(String.class));
        Mockito.doCallRealMethod().when(mojo)
            .setBucket(Mockito.any(String.class));
        Mockito.doCallRealMethod().when(mojo)
            .setKey(Mockito.any(String.class));
        final File war = GenericMojoTest.war(temp, "User: ed");
        mojo.setWar(war);
        mojo.setName("name");
        mojo.setBucket("bucket");
        mojo.setKey("key");
        mojo.execute();
        Assertions.assertAll(
            () -> Mockito.verify(mojo).validate(war),
            () -> Mockito.verify(mojo).createServerCredentials()
        );
    }

    @Test
    void throwsExceptionWhenNoEbextensionsDirInZip(@TempDir final Path temp)
        throws Exception {
        final AbstractBeanstalkMojo mojo =
            GenericMojoTest.mojo();
        mojo.setWar(GenericMojoTest.bare(temp));
        MatcherAssert.assertThat(
            "absent .ebextensions cannot be reported differently",
            GenericMojoTest.thrown(mojo).getMessage(),
            Matchers.equalTo(
                ".ebextensions directory does not exist in the WAR file"
            )
        );
    }

    @Test
    void throwsExceptionWhenNoConfigFilesInEbextensionsDir(
        @TempDir final Path temp) throws Exception {
        final AbstractBeanstalkMojo mojo =
            GenericMojoTest.mojo();
        mojo.setWar(GenericMojoTest.war(temp, ""));
        MatcherAssert.assertThat(
            "empty .ebextensions cannot be reported differently",
            GenericMojoTest.thrown(mojo).getMessage(),
            Matchers.equalTo(".ebextensions contains no config files.")
        );
    }

    private static AbstractBeanstalkMojo mojo() throws MojoFailureException {
        final AbstractBeanstalkMojo mojo =
            Mockito.mock(AbstractBeanstalkMojo.class);
        Mockito.doCallRealMethod().when(mojo).execute();
        Mockito.doCallRealMethod().when(mojo).getLog();
        Mockito.doCallRealMethod().when(mojo)
            .setWar(Mockito.any(File.class));
        Mockito.doCallRealMethod().when(mojo)
            .validate(Mockito.any(File.class));
        return mojo;
    }

    private static File war(final Path temp, final String config)
        throws IOException {
        final Path war = temp.resolve("test.war");
        try (
            ZipOutputStream out = new ZipOutputStream(
                Files.newOutputStream(war)
            )
        ) {
            out.putNextEntry(new ZipEntry(".ebextensions/"));
            if (!config.isEmpty()) {
                out.putNextEntry(new ZipEntry(".ebextensions/config.yaml"));
                out.write(config.getBytes(StandardCharsets.UTF_8));
            }
        }
        return war.toFile();
    }

    private static File bare(final Path temp) throws IOException {
        final Path war = temp.resolve("bare.war");
        try (
            ZipOutputStream out = new ZipOutputStream(
                Files.newOutputStream(war)
            )
        ) {
            out.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
            out.write(
                "Manifest-Version: 1.0".getBytes(StandardCharsets.UTF_8)
            );
        }
        return war.toFile();
    }

    private static Throwable thrown(final AbstractBeanstalkMojo mojo) {
        return Assertions.assertThrows(
            MojoFailureException.class,
            mojo::execute,
            "the mojo cannot execute without a valid .ebextensions"
        );
    }
}
