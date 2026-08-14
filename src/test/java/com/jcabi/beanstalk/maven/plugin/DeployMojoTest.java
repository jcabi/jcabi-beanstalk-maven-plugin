/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link DeployMojo} (more detailed test is in maven invoker).
 *
 * @since 0.3
 */
final class DeployMojoTest {

    @Test
    void skipsExecutionWhenRequired() {
        final DeployMojo mojo = new DeployMojo();
        mojo.setSkip(true);
        Assertions.assertDoesNotThrow(
            mojo::execute,
            "skipped execution cannot fail"
        );
    }

    @Test
    void mentionsMojoAnnotationInClassFile() throws IOException {
        MatcherAssert.assertThat(
            "the goal cannot be declared without the native annotation",
            IOUtils.resourceToString(
                "/com/jcabi/beanstalk/maven/plugin/DeployMojo.class",
                StandardCharsets.ISO_8859_1
            ),
            Matchers.containsString(
                "Lorg/apache/maven/plugins/annotations/Mojo;"
            )
        );
    }

}
