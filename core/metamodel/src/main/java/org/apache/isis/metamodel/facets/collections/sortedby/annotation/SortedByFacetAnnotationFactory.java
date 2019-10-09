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

package org.apache.isis.metamodel.facets.collections.sortedby.annotation;

import java.util.Comparator;
import java.util.stream.Stream;

import org.apache.isis.config.IsisConfigurationLegacy;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.collections.sortedby.SortedByFacet;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorVisiting.Visitor;

/**
 * There is no check that the value is a {@link Comparator}; instead this is done through
 * the {@link #refineMetaModelValidator(MetaModelValidatorComposite, IsisConfigurationLegacy)}.
 */
public class SortedByFacetAnnotationFactory extends FacetFactoryAbstract
implements MetaModelRefiner {

    public SortedByFacetAnnotationFactory() {
        super(FeatureType.COLLECTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        // previously this handled the (now deleted) @SortedBy annotation.
        // nothing now to do here, but there is still validation to be contributed.
        // TODO: move this validator to a different facet factory, eg @CollectionLayout facet factory.

    }

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {
        programmingModel.addValidator(newValidatorVisitor());
    }

    protected Visitor newValidatorVisitor() {
        return new MetaModelValidatorVisiting.Visitor() {

            @Override
            public boolean visit(ObjectSpecification objectSpec, MetaModelValidator validator) {
                final Stream<OneToManyAssociation> objectCollections = objectSpec.streamCollections(Contributed.EXCLUDED);

                objectCollections.forEach(objectCollection->{
                    final SortedByFacet facet = objectCollection.getFacet(SortedByFacet.class);
                    if(facet != null) {
                        final Class<? extends Comparator<?>> cls = facet.value();
                        if(!Comparator.class.isAssignableFrom(cls)) {
                            validator.onFailure(
                                    objectSpec,
                                    objectSpec.getIdentifier(),
                                    "%s#%s: is annotated with @SortedBy, but the class specified '%s' is not a Comparator",
                                    objectSpec.getIdentifier().getClassName(), 
                                    objectCollection.getId(),
                                    facet.value().getName());
                        }
                    }
                });

                return true;
            }
        };
    }



}
