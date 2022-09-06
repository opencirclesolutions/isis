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
package org.apache.isis.core.metamodel.facets;

import java.util.function.BiConsumer;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.TypeOfAnyCardinality;

import lombok.NonNull;

public abstract class SingleTypeValueFacetAbstract
extends FacetAbstract
implements SingleTypeValueFacet {

    private final @NonNull TypeOfAnyCardinality value;

    protected SingleTypeValueFacetAbstract(
            final Class<? extends Facet> facetType,
            final FacetHolder holder,
            final TypeOfAnyCardinality value) {
        super(facetType, holder);
        this.value = value;
    }

    @Override
    public final TypeOfAnyCardinality value() {
        return value;
    }

    @Override
    public final ObjectSpecification elementSpec() {
        return getSpecificationLoader().specForTypeElseFail(value().getElementType());
    }

    @Override
    public final void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("value", value());
        getCollectionSemantics()
            .ifPresent(sem->visitor.accept("collection-semantics", sem.name()));
    }

    @Override
    public final boolean semanticEquals(final @NonNull Facet other) {
        return other instanceof SingleTypeValueFacet
                ? this.value() == ((SingleTypeValueFacet)other).value()
                : false;
    }


}
