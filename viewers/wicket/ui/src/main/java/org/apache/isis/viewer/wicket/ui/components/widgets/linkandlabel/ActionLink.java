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
package org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel;

import org.apache.wicket.Application;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.markup.ComponentTag;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.debug._Probe.EntryPoint;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettingsAccessor;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.util.CommonContextUtils;
import org.apache.isis.viewer.wicket.ui.panels.PanelUtil;

import lombok.val;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;

public abstract class ActionLink extends AjaxLink<ManagedObject> implements IAjaxIndicatorAware {

    private static final long serialVersionUID = 1L;

    private final AjaxIndicatorAppender indicatorAppenderIfAny;

    protected transient IsisAppCommonContext commonContext;

    ActionLink(IsisAppCommonContext commonContext, String id, ActionModel model) {
        super(id, model);

        this.commonContext = commonContext;

        final boolean useIndicatorForNoArgAction = getSettings().isUseIndicatorForNoArgAction();
        this.indicatorAppenderIfAny =
                useIndicatorForNoArgAction
                ? new AjaxIndicatorAppender()
                : null;

        if(this.indicatorAppenderIfAny != null) {
            this.add(this.indicatorAppenderIfAny);
        }

        // trivial optimization; also store the objectAction if it is available (saves looking it up)
        objectAction = model.getMetaModel();

    }

    public IsisAppCommonContext getCommonContext() {
        return commonContext = CommonContextUtils.computeIfAbsent(commonContext);
    }

    @Override
    public void onClick(AjaxRequestTarget target) {

        _Probe.entryPoint(EntryPoint.USER_INTERACTION, "Wicket Ajax Request, "
                + "originating from User clicking an Action Link.");

        doOnClick(target);
    }

    protected abstract void doOnClick(AjaxRequestTarget target);

    ActionModel getActionModel() {
        val actionModel = (ActionModel) getModel();
        return actionModel;
    }

    private transient ObjectAction objectAction;

    public ObjectAction getObjectAction() {
        return objectAction != null
                ? objectAction
                : (objectAction = getActionModel().getMetaModel());
    }


    @Override
    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
        super.updateAjaxAttributes(attributes);
        if(getSettings().isPreventDoubleClickForNoArgAction()) {
            PanelUtil.disableBeforeReenableOnComplete(attributes, this);
        }

        // allow the event to bubble so the menu is hidden after click on an item
        attributes.setEventPropagation(AjaxRequestAttributes.EventPropagation.BUBBLE);
    }

    public String getReasonDisabledIfAny() {
        // no point evaluating if not visible
        return isVisible() ? getActionModel().getUsabilityConsent().getReason() : null;
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        // according to
        //   http://wicketinaction.com/2011/11/implement-wicket-component-visibility-changes-properly/
        // and
        //   http://apache-wicket.1842946.n4.nabble.com/vote-release-Wicket-1-4-14-td3056628.html#a3063795
        // should be using onConfigure rather than overloading.
        //
        // eg:
        //        setVisible(determineIfVisible());
        //        setEnabled(determineIfEnabled());
        //
        // and no longer override isVisible() and isEnabled().
        //
        // however, in the case of a button already rendered as visible/enabled that (due to changes
        // elsewhere in the state of the server-side system) should then become invisible/disabled, it seems
        // that onConfigure isn't called and so the action continues to display the prompt.
        // A check is only made when hit OK of the prompt.  This is too late to display a message, so (until
        // figure out a better way) gonna continue to override isVisible() and isEnabled()
    }

    @Override
    public boolean isVisible() {
        return getActionModel().getVisibilityConsent().isAllowed();
    }

    @Override
    @Programmatic
    public boolean isEnabled() {
        return getActionModel().getUsabilityConsent().isAllowed();
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        Buttons.fixDisabledState(this, tag);
    }

    @Override
    public String getAjaxIndicatorMarkupId() {
        return this.indicatorAppenderIfAny != null
                ? this.indicatorAppenderIfAny.getMarkupId()
                : null;
    }

    protected WicketViewerSettings getSettings() {
        return ((WicketViewerSettingsAccessor) Application.get()).getSettings();
    }

}
