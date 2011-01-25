/*******************************************************************************
 * Copyright (c) 2010 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.versions.core;

/**
 * @author Steffen Pingel
 */
public class ScmRepository {

	private String name;

	private String url;

	protected ScmRepository() {
	}

	public ScmRepository(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected void setUrl(String url) {
		this.url = url;
	}

}
