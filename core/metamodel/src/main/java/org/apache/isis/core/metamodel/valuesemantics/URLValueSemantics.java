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
package org.apache.isis.core.metamodel.valuesemantics;

import java.net.MalformedURLException;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.adapters.AbstractValueSemanticsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.Renderer;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.commons.internal.base._Strings;

import lombok.val;

@Component
@Named("isis.val.URLValueSemantics")
public class URLValueSemantics
extends AbstractValueSemanticsProvider<java.net.URL>
implements
    EncoderDecoder<java.net.URL>,
    Parser<java.net.URL>,
    Renderer<java.net.URL> {

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final java.net.URL url) {
        return url != null? url.toString(): "NULL";
    }

    @Override
    public java.net.URL fromEncodedString(final String data) {
        if("NULL".equals(data)) {
            return null;
        }
        try {
            return new java.net.URL(data);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    // -- RENDERER

    @Override
    public String simpleTextRepresentation(final ValueSemanticsProvider.Context context, final java.net.URL value) {
        return value != null ? value.toString(): "";
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final java.net.URL value) {
        return value != null ? value.toString(): null;
    }

    @Override
    public java.net.URL parseTextRepresentation(final ValueSemanticsProvider.Context context, final String text) {
        val input = _Strings.blankToNullOrTrim(text);
        if(input==null) {
            return null;
        }
        try {
            return new java.net.URL(input);
        } catch (final MalformedURLException ex) {
            throw new IllegalArgumentException("Not parseable as an URL ('" + input + "')", ex);
        }
    }

    @Override
    public int typicalLength() {
        return 100;
    }

    @Override
    public int maxLength() {
        return 2083;
    }

}
