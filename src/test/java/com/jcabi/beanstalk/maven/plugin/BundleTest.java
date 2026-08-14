/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link Bundle}.
 *
 * @since 0.3
 */
final class BundleTest {

    @Test
    void fixesBrokenNames() {
        final Bundle bundle = Mockito.mock(Bundle.class);
        Mockito.doReturn("safe/name").when(bundle).name();
        MatcherAssert.assertThat(
            "slashes cannot stay in the name",
            new Bundle.Safe(bundle).name(),
            Matchers.equalTo("safe_name")
        );
    }

}
