/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link UpdateMojo} (more detailed test is in maven invoker).
 * @since 0.7.1
 */
final class UpdateMojoTest {

    @Test
    void skipsExecutionWhenRequired() {
        final UpdateMojo mojo = new UpdateMojo();
        mojo.setSkip(true);
        Assertions.assertDoesNotThrow(
            mojo::execute,
            "skipped execution cannot fail"
        );
    }
}
