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
package org.apache.isis.viewer.wicket.model.models;

import java.util.List;
import java.util.Optional;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.core.metamodel.interactions.managed.ManagedValue;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.common.model.feature.ScalarUiModel;
import org.apache.isis.viewer.common.model.object.ObjectUiModel;
import org.apache.isis.viewer.common.model.object.ObjectUiModel.EitherViewOrEdit;
import org.apache.isis.viewer.common.model.object.ObjectUiModel.HasRenderingHints;
import org.apache.isis.viewer.common.model.object.ObjectUiModel.RenderingHint;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.links.LinksProvider;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Represents a scalar of an entity, either a {@link EitherParamOrProp#PROPERTY property} or
 * a {@link EitherParamOrProp#PARAMETER parameter}.
 *
 * <p>
 * Is the backing model to each of the fields that appear in forms (for entities
 * or action dialogs).
 *
 */
//@Log4j2
public abstract class ScalarModel
extends ManagedObjectModel
implements HasRenderingHints, ScalarUiModel, LinksProvider, FormExecutorContext {

    private static final long serialVersionUID = 1L;

    private enum EitherParamOrProp {
        PROPERTY,
        PARAMETER;
    }

    @Getter @NonNull private final EitherParamOrProp paramOrProp;
    public boolean isProperty() { return paramOrProp == EitherParamOrProp.PROPERTY; }
    public boolean isParameter() { return paramOrProp == EitherParamOrProp.PARAMETER; }

    private final EntityModel parentEntityModel;

    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private EitherViewOrEdit mode;

    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private RenderingHint renderingHint;

    /**
     * Creates a model representing an action parameter of an action of a parent
     * object, with the {@link #getObject() value of this model} to be default
     * value (if any) of that action parameter.
     */
    protected ScalarModel(
            final EntityModel parentEntityModel) {
        this(EitherParamOrProp.PARAMETER,
                parentEntityModel, EitherViewOrEdit.EDIT, RenderingHint.REGULAR);
    }

    /**
     * Creates a model representing a property of a parent object, with the
     * {@link #getObject() value of this model} to be current value of the
     * property.
     */
    protected ScalarModel(
            final EntityModel parentEntityModel,
            final ObjectUiModel.EitherViewOrEdit viewOrEdit,
            final ObjectUiModel.RenderingHint renderingHint) {
        this(EitherParamOrProp.PROPERTY,
                parentEntityModel, viewOrEdit, renderingHint);
    }

    private ScalarModel(
            final @NonNull EitherParamOrProp paramOrProp,
            final @NonNull EntityModel parentEntityModel,
            final @NonNull ObjectUiModel.EitherViewOrEdit viewOrEdit,
            final @NonNull ObjectUiModel.RenderingHint renderingHint) {

        super(parentEntityModel.getCommonContext());
        this.paramOrProp = paramOrProp;
        this.parentEntityModel = parentEntityModel;
        this.pendingModel = new PendingModel(this);
        this.mode = viewOrEdit;
        this.renderingHint = renderingHint;
    }


    protected ManagedObject loadFromSuper() {
        return super.load();
    }

    @Override
    public EntityModel getParentUiModel() {
        return parentEntityModel;
    }

    @Override
    public ManagedObject getOwner() {
        return getParentUiModel().getObject();
    }

    /**
     * Overrides superclass' implementation, because a {@link ScalarModel} can
     * know the {@link ObjectSpecification of} the {@link ManagedObject adapter}
     * without there necessarily having any adapter
     * {@link #setObject(ManagedObject) set}.
     */
    @Override
    public ObjectSpecification getTypeOfSpecification() {
        return getScalarTypeSpec();
    }

    @Override
    public Optional<LogicalType> getLogicalElementType() {
        return Optional.ofNullable(getScalarTypeSpec())
                .map(ObjectSpecification::getLogicalType);
    }


    public boolean isScalarTypeAnyOf(final Class<?>... requiredClass) {
        final String fullName = getTypeOfSpecification().getFullIdentifier();
        return _NullSafe.stream(requiredClass)
                .map(Class::getName)
                .anyMatch(fullName::equals);
    }

    public boolean isScalarTypeSubtypeOf(final Class<?> requiredClass) {
        final Class<?> scalarType = getTypeOfSpecification().getCorrespondingClass();
        return _NullSafe.streamNullable(requiredClass)
                .anyMatch(x -> x.isAssignableFrom(scalarType));
    }

    /** get the proposed value, subject to negotiation */
    public String getObjectAsString() {
        final var proposedValue = proposedValue();
        setObject(proposedValue.getValue().getValue()); // keep the wicket model in sync
        return proposedValue.getValueAsParsableText().getValue();
    }

    /**
     * set the proposed value, subject to negotiation, only updates the negotiation model
     * actual application of the proposed value is only applied after passing verification (not done here)
     */
    public void setObjectAsString(final String enteredText) {
        final var proposedValue = proposedValue();
        proposedValue.getValueAsParsableText().setValue(enteredText);
        setObject(proposedValue.getValue().getValue()); // keep the wicket model in sync
    }

    public abstract ManagedValue proposedValue();

    public abstract boolean whetherHidden();

    public abstract String whetherDisabled();

    public abstract String validate(ManagedObject proposedAdapter);

    public boolean isRequired() {
        return !getMetaModel().isOptional();
    }

    public abstract String getCssClass();

    public final <T extends Facet> T getFacet(final Class<T> facetType) {
        return getMetaModel().getFacet(facetType);
    }

    /**
     * Additional links to render (if any)
     */
    private List<LinkAndLabel> linkAndLabels = _Lists.newArrayList();

    @Override
    public Can<LinkAndLabel> getLinks() {
        return Can.ofCollection(linkAndLabels);
    }

    @Override
    public final PromptStyle getPromptStyle() {
        final PromptStyleFacet facet = getFacet(PromptStyleFacet.class);
        if(facet == null) {
            // don't think this can happen actually, see PromptStyleFacetFallback
            return PromptStyle.INLINE;
        }
        PromptStyle promptStyle = facet.value();
        if (promptStyle == PromptStyle.AS_CONFIGURED) {
            // I don't think this can happen, actually...
            // when the metamodel is built, it should replace AS_CONFIGURED with one of the other prompts
            // (see PromptStyleConfiguration and PromptStyleFacetFallback)
            return PromptStyle.INLINE;
        }
        return promptStyle;
    }

    public boolean canEnterEditMode() {
        boolean editable = isEnabled();
        return editable && isViewMode();
    }

    public boolean isEnabled() {
        return whetherDisabled() == null;
    }


    @Override
    protected void onDetach() {
        clearPending();
        super.onDetach();
    }

    // //////////////////////////////////////

    private InlinePromptContext inlinePromptContext;

    /**
     * Further hint, to support inline prompts...
     */
    @Override
    public InlinePromptContext getInlinePromptContext() {
        return inlinePromptContext;
    }

    public void setInlinePromptContext(final InlinePromptContext inlinePromptContext) {
        if (this.inlinePromptContext != null) {
            // otherwise the components created for an property edit inline prompt will overwrite the original
            // components on the underlying page (which we go back to if the prompt is cancelled).
            return;
        }
        this.inlinePromptContext = inlinePromptContext;
    }

    protected transient AssociatedActions associatedActions;

    public static class AssociatedActions {
        private final ObjectAction firstAssociatedWithInlineAsIfEdit;
        private final List<ObjectAction> remainingAssociated;

        AssociatedActions(final Can<ObjectAction> allAssociated) {
            firstAssociatedWithInlineAsIfEdit = firstAssociatedActionWithInlineAsIfEdit(allAssociated);
            remainingAssociated = (firstAssociatedWithInlineAsIfEdit != null)
                    ? allAssociated.remove(firstAssociatedWithInlineAsIfEdit).toList()
                    : allAssociated.toList();
        }

        public List<ObjectAction> getRemainingAssociated() {
            return remainingAssociated;
        }
        public ObjectAction getFirstAssociatedWithInlineAsIfEdit() {
            return firstAssociatedWithInlineAsIfEdit;
        }
        public boolean hasAssociatedActionWithInlineAsIfEdit() {
            return firstAssociatedWithInlineAsIfEdit != null;
        }

        private static ObjectAction firstAssociatedActionWithInlineAsIfEdit(final Can<ObjectAction> objectActions) {
            for (ObjectAction objectAction : objectActions) {
                final PromptStyle promptStyle = ObjectAction.Util.promptStyleFor(objectAction);
                if(promptStyle.isInlineAsIfEdit()) {
                    return objectAction;
                }
            }
            return null;
        }
    }


    /**
     * Whether this model should be surfaced in the UI using a widget rendered such that it is either already in
     * edit mode (eg for a parameter), or can be switched into edit mode, eg for an editable property or an
     * associated action of a property with 'inline_as_if_edit'
     *
     * @return <tt>true</tt> if the widget for this model must be editable.
     */
    public boolean mustBeEditable() {
        return getMode() == EitherViewOrEdit.EDIT
                || getParamOrProp() == EitherParamOrProp.PARAMETER
                || hasAssociatedActionWithInlineAsIfEdit();
    }

    /**
     * Similar to {@link #mustBeEditable()}, though not called from the same locations.
     *
     * My suspicion is that it amounts to more or less the same set of conditions.
     */
    @Override
    public boolean isInlinePrompt() {
        return (getPromptStyle().isInline() && canEnterEditMode())
                || hasAssociatedActionWithInlineAsIfEdit();
    }

    @Override
    public String toString() {
        return toStringOf();
    }

    protected abstract String toStringOf();

    protected abstract ObjectSpecification getScalarTypeSpec();

    public abstract String getIdentifier();

    public AssociatedActions getAssociatedActions() {
        if (associatedActions == null) {
            associatedActions = new AssociatedActions(calcAssociatedActions());
        }
        return associatedActions;
    }

    protected abstract Can<ObjectAction> calcAssociatedActions();

    public final boolean hasAssociatedActionWithInlineAsIfEdit() {
        return getAssociatedActions().hasAssociatedActionWithInlineAsIfEdit();
    }

    // -- PENDING STUFF

    @Getter(value = AccessLevel.PACKAGE)
    private final PendingModel pendingModel;

    public ManagedObject getPendingElseCurrentAdapter() {
        return pendingModel.getPendingElseCurrentAdapter();
    }

    public void clearPending() {
        pendingModel.clearPending();
    }


    // --

}
