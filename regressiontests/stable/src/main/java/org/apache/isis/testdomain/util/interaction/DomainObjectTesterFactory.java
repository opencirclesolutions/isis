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
package org.apache.isis.testdomain.util.interaction;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.exceptions.unrecoverable.DomainModelException;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.object.icon.IconFacet;
import org.apache.isis.core.metamodel.facets.object.layout.LayoutFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.CollectionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.isis.core.metamodel.interactions.managed.ManagedMember;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.testdomain.util.CollectionAssertions;
import org.apache.isis.testing.integtestsupport.applib.validate.DomainModelValidator;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class DomainObjectTesterFactory {

    private final @NonNull ServiceInjector serviceInjector;

    public <T> ObjectTester<T> objectTester(
            final Class<T> domainObjectType) {
        val tester = serviceInjector.injectServicesInto(
                new ObjectTester<T>(domainObjectType));
        tester.init();
        return tester;
    }

    public <T> ActionTester<T> actionTester(
            final Class<T> domainObjectType,
            final String actionName,
            final Where where) {
        val tester = serviceInjector.injectServicesInto(
                new ActionTester<T>(domainObjectType, actionName, where));
        tester.init();
        return tester;
    }

    public <T> PropertyTester<T> propertyTester(
            final Class<T> domainObjectType,
            final String propertyName,
            final Where where) {
        val tester = serviceInjector.injectServicesInto(
                new PropertyTester<T>(domainObjectType, propertyName, where));
        tester.init();
        return tester;
    }

    public <T> CollectionTester<T> collectionTester(
            final Class<T> domainObjectType,
            final String collectionName,
            final Where where) {
        val tester = serviceInjector.injectServicesInto(
                new CollectionTester<T>(domainObjectType, collectionName, where));
        tester.init();
        return tester;
    }

    // -- SHORTCUTS

    public <T> ActionTester<T> actionTester(
            final Class<T> domainObjectType,
            final String actionName) {
        return actionTester(domainObjectType, actionName, Where.ANYWHERE);
    }

    public <T> PropertyTester<T> propertyTester(
            final Class<T> domainObjectType,
            final String propertyName) {
        return propertyTester(domainObjectType, propertyName, Where.ANYWHERE);
    }

    public <T> CollectionTester<T> collectionTester(
            final Class<T> domainObjectType,
            final String collectionName) {
        return collectionTester(domainObjectType, collectionName, Where.ANYWHERE);
    }

    // -- OBJECT TESTER

    public static class ObjectTester<T>
    extends Tester<T> {

        protected ObjectTester(final @NonNull Class<T> domainObjectType) {
            super(domainObjectType);
        }

        public void assertTitle(final @Nullable String expectedResult) {
            assertEquals(expectedResult,
                    super.objectSpecification.getTitleService().titleOf(vm.getPojo()));
            assertEquals(expectedResult,
                    super.objectSpecification.lookupFacet(TitleFacet.class)
                    .map(titleFacet->titleFacet.title(vm))
                    .orElse(null));
        }

        public void assertIcon(final @Nullable String expectedResult) {
            assertEquals(expectedResult,
                    super.objectSpecification.getTitleService().iconNameOf(vm.getPojo()));
            assertEquals(expectedResult,
                    super.objectSpecification.lookupFacet(IconFacet.class)
                    .map(iconFacet->iconFacet.iconName(vm))
                    .orElse(null));
        }

        public void assertCssClass(final @Nullable String expectedResult) {
            assertEquals(expectedResult,
                    super.objectSpecification.lookupFacet(CssClassFacet.class)
                    .map(cssClassFacet->cssClassFacet.cssClass(vm))
                    .orElse(null));
        }

        public void assertLayout(final @Nullable String expectedResult) {
            assertEquals(expectedResult,
                    super.objectSpecification.lookupFacet(LayoutFacet.class)
                    .map(layoutFacet->layoutFacet.layout(vm))
                    .orElse(null));
        }

        public void assertValidationFailureOnMember(
                final ProgrammingModelConstants.Validation validationEnum,
                final String memberName) {

            val validateDomainModel =
                    new DomainModelValidator(specificationLoader, configuration, isisSystemEnvironment);

            assertThrows(DomainModelException.class, validateDomainModel::throwIfInvalid);
            validateDomainModel.assertAnyFailuresContaining(
                    Identifier.classIdentifier(LogicalType.fqcn(getDomainObjectType())),
                    validationEnum
                    .getMessage(Map.of(
                            "type", getDomainObjectType().getName(),
                            "member", memberName)));
        }

    }

    // -- ACTION TESTER

    public static class ActionTester<T>
    extends MemberTester<T> {

        @Getter
        private Optional<ManagedAction> managedActionIfAny;

        private ActionTester(
                final @NonNull Class<T> domainObjectType,
                final @NonNull String actionName,
                final @NonNull Where where) {
            super(domainObjectType, actionName, "actionName", where);
        }

        @Override
        public Optional<ObjectAction> getMetaModel() {
            return managedActionIfAny
            .map(ManagedAction::getMetaModel)
            .map(ObjectAction.class::cast);
        }

        @Override
        protected Optional<ObjectSpecification> getElementType() {
            return getMetaModel()
                    .map(ObjectAction::getReturnType);
        }

        @Override
        protected Optional<? extends ManagedMember> startInteractionOn(final ManagedObject viewModel) {
            return this.managedActionIfAny = ActionInteraction
                    .start(viewModel, getMemberName(), Where.NOT_SPECIFIED)
                    .getManagedAction();
        }

        /**
         * circumvents rule checking
         */
        public void assertInvocationResult(
                final @Nullable Object expectedResult,
                final @Nullable List<Object> pojoArgList) {

            assertExists(true);

            managedActionIfAny
            .ifPresent(managedAction->{
                interactionService.runAnonymous(()->{

                    val args = managedAction.getInteractionHead()
                            .getPopulatedParameterValues(pojoArgList);

                    // spawns its own transactional boundary, or reuses an existing one if available
                    val either = managedAction.invoke(args);

                    assertTrue(either.isLeft()); // assert action did not throw

                    val actionResultAsPojo = either.leftIfAny().getPojo();

                    assertEquals(expectedResult, actionResultAsPojo);

                });
            });

        }

    }

    // -- PROPERTY TESTER

    public static class PropertyTester<T>
    extends MemberTester<T> {

        @Getter
        private Optional<ManagedProperty> managedPropertyIfAny;

        private PropertyTester(
                final @NonNull Class<T> domainObjectType,
                final @NonNull String propertyName,
                final @NonNull Where where) {
            super(domainObjectType, propertyName, "property", where);
        }

        @Override
        public Optional<OneToOneAssociation> getMetaModel() {
            return managedPropertyIfAny
            .map(ManagedProperty::getMetaModel)
            .map(OneToOneAssociation.class::cast);
        }

        @Override
        protected Optional<ObjectSpecification> getElementType() {
            return getMetaModel()
                    .map(OneToOneAssociation::getOnType);
                        //getElementType);
        }

        @Override
        protected Optional<? extends ManagedMember> startInteractionOn(final ManagedObject viewModel) {
            return this.managedPropertyIfAny = PropertyInteraction
                    .start(viewModel, getMemberName(), where)
                    .getManagedProperty();
        }

        /**
         * circumvents rule checking
         */
        public void assertValue(final Object expectedPropertyValue) {

            assertExists(true);

            managedPropertyIfAny
            .ifPresent(managedProperty->{
                interactionService.runAnonymous(()->{
                    assertEquals(expectedPropertyValue, managedProperty.getPropertyValue().getPojo());
                });
            });
        }

        /**
         * circumvents rule checking
         */
        public void assertValueUpdate(final Object proposedNewPropertyValue) {

            assertExists(true);

            managedPropertyIfAny
            .ifPresent(managedProperty->{
                interactionService.runAnonymous(()->{

                    val newPropertyValue = managedProperty.getMetaModel().getMetaModelContext()
                          .getObjectManager().adapt(proposedNewPropertyValue);

                    managedProperty.modifyProperty(newPropertyValue);
                    assertEquals(proposedNewPropertyValue, managedProperty.getPropertyValue().getPojo());
                });
            });

        }

        public void assertValueUpdateUsingNegotiation(final Object proposedNewPropertyValue) {

            assertExists(true);

            managedPropertyIfAny
            .ifPresent(managedProperty->{
                interactionService.runAnonymous(()->{

                    val propNeg = managedProperty.startNegotiation();
                    val initialValue = managedProperty.getPropertyValue();

                    assertEquals(initialValue, propNeg.getValue().getValue());

                    val newPropertyValue = managedProperty.getMetaModel().getMetaModelContext()
                            .getObjectManager().adapt(proposedNewPropertyValue);
                    propNeg.getValue().setValue(newPropertyValue);

                    // yet just pending
                    assertEquals(initialValue, managedProperty.getPropertyValue());
                    assertEquals(newPropertyValue, propNeg.getValue().getValue());

                    propNeg.submit();

                    // after submission
                    assertEquals(newPropertyValue, managedProperty.getPropertyValue());
                    assertEquals(newPropertyValue, propNeg.getValue().getValue());

                });
            });

        }

        /**
         * Supported by all properties that reflect a value type.
         * Uses value-semantics under the hood to do the conversion.
         */
        public void assertValueUpdateUsingNegotiationTextual(
                final String parsableProposedValue) {

            assertExists(true);

            managedPropertyIfAny
            .ifPresent(managedProperty->{
                interactionService.runAnonymous(()->{

                    final var propNeg = managedProperty.startNegotiation();
                    final var initialValue = managedProperty.getPropertyValue();

                    assertEquals(initialValue, propNeg.getValue().getValue());

                    propNeg.getValueAsParsableText().setValue(parsableProposedValue);

                    // yet just pending
                    assertEquals(initialValue, managedProperty.getPropertyValue());
                    assertEquals(parsableProposedValue, propNeg.getValueAsParsableText().getValue());

                    propNeg.submit();

                    // after submission
                    assertEquals(parsableProposedValue, asParsebleText());
                    assertEquals(parsableProposedValue, propNeg.getValueAsParsableText().getValue());

                });
            });

        }

        @SuppressWarnings("unchecked")
        public String asParsebleText() {
            assertExists(true);
            final var valueFacet = getFacetOnElementTypeElseFail(ValueFacet.class);
            final var prop = this.getMetaModel().get();

            final var context = valueFacet
                    .createValueSemanticsContext(prop.getFeatureIdentifier());

            return valueFacet.selectParserForPropertyElseFallback(prop)
                    .parseableTextRepresentation(context, managedPropertyIfAny.get().getPropertyValue().getPojo());
        }

    }


    // -- COLLECTION TESTER

    public static class CollectionTester<T>
    extends MemberTester<T> {

        @Getter
        private Optional<ManagedCollection> managedCollectionIfAny;

        private CollectionTester(
                final @NonNull Class<T> domainObjectType,
                final @NonNull String collectionName,
                final @NonNull Where where) {
            super(domainObjectType, collectionName, "collection", where);
        }

        @Override
        public Optional<OneToManyAssociation> getMetaModel() {
            return managedCollectionIfAny
            .map(ManagedCollection::getMetaModel)
            .map(OneToManyAssociation.class::cast);
        }

        @Override
        protected Optional<ObjectSpecification> getElementType() {
            return getMetaModel()
                    .map(OneToManyAssociation::getElementType);
        }

        @Override
        protected Optional<? extends ManagedMember> startInteractionOn(final ManagedObject viewModel) {
            return this.managedCollectionIfAny = CollectionInteraction
                    .start(viewModel, getMemberName(), Where.NOT_SPECIFIED)
                    .getManagedCollection();
        }

        /**
         * circumvents rule checking
         */
        public void assertCollectionElements(final Iterable<?> expectedCollectionElements) {

            assertExists(true);

            managedCollectionIfAny
            .ifPresent(managedCollection->{
                interactionService.runAnonymous(()->{
                    CollectionAssertions
                    .assertComponentWiseEquals(expectedCollectionElements, managedCollection.getCollectionValue().getPojo());
                });
            });
        }

    }

    // -- COMMON ABSTRACT MEMBER TESTER

    private static abstract class MemberTester<T>
    extends Tester<T>{

        @Getter private final String memberName;
        private final String memberSort;
        protected final Where where;

        private Optional<? extends ManagedMember> managedMemberIfAny;

        protected MemberTester(
                final @NonNull Class<T> domainObjectType,
                final @NonNull String memberName,
                final @NonNull String memberSort,
                final @NonNull Where where) {
            super(domainObjectType);
            this.memberName = memberName;
            this.memberSort = memberSort;
            this.where = where;
        }

        @Override
        protected final MemberTester<T> init() {
            super.init();
            this.managedMemberIfAny = startInteractionOn(vm);
            return this;
        }

        protected abstract Optional<? extends ObjectMember> getMetaModel();
        protected abstract Optional<ObjectSpecification> getElementType();
        protected abstract Optional<? extends ManagedMember> startInteractionOn(ManagedObject viewModel);

        public <F extends Facet> F getFacetOnMemberElseFail(final Class<F> facetType) {
            return getMetaModel()
            .map(m->m.getFacet(facetType))
            .orElseThrow(()->_Exceptions.noSuchElement(
                    String.format("%s has no %s", memberName, facetType.getSimpleName())
                    ));
        }

        public <F extends Facet> F getFacetOnElementTypeElseFail(final Class<F> facetType) {
            return getElementType()
            .map(m->m.getFacet(facetType))
            .orElseThrow(()->_Exceptions.noSuchElement(
                    String.format("'%s: %s' has no %s on its element type",
                            memberName,
                            getObjectSpecification().getCorrespondingClass().getSimpleName(),
                            facetType.getSimpleName())
                    ));
        }

        public final void assertExists(final boolean isExpectedToExist) {

            if(isExpectedToExist
                    && managedMemberIfAny.isEmpty()) {
                fail(String.format("%s %s does not exist", memberSort, memberName));
            }

            if(!isExpectedToExist
                    && managedMemberIfAny.isPresent()) {
                fail(String.format("%s %s does exist", memberSort, memberName));
            }
        }

        public void assertMemberId(final String expectedMemberId) {
            assertEquals(expectedMemberId,
                    getMetaModel()
                    .orElseThrow(_Exceptions::noSuchElement).getId());
        }

        public final void assertVisibilityIsNotVetoed() {
            assertVisibilityIsVetoedWith(null);
        }

        public final void assertVisibilityIsVetoedWith(final @Nullable String expectedVetoReason) {

            final boolean isExpectedVisible = expectedVetoReason == null;

            if(isExpectedVisible) {
                assertExists(true);
            }

            managedMemberIfAny
            .ifPresent(managedCollection->{
                interactionService.runAnonymous(()->{

                    final String actualVetoResaon = managedCollection
                        .checkVisibility()
                        .map(veto->veto.getReason())
                        .orElse(null);

                    assertEquals(expectedVetoReason, actualVetoResaon);
                });
            });
        }

        public final void assertUsabilityIsNotVetoed() {
            assertUsabilityIsVetoedWith(null);
        }

        public final void assertUsabilityIsVetoedWithAll(final Can<String> expectedVetoReasons) {

            assertExists(true);

            managedMemberIfAny
            .ifPresent(managedCollection->{
                interactionService.runAnonymous(()->{
                    final String actualVetoReason = managedCollection
                            .checkUsability()
                            .map(veto->veto.getReason())
                            .orElse(null);

                    if(!expectedVetoReasons.isEmpty()
                            && actualVetoReason==null) {
                        fail("usability not vetoed, while expecting all of: " + expectedVetoReasons.toList());
                    }

                    expectedVetoReasons
                    .forEach(expectedVetoReason->{
                        assertTrue(actualVetoReason.contains(expectedVetoReason),
                               ()->String.format("usability veto %s is not containing %s",
                                       actualVetoReason, expectedVetoReason));
                    });
                });
            });
        }

        public final void assertUsabilityIsVetoedWith(final @Nullable String expectedVetoReason) {

            final boolean isExpectedUsable = expectedVetoReason == null;

            if(isExpectedUsable) {
                assertExists(true);
            }

            managedMemberIfAny
            .ifPresent(managedCollection->{
                interactionService.runAnonymous(()->{
                    final String actualVetoReason = managedCollection
                            .checkUsability()
                            .map(veto->veto.getReason())
                            .orElse(null);

                        assertEquals(expectedVetoReason, actualVetoReason);
                });
            });
        }

        public final void assertIsExplicitlyAnnotated(final boolean isExpectedExplicitlyAnnotated) {
            if(isExpectedExplicitlyAnnotated
                    && !getMetaModel().get().isExplicitlyAnnotated()) {
                fail(String.format("%s %s is not explicitly annotated", memberSort, memberName));
            }

            if(!isExpectedExplicitlyAnnotated
                    && getMetaModel().get().isExplicitlyAnnotated()) {
                fail(String.format("%s %s is explicitly annotated", memberSort, memberName));
            }
        }


    }

    private static abstract class Tester<T> {

        @Inject protected IsisConfiguration configuration;
        @Inject protected IsisSystemEnvironment isisSystemEnvironment;
        @Inject protected SpecificationLoader specificationLoader;
        @Inject protected InteractionService interactionService;
        @Inject protected FactoryService factoryService;

        @Getter private final Class<T> domainObjectType;

        @Getter private ObjectSpecification objectSpecification;
        protected ManagedObject vm;

        protected Tester(
                final @NonNull Class<T> domainObjectType) {
            this.domainObjectType = domainObjectType;
        }

        protected Tester<T> init() {
            this.objectSpecification = specificationLoader.specForTypeElseFail(domainObjectType);
            this.vm = ManagedObject.of(objectSpecification, factoryService.viewModel(domainObjectType));
            return this;
        }

    }

}