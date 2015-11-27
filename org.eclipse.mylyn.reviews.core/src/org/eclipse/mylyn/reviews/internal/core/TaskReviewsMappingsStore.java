package org.eclipse.mylyn.reviews.internal.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Set;

import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener;
import org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta;
import org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta.Kind;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.core.util.TasksCoreUtil;
import org.eclipse.mylyn.reviews.core.spi.ReviewsConnector;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

/**
 * This class is used by review connectors to provide a mapping from tasks to reviews. These mappings are used by
 * TaskEditorReviewPart to give a table of the reviews pertaining to a task. The class is limited as it maps one task to
 * many reviews. It is however possible (albeit strange) to have multiple tasks for one review. This is a limitation by
 * design.
 * 
 * @author Blaine Lewis
 */

@SuppressWarnings("restriction")
public class TaskReviewsMappingsStore implements ITaskListChangeListener {
	static final String ATTR_ASSOCIATED_TASK = "org.eclipse.mylyn.associated.task"; //$NON-NLS-1$

	private final SetMultimap<String, String> taskReviewsMap;

	private final TaskRepositoryManager repositoryManager;

	private final TaskList taskList;

	private static TaskReviewsMappingsStore instance;

	public TaskReviewsMappingsStore(TaskList taskList, TaskRepositoryManager repositoryManager) {
		this.taskList = taskList;
		this.repositoryManager = repositoryManager;
		taskReviewsMap = Multimaps.synchronizedSetMultimap(LinkedHashMultimap.<String, String> create());
	}

	public void readFromTaskList() {
		for (AbstractTask review : taskList.getAllTasks()) {
			String task = getTaskUrl(review);
			if (task != null) {
				taskReviewsMap.put(review.getUrl(), task);
			}
		}
	}

	private void updateMapping(ITask review, String newTaskUrl) {
		String reviewUrl = review.getUrl();
		String oldTaskUrl = getTaskUrl(review);

		if (oldTaskUrl != null && !oldTaskUrl.equals(newTaskUrl)) {
			taskReviewsMap.remove(oldTaskUrl, reviewUrl);
		}
		taskReviewsMap.put(newTaskUrl, reviewUrl);
		review.setAttribute(ATTR_ASSOCIATED_TASK, newTaskUrl);
	}

	/*
	 * This method of extracting URLs is deficient because if we have "(www.helloworld.com)" it will be
	 * a valid URL. This is difficult to format though so we won't handle that case.
	 */
	public void addTaskAssocation(ITask review, TaskData taskData) {
		TaskAttribute attr = taskData.getRoot().getMappedAttribute(TaskAttribute.DESCRIPTION);

		if (attr == null) {
			return;
		}

		String oldTaskUrl = getTaskUrl(review);
		String description = attr.getValue();
		for (String token : description.split("\\s+")) { //$NON-NLS-1$
			if (token.equals(oldTaskUrl)) {
				return;// don't do expensive getTaskByUrl lookup if nothing changed
			} else if (token.contains("://")) { //$NON-NLS-1$
				try {
					new URL(token);
					ITask task = getTaskByUrl(token);

					if (task != null) {
						updateMapping(review, token);
						return;
					}
				} catch (MalformedURLException e) {
					//Do nothing, this is expected behavior when there is no URL
				}
			}
		}

		deleteMappingsTo(review);
	}

	public String getTaskUrl(ITask review) {
		return review.getAttribute(ATTR_ASSOCIATED_TASK);
	}

	public Collection<String> getReviewUrls(String taskUrl) {
		synchronized (taskReviewsMap) {
			return ImmutableSet.copyOf(taskReviewsMap.get(taskUrl));
		}
	}

	ITask getTaskByUrl(String url) {
		return TasksCoreUtil.getTaskByUrl(taskList, repositoryManager, url);
	}

	@Override
	public void containersChanged(Set<TaskContainerDelta> containers) {
		for (TaskContainerDelta delta : containers) {
			if (delta.getKind() == Kind.DELETED) {
				IRepositoryElement reviewRepoElement = delta.getElement();

				if (!(reviewRepoElement instanceof ITask)) {
					continue;
				}

				ITask review = (ITask) reviewRepoElement;

				//We need to check it in case the mapping was removed from the task
				AbstractRepositoryConnector connector = repositoryManager.getRepositoryConnector(review.getConnectorKind());

				if (review != null && connector instanceof ReviewsConnector) {
					deleteMappingsTo(review);
				}
			}
		}
	}

	private void deleteMappingsTo(ITask review) {
		String taskUrl = getTaskUrl(review);
		if (taskUrl != null) {
			taskReviewsMap.remove(taskUrl, review.getUrl());
			review.setAttribute(ATTR_ASSOCIATED_TASK, null);
		}
	}

	public static void setInstance(TaskReviewsMappingsStore instance) {
		TaskReviewsMappingsStore.instance = instance;
	}

	public static TaskReviewsMappingsStore getInstance() {
		return instance;
	}
}