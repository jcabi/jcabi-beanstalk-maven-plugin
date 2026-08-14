/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.s3.AmazonS3Client;
import com.jcabi.log.Logger;
import java.io.File;
import java.util.concurrent.TimeUnit;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Settings;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * Abstract MOJO for this plugin.
 *
 * @since 0.7.1
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
abstract class AbstractBeanstalkMojo extends AbstractMojo {
    /**
     * Setting.xml.
     */
    @Parameter(defaultValue = "${settings}", readonly = true, required = true)
    private transient Settings settings;

    /**
     * Shall we skip execution?
     */
    @Parameter(defaultValue = "false")
    private transient boolean skip;

    /**
     * Server ID to deploy to.
     */
    @Parameter(defaultValue = "aws.amazon.com")
    private transient String server;

    /**
     * Application name (also the name of environment and CNAME).
     */
    @Parameter(required = true)
    private transient String name;

    /**
     * S3 bucket.
     */
    @Parameter(required = true)
    private transient String bucket;

    /**
     * S3 key name.
     */
    @Parameter(required = true)
    private transient String key;

    /**
     * Template name.
     */
    @Parameter(required = true)
    private transient String template;

    /**
     * WAR file to deploy.
     */
    @Parameter(
        defaultValue = "${project.build.directory}/${project.build.finalName}.war"
    )
    private transient File war;

    /**
     * Set skip option.
     * @param skp Shall we skip execution?
     */
    public void setSkip(final boolean skp) {
        this.skip = skp;
    }

    /**
     * Set war file.
     * @param warfile The war file
     */
    public void setWar(final File warfile) {
        this.war = warfile;
    }

    /**
     * Set the EBT application name, environment name, and CNAME.
     * @param thename The application name
     */
    public void setName(final String thename) {
        this.name = thename;
    }

    /**
     * Set the Amazon S3 bucket name.
     * @param thebucket The bucket name
     */
    public void setBucket(final String thebucket) {
        this.bucket = thebucket;
    }

    /**
     * Set the Amazon S3 bucket key.
     * @param thekey The bucket key
     */
    public void setKey(final String thekey) {
        this.key = thekey;
    }

    @Override
    public void execute() throws MojoFailureException {
        StaticLoggerBinder.getSingleton().setMavenLog(this.getLog());
        if (this.skip) {
            Logger.info(this, "execution skipped because of 'skip' option");
            return;
        }
        if (!this.war.exists()) {
            throw new MojoFailureException(
                String.format("WAR file '%s' doesn't exist", this.war)
            );
        }
        this.validate(this.war);
        final AWSCredentials creds = this.createServerCredentials();
        final AWSElasticBeanstalk ebt = new AWSElasticBeanstalkClient(creds);
        Logger.info(this, "Working with application '%s'", this.name);
        try {
            this.exec(
                new Application(ebt, this.name),
                new OverridingVersion(
                    ebt,
                    this.name,
                    new Bundle.Safe(
                        new OverridingBundle(
                            new AmazonS3Client(creds),
                            this.bucket,
                            this.key,
                            this.war
                        )
                    )
                ),
                this.template
            );
        } catch (final DeploymentException ex) {
            throw new MojoFailureException("failed to deploy", ex);
        } finally {
            ebt.shutdown();
        }
    }

    /**
     * Creates server crecentials.
     * @return Server credentials based on settings and server attributes.
     * @throws MojoFailureException Thrown in case of error.
     */
    protected ServerCredentials createServerCredentials()
        throws MojoFailureException {
        return new ServerCredentials(
            this.settings,
            this.server
        );
    }

    /**
     * Deploy using this EBT client.
     * @param app Application to deploy to
     * @param version Version to deploy
     * @param tmpl Template to use
     */
    protected abstract void exec(Application app, Version version,
        String tmpl);

    /**
     * Report when environment is failed.
     * @param env The environment
     */
    protected void postMortem(final Environment env) {
        Logger.error(this, "Failed to deploy to '%s'", env);
        if (!env.terminated()) {
            Logger.error(
                this,
                "TAIL report should explain the cause of failure:"
            );
            this.log(env.tail().split("\n"));
        }
        Logger.error(this, "Latest EBT events (in reverse order):");
        this.log(env.events());
        env.terminate();
    }

    /**
     * Wait for green status.
     * @param env The environment
     * @return TRUE if green
     */
    protected boolean isGreen(final Environment env) {
        boolean green = env.green();
        final long start = System.currentTimeMillis();
        while (!green) {
            final long age = System.currentTimeMillis() - start;
            if (age > TimeUnit.MINUTES.toMillis(15L)) {
                Logger.warn(this, "Waiting for %[ms]s, time to give up", age);
                break;
            }
            Logger.warn(
                this,
                "%s is not GREEN yet, let's wait another 15 second...", env
            );
            try {
                TimeUnit.SECONDS.sleep(15L);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new DeploymentException(ex);
            }
            green = env.green();
        }
        return green;
    }

    /**
     * Verifies that the .ebextensions contains valid configuration file or
     * files.
     * @param file WAR file
     * @throws MojoFailureException Thrown, if the .ebextensions does not
     *  exist in the WAR file, is empty or one of its files is neither valid
     *  JSON, nor valid YAML
     */
    protected void validate(final File file) throws MojoFailureException {
        Logger.info(this, "Checking .ebextensions in %s", file);
        new Ebextensions(file).validate();
    }

    /**
     * Log all lines from the collection.
     * @param lines All lines to log
     */
    private void log(final String... lines) {
        for (final String line : lines) {
            Logger.info(this, ">> %s", line);
        }
    }

}
