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
package org.apache.isis.core.metamodel.postprocessors.members.navigation;

import javax.inject.Inject;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.object.hidden.HiddenTypeFacet;
import org.apache.isis.core.metamodel.postprocessors.ObjectSpecificationPostProcessorAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

/**
 * Installs the {@link NavigationFacetDerivedFromHiddenType} on all of the
 * {@link ObjectMember}s of the {@link ObjectSpecification}.
 */
public class DeriveNavigationFacetFromHiddenTypePostProcessor extends ObjectSpecificationPostProcessorAbstract {

    @Inject
    public DeriveNavigationFacetFromHiddenTypePostProcessor(final MetaModelContext metaModelContext) {
        super(metaModelContext);
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification) {
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final ObjectAction act) {
        addFacetIfRequired(act, act.getReturnType());
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final ObjectAction objectAction, final ObjectActionParameter param) {
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final OneToOneAssociation prop) {
        addFacetIfRequired(prop, prop.getElementType());
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final OneToManyAssociation coll) {
        addFacetIfRequired(coll, coll.getElementType());
    }

    private static void addFacetIfRequired(final FacetHolder facetHolder, final ObjectSpecification navigatedType) {
        if(navigatedType.containsNonFallbackFacet(HiddenTypeFacet.class)) {
            FacetUtil.addFacet(new NavigationFacetDerivedFromHiddenType(facetHolder, navigatedType));
        }
    }

}
