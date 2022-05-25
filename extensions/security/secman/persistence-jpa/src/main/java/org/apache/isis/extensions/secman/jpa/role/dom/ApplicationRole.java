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
package org.apache.isis.extensions.secman.jpa.role.dom;

import java.util.Set;
import java.util.TreeSet;

import javax.inject.Named;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.Bounding;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.extensions.secman.applib.role.dom.ApplicationRole.Nq;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.isis.persistence.jpa.applib.integration.IsisEntityListener;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        schema = ApplicationRole.SCHEMA,
        name = ApplicationRole.TABLE,
        uniqueConstraints =
            @UniqueConstraint(
                    name = "ApplicationRole_name_UNQ",
                    columnNames={"name"})
)
@NamedQueries({
    @NamedQuery(
            name = Nq.FIND_BY_NAME,
            query = "SELECT r "
                  + "FROM ApplicationRole r "
                  + "WHERE r.name = :name"),
    @NamedQuery(
            name = Nq.FIND_BY_NAME_CONTAINING,
            query = "SELECT r "
                  + "FROM ApplicationRole r "
                  + "WHERE r.name LIKE :regex"),
})
@EntityListeners(IsisEntityListener.class)
@Named(ApplicationRole.LOGICAL_TYPE_NAME)
@DomainObject(
        bounding = Bounding.BOUNDED,
        autoCompleteRepository = ApplicationRoleRepository.class,
        autoCompleteMethod = "findMatching"
        )
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
        )
public class ApplicationRole
    extends org.apache.isis.extensions.secman.applib.role.dom.ApplicationRole {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private Long version;


    @Column(nullable = Name.NULLABLE, length = Name.MAX_LENGTH)
    @Name
    @Getter @Setter
    private String name;


    @Column(nullable = Description.NULLABLE, length = Description.MAX_LENGTH)
    @Description
    @Getter @Setter
    private String description;


    @ManyToMany(mappedBy = Users.MAPPED_BY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Users
    @Getter
    private Set<org.apache.isis.extensions.secman.jpa.user.dom.ApplicationUser> users = new TreeSet<>();
    // necessary for integration tests
    public void addToUsers(final org.apache.isis.extensions.secman.jpa.user.dom.ApplicationUser applicationUser) {
        getUsers().add(applicationUser);
    }


}
