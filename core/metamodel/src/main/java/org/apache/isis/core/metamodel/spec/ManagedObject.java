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
package org.apache.isis.core.metamodel.spec;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.object.icon.ObjectIcon;
import org.apache.isis.core.metamodel.facets.object.title.TitleRenderRequest;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;
import lombok.val;

/**
 * Represents an instance of some element of the meta-model managed by the framework,
 * that is IoC-container provided beans, persistence-stack provided entities or view-models.
 *
 */
public interface ManagedObject {

    /**
     * Returns the specification that details the structure (meta-model) of this object.
     */
    ObjectSpecification getSpecification();

    /**
     * Returns the adapted domain object, the 'plain old java' object this managed object
     * represents with the framework.
     */
    Object getPojo();

    /**
     * Returns the object's bookmark as identified by the ObjectManager.
     * Bookmarks are considered immutable, hence will be memoized once fetched.
     */
    Optional<Bookmark> getBookmark();

    boolean isBookmarkMemoized();

    default Supplier<ManagedObject> asProvider() {
        return ()->this;
    }

    // -- TITLE

    public default String titleString(final UnaryOperator<TitleRenderRequest.TitleRenderRequestBuilder> onBuilder) {
        return ManagedObjects.TitleUtil
                .titleString(onBuilder.apply(TitleRenderRequest.builder().object(this)).build());
    }

    public default String titleString() {
        return ManagedObjects.TitleUtil.titleString(TitleRenderRequest.builder()
                .object(this)
                .build());
    }

    // -- SHORTCUTS - MM CONTEXT

    default MetaModelContext getMetaModelContext() {
        return ManagedObjects.spec(this)
                .map(ObjectSpecification::getMetaModelContext)
                .orElseThrow(()->_Exceptions
                        .illegalArgument("Can only retrieve MetaModelContext from ManagedObjects "
                                + "that have an ObjectSpecification."));
    }

    default ObjectManager getObjectManager() {
        return getMetaModelContext().getObjectManager();
    }

    // -- SHORTCUT - ELEMENT SPECIFICATION

    /**
     * Used only for (standalone or parented) collections.
     */
    default Optional<ObjectSpecification> getElementSpecification() {
        return getSpecification().getElementSpecification();
    }

    // -- SHORTCUT - TITLE

    default String getTitle() {
        return ManagedObjects.titleOf(this);
    }

    // -- SHORTCUT - ICON

    /**
     * Returns the name of an icon to use if this object is to be displayed
     * graphically.
     * <p>
     * May return <code>null</code> if no icon is specified.
     */
    default String getIconName() {
        return getSpecification().getIconName(this);
    }

    default ObjectIcon getIcon() {
        return getSpecification().getIcon(this);
    }

    // -- FACTORIES

    /**
     * Optimized for cases, when the pojo's specification is already available.
     * @param specification
     * @param pojo - might also be a collection of pojos
     */
    public static ManagedObject of(
            final @NonNull ObjectSpecification specification,
            final @Nullable Object pojo) {

        ManagedObjects.assertPojoNotManaged(pojo);
        specification.assertPojoCompatible(pojo);

        //ISIS-2430 Cannot assume Action Param Spec to be correct when eagerly loaded
        //actual type in use (during runtime) might be a sub-class of the above
        if(pojo==null
                || pojo.getClass().equals(specification.getCorrespondingClass())
                ) {
            // if actual type matches spec's, we assume, that we don't need to reload,
            // so this is a shortcut for performance reasons
            return SimpleManagedObject.of(specification, pojo);
        }

        //_Probe.errOut("upgrading spec %s on type %s", specification, pojo.getClass());
        //ManagedObjects.warnIfAttachedEntity(adapter, "consider using ManagedObject.identified(...) for entity");

        val specLoader = specification.getMetaModelContext().getSpecificationLoader();
        return ManagedObject.lazy(specLoader, pojo);
    }

    /**
     * Optimized for cases, when the pojo's specification and bookmark are already available.
     */
    public static ManagedObject bookmarked(
            final @NonNull ObjectSpecification specification,
            final @NonNull Object pojo,
            final @NonNull Bookmark bookmark) {

        if(!specification.getCorrespondingClass().isAssignableFrom(pojo.getClass())) {
            throw _Exceptions.illegalArgument(
                    "Pojo not compatible with ObjectSpecification, " +
                    "objectSpec.correspondingClass = %s, " +
                    "pojo.getClass() = %s, " +
                    "pojo.toString() = %s",
                    specification.getCorrespondingClass(), pojo.getClass(), pojo.toString());
        }
        ManagedObjects.assertPojoNotManaged(pojo);
        return SimpleManagedObject.identified(specification, pojo, bookmark);
    }

    /**
     * For cases, when the pojo's specification is not available and needs to be looked up.
     * @param specLoader
     * @param pojo
     */
    public static ManagedObject lazy(
            final SpecificationLoader specLoader,
            final Object pojo) {
        ManagedObjects.assertPojoNotManaged(pojo);
        val adapter = new LazyManagedObject(cls->specLoader.specForType(cls).orElse(null), pojo);
        //ManagedObjects.warnIfAttachedEntity(adapter, "consider using ManagedObject.identified(...) for entity");
        return adapter;
    }

    // -- EMPTY

    /** has no ObjectSpecification and no value (pojo) */
    static ManagedObject unspecified() {
        return ManagedObjects.UNSPECIFIED;
    }

    /** has an ObjectSpecification, but no value (pojo) */
    static ManagedObject empty(final @NonNull ObjectSpecification spec) {
        return ManagedObject.of(spec, null);
    }

    // -- SIMPLE

    @Value
    @RequiredArgsConstructor(staticName="of", access = AccessLevel.PRIVATE)
    @EqualsAndHashCode(of = "pojo")
    @ToString(of = {"specification", "pojo"}) //ISIS-2317 make sure toString() is without side-effects
    static final class SimpleManagedObject implements ManagedObject {

        public static ManagedObject identified(
                @NonNull  final ObjectSpecification spec,
                final @Nullable Object pojo,
                @NonNull  final Bookmark bookmark) {
            val managedObject = SimpleManagedObject.of(spec, pojo);
            managedObject.bookmarkLazy.set(Optional.of(bookmark));
            return managedObject;
        }

        @NonNull private final ObjectSpecification specification;
        @Nullable private final Object pojo;

        @Override
        public Optional<Bookmark> getBookmark() {
            return bookmarkLazy.get();
        }

        // -- LAZY ID HANDLING
        private final _Lazy<Optional<Bookmark>> bookmarkLazy =
                _Lazy.threadSafe(()->ManagedObjects.BookmarkUtil.bookmark(this));

        @Override
        public boolean isBookmarkMemoized() {
            return bookmarkLazy.isMemoized();
        }

    }

    // -- LAZY

    @EqualsAndHashCode(of = "pojo")
    static final class LazyManagedObject implements ManagedObject {

        @NonNull private final Function<Class<?>, ObjectSpecification> specLoader;

        @Getter @NonNull private final Object pojo;

        @Override
        public Optional<Bookmark> getBookmark() {
            return bookmarkLazy.get();
        }

        // -- LAZY ID HANDLING
        private final _Lazy<Optional<Bookmark>> bookmarkLazy =
                _Lazy.threadSafe(()->ManagedObjects.BookmarkUtil.bookmark(this));

        @Override
        public boolean isBookmarkMemoized() {
            return bookmarkLazy.isMemoized();
        }

        private final _Lazy<ObjectSpecification> specification = _Lazy.threadSafe(this::loadSpec);

        public LazyManagedObject(final @NonNull Function<Class<?>, ObjectSpecification> specLoader, final @NonNull Object pojo) {
            this.specLoader = specLoader;
            this.pojo = pojo;
        }

        @Override
        public ObjectSpecification getSpecification() {
            return specification.get();
        }

        @Override //ISIS-2317 make sure toString() is without side-effects
        public String toString() {
            if(specification.isMemoized()) {
                return String.format("ManagedObject[spec=%s, pojo=%s]",
                        ""+getSpecification(),
                        ""+getPojo());
            }
            return String.format("ManagedObject[spec=%s, pojo=%s]",
                    "[lazy not loaded]",
                    ""+getPojo());
        }

        private ObjectSpecification loadSpec() {
            return specLoader.apply(pojo.getClass());
        }



    }




}
