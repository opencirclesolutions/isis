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
package org.apache.causeway.viewer.wicket.ui.components.widgets.entitysimplelink;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory.ApplicationAdvice;
import org.apache.causeway.viewer.wicket.ui.ComponentFactoryAbstract;

public class EntityLinkSimplePanelFactory extends ComponentFactoryAbstract {

    private static final long serialVersionUID = 1L;

    public EntityLinkSimplePanelFactory() {
        super(UiComponentType.ENTITY_LINK, EntityLinkSimplePanel.class);
    }

    @Override
    public ApplicationAdvice appliesTo(final IModel<?> model) {
        if (model instanceof UiObjectWkt) {
            return ApplicationAdvice.APPLIES;
        }
        if (model instanceof ScalarModel) {
            return ApplicationAdvice.APPLIES;
        }
        return ApplicationAdvice.DOES_NOT_APPLY;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        return new EntityLinkSimplePanel(id, (IModel<ManagedObject>)model);
    }
}