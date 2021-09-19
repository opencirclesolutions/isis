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
package org.apache.isis.viewer.wicket.model.models.interaction.action;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.viewer.common.model.HasParentUiModel;
import org.apache.isis.viewer.common.model.feature.ParameterUiModel;
import org.apache.isis.viewer.common.model.object.ObjectUiModel.HasRenderingHints;
import org.apache.isis.viewer.common.model.object.ObjectUiModel.Mode;
import org.apache.isis.viewer.common.model.object.ObjectUiModel.RenderingHint;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.links.LinksProvider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public final class ParameterUiModelWkt
extends _ActionInteractionHolder
implements
    HasParentUiModel<ObjectUiModelWkt>,
    HasRenderingHints, ParameterUiModel, LinksProvider {

    private static final long serialVersionUID = 1L;

    final int paramIndex;

    public ParameterUiModelWkt(
            final ActionInteractionModelWkt model,
            final int paramIndex) {
        super(model);
        this.paramIndex = paramIndex;
    }

    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private Mode mode;

    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private RenderingHint renderingHint;

    @Override
    public ObjectUiModelWkt getParentUiModel() {
        return ()->getOwner();
    }

    @Override
    public ManagedObject getOwner() {
        return actionInteraction().getManagedAction().get().getOwner();
    }

    @Override
    public ObjectActionParameter getMetaModel() {
        return actionInteraction().getMetamodel().get().getParameters().getElseFail(paramIndex);
    }

    @Override
    public Can<LinkAndLabel> getLinks() {
        return Can.empty();
    }

    @Override
    public boolean isInlinePrompt() {
        return false;
    }

    @Override
    public String getCssClass() {
        return getMetaModel().getCssClass("isis-");
    }

    @Override
    public @NonNull ManagedObject getValue() {
        return getPendingParameterModel().getParamValue(paramIndex);
    }

    @Override
    public void setValue(final ManagedObject paramValue) {
        getPendingParameterModel().setParamValue(paramIndex, paramValue);
    }

    @Override
    public ParameterNegotiationModel getPendingParameterModel() {
        return containerModel().parameterNegotiationModel().get();
    }

}
