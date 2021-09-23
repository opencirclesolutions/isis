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
package org.apache.isis.core.metamodel.specloader.specimpl;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.exceptions.unrecoverable.DomainModelException;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.commons.ClassExtensions;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResultSet;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.HasFacetHolder;
import org.apache.isis.core.metamodel.facets.TypedHolder;
import org.apache.isis.core.metamodel.facets.all.described.ParamDescribedFacet;
import org.apache.isis.core.metamodel.facets.all.named.ParamNamedFacet;
import org.apache.isis.core.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.param.autocomplete.MinLengthUtil;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.isis.core.metamodel.interactions.ActionArgUsabilityContext;
import org.apache.isis.core.metamodel.interactions.ActionArgValidityContext;
import org.apache.isis.core.metamodel.interactions.ActionArgVisibilityContext;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.NonNull;
import lombok.val;

public abstract class ObjectActionParameterAbstract
implements
    ObjectActionParameter,
    HasFacetHolder {

    private final FeatureType featureType;
    private final int number;
    private final ObjectActionDefault parentAction;
    private final TypedHolder peer;
    private final String javaSourceParamName;

    protected ObjectActionParameterAbstract(
            final FeatureType featureType,
            final int number,
            final ObjectActionDefault objectAction,
            final @NonNull TypedHolder peer) {

        this.featureType = featureType;
        this.number = number;
        this.parentAction = objectAction;
        this.peer = peer;

        this.javaSourceParamName =
                objectAction.getFacetedMethod().getMethod().getParameters()[number].getName();
    }

    @Override
    public MetaModelContext getMetaModelContext() {
        return parentAction.getMetaModelContext();
    }

    @Override
    public FeatureType getFeatureType() {
        return featureType;
    }

    @Override
    public ManagedObject get(final ManagedObject owner, final InteractionInitiatedBy interactionInitiatedBy) {
        throw _Exceptions.unexpectedCodeReach(); // not available for params
    }

    /**
     * Parameter number, 0-based.
     */
    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public ObjectAction getAction() {
        return parentAction;
    }

    /**
     * NOT API, but exposed for the benefit of {@link ObjectActionParameterContributee}
     * and {@link ObjectActionParameterMixedIn}.
     */
    public TypedHolder getPeer() {
        return peer;
    }

    @Override
    public ObjectSpecification getElementType() {
        return getSpecificationLoader().loadSpecification(peer.getType());
    }

    @Override
    public Identifier getFeatureIdentifier() {
        return parentAction.getFeatureIdentifier();
    }

    @Override
    public String getId() {
        return javaSourceParamName;
    }

    @Override
    public final String getFriendlyName(final Supplier<ManagedObject> domainObjectProvider) {
        //as we don't support imperative naming for parameters yet ..
        return staticFriendlyName();
    }

    @Override
    public final Optional<String> getStaticFriendlyName() {
        return Optional.of(staticFriendlyName());
    }

    @Override
    public final String getCanonicalFriendlyName() {
        //as we don't support imperative naming for parameters yet ..
        return staticFriendlyName();
    }

    private String staticFriendlyName() {
        return lookupFacet(ParamNamedFacet.class)
        .map(ParamNamedFacet::translated)
        .orElseThrow(()->_Exceptions
                .unrecoverableFormatted("action parameters must have a ParamNamedFacet %s", this));
    }

    @Override
    public final String getDescription(final Supplier<ManagedObject> domainObjectProvider) {
        //as we don't support imperative naming for parameters yet ..
        return staticDescription();
    }

    @Override
    public final Optional<String> getStaticDescription() {
        return Optional.of(staticDescription());
    }

    @Override
    public final String getCanonicalDescription() {
        //as we don't support imperative naming for parameters yet ..
        return staticDescription();
    }

    private String staticDescription() {
        return lookupFacet(ParamDescribedFacet.class)
        .map(ParamDescribedFacet::translated)
        .orElse("");
    }

    public Consent isUsable() {
        return Allow.DEFAULT;
    }

    // -- FacetHolder

    @Override
    public FacetHolder getFacetHolder() {
        return peer;
    }

    // -- AutoComplete

    @Override
    public boolean hasAutoComplete() {
        val actionParameterAutoCompleteFacet = getFacet(ActionParameterAutoCompleteFacet.class);
        return actionParameterAutoCompleteFacet != null;
    }

    @Override
    public Can<ManagedObject> getAutoComplete(
            final ParameterNegotiationModel pendingArgs,
            final String searchArg,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val autoCompleteFacet = getFacet(ActionParameterAutoCompleteFacet.class);
        if (autoCompleteFacet == null) {
            return Can.empty();
        }

        val visibleChoices = autoCompleteFacet
                .autoComplete(pendingArgs.getActionTarget(), pendingArgs.getParamValues(), searchArg, interactionInitiatedBy);
        checkChoicesOrAutoCompleteType(getSpecificationLoader(), visibleChoices, getElementType());

        return visibleChoices;
    }

    @Override
    public int getAutoCompleteMinLength() {
        final ActionParameterAutoCompleteFacet facet = getFacet(ActionParameterAutoCompleteFacet.class);
        return facet != null? facet.getMinLength(): MinLengthUtil.MIN_LENGTH_DEFAULT;
    }


    // -- Choices

    @Override
    public boolean hasChoices() {
        val choicesFacet = getFacet(ActionParameterChoicesFacet.class);
        return choicesFacet != null;
    }

    @Override
    public Can<ManagedObject> getChoices(
            final ParameterNegotiationModel pendingArgs,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val paramSpec = getElementType();
        val choicesFacet = getFacet(ActionParameterChoicesFacet.class);
        if (choicesFacet == null) {
            return Can.empty();
        }

        val visibleChoices = choicesFacet.getChoices(paramSpec,
                pendingArgs.getHead(),
                pendingArgs.getParamValues(),
                interactionInitiatedBy);
        checkChoicesOrAutoCompleteType(getSpecificationLoader(), visibleChoices, paramSpec);

        return visibleChoices;
    }

    // -- Defaults

    @Override
    @NonNull
    public ManagedObject getDefault(
            final @NonNull ParameterNegotiationModel pendingArgs) {

        val paramSpec = getElementType();
        val defaultsFacet = getFacet(ActionParameterDefaultsFacet.class);
        if (defaultsFacet != null && !defaultsFacet.getPrecedence().isFallback()) {
            final Object paramValuePojo = defaultsFacet.getDefault(pendingArgs);
            return ManagedObjects.emptyToDefault(
                    !isOptional(),
                    ManagedObject.of(paramSpec, paramValuePojo));
        }
        return ManagedObjects.emptyToDefault(
                !isOptional(),
                pendingArgs.getParamValue(getNumber()));
    }

    // helpers
    static void checkChoicesOrAutoCompleteType(
            final SpecificationLoader specificationLookup,
            final Can<ManagedObject> choices,
            final ObjectSpecification paramSpec) {

        for (final ManagedObject choice : choices) {

            val choicePojo = choice.getPojo();

            if(choicePojo == null) {
                continue;
            }

            // check type, but wrap first
            // (eg we treat int.class and java.lang.Integer.class as compatible with each other)
            final Class<?> choiceClass = choicePojo.getClass();
            final Class<?> paramClass = paramSpec.getCorrespondingClass();

            final Class<?> choiceWrappedClass = ClassExtensions.asWrappedIfNecessary(choiceClass);
            final Class<?> paramWrappedClass = ClassExtensions.asWrappedIfNecessary(paramClass);

            final ObjectSpecification choiceWrappedSpec = specificationLookup.loadSpecification(choiceWrappedClass);
            final ObjectSpecification paramWrappedSpec = specificationLookup.loadSpecification(paramWrappedClass);

            // type returned by choices must be an instance of the param type
            // in other words <param type> is assignable from <choices type>

            // TODO: should implement this instead as a MetaModelValidator
            if (!choiceWrappedSpec.isOfType(paramWrappedSpec)) {
                throw new DomainModelException(String.format(
                        "Type incompatible with parameter type; expected %s, but was %s",
                        paramSpec.getFullIdentifier(), choiceClass.getName()));
            }
        }
    }


    // > Visibility

    private ActionArgVisibilityContext createArgumentVisibilityContext(
            final InteractionHead head,
            final Can<ManagedObject> pendingArgs,
            final int position,
            final InteractionInitiatedBy interactionInitiatedBy) {

        return new ActionArgVisibilityContext(
                head, parentAction, getFeatureIdentifier(), pendingArgs, position, interactionInitiatedBy);
    }

    @Override
    public Consent isVisible(
            final InteractionHead head,
            final Can<ManagedObject> pendingArgs,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val visibilityContext = createArgumentVisibilityContext(
                head, pendingArgs, getNumber(), interactionInitiatedBy);

        return InteractionUtils.isVisibleResult(this, visibilityContext).createConsent();
    }

    // > Usability

    private ActionArgUsabilityContext createArgumentUsabilityContext(
            final InteractionHead head,
            final Can<ManagedObject> pendingArgs,
            final int position,
            final InteractionInitiatedBy interactionInitiatedBy) {

        return new ActionArgUsabilityContext(
                head,
                parentAction,
                getFeatureIdentifier(),
                pendingArgs,
                position,
                interactionInitiatedBy);
    }

    @Override
    public Consent isUsable(
            final InteractionHead head,
            final Can<ManagedObject> pendingArgs,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val usabilityContext = createArgumentUsabilityContext(
                head, pendingArgs, getNumber(), interactionInitiatedBy);

        val usableResult = InteractionUtils.isUsableResult(this, usabilityContext);
        return usableResult.createConsent();
    }


    // -- Validation

    @Override
    public ActionArgValidityContext createProposedArgumentInteractionContext(
            final InteractionHead head,
            final Can<ManagedObject> proposedArguments,
            final int position,
            final InteractionInitiatedBy interactionInitiatedBy) {

        return new ActionArgValidityContext(
                head, parentAction, getFeatureIdentifier(), proposedArguments, position, interactionInitiatedBy);
    }

    @Override
    public Consent isValid(
            final InteractionHead head,
            final Can<ManagedObject> pendingArgs,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val validityContext = createProposedArgumentInteractionContext(
                head, pendingArgs, getNumber(), interactionInitiatedBy);

        val validResult = InteractionUtils.isValidResult(this, validityContext);
        return validResult.createConsent();
    }

    @Override @Deprecated
    public String isValid(
            final InteractionHead head,
            final ManagedObject proposedValue,
            final InteractionInitiatedBy interactionInitiatedBy) {

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(proposedValue)) {
            return null;
        }

        val argumentAdapters = arguments(proposedValue);
        val validityContext = createProposedArgumentInteractionContext(
                head, argumentAdapters, getNumber(), interactionInitiatedBy);

        final InteractionResultSet buf = new InteractionResultSet();
        InteractionUtils.isValidResultSet(this, validityContext, buf);
        if (buf.isVetoed()) {
            return buf.getInteractionResult().getReason();
        }
        return null;

    }

    /**
     * TODO: this is not ideal, because we can only populate the array for
     * single argument, rather than the entire argument set. Instead, we ought
     * to do this in two passes, one to build up the argument set as a single
     * unit, and then validate each in turn.
     */
    @Deprecated
    private Can<ManagedObject> arguments(final ManagedObject proposedValue) {
        final int paramCount = getAction().getParameterCount();
        final int paramIndex = getNumber();
        val arguments = new ArrayList<ManagedObject>(paramCount);
        for(int i=0; i<paramCount; ++i) {
            arguments.add(i==paramIndex ? proposedValue : ManagedObject.empty(getAction().getParameterTypes().getElseFail(paramIndex)));
        }
        return Can.ofCollection(arguments);
    }

}
