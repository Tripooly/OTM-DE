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
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VersionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add or remove children from libraries.
 * 
 * @author Dave Hollander
 *
 */
public class LibraryNodeListener extends BaseNodeListener implements INodeListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryNodeListener.class);

	public LibraryNodeListener(Node node) {
		super(node);
	}

	@Override
	public void processOwnershipEvent(OwnershipEvent<?, ?> event) {
		Node affectedNode = getAffectedNode(event);
		// LOGGER.debug("Library Ownership event: " + event.getType() + " this = " + thisNode + " affected = " +
		// affectedNode);
		LibraryNode ln = (LibraryNode) thisNode;

		switch (event.getType()) {
		case MEMBER_ADDED:
			// LOGGER.debug("Ownership change event: added" + affectedNode + " to " + thisNode);
			// Add the affected node to this library
			if (affectedNode instanceof VersionNode)
				ln.linkMember(((VersionNode) affectedNode).getNewestVersion());
			else
				ln.linkMember(affectedNode);
			if (ln.isInChain()) {
				ln.getChain().add((ComponentNode) affectedNode);
			}
			break;

		case MEMBER_REMOVED:
			// LOGGER.debug("Ownership change event: removed " + affectedNode + " from " + thisNode);
			// Remove affected from this library
			if (ln.isInChain()) {
				ln.getChain().removeAggregate((ComponentNode) affectedNode);
			}
			affectedNode.unlinkNode();
			break;

		default:
			LOGGER.debug("Unhandled Ownership event: " + event.getType() + " this = " + thisNode + " affected = "
					+ affectedNode);
			break;
		}
	}
}