/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * Configuration file from the .ebextensions directory, either
 * in JSON or in YAML format.
 * @since 0.14
 */
final class ConfigFile {

    /**
     * Text of the file.
     */
    private final transient String text;

    /**
     * Public ctor.
     * @param txt Text of the file
     */
    ConfigFile(final String txt) {
        this.text = txt;
    }

    /**
     * This file is a valid config file?
     * @return TRUE if it is valid JSON or valid YAML
     */
    boolean valid() {
        return this.object() || this.array() || this.yaml();
    }

    /**
     * This is a valid JSON object?
     * @return TRUE if it is
     */
    private boolean object() {
        boolean valid = true;
        try {
            new JSONObject(this.text);
        } catch (final JSONException ex) {
            valid = false;
        }
        return valid;
    }

    /**
     * This is a valid JSON array?
     * @return TRUE if it is
     */
    private boolean array() {
        boolean valid = true;
        try {
            new JSONArray(this.text);
        } catch (final JSONException ex) {
            valid = false;
        }
        return valid;
    }

    /**
     * This is a valid YAML document?
     * @return TRUE if it is
     */
    private boolean yaml() {
        boolean valid = true;
        try {
            new Yaml().load(this.text);
        } catch (final YAMLException ex) {
            valid = false;
        }
        return valid;
    }
}
