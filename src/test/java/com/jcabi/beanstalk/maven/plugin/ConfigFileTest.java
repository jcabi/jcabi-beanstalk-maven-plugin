/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link ConfigFile}.
 *
 * @since 0.14
 */
final class ConfigFileTest {

    @Test
    void acceptsValidJson() {
        MatcherAssert.assertThat(
            "valid JSON cannot be rejected",
            new ConfigFile(
                String.join(
                    "\n",
                    "{",
                    "\"id'\": 102,",
                    "\"name\": \"Rudy\",",
                    "\"colors\": [\"green\", \"yellow\"],",
                    "}"
                )
            ).valid(),
            Matchers.is(true)
        );
    }

    @Test
    void acceptsValidYaml() {
        MatcherAssert.assertThat(
            "valid YAML cannot be rejected",
            new ConfigFile(
                String.join(
                    "\n",
                    "Time: 2001-11-23 15:01:42 -5",
                    "User: ed",
                    "Warning:",
                    "  This is an error message",
                    "  for the log file"
                )
            ).valid(),
            Matchers.is(true)
        );
    }

    @Test
    void rejectsBrokenJson() {
        MatcherAssert.assertThat(
            "broken JSON cannot be accepted",
            new ConfigFile(
                String.join(
                    "\n",
                    "[",
                    "id: 102",
                    "name: \"Rudy\"",
                    "colors: [green, yellow]",
                    "]"
                )
            ).valid(),
            Matchers.is(false)
        );
    }

    @Test
    void rejectsBrokenYaml() {
        MatcherAssert.assertThat(
            "broken YAML cannot be accepted",
            new ConfigFile(
                String.join(
                    "\n",
                    "Some illegal Prefix",
                    "Time: 2005-11-23 10:01:42 -5",
                    "Admin: ed",
                    "Messages:",
                    " Hello is an error information",
                    " for the configuration file"
                )
            ).valid(),
            Matchers.is(false)
        );
    }

}
