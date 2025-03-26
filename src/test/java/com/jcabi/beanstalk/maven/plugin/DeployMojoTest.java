/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import org.junit.Test;

/**
 * Test case for {@link DeployMojo} (more detailed test is in maven invoker).
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @checkstyle MultipleStringLiteralsCheck (10 lines)
 */
public final class DeployMojoTest {
    /**
     * DeployMojo can skip execution when flag is set.
     * @throws Exception If something is wrong
     */
    @Test
    public void skipsExecutionWhenRequired() throws Exception {
        final DeployMojo mojo = new DeployMojo();
        mojo.setSkip(true);
        mojo.execute();
    }
}
