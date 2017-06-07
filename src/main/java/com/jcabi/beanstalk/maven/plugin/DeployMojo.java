/**
 * Copyright (c) 2012-2017, jcabi.com
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

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import javax.validation.constraints.NotNull;

/**
 * Deploys WAR artifact to AWS Elastic Beanstalk.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.3
 * @goal deploy
 * @phase deploy
 */
@Loggable(Loggable.INFO)
public final class DeployMojo extends AbstractBeanstalkMojo {

    /**
     * {@inheritDoc}
     */
    @Override
    public void exec(@NotNull final Application app,
        @NotNull final Version version, @NotNull final String template) {
        try {
            this.deploy(app, version, template);
        } catch (final DeploymentException ex) {
            app.clean(false);
            throw ex;
        }
    }

    /**
     * Deploy using this EBT client.
     * @param app The application to deploy to
     * @param version Version to deploy
     * @param template Template to use
     */
    private void deploy(final Application app, final Version version,
        final String template) {
        app.clean(false);
        final Environment candidate = app.candidate(version, template);
        if (this.isGreen(candidate)) {
            if (candidate.primary()) {
                Logger.info(
                    this,
                    "Candidate env '%s' is already primary, no need to swap",
                    candidate
                );
            } else {
                Logger.info(
                    this,
                    "Candidate env '%s' is not primary, let's swap",
                    candidate
                );
                app.swap(candidate);
            }
        } else {
            this.postMortem(candidate);
            throw new DeploymentException(
                String.format(
                    "failed to deploy, since %s never got GREEN", candidate
                )
            );
        }
    }

}
