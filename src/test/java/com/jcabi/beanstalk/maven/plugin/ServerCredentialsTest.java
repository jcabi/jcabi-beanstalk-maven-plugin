/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import java.util.Arrays;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link ServerCredentials}.
 * @since 0.3
 */
final class ServerCredentialsTest {

    @Test
    void fetchesCredentialsFromMavenSettings() throws Exception {
        final String key = "AAAABBBBCCCCDDDDZ9Y1";
        final String secret = "AbCdEfGhAbCdEfG/AbCdE7GhAbCdE9Gh+bCdEfGh";
        final Server server = new Server();
        server.setUsername(key);
        server.setPassword(secret);
        server.setId("srv1");
        final Settings settings = new Settings();
        settings.addServer(server);
        final ServerCredentials creds =
            new ServerCredentials(settings, "srv1");
        MatcherAssert.assertThat(
            "credentials cannot come from another server",
            Arrays.asList(
                creds.getAWSAccessKeyId(),
                creds.getAWSSecretKey()
            ),
            Matchers.contains(key, secret)
        );
    }

    @Test
    void throwsWhenServerIsNotDefined() {
        Assertions.assertThrows(
            MojoFailureException.class,
            () -> new ServerCredentials(new Settings(), "foo"),
            "absent server cannot be accepted"
        );
    }
}
