/*******************************************************************************
 * Copyright (c) 2014 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.gerrit.tests.core.client.compat;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.mylyn.commons.sdk.util.CommonTestUtil;
import org.eclipse.mylyn.internal.gerrit.core.client.JSonSupport;
import org.eclipse.mylyn.internal.gerrit.core.client.compat.PatchScriptX;
import org.junit.Test;

public class PatchScriptXTest extends TestCase {
	@Test
	public void testPatchScriptIsNotBinary() throws Exception {
		PatchScriptX patchScript = parseFile("testdata/PatchScript_java.json");

		assertFalse(patchScript.isBinary());
	}

	@Test
	public void testPatchScriptIsBinary() throws Exception {
		PatchScriptX patchScript = parseFile("testdata/PatchScript_gif.json");

		assertTrue(patchScript.isBinary());
	}

	private PatchScriptX parseFile(String path) throws IOException {
		File file = CommonTestUtil.getFile(this, path);
		String content = CommonTestUtil.read(file);
		PatchScriptXAsResult result = new JSonSupport().parseResponse(content, PatchScriptXAsResult.class);

		assertEquals("2.0", result.jsonrpc);
		assertTrue(result.id > 0); //any positive integer is acceptable
		assertNotNull(result.result);
		return result.result;
	}

	private class PatchScriptXAsResult {
		private String jsonrpc;

		private int id;

		private PatchScriptX result;
	}
}
