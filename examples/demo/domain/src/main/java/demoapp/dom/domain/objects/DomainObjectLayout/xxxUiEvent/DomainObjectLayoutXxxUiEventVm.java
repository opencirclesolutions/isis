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
package demoapp.dom.domain.objects.DomainObjectLayout.xxxUiEvent;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Property;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.Getter;
import lombok.Setter;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Named("demo.DomainObjectLayoutXxxUiEventVm")
@DomainObject(
        nature=Nature.VIEW_MODEL)
@DomainObjectLayout(
        titleUiEvent = DomainObjectLayoutXxxUiEventVm.TitleUiEvent.class,
        iconUiEvent = DomainObjectLayoutXxxUiEventVm.IconUiEvent.class,
        cssClassUiEvent = DomainObjectLayoutXxxUiEventVm.CssClassUiEvent.class,
        layoutUiEvent = DomainObjectLayoutXxxUiEventVm.LayoutUiEvent.class
        )
public class DomainObjectLayoutXxxUiEventVm implements HasAsciiDocDescription {

    public static class TitleUiEvent extends org.apache.causeway.applib.events.ui.TitleUiEvent<DomainObjectLayoutXxxUiEventVm> { }
    public static class IconUiEvent extends org.apache.causeway.applib.events.ui.IconUiEvent<DomainObjectLayoutXxxUiEventVm> { }
    public static class CssClassUiEvent extends org.apache.causeway.applib.events.ui.CssClassUiEvent<DomainObjectLayoutXxxUiEventVm> { }
    public static class LayoutUiEvent extends org.apache.causeway.applib.events.ui.LayoutUiEvent<DomainObjectLayoutXxxUiEventVm> { }

    @ObjectSupport public String title() {
        return "DomainObjectLayout#xxxUiEvent (should be overwritten by ui-title-event-listener)";
    }

    @ObjectSupport public String layout() {
        return "alternative1"; // should be overwritten by ui-layout-event-listener
    }

    //TODO[CAUSEWAY-3309]
    @Property(optionality = Optionality.OPTIONAL)
    @XmlElement(required = false)
    @Getter @Setter
    private String dummy;

}
//end::class[]
