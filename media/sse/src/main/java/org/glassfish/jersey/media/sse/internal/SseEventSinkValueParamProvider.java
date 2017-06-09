/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.jersey.media.sse.internal;

import java.util.function.Function;

import javax.ws.rs.core.Context;
import javax.ws.rs.sse.SseEventSink;

import javax.inject.Inject;
import javax.inject.Provider;

import org.glassfish.jersey.server.AsyncContext;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.AbstractValueParamProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

/**
 * {@link ValueParamProvider} for binding {@link SseEventSink} to its implementation.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class SseEventSinkValueParamProvider extends AbstractValueParamProvider {

    private final Provider<AsyncContext> asyncContextSupplier;

    /**
     * Constructor.
     *
     * @param mpep multivalued map parameter extractor provider.
     */
    @Inject
    public SseEventSinkValueParamProvider(Provider<MultivaluedParameterExtractorProvider> mpep,
                                          Provider<AsyncContext> asyncContextSupplier) {
        super(mpep, Parameter.Source.CONTEXT);
        this.asyncContextSupplier = asyncContextSupplier;
    }

    @Override
    protected Function<ContainerRequest, SseEventSink> createValueProvider(Parameter parameter) {
        if (parameter == null) {
            return null;
        }

        final Class<?> rawParameterType = parameter.getRawType();
        if (rawParameterType == SseEventSink.class && parameter.isAnnotationPresent(Context.class)) {
            return new SseEventSinkValueSupplier(asyncContextSupplier);
        }
        return null;
    }

    private static final class SseEventSinkValueSupplier implements Function<ContainerRequest, SseEventSink> {

        private final Provider<AsyncContext> asyncContextSupplier;

        public SseEventSinkValueSupplier(Provider<AsyncContext> asyncContextSupplier) {
            this.asyncContextSupplier = asyncContextSupplier;
        }

        @Override
        public SseEventSink apply(ContainerRequest containerRequest) {
            return new JerseyEventSink(asyncContextSupplier);
        }
    }
}
