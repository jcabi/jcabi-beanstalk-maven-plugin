/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import com.amazonaws.auth.AWSCredentials;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link ServerCredentials}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 */
public final class ServerCredentialsTest {

    /**
     * ServerCredentials can fetch credentials from Maven settings.
     * @throws Exception If something is wrong
     */
    @Test
    public void fetchesCredentialsFromMavenSettings() throws Exception {
        final String key = "AAAABBBBCCCCDDDDZ9Y1";
        final String secret = "AbCdEfGhAbCdEfG/AbCdE7GhAbCdE9Gh+bCdEfGh";
        final Server server = new Server();
        server.setUsername(key);
        server.setPassword(secret);
        final String name = "srv1";
        server.setId(name);
        final Settings settings = new Settings();
        settings.addServer(server);
        final AWSCredentials creds = new ServerCredentials(settings, name);
        MatcherAssert.assertThat(
            creds.getAWSAccessKeyId(),
            Matchers.equalTo(key)
        );
        MatcherAssert.assertThat(
            creds.getAWSSecretKey(),
            Matchers.equalTo(secret)
        );
    }

    /**
     * ServerCredentials can throw when server is not defined.
     * @throws Exception If something is wrong
     */
    @Test(expected = org.apache.maven.plugin.MojoFailureException.class)
    public void throwsWhenServerIsNotDefined() throws Exception {
        new ServerCredentials(new Settings(), "foo");
    }

}
