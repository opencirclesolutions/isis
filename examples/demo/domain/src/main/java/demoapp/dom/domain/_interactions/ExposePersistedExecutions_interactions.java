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
package demoapp.dom.domain._interactions;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntry;
import org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntryRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;

//tag::class[]
@Collection
@RequiredArgsConstructor
public class ExposePersistedExecutions_interactions {
    // ...
//end::class[]

    @SuppressWarnings("unused")
    private final ExposePersistedExecutions exposePersistedExecutions;

//tag::class[]
    public List<InteractionDtoVm> coll() {
        val list = new LinkedList<InteractionDtoVm>();
        executionLogEntryRepository.findAll()
                .stream()
                .map(x -> x.getInteractionDto())
                .map(InteractionDtoVm::new)
                .forEach(list::push);   // reverse order
        return list;
    }

    @Inject ExecutionLogEntryRepository<? extends ExecutionLogEntry> executionLogEntryRepository;
}
//end::class[]
