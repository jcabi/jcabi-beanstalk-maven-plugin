/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * Test case for {@link ConfigFile}.
 *
 * @since 0.14
 */
final class ConfigFileTest {

    @ParameterizedTest
    @ValueSource(
        strings = {
            "{}", "[]", "[1,true,null,{\"name\":\"Rudy\"}]",
            "{'name':'Rudy',}", "{name:Rudy; colors:[green,yellow,]}",
            "{name=Rudy; colors=>['green';'yellow';]}",
            "[,{name=>Rudy},,]", "[ (\"green\",\"yellow\") ]",
            "{colors:(\"green\",\"yellow\")}",
            "{\"text\":\"a=>b;c(d)\",'escaped':'it\\'s;=>(ok)'}",
            "{name:Rudy} ignored trailing text", "[,)]", "{)key=>[1,,2]}",
            "{colors:(]}", "[\"green\";\"yellow\";)]"
        }
    )
    void acceptsLegacyJson(final String text) {
        MatcherAssert.assertThat(
            String.format("accepted JSON syntax must remain valid: %s", text),
            new ConfigFile(text).valid(), Matchers.is(true)
        );
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "[", "{", "[\"unterminated]", "[1)",
            "[(\"green\",\"yellow\"]]", "[ (\"green\",\"yellow\";) ]",
            "{name=>[1,,2]", "[1, {broken:]",
            "[\"bad\\q\"]", "key: [unclosed"
        }
    )
    void rejectsMalformedInput(final String text) {
        MatcherAssert.assertThat(
            String.format("malformed configuration must remain invalid: %s", text),
            new ConfigFile(text).valid(), Matchers.is(false)
        );
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "{name: Rudy, colors: [green, yellow]}",
            "{name: [green, yellow]}", "name: a=>b;c(d)",
            "just a scalar", "", "{\"name\":}", "[;]", "[;\"green\"]", "{name==>Rudy}"
        }
    )
    void fallsBackToYaml(final String text) {
        MatcherAssert.assertThat(
            String.format("valid YAML must reach the unchanged fallback: %s", text),
            new ConfigFile(text).valid(), Matchers.is(true)
        );
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "[\"green\";\"yellow\";]", "{colors=>[1,,2]}",
            "[ (\"green\",\"yellow\") ]", "{colors:(\"green\",\"yellow\")}"
        }
    )
    void acceptsJsonBeyondYaml(final String text) {
        Assertions.assertThrows(
            YAMLException.class, () -> new Yaml().load(text),
            "the YAML fallback must not mask loss of legacy JSON syntax"
        );
        MatcherAssert.assertThat(
            String.format("legacy JSON must be validated independently of YAML: %s", text),
            new ConfigFile(text).valid(), Matchers.is(true)
        );
    }

    @Test
    void acceptsLongQuotedValue() {
        MatcherAssert.assertThat(
            "long strings must not exhaust the punctuation adapter's stack",
            new ConfigFile(
                String.format(
                    "{text=>\"%s\"}",
                    String.join("", Collections.nCopies(16_384, "a=>b;c(d)"))
                )
            ).valid(),
            Matchers.is(true)
        );
    }

    @Test
    void acceptsValidJson() {
        MatcherAssert.assertThat(
            "valid JSON cannot be rejected",
            new ConfigFile(
                String.join(
                    System.lineSeparator(),
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
                    System.lineSeparator(),
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
                    System.lineSeparator(),
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
                    System.lineSeparator(),
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
