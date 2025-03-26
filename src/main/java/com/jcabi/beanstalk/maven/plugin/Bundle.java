/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import com.amazonaws.services.elasticbeanstalk.model.S3Location;
import com.jcabi.aspects.Loggable;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Bundle with a WAR application.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.3
 */
interface Bundle {

    /**
     * Name of this version to use.
     * @return The name
     */
    String name();

    /**
     * Get S3 location of an app.
     * @return The location
     */
    S3Location location();

    /**
     * Get MD5 ETag hex of the bundle, according to RFC-1864.
     * @return The ETag
     * @since 0.7.1
     */
    String etag();

    /**
     * Safe bundle, with a safe name.
     */
    @ToString
    @EqualsAndHashCode(of = "origin")
    @Loggable(Loggable.DEBUG)
    final class Safe implements Bundle {
        /**
         * Original bundle.
         */
        private final transient Bundle origin;
        /**
         * Public ctor.
         * @param bundle Original bundle
         */
        public Safe(@NotNull final Bundle bundle) {
            this.origin = bundle;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String name() {
            return this.origin.name().replace("/", "_");
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public S3Location location() {
            return this.origin.location();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String etag() {
            return this.origin.etag();
        }
    }

}
