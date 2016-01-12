/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemas.node.listeners;

import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.resources.ActionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Updates the path template if the event source is a parameter group. Listen for changes to the action request. If the
 * change is to the parameters, the path has to change.
 * 
 * @author Dave
 *
 */
public class ActionRequestListener extends BaseNodeListener implements INodeListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(ActionRequestListener.class);

	public ActionRequestListener(Node node) {
		super(node);
	}

	@Override
	public void processOwnershipEvent(OwnershipEvent<?, ?> event) {
		super.processOwnershipEvent(event);
		LOGGER.debug("Ownership event: " + event);
	}

	@Override
	public void processValueChangeEvent(ValueChangeEvent<?, ?> event) {
		super.processValueChangeEvent(event);
		LOGGER.debug("Value change event: " + event.getType());
		if (event.getSource() instanceof TLParamGroup && thisNode instanceof ActionRequest) {
			((ActionRequest) thisNode).createPathTemplate();
		}
	}
}
