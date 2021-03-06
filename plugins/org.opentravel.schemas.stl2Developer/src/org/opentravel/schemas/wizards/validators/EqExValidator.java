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
package org.opentravel.schemas.wizards.validators;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.wizards.NewPropertiesWizard;

/**
 * @author Agnieszka Janowska
 * 
 */
public class EqExValidator implements FormValidator {

	// private final Node parentNode;
	// private final NewPropertiesWizard wizard;

	public EqExValidator(final Node parent, final NewPropertiesWizard wizard) {
		// parentNode = parent;
		// this.wizard = wizard;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.wizards.FormValidator#validate()
	 */
	@Override
	public void validate() throws ValidationException {
		// final PropertyNode n = wizard.getSelectedNode();
		// if (n == null) {
		// return;
		// }
		// // TODO - why does validate have side effect?
		// n.setLibrary(parentNode.getLibrary());
		// if (n.getName() == null || n.getName().isEmpty()) {
		// throw new ValidationException(Messages.getString("error.newPropertyName"));
		// }
		// if (n.getPropertyType() == null || n.getPropertyType() == PropertyNodeType.UNKNOWN) {
		// throw new ValidationException(Messages.getString("error.newPropertyType") + " (" + n.getName() + ")");
		// }
		// // if (n.getModelObject() == null || n.getModelObject().getTLType() == null) {
		// // if (n.getPropertyType() == PropertyNodeType.ATTRIBUTE || n.getPropertyType() ==
		// // PropertyNodeType.ELEMENT) {
		// // throw new ValidationException(Messages.getString("error.newPropertyTLType") + " (" +
		// // n.getName() + ")");
		// // }
		// // }
		// if (!parentNode.isUnique(n)) {
		// throw new ValidationException(Messages.getString("error.newProperty"));
		// }
	}

	@Override
	public void validate(Node selectedNode) throws ValidationException {
		// TODO Auto-generated method stub

	}

}
