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
package org.apache.isis.persistence.jdo.metamodel.testing;

import java.lang.reflect.Method;
import java.util.Optional;

import org.jmock.Expectations;
import org.junit.Rule;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.iactn.InteractionProvider;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel._testing.MethodRemover_forTesting;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderAbstract;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.core.metamodel.facets.object.entity.PersistenceStandard;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.security.authentication.InteractionContextFactory;
import org.apache.isis.persistence.jdo.provider.entities.JdoFacetContext;

import junit.framework.TestCase;

public abstract class AbstractFacetFactoryTest extends TestCase {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    public static class Customer {

        private String firstName;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(final String firstName) {
            this.firstName = firstName;
        }
    }

    protected TranslationService mockTranslationService;
    protected InteractionProvider mockInteractionProvider;
    protected final InteractionContext iaContext = InteractionContextFactory.testing();
    protected SpecificationLoader mockSpecificationLoader;
    protected MethodRemover_forTesting methodRemover;

    protected FacetHolder facetHolder;
    protected FacetedMethod facetedMethod;
    protected FacetedMethodParameter facetedMethodParameter;
    protected MetaModelContext_forTesting metaModelContext;
    protected JdoFacetContext jdoFacetContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // PRODUCTION

        methodRemover = new MethodRemover_forTesting();

        mockInteractionProvider = context.mock(InteractionProvider.class);
        mockTranslationService = context.mock(TranslationService.class);
        mockSpecificationLoader = context.mock(SpecificationLoader.class);

        metaModelContext = MetaModelContext_forTesting.builder()
                .specificationLoader(mockSpecificationLoader)
                .translationService(mockTranslationService)
                .interactionProvider(mockInteractionProvider)
                .build();

        context.checking(new Expectations() {{

            allowing(mockInteractionProvider).currentInteractionContext();
            will(returnValue(Optional.of(iaContext)));
        }});

        facetHolder = FacetHolderAbstract.simple(
                metaModelContext,
                Identifier.propertyOrCollectionIdentifier(LogicalType.fqcn(Customer.class), "firstName"));

        facetedMethod = FacetedMethod.createForProperty(metaModelContext, Customer.class, "firstName");
        facetedMethodParameter = new FacetedMethodParameter(
                metaModelContext,
                FeatureType.ACTION_PARAMETER_SCALAR, facetedMethod.getOwningType(),
                facetedMethod.getMethod(), String.class, 0);

        jdoFacetContext = jdoFacetContextForTesting();
    }

    protected void allowing_specificationLoader_loadSpecification_any_willReturn(final ObjectSpecification objectSpecification) {
        context.checking(new Expectations() {{
            allowing(mockSpecificationLoader).specForType(with(any(Class.class)));
            will(returnValue(Optional.of(objectSpecification)));
        }});
    }

    @Override
    protected void tearDown() throws Exception {
        mockSpecificationLoader = null;
        methodRemover = null;
        facetedMethod = null;
        super.tearDown();
    }

    protected static boolean contains(final Class<?>[] types, final Class<?> type) {
        return Utils.contains(types, type);
    }

    protected static boolean contains(final ImmutableEnumSet<FeatureType> featureTypes, final FeatureType featureType) {
        return Utils.contains(featureTypes, featureType);
    }

    protected static Method findMethod(final Class<?> type, final String methodName, final Class<?>[] methodTypes) {
        return Utils.findMethod(type, methodName, methodTypes);
    }

    protected Method findMethod(final Class<?> type, final String methodName) {
        return Utils.findMethod(type, methodName);
    }

    protected void assertNoMethodsRemoved() {
        assertTrue(methodRemover.getRemovedMethodMethodCalls().isEmpty());
        assertTrue(methodRemover.getRemoveMethodArgsCalls().isEmpty());
    }

    public static JdoFacetContext jdoFacetContextForTesting() {
        return new JdoFacetContext() {
            @Override public boolean isPersistenceEnhanced(final Class<?> cls) {
                return true;
            }
            @Override public boolean isMethodProvidedByEnhancement(final Method method) {
                return false;
            }
            @Override public EntityState getEntityState(final Object pojo) {
                return null;
            }
            @Override
            public EntityFacet createEntityFacet(final FacetHolder facetHolder) {
                return EntityFacet.forTesting(PersistenceStandard.JDO, facetHolder);
            }
        };
    }

}
