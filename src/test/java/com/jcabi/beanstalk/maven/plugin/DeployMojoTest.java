/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import org.apache.maven.plugins.annotations.Mojo;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
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

    /**
     * DeployMojo must carry a native Maven @Mojo annotation so that the
     * plugin descriptor can be generated without the jfrog APT extractor,
     * which relied on com.sun.mirror.apt removed in Java 8.
     */
    @Test
    public void declaresGoalWithModernAnnotation() {
        MatcherAssert.assertThat(
            "DeployMojo must be annotated with @Mojo, not Javadoc @goal",
            DeployMojo.class.isAnnotationPresent(Mojo.class),
            Matchers.is(true)
        );
    }
}
