/**
 * Copyright (c) 2012-2014, jcabi.com
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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.s3.AmazonS3Client;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipFile;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * Abstract MOJO for this plugin.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.7.1
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
abstract class AbstractBeanstalkMojo extends AbstractMojo {
    /**
     * Object with the data of the project being built.
     */
    @MojoParameter(expression = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Setting.xml.
     */
    @MojoParameter(
        expression = "${settings}",
        required = true,
        readonly = true,
        description = "Maven settings.xml reference"
    )
    private transient Settings settings;

    /**
     * Shall we skip execution?
     */
    @MojoParameter(
        defaultValue = "false",
        required = false,
        description = "Skips execution"
    )
    private transient boolean skip;

    /**
     * Server ID to deploy to.
     */
    @MojoParameter(
        defaultValue = "aws.amazon.com",
        required = false,
        description = "ID of the server to deploy to, from settings.xml"
    )
    private transient String server;

    /**
     * Application name (also the name of environment and CNAME).
     */
    @MojoParameter(
        required = true,
        description = "EBT application name, environment name, and CNAME"
    )
    private transient String name;

    /**
     * S3 bucket.
     */
    @MojoParameter(
        required = true,
        description = "Amazon S3 bucket name where to upload WAR file"
    )
    private transient String bucket;

    /**
     * S3 key name.
     */
    @MojoParameter(
        required = true,
        description = "Amazon S3 bucket key where to upload WAR file"
    )
    private transient String key;

    /**
     * Template name.
     */
    @MojoParameter(
        required = true,
        description = "Amazon Elastic Beanstalk configuration template name"
    )
    private transient String template;

    /**
     * WAR file to deploy.
     * @checkstyle LineLength (3 lines)
     */
    @MojoParameter(
        defaultValue = "${project.build.directory}/${project.build.finalName}.war",
        required = false,
        description = "Location of .WAR file to deploy"
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
     * {@inheritDoc}
     */
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
        try {
            new WarFile(new ZipFile(this.war)).checkEbextensionsValidity();
        } catch (final IOException ex) {
            throw new MojoFailureException(
                ".ebextensions validity check failed",
                ex
            );
        }
        final AWSCredentials creds = this.createServerCredentials();
        final AWSElasticBeanstalk ebt = new AWSElasticBeanstalkClient(creds);
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
    protected abstract void exec(final Application app,
        final Version version, final String tmpl);

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
            if (age > TimeUnit.MINUTES.toMillis(Tv.FIFTEEN)) {
                Logger.warn(this, "Waiting for %[ms]s, time to give up", age);
                break;
            }
            Logger.warn(
                this,
                "%s is not GREEN yet, let's wait another 15 second...", env
            );
            try {
                TimeUnit.SECONDS.sleep(Tv.FIFTEEN);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new DeploymentException(ex);
            }
            green = env.green();
        }
        return green;
    }

    /**
     * Log all lines from the collection.
     * @param lines All lines to log
     */
    private void log(final String[] lines) {
        for (final String line : lines) {
            Logger.info(this, ">> %s", line);
        }
    }
}
