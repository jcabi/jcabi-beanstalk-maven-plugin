/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link Bundle}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 */
public final class BundleTest {

    /**
     * Bundle.Safe can remove invalid symbols from label name.
     * @throws Exception If something is wrong
     */
    @Test
    public void fixesBrokenNames() throws Exception {
        final Bundle bundle = Mockito.mock(Bundle.class);
        Mockito.doReturn("safe/name").when(bundle).name();
        MatcherAssert.assertThat(
            new Bundle.Safe(bundle).name(),
            Matchers.equalTo("safe_name")
        );
    }

}
