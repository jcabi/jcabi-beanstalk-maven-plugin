/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.beanstalk.maven.plugin;

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription;
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest;
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult;
import com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * EBT application version.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.3
 */
@EqualsAndHashCode(of = { "client", "application", "bundle" })
@Loggable(Loggable.DEBUG)
final class OverridingVersion implements Version {

    /**
     * AWS beanstalk client.
     */
    private final transient AWSElasticBeanstalk client;

    /**
     * Application name.
     */
    private final transient String application;

    /**
     * Bundle with a file.
     */
    private final transient Bundle bundle;

    /**
     * Public ctor.
     * @param clnt Client
     * @param app Application name
     * @param bndl Bundle
     */
    protected OverridingVersion(@NotNull final AWSElasticBeanstalk clnt,
        @NotNull final String app, @NotNull final Bundle bndl) {
        this.client = clnt;
        this.application = app;
        this.bundle = bndl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.bundle.name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String label() {
        if (this.exists()) {
            Logger.info(
                this,
                "Version '%s' already exists for '%s'",
                this.bundle.name(),
                this.application
            );
        } else {
            final CreateApplicationVersionResult res =
                this.client.createApplicationVersion(
                    new CreateApplicationVersionRequest()
                        .withApplicationName(this.application)
                        .withVersionLabel(this.bundle.name())
                        .withSourceBundle(this.bundle.location())
                        .withDescription(this.bundle.etag())
                );
            final ApplicationVersionDescription desc =
                res.getApplicationVersion();
            Logger.info(
                this,
                "Version '%s' created for '%s' (%s): '%s'",
                desc.getVersionLabel(),
                desc.getApplicationName(),
                this.bundle.location(),
                desc.getDescription()
            );
            if (!desc.getVersionLabel().equals(this.bundle.name())) {
                throw new DeploymentException(
                    String.format(
                        "version label is '%s' while '%s' expected",
                        desc.getVersionLabel(),
                        this.bundle.name()
                    )
                );
            }
        }
        return this.bundle.name();
    }

    /**
     * This label exists already?
     * @return Yes or no
     */
    private boolean exists() {
        final DescribeApplicationVersionsResult res =
            this.client.describeApplicationVersions(
                new DescribeApplicationVersionsRequest()
                    .withApplicationName(this.application)
                    .withVersionLabels(this.bundle.name())
            );
        boolean exists = false;
        if (res.getApplicationVersions().isEmpty()) {
            Logger.info(
                this,
                "Version '%s' is absent in '%s'",
                this.bundle.name(),
                this.application
            );
        } else {
            final ApplicationVersionDescription ver =
                res.getApplicationVersions().get(0);
            if (ver.getSourceBundle().equals(this.bundle.location())
                && ver.getDescription().equals(this.bundle.etag())) {
                Logger.info(
                    this,
                    "Version '%s' already exists for '%s', etag='%s'",
                    ver.getVersionLabel(),
                    ver.getApplicationName(),
                    ver.getDescription()
                );
                exists = true;
            } else {
                this.client.deleteApplicationVersion(
                    new DeleteApplicationVersionRequest()
                        .withApplicationName(this.application)
                        .withVersionLabel(this.bundle.name())
                );
                Logger.info(
                    this,
                    // @checkstyle LineLength (1 line)
                    "Version '%s' deleted in '%s' because of its outdated S3 location",
                    this.bundle.name(),
                    this.application
                );
            }
        }
        return exists;
    }

}
