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
package org.apache.isis.viewer.wicket.model.models;

import java.util.List;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

import lombok.Getter;
import lombok.NonNull;

public class EntityCollectionModelStandalone
extends EntityCollectionModelAbstract {

    private static final long serialVersionUID = 1L;

    /**
     * model of the action this collection is the returned result of
     */
    @Getter private final @NonNull ActionModel actionModel;

    // -- FACTORIES

    public static EntityCollectionModelStandalone forActionModel(
            final @NonNull ManagedObject collectionAsAdapter,
            final @NonNull ActionModel actionModel) {

        // take a copy of the actionModel,
        // because the original can get mutated (specifically: its arguments cleared)
        return new EntityCollectionModelStandalone(
                actionModel.copy(), collectionAsAdapter);
    }

    // -- CONSTRUCTOR

    protected EntityCollectionModelStandalone(
            final @NonNull ActionModel actionModel,
            final @NonNull ManagedObject collectionAsAdapter) {
        super(
                actionModel.getCommonContext(),
                actionModel.getMetaModel());
        this.actionModel = actionModel;
        this.mementoList = _NullSafe.streamAutodetect(collectionAsAdapter.getPojo()) // pojos
                .filter(_NullSafe::isPresent)
                .map(actionModel.getMementoService()::mementoForPojo)
                .collect(Can.toCan());

    }

    // -- VARIANT SUPPORT

    @Override
    public Variant getVariant() {
        return Variant.STANDALONE;
    }

    // --

    private Can<ObjectMemento> mementoList;

    @Override
    protected List<ManagedObject> load() {
        return mementoList.stream()
        .map(getCommonContext()::reconstructObject)
        .sorted(super.getElementComparator())
        .collect(Can.toCan())
        .toList();
    }

    @Override
    public int getCount() {
        return mementoList.size();
    }

    @Override
    public String getName() {

        return getTypeOfSpecification()
            .lookupFacet(MemberNamedFacet.class)
            .map(MemberNamedFacet::getSpecialization)
            .map(specialization->specialization
                    .fold(namedFacet->namedFacet.translated(),
                          namedFacet->namedFacet.textElseNull(actionModel.getOwner())))
            .orElse(getIdentifier().getMemberLogicalName());
    }

    @Override
    public ObjectMember getMetaModel() {
        return actionModel.getMetaModel();
    }

    @Override
    public ManagedObject getParentObject() {
        return actionModel.getOwner();
    }

}
