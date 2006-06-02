/*******************************************************************************
 * Copyright (c) 2004 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.tasklist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author Rob Elves
 */
public class AttributeContainer implements Serializable {
	
	private static final long serialVersionUID = -3990742719133977940L;

	/** The keys for the report attributes */
	private ArrayList<Object> attributeKeys;

	/** report attributes (status, resolution, etc.) */
	private HashMap<Object, AbstractRepositoryTaskAttribute> attributes;
	
	public AttributeContainer() {
		attributeKeys = new ArrayList<Object>();
		attributes = new HashMap<Object, AbstractRepositoryTaskAttribute>();
	}
	
	public void addAttribute(Object key, AbstractRepositoryTaskAttribute attribute) {
		if (!attributes.containsKey(attribute.getName())) {
			attributeKeys.add(key);
		}
		attributes.put(key, attribute);
	}
	
	public AbstractRepositoryTaskAttribute getAttribute(Object key) {
		return attributes.get(key);
	}

	public void removeAttribute(Object key) {
		attributeKeys.remove(key);
		attributes.remove(key);
	}
	
	public List<AbstractRepositoryTaskAttribute> getAttributes() {
		ArrayList<AbstractRepositoryTaskAttribute> attributeEntries = new ArrayList<AbstractRepositoryTaskAttribute>(
				attributeKeys.size());
		for (Iterator<Object> it = attributeKeys.iterator(); it.hasNext();) {
			Object key = it.next();
			AbstractRepositoryTaskAttribute attribute = attributes.get(key);
			attributeEntries.add(attribute);
		}
		return attributeEntries;
	}

	public String getAttributeValue(Object key) {
		AbstractRepositoryTaskAttribute attribute = getAttribute(key);
		if(attribute != null) {
			// TODO: unescape should happen on connector side not here
			//return HtmlStreamTokenizer.unescape(attribute.getValue());
			return attribute.getValue();
		}
		return "";
	}

	public void removeAllAttributes() {
		attributeKeys.clear();
		attributes.clear();
	}
}
