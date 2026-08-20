/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import com.amazonaws.auth.AWSCredentials;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;

/**
 * AWS credentials from settings.xml.
 * @since 0.3
 */
@ToString
@EqualsAndHashCode(of = { "key", "secret" })
@Loggable(Loggable.DEBUG)
final class ServerCredentials implements AWSCredentials {

    /**
     * AWS key.
     */
    private final transient String key;

    /**
     * AWS secret.
     */
    private final transient String secret;

    /**
     * Public ctor.
     * @param settings Maven settings
     * @param name Name of server ID
     * @throws MojoFailureException If some error
     */
    ServerCredentials(@NotNull final Settings settings,
        @NotNull final String name)
        throws MojoFailureException {
        this(ServerCredentials.server(settings, name), name);
    }

    /**
     * Private ctor.
     * @param server Server from settings
     * @param name Name of server ID
     * @throws MojoFailureException If some error
     */
    private ServerCredentials(final Server server, final String name)
        throws MojoFailureException {
        this(
            ServerCredentials.matching(
                server.getUsername().trim(),
                "[A-Z0-9]{20}",
                String.format(
                    "Key for server '%s' is not a valid AWS key", name
                )
            ),
            ServerCredentials.matching(
                server.getPassword().trim(),
                "[a-zA-Z0-9\\+/]{40}",
                String.format(
                    "Secret for server '%s' is not a valid AWS secret", name
                )
            )
        );
    }

    /**
     * Private ctor.
     * @param akey AWS key
     * @param scrt AWS secret
     */
    private ServerCredentials(final String akey, final String scrt) {
        this.key = akey;
        this.secret = scrt;
    }

    @Override
    public String getAWSAccessKeyId() {
        return this.key;
    }

    @Override
    public String getAWSSecretKey() {
        return this.secret;
    }

    private static Server server(final Settings settings, final String name)
        throws MojoFailureException {
        final Server server = settings.getServer(name);
        if (server == null) {
            throw new MojoFailureException(
                String.format("Server '%s' is absent in settings.xml", name)
            );
        }
        Logger.info(
            ServerCredentials.class,
            "Using server '%s' with AWS key '%s'",
            name, server.getUsername().trim()
        );
        return server;
    }

    private static String matching(final String value, final String regex,
        final String error) throws MojoFailureException {
        if (!value.matches(regex)) {
            throw new MojoFailureException(error);
        }
        return value;
    }
}
