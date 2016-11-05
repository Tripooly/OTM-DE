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
package org.opentravel.schemas.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemas.commands.OtmAbstractHandler;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.types.TypeNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.wizards.TypeSelectionWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assign type to navigator selected nodes. Wizard is run to allow the user to select the type to assign. If a selected
 * node is a TypeNode, the users of that type will be replaced.
 * 
 * TODO - move execute to a handler
 * 
 * TEST - TODO - use AbstractAction's main window
 * 
 * TODO - move the performFinish code from the wizard to here and make public.
 * 
 * TODO - make mainWindow=null safe
 * 
 * @author Dave Hollander
 * 
 */
public class AssignTypeAction extends OtmAbstractAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(AssignTypeAction.class);
	private static StringProperties propsDefault = new ExternalizedStringProperties("action.replaceUsers");

	// private MainController mc;

	/**
	 *
	 */
	public AssignTypeAction(final MainWindow mainWindow) {
		super(mainWindow, propsDefault);
		// mc = OtmRegistry.getMainController();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		LOGGER.debug("Replace starting.");
		typeSelector();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.actions.IWithNodeAction.AbstractWithNodeAction#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		Node n = getMainController().getCurrentNode_NavigatorView();
		if (n == null || !(n instanceof TypeNode) || !((TypeNode) n).isUser())
			return false;
		return n.getChain() == null ? n.isEditable() : n.getChain().isMajor();
	}

	public static boolean execute(List<Node> toChange, Node newType) {
		if (newType == null || !newType.isNamedEntity()) {
			LOGGER.warn("No type to assign. Early Exit.");
			return false;
		}
		if (toChange == null || toChange.size() <= 0) {
			LOGGER.warn("Nothing to assign to. Early Exit.");
			return false;
		}
		Node last = null;
		boolean ret = true;
		for (Node cn : toChange) {
			// 10/2016 dmh - clean up logic and skip fixName since done in assign type
			ret = ((TypeUser) cn).setAssignedType((TypeProvider) newType);
			if (!ret)
				DialogUserNotifier.openWarning("Warning", "Invalid type assignment");

			// // TEST - this can be very slow when doing 150+ nodes because the setAssigned refreshes the display
			// if (!((TypeUser) cn).setAssignedType((TypeProvider) newType)) {
			// ret = false;
			// DialogUserNotifier.openWarning("Warning", "Invalid type assignment");
			// // LOGGER.debug("Invalid type assigment: " + newType + " to " + cn);
			// } else {
			// // this is all notyfications() does: NodeNameUtils.fixName(cn);
			// PostTypeChange.notyfications(cn, newType);
			// }
			if (last != null && cn.getParent() != last.getParent()) {
				last = cn;
				OtmRegistry.getNavigatorView().refresh(last.getParent());
			}
		}
		OtmRegistry.getMainController().refresh();

		// LOGGER.debug("Assigned " + newType.getName() + " to " + toChange.size() + " nodes.");
		return ret;
	}

	// ** THIS IS NOT USED FOR TYPE SELECTION BUTTONS IN THE FACET TABLE
	// This is used for Replace Where Used
	private void typeSelector() {
		List<Node> users = new ArrayList<Node>(); // List of type users to replace type assignments with provider to be
													// selected

		List<Node> selections = getMainController().getSelectedNodes_NavigatorView();
		if (selections != null)
			for (Node s : selections) {
				if (s instanceof TypeNode)
					addTypeUsers((TypeNode) s, users); // get the users of the node, not just the node.
				else
					users.add(s);
			}

		// If the node is not in the head library, then create one.
		OtmAbstractHandler handler = new OtmAbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event) throws ExecutionException {
				return null;
			}
		};

		if (users == null || users.isEmpty())
			return;
		Node n = users.get(0);
		if (n.getChain() != null && !n.isInHead2())
			n = handler.createVersionExtension(n);
		if (n == null)
			return;

		// runSetTypeWizard(OtmRegistry.getActiveShell(), users );
		final TypeSelectionWizard wizard = new TypeSelectionWizard(new ArrayList<Node>(users));
		if (wizard.run(OtmRegistry.getActiveShell())) {
			execute(wizard.getList(), wizard.getSelection());
		}

		mc.refresh(users.get(0));
	}

	private void addTypeUsers(TypeNode tn, List<Node> users) {
		// throw new IllegalStateException("Not Implemented Yet.");
		if (tn.isUser()) {
			users.add(tn.getParent()); // This is a type node for a specific type user.
			// } else {
			// // This is a type node for the type provider.
			// users.addAll(tn.getParent().getTypeClass().getTypeUsersAndDescendants());
		}
	}

}
