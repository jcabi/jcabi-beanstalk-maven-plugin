/**
 * Copyright (c) 2012-2015, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jcabi.beanstalk.maven.plugin;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Test case for {@link DeployMojo} (more detailed test is in maven invoker).
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @checkstyle MultipleStringLiteralsCheck (100 lines)
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
     * Test validating an incorrect yaml format.
     * @throws Exception If something is wrong
     */
    @Test
    public void testInvalidYaml() throws Exception {
        // @checkstyle StringLiteralsConcatenationCheck (6 lines)
        final String invalid = "ssssd\n"
            + "Time: 2001-11-23 15:01:42 -5\n"
            + "User: ed\n"
            + "Warning:\n"
            + "  This is an error message\n"
            + "  for the log file\n";
        final DeployMojo mojo = new DeployMojo();
        TestCase.assertFalse(
            "yaml should be invalid",
            mojo.validYaml(invalid)
        );
    }

    /**
     * Test validating an correct yaml format.
     * @throws Exception If something is wrong
     */
    @Test
    public void testValidYaml() throws Exception {
        // @checkstyle StringLiteralsConcatenationCheck (6 lines)
        final String valid =
            "Time: 2001-11-23 15:01:42 -5\n"
            + "User: ed\n"
            + "Warning:\n"
            + "  This is an error message\n"
            + "  for the log file\n";
        final DeployMojo mojo = new DeployMojo();
        TestCase.assertTrue(
            "yaml should be valid",
            mojo.validYaml(valid)
        );
    }
}
