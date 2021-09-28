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
package org.apache.isis.extensions.fullcalendar.ui.component.calendarable;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.extensions.fullcalendar.ui.component.CalendaredCollectionAbstract;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;

import net.ftlines.wicket.fullcalendar.EventProvider;

public class CalendarableCollectionAsFullCalendar
extends CalendaredCollectionAbstract {

    private static final long serialVersionUID = 1L;

    public CalendarableCollectionAsFullCalendar(final String id, final EntityCollectionModel model) {
        super(id, model);
    }

    @Override
    protected EventProvider newEventProvider(
            final EntityCollectionModel model,
            final String calendarName) {
        return new CalendarableEventProvider(model, calendarName);
    }

    @Override
    protected Set<String> getCalendarNames(final Iterable<ManagedObject> entityList) {
        return _NullSafe.stream(entityList)
                .map(CalendarableEventProvider.GET_CALENDAR_NAMES)
                .flatMap(_NullSafe::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
