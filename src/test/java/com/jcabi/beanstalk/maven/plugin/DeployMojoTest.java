/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
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
     * DeployMojo class file must carry the native @Mojo annotation in its
     * constant pool, proving the jfrog APT extractor (which required
     * com.sun.mirror.apt removed in Java 8) is no longer used to declare
     * the plugin goal.
     * @throws IOException If the class file cannot be read
     */
    @Test
    public void classFileMentionsMojoAnnotation() throws IOException {
        final String path = "com/jcabi/beanstalk/maven/plugin/DeployMojo.class";
        final InputStream stream = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream(path);
        final String bytecode = IOUtils.toString(stream, StandardCharsets.ISO_8859_1);
        MatcherAssert.assertThat(
            "DeployMojo.class must reference native @Mojo, not Javadoc @goal",
            bytecode,
            Matchers.containsString(
                "Lorg/apache/maven/plugins/annotations/Mojo;"
            )
        );
    }
}
