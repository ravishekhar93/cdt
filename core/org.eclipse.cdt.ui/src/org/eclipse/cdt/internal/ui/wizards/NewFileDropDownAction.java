/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards;

import org.eclipse.jface.action.IAction;

public class NewFileDropDownAction extends AbstractWizardDropDownAction {

	public NewFileDropDownAction() {
	    super();
//		WorkbenchHelp.setHelp(this, ICHelpContextIds.OPEN_FILE_WIZARD_ACTION);
	}

	@Override
	protected IAction[] getWizardActions() {
		return CWizardRegistry.getFileWizardActions();
	}
}
