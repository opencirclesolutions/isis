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
package org.apache.isis.core.runtime.services.metrics;

import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.jdo.listener.LoadLifecycleListener;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.metrics.MetricsService;
import org.apache.isis.core.runtime.services.enlist.EnlistedObjectsServiceInternal;

@RequestScoped
@DomainService(nature = NatureOfService.DOMAIN)
public class MetricsServiceDefault implements MetricsService, InstanceLifecycleListener, LoadLifecycleListener {

    private AtomicInteger numberLoaded = new AtomicInteger(0);

    @Override
    public int numberObjectsLoaded() {
        return numberLoaded.get();
    }

    @Override
    public int numberObjectsDirtied() {
        return enlistedObjectsServiceInternal.numberObjectsDirtied();
    }

    @Override
    public int numberObjectPropertiesModified() {
        return enlistedObjectsServiceInternal.numberObjectPropertiesModified();
    }

    @Programmatic
    @Override
    public void postLoad(final InstanceLifecycleEvent event) {
        numberLoaded.incrementAndGet();
    }


    @Inject
    EnlistedObjectsServiceInternal enlistedObjectsServiceInternal;


}
