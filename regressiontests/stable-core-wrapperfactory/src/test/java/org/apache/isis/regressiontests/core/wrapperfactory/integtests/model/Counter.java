/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.isis.regressiontests.core.wrapperfactory.integtests.model;

import lombok.*;

import javax.inject.Named;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.applib.annotation.*;

@PersistenceCapable(
        schema = TestDomainModel.SCHEMA,
        table = "Counter"
)
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@Named(TestDomainModel.NAMESPACE + ".Counter")
@DomainObject(nature = Nature.ENTITY)
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Counter implements Comparable<Counter> {

    @Property(editing = Editing.ENABLED, commandPublishing = Publishing.ENABLED)
    @Column(allowsNull = "true")
    @Getter @Setter
    private Long num;

    @Column(allowsNull = "false")
    @Getter @Setter
    private String name;

    @Column(allowsNull = "true")
    @Getter @Setter
    private Long num2;

    @Action(commandPublishing = Publishing.ENABLED)
    public Counter bumpUsingDeclaredAction() {
        return doBump();
    }

    Counter doBump() {
        if (getNum() == null) {
            setNum(1L);
        } else {
            setNum(getNum() + 1);
        }
        return this;
    }

    @Override
    public int compareTo(final Counter o) {
        return this.getName().compareTo(o.getName());
    }
}
