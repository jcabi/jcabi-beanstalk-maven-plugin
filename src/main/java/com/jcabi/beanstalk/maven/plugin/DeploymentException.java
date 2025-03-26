/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

/**
 * Deployment exception (if something goes wrong in between).
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.3
 */
final class DeploymentException extends RuntimeException {

    /**
     * Serialization marker.
     */
    private static final long serialVersionUID = 0x7529FA78EED21E7CL;

    /**
     * Public ctor.
     * @param cause The cause of it
     */
    protected DeploymentException(final Throwable cause) {
        super(cause);
    }

    /**
     * Public ctor.
     * @param cause The cause of it
     */
    protected DeploymentException(final String cause) {
        super(cause);
    }

}
