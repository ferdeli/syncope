/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.client.cli.commands.logger;

import java.util.LinkedList;
import javax.xml.ws.WebServiceException;
import org.apache.syncope.client.cli.Input;
import org.apache.syncope.client.cli.messages.Messages;
import org.apache.syncope.client.cli.util.CommandUtils;
import org.apache.syncope.common.lib.SyncopeClientException;
import org.apache.syncope.common.lib.to.LoggerTO;
import org.apache.syncope.common.lib.types.LoggerLevel;
import org.apache.syncope.common.lib.types.LoggerType;

public class LoggerUpdate extends AbstractLoggerCommand {

    private static final String UPDATE_HELP_MESSAGE
            = "logger --update {LOG-NAME}={LOG-LEVEL} {LOG-NAME}={LOG-LEVEL} [...]";

    private final Input input;

    public LoggerUpdate(final Input input) {
        this.input = input;
    }

    public void update() {
        if (input.parameterNumber() >= 1) {
            Input.PairParameter pairParameter;
            LoggerTO loggerTO;
            final LinkedList<LoggerTO> loggerTOs = new LinkedList<>();
            boolean failed = false;
            for (final String parameter : input.getParameters()) {
                try {
                    pairParameter = input.toPairParameter(parameter);
                    loggerTO = loggerService.read(LoggerType.LOG, pairParameter.getKey());
                    loggerTO.setLevel(LoggerLevel.valueOf(pairParameter.getValue()));
                    loggerService.update(LoggerType.LOG, loggerTO);
                    loggerTOs.add(loggerTO);
                } catch (final WebServiceException | SyncopeClientException | IllegalArgumentException ex) {
                    if (ex.getMessage().startsWith("No enum constant org.apache.syncope.common.lib.types.")) {
                        Messages.printTypeNotValidMessage(
                                "logger level",
                                input.firstParameter(), CommandUtils.fromEnumToArray(LoggerLevel.class));
                    } else if ("Parameter syntax error!".equalsIgnoreCase(ex.getMessage())) {
                        Messages.printMessage(ex.getMessage(), UPDATE_HELP_MESSAGE);
                    } else if (ex.getMessage().startsWith("NotFound")) {
                        Messages.printNofFoundMessage("Logger", parameter);
                    } else {
                        Messages.printMessage(ex.getMessage(), UPDATE_HELP_MESSAGE);
                    }
                    failed = true;
                    break;
                }
            }
            if (!failed) {
                resultManager.fromUpdate(loggerTOs);
            }
        } else {
            Messages.printCommandOptionMessage(UPDATE_HELP_MESSAGE);
        }
    }
}
