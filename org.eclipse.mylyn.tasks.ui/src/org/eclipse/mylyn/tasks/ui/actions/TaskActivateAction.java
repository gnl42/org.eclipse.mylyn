/*******************************************************************************
 * Copyright (c) 2004 - 2005 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.tasks.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.mylar.tasks.ITask;
import org.eclipse.mylar.tasks.MylarTasksPlugin;

/**
 * @author Mik Kersten and Ken Sueda
 */
public class TaskActivateAction extends Action {
	
	public static final String ID = "org.eclipse.mylar.tasks.actions.context.activate";
		
	private ITask task;
	
	public TaskActivateAction(ITask task) {
		this.task = task;
		setId(ID);
	}
	
	public void run() {
        MylarTasksPlugin.getTaskListManager().activateTask(task);
	}
}