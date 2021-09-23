/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.core.metamodel.facets.object.mixin;

import java.lang.reflect.Method;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderAbstract;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;

import lombok.val;

abstract class MixinIntendedAs {

    protected ProgrammingModel programmingModel;
    private MetaModelContext_forTesting metaModelContext;

    protected void setUp() throws Exception {

        metaModelContext = MetaModelContext_forTesting.builder()
                .build();

        programmingModel = metaModelContext.getProgrammingModel();
    }

    protected void tearDown() {
        metaModelContext.getSpecificationLoader().disposeMetaModel();
    }

    protected void newContext(
            final Class<?> cls,
            final Method method,
            final int paramNum,
            final MethodRemover methodRemover) {

    }

    protected FacetHolder runTypeContextOn(final Class<?> type) {

        val facetHolder = FacetHolderAbstract.simple(
                metaModelContext,
                Identifier.classIdentifier(LogicalType.fqcn(type)));

        val processClassContext = ProcessClassContext
                .forTesting(type, MethodRemover.NOOP, facetHolder);

        programmingModel.streamFactories()
//        .filter(facetFactory->!facetFactory.getClass().getSimpleName().startsWith("Grid"))
//        .peek(facetFactory->System.out.println("### " + facetFactory.getClass().getName()))
        .forEach(facetFactory->facetFactory.process(processClassContext));

        return facetHolder;
    }

    protected FacetedMethodParameter runScalarParameterContextOn(final Method actionMethod, final int paramIndex) {

        val owningType = actionMethod.getDeclaringClass();
        val parameterType = actionMethod.getParameterTypes()[paramIndex];

        val facetedMethodParameter = new FacetedMethodParameter(
                metaModelContext,
                FeatureType.ACTION_PARAMETER_SCALAR,
                owningType,
                actionMethod,
                parameterType,
                0);

        val processParameterContext =
                new ProcessParameterContext(
                        owningType,
                        IntrospectionPolicy.ANNOTATION_OPTIONAL,
                        actionMethod,
                        MethodRemover.NOOP,
                        facetedMethodParameter);

        programmingModel.streamFactories()
        .forEach(facetFactory->facetFactory.processParams(processParameterContext));

        return facetedMethodParameter;
    }

}
