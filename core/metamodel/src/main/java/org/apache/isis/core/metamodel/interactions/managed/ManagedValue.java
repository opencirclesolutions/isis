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
package org.apache.isis.core.metamodel.interactions.managed;

import java.util.function.UnaryOperator;

import org.apache.isis.commons.binding.Bindable;
import org.apache.isis.commons.binding.Observable;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.val;

public interface ManagedValue {

    ObjectSpecification getElementType();

    Bindable<ManagedObject> getValue();
    Bindable<String> getValueAsParsableText();

    Observable<String> getValidationMessage();
    Bindable<String> getSearchArgument();
    Observable<Can<ManagedObject>> getChoices();

    default void update(final UnaryOperator<ManagedObject> updater) {
        val value = getValue();
        value.setValue(updater.apply(value.getValue()));
    }

    /**
     * Requires specified objects, that is ManagedObjects require an ObjectSpecification.
     */
    default void updatePojo(final UnaryOperator<Object> updater) {
        update(v->ManagedObject.of(v.getSpecification(), updater.apply(v.getPojo())));
    }

}
