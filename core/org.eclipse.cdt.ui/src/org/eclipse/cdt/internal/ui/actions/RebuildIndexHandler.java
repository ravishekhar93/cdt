/*******************************************************************************
 * Copyright (c) 2013, 2014 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

/**
 * Handler for {@link org.eclipse.cdt.internal.ui.actions.RebuildIndexAction}
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class RebuildIndexHandler extends AbstractUpdateIndexHandler {
	private final RebuildIndexAction rebuildIndexAction = new RebuildIndexAction();
	
	@Override
	public AbstractUpdateIndexAction getAction() {
		return rebuildIndexAction;
	}
}
