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
package org.apache.isis.viewer.wicket.model.models.interaction.act;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.wicket.model.ChainingModel;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.viewer.wicket.model.models.interaction.BookmarkedObjectWkt;
import org.apache.isis.viewer.wicket.model.models.interaction.HasBookmarkedOwnerAbstract;

/**
 * The parent (container) model of multiple <i>parameter models</i> which implement
 * {@link ChainingModel}.
 * <pre>
 * IModel[ActionInteraction] ... placeOrder(X x, Yy)
 * |
 * +-- ParameterUiModel ... bound to X x (ParameterNegotiationModel)
 * +-- ParameterUiModel ... bound to Y y (ParameterNegotiationModel)
 * </pre>
 *
 * @implSpec the state of pending parameters ParameterNegotiationModel is held transient,
 * that means it does not survive a serialization/de-serialization cycle; instead
 * is recreated with parameter defaults
 *
 * @see ChainingModel
 */
public class ActionInteractionWkt
extends HasBookmarkedOwnerAbstract<ActionInteraction> {

    private static final long serialVersionUID = 1L;

    private final String memberId;
    private final Where where;
    private Can<ParameterUiModelWkt> childModels;

    public ActionInteractionWkt(
            final BookmarkedObjectWkt bookmarkedObject,
            final String memberId,
            final Where where) {

        super(bookmarkedObject);
        this.memberId = memberId;
        this.where = where;
    }

    @Override
    protected ActionInteraction load() {
        return ActionInteraction.wrap(
                ManagedAction.lookupAction(getBookmarkedOwner(), memberId, where)
                .orElseThrow(()->_Exceptions.noSuchElement(memberId)));
    }

    public final ActionInteraction actionInteraction() {
        return getObject();
    }

    // -- LAZY BINDING

    public Stream<ParameterUiModelWkt> streamParameterUiModels() {
        if(childModels==null) {
            final int paramCount = actionInteraction().getMetamodel().get().getParameterCount();
            final int tupleIndex = 0;
            this.childModels = IntStream.range(0, paramCount)
                    .mapToObj(paramIndex -> new ParameterUiModelWkt(this, paramIndex, tupleIndex))
                    .collect(Can.toCan());
        }
        return childModels.stream();
    }

    // -- PARAMETER NEGOTIATION WITH MEMOIZATION (TRANSIENT)

    private final transient _Lazy<Optional<ParameterNegotiationModel>> parameterNegotiationModel =
            _Lazy.threadSafe(()->actionInteraction().startParameterNegotiation());

    public final Optional<ParameterNegotiationModel> parameterNegotiationModel() {
        _Assert.assertTrue(this.isAttached(), "model is not attached");
        return parameterNegotiationModel.get();
    }

    public void resetParametersToDefault() {
        parameterNegotiationModel.clear();
    }

}
