/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import org.junit.Test;

/**
 * Test case for {@link UpdateMojo} (more detailed test is in maven invoker).
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 */
public final class UpdateMojoTest {

    /**
     * UpdateMojo can skip execution when flag is set.
     * @throws Exception If something is wrong
     */
    @Test
    public void skipsExecutionWhenRequired() throws Exception {
        final UpdateMojo mojo = new UpdateMojo();
        mojo.setSkip(true);
        mojo.execute();
    }

}
