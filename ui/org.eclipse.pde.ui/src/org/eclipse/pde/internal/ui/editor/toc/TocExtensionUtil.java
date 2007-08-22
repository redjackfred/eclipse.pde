/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.pde.internal.ui.editor.toc;

import java.util.HashSet;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.pde.internal.core.itoc.ITocConstants;
import org.eclipse.pde.internal.ui.util.XMLRootElementMatcher;

public class TocExtensionUtil {
	public static final String[] pageExtensions = {"htm","shtml","html","xhtml"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	public static final String tocExtension = "xml"; //$NON-NLS-1$
	private static HashSet pageExtensionSet = new HashSet(3);

	private static void populateHashSet()
	{	for(int i = 0; i < pageExtensions.length; ++i)
		{	pageExtensionSet.add(pageExtensions[i]);
		}
	}
	
	public static boolean hasValidPageExtension(IPath path)
	{	String fileExtension = path.getFileExtension();	
		if(fileExtension != null)
		{	fileExtension = fileExtension.toLowerCase(Locale.ENGLISH);
			if(pageExtensionSet.isEmpty())
			{	populateHashSet();
			}
			
			return pageExtensionSet.contains(fileExtension);
		}

		return false;
	}

	private static boolean hasValidTocExtension(IPath path)
	{	String fileExtension = path.getFileExtension();
		return fileExtension != null && fileExtension.equals(tocExtension); 
	}

	/**
	 * @param file
	 */
	public static boolean isTOCFile(IPath path) {
		if(!hasValidTocExtension(path))
			return false;
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		IResource resource = root.findMember(path);
		if(resource != null && resource instanceof IFile)
		{	return XMLRootElementMatcher.fileMatchesElement((IFile)resource, ITocConstants.ELEMENT_TOC);
		}

		return XMLRootElementMatcher.fileMatchesElement(path.toFile(), ITocConstants.ELEMENT_TOC);
	}

}
