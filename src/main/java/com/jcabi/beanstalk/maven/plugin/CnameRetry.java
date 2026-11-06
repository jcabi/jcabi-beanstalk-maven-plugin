/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import com.jcabi.log.Logger;
import java.time.Clock;
import java.util.concurrent.TimeUnit;

/**
 * Bounded wait for an application's CNAME to become available.
 *
 * @since 1.0
 */
final class CnameRetry {

    /**
     * Start of the wait, in milliseconds.
     */
    private final transient long started = Clock.systemUTC().millis();

    /**
     * Pause before another unsuccessful CNAME poll.
     *
     * @param name Application CNAME
     */
    void pause(final String name) {
        if (Clock.systemUTC().millis() - this.started
            >= TimeUnit.MINUTES.toMillis(10L)) {
            throw new DeploymentException(
                String.format("Timed out waiting for '%s' CNAME", name)
            );
        }
        Logger.info(this, "Waiting for '%s' CNAME", name);
        try {
            TimeUnit.SECONDS.sleep(5L);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new DeploymentException(ex);
        }
    }
}
