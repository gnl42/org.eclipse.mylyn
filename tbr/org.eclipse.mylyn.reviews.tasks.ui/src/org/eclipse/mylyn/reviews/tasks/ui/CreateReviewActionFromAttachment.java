/*******************************************************************************
 * Copyright (c) 2010 Research Group for Industrial Software (INSO), Vienna University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kilian Matt (Research Group for Industrial Software (INSO), Vienna University of Technology) - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.reviews.tasks.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.reviews.tasks.core.Attachment;
import org.eclipse.mylyn.reviews.tasks.core.IReviewMapper;
import org.eclipse.mylyn.reviews.tasks.core.ITaskProperties;
import org.eclipse.mylyn.reviews.tasks.core.PatchScopeItem;
import org.eclipse.mylyn.reviews.tasks.core.ResourceScopeItem;
import org.eclipse.mylyn.reviews.tasks.core.ReviewScope;
import org.eclipse.mylyn.reviews.tasks.core.internal.ReviewsUtil;
import org.eclipse.mylyn.reviews.tasks.core.internal.TaskProperties;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.ITaskDataManager;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskMapper;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.IActionDelegate;

public class CreateReviewActionFromAttachment extends Action implements
		IActionDelegate {

	private Attachment attachment;
	private ITaskAttachment taskAttachment;

	public void run(IAction action) {
		if (attachment != null) {
			try {
				// FIXME move common creation to a subclass
				TaskRepository taskRepository = taskAttachment
						.getTaskRepository();
				ITaskDataManager manager = TasksUi.getTaskDataManager();
				ITask parentTask = taskAttachment.getTask();
				TaskData parentTaskData = manager.getTaskData(taskAttachment
						.getTask());

				TaskMapper initializationData = new TaskMapper(parentTaskData);
				IReviewMapper taskMapper = ReviewsUiPlugin.getMapper();

				TaskData taskData = TasksUiInternal.createTaskData(
						taskRepository, initializationData, null,
						new NullProgressMonitor());
				AbstractRepositoryConnector connector = TasksUiPlugin
						.getConnector(taskRepository.getConnectorKind());

				connector.getTaskDataHandler().initializeSubTaskData(
						taskRepository, taskData, parentTaskData,
						new NullProgressMonitor());

				ITaskProperties taskProperties = TaskProperties.fromTaskData(
						manager, taskData);
				taskProperties
						.setSummary("[review] " + parentTask.getSummary());

				String reviewer = taskRepository.getUserName();
				taskProperties.setAssignedTo(reviewer);

				initTaskProperties(taskMapper, taskProperties);

				TasksUiInternal.createAndOpenNewTask(taskData);
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
		}

	}

	private void initTaskProperties(IReviewMapper taskMapper,
			ITaskProperties taskProperties) {
		ReviewScope scope = new ReviewScope();
		if (attachment.isPatch()) {
			scope.addScope(new PatchScopeItem(attachment));
		} else {
			scope.addScope(new ResourceScopeItem(attachment));
		}
		taskMapper.mapScopeToTask(scope, taskProperties);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(false);
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			if (structuredSelection.size() == 1) {
				if (structuredSelection.getFirstElement() instanceof ITaskAttachment) {
					action.setEnabled(true);
					taskAttachment = (ITaskAttachment) structuredSelection
							.getFirstElement();
					ITaskProperties taskProperties = TaskProperties
							.fromTaskData(TasksUi.getTaskDataManager(),
									taskAttachment.getTaskAttribute()
											.getTaskData());
					// FIXME date from task attachment
					this.attachment = ReviewsUtil.findAttachment(taskAttachment
							.getFileName(), taskAttachment.getAuthor()
							.getPersonId(), taskAttachment.getCreationDate()
							.toString(), taskProperties);
				}
			}
		}
	}

}
