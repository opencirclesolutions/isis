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
package org.apache.isis.viewer.wicket.ui.components.footer;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.config.IsisConfiguration.Viewer.Wicket.Credit;
import org.apache.isis.viewer.wicket.model.util.PageParameterUtils;
import org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbPanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.themepicker.ThemeChooser;
import org.apache.isis.viewer.wicket.ui.pages.about.AboutPage;
import org.apache.isis.viewer.wicket.ui.pages.home.HomePage;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

import lombok.val;

/**
 * A panel for the default page footer
 */
public class FooterPanel
extends PanelAbstract<String, Model<String>> {

    private static final long serialVersionUID = 1L;

    private static final String ID_BREADCRUMBS = "breadcrumbs";
    private static final String ID_ABOUT_LINK = "aboutLink";
    private static final String ID_ABOUT_MESSAGE = "aboutMessage";
    private static final String ID_THEME_PICKER = "themePicker";

    /**
     * Constructor.
     *
     * @param id The component id
     */
    public FooterPanel(String id) {
        super(id);
    }


    @Override
    protected void onInitialize() {
        super.onInitialize();

        addBreadcrumbs();
        addCredits();
        addAboutLink();
        addThemePicker();
    }

    private void addCredits() {

        val credits = super.getConfiguration().getViewer().getWicket().getCredit();
        val hasAnyCredits = !_NullSafe.isEmpty(credits);

        val creditItems = new RepeatingView("creditItems");
        add(creditItems);

        if(hasAnyCredits) {

            val webAppContextPath = super.getWebAppContextPath();

            _NullSafe.stream(credits)
            .forEach(credit->{

                val listItem = new WebMarkupContainer(creditItems.newChildId());
                creditItems.add(listItem);

                val creditLink = newCreditLinkComponent(credit);
                listItem.add(creditLink);

                val imageUrl = webAppContextPath.prependContextPathIfLocal(credit.getImage());

                creditLink.add(new CreditImage("creditImage", imageUrl));
                creditLink.add(new CreditName("creditName", credit.getName()));

            });
        }

        val creditsLabel = new Label("creditsLabel", "Credits: ");
        add(creditsLabel);
        creditsLabel.setVisibilityAllowed(hasAnyCredits);
    }

    private WebMarkupContainer newCreditLinkComponent(Credit credit) {

        val creditLinkId = "creditLink";

        final WebMarkupContainer creditLink = (credit.getUrl() != null)

                ? new ExternalLink(creditLinkId, credit.getUrl()) {
                        private static final long serialVersionUID = 1L;

                        @Override
                        protected void onComponentTag(ComponentTag tag) {
                            super.onComponentTag(tag);
                            tag.put("target", "_blank");
                        }
                    }

                : new BookmarkablePageLink<>(creditLinkId, HomePage.class);

        return creditLink;
    }

    private void addBreadcrumbs() {

        boolean showBreadcrumbs = getConfiguration().getViewer().getWicket().getBookmarkedPages().isShowDropDownOnFooter();
        final Component breadcrumbPanel =
                showBreadcrumbs
                ? new BreadcrumbPanel(ID_BREADCRUMBS)
                        : new EmptyPanel(ID_BREADCRUMBS).setVisible(false);
                addOrReplace(breadcrumbPanel);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        final boolean showFooter = getConfiguration().getViewer().getWicket().isShowFooter();

        if(!showFooter) {
            setVisible(false);
            return;
        }

        PageParameters parameters = getPage().getPageParameters();
        setVisible(parameters.get(PageParameterUtils.ISIS_NO_FOOTER_PARAMETER_NAME).isNull());
    }

    private void addAboutLink() {
        final BookmarkablePageLink<Void> aboutLink = new BookmarkablePageLink<>(ID_ABOUT_LINK, AboutPage.class);
        add(aboutLink);

        String applicationVersion = getIsisConfiguration().getViewer().getWicket().getApplication().getVersion();

        final Label aboutLabel =
                applicationVersion != null && !applicationVersion.isEmpty()?
                        new Label(ID_ABOUT_MESSAGE,  applicationVersion) :
                            new Label(ID_ABOUT_MESSAGE,  new ResourceModel("aboutLabel"))
                            ;
                        aboutLink.add(aboutLabel);
                        addDevModeWarning(aboutLink);
    }

    /**
     * Adds a component that shows a warning sign next to "About" link in development mode
     * @param container The parent component
     */
    private void addDevModeWarning(final MarkupContainer container) {
        final WebComponent devModeWarning = new WebComponent("devModeWarning");
        devModeWarning.setVisible(getApplication().usesDevelopmentConfig());
        container.add(devModeWarning);
    }

    private void addThemePicker() {
        final ThemeChooser themeChooser = new ThemeChooser(ID_THEME_PICKER);
        addOrReplace(themeChooser);
    }

}
