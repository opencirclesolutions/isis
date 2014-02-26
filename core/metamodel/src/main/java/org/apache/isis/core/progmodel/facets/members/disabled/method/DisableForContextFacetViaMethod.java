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

package org.apache.isis.core.progmodel.facets.members.disabled.method;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.events.UsabilityEvent;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.util.AdapterInvokeUtils;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.progmodel.facets.members.disabled.DisableForContextFacetAbstract;

public class DisableForContextFacetViaMethod extends DisableForContextFacetAbstract implements ImperativeFacet {

    private final Method method;

    public DisableForContextFacetViaMethod(final Method method, final FacetHolder holder) {
        super(holder);
        this.method = method;
    }

    /**
     * Returns a singleton list of the {@link Method} provided in the
     * constructor.
     */
    @Override
    public List<Method> getMethods() {
        return Collections.singletonList(method);
    }

    @Override
    public boolean impliesResolve() {
        return true;
    }

    @Override
    public boolean impliesObjectChanged() {
        return false;
    }

    /**
     * The reason this object is disabled, or <tt>null</tt> otherwise.
     */
    @Override
    public String disables(final UsabilityContext<? extends UsabilityEvent> ic) {
        final ObjectAdapter target = ic.getTarget();
        if (target == null) {
            return null;
        }
        return (String) AdapterInvokeUtils.invoke(method, target, ic.getContributeeAsMap());
    }

    @Override
    protected String toStringValues() {
        return "method=" + method;
    }

}
