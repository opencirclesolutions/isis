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

import java.util.Optional;
import java.util.function.Function;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.core.metamodel.interactions.managed.ManagedMember.MemberType;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.NonNull;
import lombok.val;

public final class CollectionInteraction
extends MemberInteraction<ManagedCollection, CollectionInteraction> {

    public static final CollectionInteraction start(
            final @NonNull ManagedObject owner,
            final @NonNull String memberId,
            final @NonNull Where where) {

        val managedCollection = ManagedCollection.lookupCollection(owner, memberId, where);

        final _Either<ManagedCollection, InteractionVeto> chain = managedCollection.isPresent()
                ? _Either.left(managedCollection.get())
                : _Either.right(InteractionVeto.notFound(MemberType.COLLECTION, memberId));

        return new CollectionInteraction(chain);
    }

    CollectionInteraction(@NonNull final _Either<ManagedCollection, InteractionVeto> chain) {
        super(chain);
    }

    /**
     * @return optionally the ManagedCollection based on whether there
     * was no interaction veto within the originating chain
     */
    public Optional<ManagedCollection> getManagedCollection() {
        return super.getManagedMember();
    }

    /**
     * @return this Interaction's ManagedCollection
     * @throws X if there was any interaction veto within the originating chain
     */
    public <X extends Throwable>
    ManagedCollection getManagedCollectionElseThrow(final Function<InteractionVeto, ? extends X> onFailure) throws X {
        return super.getManagedMemberElseThrow(onFailure);
    }


}

