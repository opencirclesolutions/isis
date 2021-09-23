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
package org.apache.isis.core.metamodel.facets.jaxb;

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.recreatable.RecreatableObjectFacetForXmlRootElementAnnotation;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;

import static org.apache.isis.commons.internal.reflection._Reflect.Filter.isPublic;
import static org.apache.isis.commons.internal.reflection._Reflect.Filter.paramCount;

import lombok.val;

/**
 * just adds a validator
 */
public class JaxbFacetFactory
extends FacetFactoryAbstract
implements MetaModelRefiner {

    @Inject
    public JaxbFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_AND_PROPERTIES);
    }

    // -- CLASS CONTEXT

    @Override
    public void process(final ProcessClassContext processClassContext) {
        processXmlJavaTypeAdapter(processClassContext);
        processXmlAccessorTypeFacet(processClassContext);
    }

    private void processXmlJavaTypeAdapter(final ProcessClassContext processClassContext) {

        val xmlJavaTypeAdapterIfAny = processClassContext.synthesizeOnType(XmlJavaTypeAdapter.class);
        if(!xmlJavaTypeAdapterIfAny.isPresent()) {
            return;
        }

        val facetHolder = processClassContext.getFacetHolder();

        addFacet(
                new XmlJavaTypeAdapterFacetDefault(facetHolder, xmlJavaTypeAdapterIfAny.get().value()));
    }

    private void processXmlAccessorTypeFacet(final ProcessClassContext processClassContext) {

        val xmlAccessorTypeIfAny = processClassContext.synthesizeOnType(XmlAccessorType.class);
        if(!xmlAccessorTypeIfAny.isPresent()) {
            return;
        }

        val facetHolder = processClassContext.getFacetHolder();
        addFacet(
                new XmlAccessorTypeFacetDefault(facetHolder, xmlAccessorTypeIfAny.get().value()));
    }

    // -- METHOD CONTEXT

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        //[ahuber] accessType not yet used, but could be in future extensions
        final Optional<XmlAccessorTypeFacet> accessorTypeFacet =
                Optional.ofNullable(processMethodContext.getFacetHolder().getFacet(XmlAccessorTypeFacet.class));
        final XmlAccessType accessType = accessorTypeFacet
                .map(facet->facet.value())
                .orElse(XmlAccessType.PUBLIC_MEMBER); // the annotation's default value
        // ---

        processXmlJavaTypeAdapter(processMethodContext, accessType);
        processXmlTransient(processMethodContext, accessType);

    }

    private void processXmlJavaTypeAdapter(final ProcessMethodContext processMethodContext, final XmlAccessType accessType) {

        val xmlJavaTypeAdapterIfAny = processMethodContext.synthesizeOnMethod(XmlJavaTypeAdapter.class);

        if(!xmlJavaTypeAdapterIfAny.isPresent()) {
            return;
        }

        val facetHolder = processMethodContext.getFacetHolder();
        addFacet(
                new XmlJavaTypeAdapterFacetDefault(facetHolder, xmlJavaTypeAdapterIfAny.get().value()));
    }

    private void processXmlTransient(final ProcessMethodContext processMethodContext, final XmlAccessType accessType) {

        val xmlTransientIfAny = processMethodContext.synthesizeOnMethod(XmlTransient.class);

        if(!xmlTransientIfAny.isPresent()) {
            return;
        }

        val facetHolder = processMethodContext.getFacetHolder();
        addFacet(new XmlTransientFacetDefault(facetHolder));
    }

    // --

    @Override
    public void refineProgrammingModel(final ProgrammingModel programmingModel) {

        final List<TypeValidator> typeValidators = getTypeValidators(getConfiguration());
        final List<PropertyValidator> propertyValidators = getPropertyValidators(getConfiguration());

        programmingModel.addVisitingValidatorSkipManagedBeans(objectSpec->{

            final boolean viewModel = objectSpec.isViewModel();
            if(!viewModel) {
                return;
            }

            final ViewModelFacet facet = objectSpec.getFacet(ViewModelFacet.class);
            if (!(facet instanceof RecreatableObjectFacetForXmlRootElementAnnotation)) {
                return;
            }

            for (final TypeValidator typeValidator : typeValidators) {
                typeValidator.validate(objectSpec);
            }

            final Stream<OneToOneAssociation> properties = objectSpec
                    .streamProperties(MixedIn.EXCLUDED);

            properties
            // ignore derived
            .filter(property->property.containsNonFallbackFacet(PropertySetterFacet.class))
            .forEach(property->{
                for (final PropertyValidator adapterValidator : propertyValidators) {
                    adapterValidator.validate(objectSpec, property);
                }
            });

        });

    }

    private List<TypeValidator> getTypeValidators(final IsisConfiguration configuration) {

        final List<TypeValidator> typeValidators = _Lists.newArrayList();
        if(configuration.getCore().getMetaModel().getValidator().getJaxbViewModel().isNotAbstract()) {
            typeValidators.add(new JaxbViewModelNotAbstractValidator());
        }
        if(configuration.getCore().getMetaModel().getValidator().getJaxbViewModel().isNotInnerClass()) {
            typeValidators.add(new JaxbViewModelNotInnerClassValidator());
        }
        if(configuration.getCore().getMetaModel().getValidator().getJaxbViewModel().isNoArgConstructor()) {
            typeValidators.add(new JaxbViewModelPublicNoArgConstructorValidator());
        }
        return typeValidators;
    }

    private List<PropertyValidator> getPropertyValidators(final IsisConfiguration configuration) {
        final List<PropertyValidator> propertyValidators = _Lists.newArrayList();
        if(configuration.getCore().getMetaModel().getValidator().getJaxbViewModel().isReferenceTypeAdapter()) {
            propertyValidators.add(new PropertyValidatorForReferenceTypes());
        }
        if(configuration.getCore().getMetaModel().getValidator().getJaxbViewModel().isDateTimeTypeAdapter()) {
            propertyValidators.add(new PropertyValidatorForDateTypes(java.sql.Timestamp.class));
            propertyValidators.add(new PropertyValidatorForDateTypes(ZonedDateTime.class));
            propertyValidators.add(new PropertyValidatorForDateTypes(OffsetDateTime.class));
            propertyValidators.add(new PropertyValidatorForDateTypes(LocalDate.class));
            propertyValidators.add(new PropertyValidatorForDateTypes(LocalDateTime.class));
            propertyValidators.add(new PropertyValidatorForDateTypes(LocalTime.class));
            propertyValidators.add(new PropertyValidatorForDateTypes(org.joda.time.DateTime.class));
            propertyValidators.add(new PropertyValidatorForDateTypes(org.joda.time.LocalDate.class));
            propertyValidators.add(new PropertyValidatorForDateTypes(org.joda.time.LocalDateTime.class));
            propertyValidators.add(new PropertyValidatorForDateTypes(org.joda.time.LocalTime.class));
        }
        return propertyValidators;
    }

    private static abstract class TypeValidator {
        abstract void validate(ObjectSpecification objectSpec);

    }

    private static abstract class PropertyValidator {
        abstract void validate(
                ObjectSpecification objectSpec,
                OneToOneAssociation property);

    }

    private static class PropertyValidatorForReferenceTypes extends PropertyValidator {

        @Override
        void validate(
                final ObjectSpecification objectSpec,
                final OneToOneAssociation property) {

            final ObjectSpecification propertyTypeSpec = property.getElementType();
            if (!propertyTypeSpec.isEntity()) {
                return;
            }

            final XmlJavaTypeAdapterFacet xmlJavaTypeAdapterFacet =
                    propertyTypeSpec.getFacet(XmlJavaTypeAdapterFacet.class);
            if(xmlJavaTypeAdapterFacet != null) {
                return;
            }
            final Class<?> propertyType = propertyTypeSpec.getCorrespondingClass();
            ValidationFailure.raiseFormatted(
                    property,
                    "JAXB view model '%s' property '%s' is of type '%s' but that type is not annotated with @XmlJavaTypeAdapter.  The type must be annotated with @XmlJavaTypeAdapter(org.apache.isis.applib.jaxb.PersistentEntityAdapter.class) or equivalent.",
                    objectSpec.getFullIdentifier(),
                    property.getId(),
                    propertyType.getName());

        }
    }

    private static class PropertyValidatorForDateTypes extends PropertyValidator {
        private final Class<?> jodaType;

        private PropertyValidatorForDateTypes(final Class<?> jodaType) {
            this.jodaType = jodaType;
        }

        @Override
        void validate(
                final ObjectSpecification objectSpec,
                final OneToOneAssociation property) {

            final ObjectSpecification propertyTypeSpec = property.getElementType();
            final Class<?> propertyType = propertyTypeSpec.getCorrespondingClass();

            if (!jodaType.isAssignableFrom(propertyType)) {
                return;
            }

            final XmlJavaTypeAdapterFacet xmlJavaTypeAdapterFacet = property.getFacet(XmlJavaTypeAdapterFacet.class);
            if (xmlJavaTypeAdapterFacet != null) {
                return;
            }

            final XmlTransientFacet xmlTransientFacet =
                    property.getFacet(XmlTransientFacet.class);
            if(xmlTransientFacet != null) {
                return;
            }

            // else
            ValidationFailure.raiseFormatted(
                    property,
                    "JAXB view model '%s' property '%s' is of type '%s' but is not annotated with @XmlJavaTypeAdapter.  The field/method must be annotated with @XmlJavaTypeAdapter(org.apache.isis.schema.utils.jaxbadapters.XxxAdapter.ForJaxb.class) or equivalent, or be ignored by being annotated with @XmlTransient.",
                    objectSpec.getFullIdentifier(),
                    property.getId(),
                    jodaType.getName());
        }
    }

    private static class JaxbViewModelNotAbstractValidator extends TypeValidator {
        @Override
        void validate(
                final ObjectSpecification objectSpec) {

            if(objectSpec.isAbstract()) {
                ValidationFailure.raise(
                        objectSpec,
                        String.format("JAXB view model '%s' is abstract", objectSpec.getFullIdentifier())
                        );
            }
        }
    }

    private static class JaxbViewModelNotInnerClassValidator extends TypeValidator {
        @Override
        void validate(
                final ObjectSpecification objectSpec) {

            final Class<?> correspondingClass = objectSpec.getCorrespondingClass();
            if(correspondingClass.isAnonymousClass()) {
                ValidationFailure.raiseFormatted(
                        objectSpec,
                        "JAXB view model '%s' is an anonymous class",
                        objectSpec.getFullIdentifier());
            } else if(correspondingClass.isLocalClass()) {
                ValidationFailure.raiseFormatted(
                        objectSpec,
                        "JAXB view model '%s' is a local class",
                        objectSpec.getFullIdentifier());
            } else if(correspondingClass.isMemberClass() && !Modifier.isStatic(correspondingClass.getModifiers())) {
                ValidationFailure.raiseFormatted(
                        objectSpec,
                        "JAXB view model '%s' is an non-static inner class",
                        objectSpec.getFullIdentifier());
            }
        }
    }

    private static class JaxbViewModelPublicNoArgConstructorValidator extends TypeValidator {
        @Override
        void validate(final ObjectSpecification objectSpec) {

            val correspondingClass = objectSpec.getCorrespondingClass();

            val publicNoArgConstructors = _Reflect
                    .getPublicConstructors(correspondingClass)
                    .filter(paramCount(0));

            if(publicNoArgConstructors.getCardinality().isOne()) {
                return; // happy case
            }

            val privateNoArgConstructors = _Reflect
                    .getDeclaredConstructors(correspondingClass)
                    .filter(paramCount(0).and(isPublic().negate()));

            if(privateNoArgConstructors.isNotEmpty()) {
                ValidationFailure.raiseFormatted(
                        objectSpec,
                        "JAXB view model '%s' has a no-arg constructor, however it is not public",
                        objectSpec.getFullIdentifier());
            } else {
                ValidationFailure.raiseFormatted(
                        objectSpec,
                        "JAXB view model '%s' does not have a public no-arg constructor",
                        objectSpec.getFullIdentifier());
            }
        }
    }
}
