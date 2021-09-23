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
package org.apache.isis.core.metamodel.postprocessors.all;

import javax.inject.Inject;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.all.described.MemberDescribedFacet;
import org.apache.isis.core.metamodel.facets.all.described.ObjectDescribedFacet;
import org.apache.isis.core.metamodel.facets.all.described.ParamDescribedFacet;
import org.apache.isis.core.metamodel.facets.members.described.annotprop.DescribedAsFacetOnMemberInferredFromType;
import org.apache.isis.core.metamodel.facets.param.described.annotderived.DescribedAsFacetOnParameterInferredFromType;
import org.apache.isis.core.metamodel.postprocessors.ObjectSpecificationPostProcessorAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

public class DeriveDescribedAsFromTypePostProcessor
extends ObjectSpecificationPostProcessorAbstract {

    @Inject
    public DeriveDescribedAsFromTypePostProcessor(final MetaModelContext mmc) {
        super(mmc);
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification) {
        // no-op
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final ObjectAction objectAction) {
        if(objectAction.containsNonFallbackFacet(MemberDescribedFacet.class)) {
            return;
        }
        objectAction.getReturnType()
        .lookupNonFallbackFacet(ObjectDescribedFacet.class)
        .ifPresent(specFacet -> FacetUtil.addFacetIfPresent(
                DescribedAsFacetOnMemberInferredFromType
                .create(
                        specFacet,
                        facetedMethodFor(objectAction))));
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final ObjectAction objectAction, final ObjectActionParameter parameter) {
        if(parameter.containsNonFallbackFacet(ParamDescribedFacet.class)) {
            return;
        }
        final ObjectSpecification paramSpec = parameter.getElementType();
        paramSpec.lookupNonFallbackFacet(ObjectDescribedFacet.class)
        .ifPresent(describedAsFacet->{
            FacetUtil.addFacetIfPresent(
                    DescribedAsFacetOnParameterInferredFromType
                    .create(describedAsFacet, peerFor(parameter)));
        });
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final OneToOneAssociation prop) {
        handle(prop);
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final OneToManyAssociation coll) {
        handle(coll);
    }

    private void handle(final ObjectAssociation objectAssociation) {
        if(objectAssociation.containsNonFallbackFacet(MemberDescribedFacet.class)) {
            return;
        }
        objectAssociation.getElementType()
        .lookupNonFallbackFacet(ObjectDescribedFacet.class)
        .ifPresent(specFacet -> FacetUtil.addFacetIfPresent(
                DescribedAsFacetOnMemberInferredFromType
                .create(specFacet, facetedMethodFor(objectAssociation))));
    }

}
