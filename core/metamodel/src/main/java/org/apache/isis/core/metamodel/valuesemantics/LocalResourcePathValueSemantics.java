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

import java.nio.file.InvalidPathException;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.adapters.AbstractValueSemanticsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.Renderer;
import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.commons.internal.base._Strings;

import lombok.val;

@Component
@Named("isis.val.LocalResourcePathValueSemantics")
public class LocalResourcePathValueSemantics
extends AbstractValueSemanticsProvider<LocalResourcePath>
implements
    EncoderDecoder<LocalResourcePath>,
    Parser<LocalResourcePath>,
    Renderer<LocalResourcePath> {

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final LocalResourcePath localResourcePath) {
        return localResourcePath != null
                ? localResourcePath.getValue()
                : "NULL";
    }

    @Override
    public LocalResourcePath fromEncodedString(final String data) {
        if("NULL".equals(data)) {
            return null;
        }
        try {
            return new LocalResourcePath(data);
        } catch (InvalidPathException e) {
            return null;
        }
    }

    // -- RENDERER

    @Override
    public String simpleTextRepresentation(final Context context, final LocalResourcePath value) {
        return render(value, LocalResourcePath::getValue);
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final Context context, final LocalResourcePath value) {
        return value != null ? value.getValue() : null;
    }

    @Override
    public LocalResourcePath parseTextRepresentation(final Context context, final String text) {
        val input = _Strings.blankToNullOrTrim(text);
        if(input==null) {
            return null;
        }
        try {
            return new LocalResourcePath(input);
        } catch (final InvalidPathException ex) {
            throw new IllegalArgumentException("Not parseable as a LocalResourcePath ('" + input + "')", ex);
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
