/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

/**
 * Version to deploy.
 *
 * @since 0.3
 */
@FunctionalInterface
interface Version {

    /**
     * Get its label.
     * @return The label
     */
    String label();

}
