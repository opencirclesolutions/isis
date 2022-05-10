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
package org.apache.isis.extensions.viewer.wicket.pdfjs.wkt.integration;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.panel.Panel;

import org.apache.isis.extensions.viewer.wicket.pdfjs.applib.config.PdfJsConfig;
import org.apache.isis.extensions.viewer.wicket.pdfjs.wkt.integration.res.PdfJsIntegrationReference;
import org.apache.isis.extensions.viewer.wicket.pdfjs.wkt.integration.res.PdfJsReference;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.Accessors;

/**
 * A panel for rendering PDF documents inline in the page
 */
public class PdfJsPanel extends Panel {

    private static final long serialVersionUID = 1L;

    private final static String ID_PDFJSCANVAS = "pdfJsCanvas";

    @Getter @Accessors(makeFinal = true)
    private final PdfJsConfig config;

    /**
     * Constructor.
     *
     * @param id The component id
     */
    public PdfJsPanel(final String id, final @NonNull PdfJsConfig config) {
        super(id);

        this.config = config;

        val pdfJsCanvas = Wkt.add(this, Wkt.ajaxEnable(new WebComponent(ID_PDFJSCANVAS)));
        config.withCanvasId(pdfJsCanvas.getMarkupId());
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        response.render(PdfJsReference.asHeaderItem(config));
        response.render(PdfJsIntegrationReference.asHeaderItem());
        response.render(PdfJsIntegrationReference.domReadyScript(config));
    }

}
