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
package org.apache.isis.commons.internal.debug;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.debug.xray.XrayDataModel;
import org.apache.isis.commons.internal.debug.xray.XrayModel.ThreadMemento;
import org.apache.isis.commons.internal.debug.xray.XrayUi;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
final class _Xray {

    void recordDebugLogEvent(
            final String logMessage,
            final Can<StackTraceElement> stackTrace) {

        if(!XrayUi.isXrayEnabled()) {
            return;
        }

        val threadId = ThreadMemento.fromCurrentThread();

        XrayUi.updateModel(model->{
            val parentNode = model.getThreadNode(threadId);

            val logModel = model.addDataNode(parentNode,
                    new XrayDataModel.LogEntry(
                            "debug-log",
                            _Strings.ellipsifyAtEnd(logMessage, 80, "..."),
                            logMessage));
            stackTrace.forEach(logModel.getData()::add);
        });
    }

}
