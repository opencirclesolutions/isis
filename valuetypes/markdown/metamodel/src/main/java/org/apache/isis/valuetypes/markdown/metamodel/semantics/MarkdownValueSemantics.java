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
package org.apache.isis.valuetypes.markdown.metamodel.semantics;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.adapters.AbstractValueSemanticsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.Renderer;
import org.apache.isis.valuetypes.markdown.applib.value.Markdown;

@Component
@Named("isis.val.MarkdownValueSemantics")
public class MarkdownValueSemantics
extends AbstractValueSemanticsProvider<Markdown>
implements
    EncoderDecoder<Markdown>,
    Parser<Markdown>,
    Renderer<Markdown> {

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final Markdown markdown) {
        if(markdown==null) {
            return null;
        }
        return markdown.getMarkdown();
    }

    @Override
    public Markdown fromEncodedString(final String data) {
        if(data==null) {
            return null;
        }
        return Markdown.valueOf(data);
    }

    // -- RENDERER

    @Override
    public String simpleTextRepresentation(final Context context, final Markdown value) {
        return render(value, Markdown::asHtml);
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final Context context, final Markdown value) {
        if(value==null) {
            return null;
        }
        return value.getMarkdown();
    }

    @Override
    public Markdown parseTextRepresentation(final Context context, final String text) {
        if(text==null) {
            return null;
        }
        return Markdown.valueOf(text);
    }

    @Override
    public int typicalLength() {
        return 0;
    }

}
