/**
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
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
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
    protected ServerCredentials(@NotNull final Settings settings,
        @NotNull final String name)
        throws MojoFailureException {
        final Server server = settings.getServer(name);
        if (server == null) {
            throw new MojoFailureException(
                String.format("Server '%s' is absent in settings.xml", name)
            );
        }
        this.key = server.getUsername().trim();
        if (!this.key.matches("[A-Z0-9]{20}")) {
            throw new MojoFailureException(
                String.format(
                    "Key '%s' for server '%s' is not a valid AWS key",
                    this.key, name
                )
            );
        }
        this.secret = server.getPassword().trim();
        if (!this.secret.matches("[a-zA-Z0-9\\+/]{40}")) {
            throw new MojoFailureException(
                String.format(
                    "Secret '%s' for server '%s' is not a valid AWS secret",
                    this.secret, name
                )
            );
        }
        Logger.info(
            ServerCredentials.class,
            "Using server '%s' with AWS key '%s'",
            name, this.key
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAWSAccessKeyId() {
        return this.key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAWSSecretKey() {
        return this.secret;
    }

}
