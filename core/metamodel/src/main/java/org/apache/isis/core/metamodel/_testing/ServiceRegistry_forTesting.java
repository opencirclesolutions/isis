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
package org.apache.isis.core.metamodel._testing;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.ioc._IocContainer;
import org.apache.isis.commons.internal.ioc._ManagedBeanAdapter;
import org.apache.isis.core.metamodel.context.MetaModelContext;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;

@RequiredArgsConstructor
class ServiceRegistry_forTesting implements ServiceRegistry {

    @NonNull private final MetaModelContext metaModelContext;

    @Getter @Setter private _IocContainer iocContainer;
    private final Set<_ManagedBeanAdapter> registeredBeans = _Sets.newHashSet();

    @Override
    public <T> Can<T> select(final Class<T> type, final Annotation[] qualifiers) {

        if(iocContainer!=null) {
            return iocContainer.select(type, qualifiers);
        }

// ignore
//        if(qualifiers!=null && qualifiers.length>0) {
//            throw _Exceptions.notImplemented();
//        }

        Optional<T> match = streamBeans()
                .filter(beanAdapter->type.isAssignableFrom(beanAdapter.getBeanClass()))
                .map(beanAdapter->beanAdapter.getInstance().getFirstOrFail())
                .map(_Casts::<T>uncheckedCast)
                .findFirst();

        if(match.isPresent()) {
            return Can.ofSingleton(match.get());
        }

        // lookup the _Context
        // XXX lombok bug, cannot use val here (https://github.com/rzwitserloot/lombok/issues/1588)
        T singleton = _Context.getIfAny(type);
        if(singleton!=null) {
            return Can.ofSingleton(singleton);
        }

        return Can.empty();
    }

    @Override
    public Stream<_ManagedBeanAdapter> streamRegisteredBeans() {
        return registeredBeans().stream();
    }

    @Override
    public Optional<_ManagedBeanAdapter> lookupRegisteredBeanById(final String id) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public Optional<?> lookupBeanById(final String id) {
        throw _Exceptions.notImplemented();
    }

    // -- HELPER

    private Set<_ManagedBeanAdapter> registeredBeans() {

        AtomicBoolean triggerPostInit = new AtomicBoolean(false);

        synchronized(registeredBeans) {
            if(registeredBeans.isEmpty()) {
                streamBeans()
                .filter(_NullSafe::isPresent)
                .forEach(registeredBeans::add);
                triggerPostInit.set(true);
            }
        }

        if(triggerPostInit.getAndSet(false)) {
            postinitWhenTesting();
        }

        return registeredBeans;
    }

    private Stream<_ManagedBeanAdapter> streamBeans() {
        // lookup the MetaModelContextBean's list of singletons
        val mmc = metaModelContext;
        if(mmc instanceof MetaModelContext_forTesting) {
            val mmcb = (MetaModelContext_forTesting) mmc;
            return mmcb.streamBeanAdapters();
        }
        return Stream.empty();
    }

    @Override
    public void clearRegisteredBeans() {
        val mmc = metaModelContext;
        if(mmc instanceof MetaModelContext_forTesting) {
            val mmcb = (MetaModelContext_forTesting) mmc;
            mmcb.objectAdaptersForBeansOfKnownSort.clear();
        }
    }

    private void postinitWhenTesting() {
        val mmc = metaModelContext;
        if(mmc instanceof MetaModelContext_forTesting) {
            val mmcb = (MetaModelContext_forTesting) mmc;
            mmcb.runPostconstruct();
        }
    }


}
