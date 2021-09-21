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

import org.apache.wicket.model.ChainingModel;

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.common.model.object.ObjectUiModel.EitherViewOrEdit;
import org.apache.isis.viewer.common.model.object.ObjectUiModel.RenderingHint;

/**
 * Wraps a {@link ScalarModel} to act as an {@link ObjectAdapterModel}.
 */
public final class AdapterForObjectReference
extends ChainingModel<ManagedObject>
implements ObjectAdapterModel {

    private static final long serialVersionUID = 1L;

    public static AdapterForObjectReference chain(final ScalarModel scalarModel) {
        return new AdapterForObjectReference(scalarModel);
    }

    private AdapterForObjectReference(final ScalarModel scalarModel) {
        super(scalarModel);
    }

    public ScalarModel scalarModel() {
        return (ScalarModel) super.getTarget();
    }

    @Override
    public ManagedObject getObject() {
        return scalarModel().getPendingElseCurrentAdapter();
    }

    @Override
    public RenderingHint getRenderingHint() {
        return scalarModel().getRenderingHint();
    }

    @Override
    public void setRenderingHint(final RenderingHint renderingHint) {
        scalarModel().setRenderingHint(renderingHint);
    }

    @Override
    public ObjectSpecification getTypeOfSpecification() {
        return scalarModel().getScalarTypeSpec();
    }

    @Override
    public EntityModel.EitherViewOrEdit getMode() {
        return EntityModel.EitherViewOrEdit.VIEW;
    }

    @Override
    public boolean isInlinePrompt() {
        return scalarModel().getPromptStyle().isInlineOrInlineAsIfEdit()
                && scalarModel().isEnabled();
    }

    @Override
    public boolean isContextAdapter(final ManagedObject other) {
        return false;
    }

    @Override
    public void setMode(final EitherViewOrEdit mode) {
        throw _Exceptions.unexpectedCodeReach();
    }

}
