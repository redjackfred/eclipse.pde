/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.services;

public interface IClipboardService {
	public void copy();
	public void paste();
	public void cut();
	public void setHandler(Handler handler);

	public interface Handler {
		public void paste();
		public void copy();
		public void cut();
//		public boolean canCopy();
//		public boolean canPaste();
	}
}