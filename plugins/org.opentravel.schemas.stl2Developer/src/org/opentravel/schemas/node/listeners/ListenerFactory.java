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

import java.util.ArrayList;
import java.util.Collection;

import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Trivial factory class created to consolidate all the listener assignments for maintenance.
 * 
 * @author Dave
 *
 */
public class ListenerFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(ListenerFactory.class);

	public ListenerFactory() {
	}

	/**
	 * Set NodeIdentity listener.
	 * 
	 * @param node
	 *            - node to identify
	 */
	public static void setListner(Node node) {
		// clearListners(node);

		if (node.getTLModelObject() == null)
			return;
		if (node instanceof ModelNode) {
			((ModelNode) node).addListeners();
			return;
		}

		// Get listener from node delegated methods.
		BaseNodeListener listener = node.getNewListener();
		// Assign if there is not already one assigned
		if (listener != null && Node.GetNode(node.getTLModelObject()) == null)
			node.getTLModelObject().addListener(listener);

		// debugging - trap if there is already a listener
		// Node lNode = Node.GetNode(node.getTLModelObject());
		// if (lNode != null) {
		// // throw new IllegalStateException(node+" already has identity listeners.");
		// // LOGGER.debug(node + " already has identity listener.");
		// } else if (listener != null) {
		// node.getTLModelObject().addListener(listener);
		//
		// // If it is an identity listener, make sure it is associated with the node
		// if (listener instanceof NodeIdentityListener)
		// assert node.getTLModelObject().getListeners().contains(listener);
		// }
	}

	/**
	 * Remove all listeners from this node's tl object.
	 */
	public static void clearListners(Node node) {
		if (node.getTLModelObject() == null)
			return;
		clearListners(node.getTLModelObject());
		// Collection<ModelElementListener> listeners = new ArrayList<>(node.getTLModelObject().getListeners());
		// if (!listeners.isEmpty())
		// for (ModelElementListener l : listeners)
		// node.getTLModelObject().removeListener(l);
	}

	/**
	 * Remove all listeners from this node's tl object.
	 */
	public static void clearListners(TLModelElement tlObj) {
		Collection<ModelElementListener> listeners = new ArrayList<>(tlObj.getListeners());
		if (!listeners.isEmpty())
			for (ModelElementListener l : listeners) {
				// just in case an event was not handled, make sure the type is unassigned.
				if (l instanceof TypeUserListener) {
					TypeUser user = (TypeUser) ((TypeUserListener) l).getNode();
					TypeProvider type = user.getAssignedType();
					type.removeTypeUser(user);
				}
				tlObj.removeListener(l);
			}
	}
}
