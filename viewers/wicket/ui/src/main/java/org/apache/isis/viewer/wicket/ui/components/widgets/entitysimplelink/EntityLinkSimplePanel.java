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
package org.apache.isis.viewer.wicket.ui.components.widgets.entitysimplelink;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;

import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent.CancelHintRequired;
import org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent.FormComponentPanelAbstract;

import lombok.val;

/**
 * {@link FormComponentPanel} representing a reference to an entity: a link and
 * (optionally) an autocomplete field.
 */
public class EntityLinkSimplePanel
extends FormComponentPanelAbstract<ManagedObject>
implements CancelHintRequired  {

    private static final long serialVersionUID = 1L;

    private static final String ID_ENTITY_ICON_AND_TITLE = "entityIconAndTitle";
    private static final String ID_ENTITY_TITLE_NULL = "entityTitleNull";

    public EntityLinkSimplePanel(final String id, final IModel<ManagedObject> model) {
        super(id, model);
        _Assert.assertTrue(model instanceof HasCommonContext);
        setType(ManagedObject.class);
        buildGui();
    }

    public EntityModel entityModelForLink() {
        val model = (HasCommonContext & IModel<ManagedObject>)getModel();
        return EntityModel.ofAdapter(model.getCommonContext(), model.getObject());
    }

    private void buildGui() {
        syncWithInput();
    }

    @Override
    protected void onBeforeRender() {
        syncWithInput();
        super.onBeforeRender();
    }

    private void syncWithInput() {
        val entityModelForLink = entityModelForLink();
        val entity = entityModelForLink.getObject();

        if(!ManagedObjects.isNullOrUnspecifiedOrEmpty(entity)) {

            val componentFactory = getComponentFactoryRegistry()
                    .findComponentFactory(ComponentType.ENTITY_ICON_AND_TITLE, entityModelForLink);

            final Component component = componentFactory
                    .createComponent(ID_ENTITY_ICON_AND_TITLE, entityModelForLink);
            addOrReplace(component);
            permanentlyHide(ID_ENTITY_TITLE_NULL);

        } else {
            // represent no object by a simple label displaying '(none)'
            addOrReplace(new Label(ID_ENTITY_TITLE_NULL, "(none)"));
            permanentlyHide(ID_ENTITY_TITLE_NULL);
            permanentlyHide(ID_ENTITY_ICON_AND_TITLE);
        }
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void validate() {
        // no-op since immutable
    }

}
