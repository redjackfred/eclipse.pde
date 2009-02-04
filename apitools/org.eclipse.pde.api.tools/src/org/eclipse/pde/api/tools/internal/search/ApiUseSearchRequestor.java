/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.internal.search;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.api.tools.internal.model.ApiBaseline;
import org.eclipse.pde.api.tools.internal.model.PluginProjectApiComponent;
import org.eclipse.pde.api.tools.internal.provisional.ApiPlugin;
import org.eclipse.pde.api.tools.internal.provisional.IApiAnnotations;
import org.eclipse.pde.api.tools.internal.provisional.VisibilityModifiers;
import org.eclipse.pde.api.tools.internal.provisional.builder.IReference;
import org.eclipse.pde.api.tools.internal.provisional.builder.ReferenceModifiers;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiComponent;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiElement;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiMember;
import org.eclipse.pde.api.tools.internal.provisional.search.IApiSearchRequestor;
import org.eclipse.pde.api.tools.internal.provisional.search.IApiSearchScope;
import org.eclipse.pde.api.tools.internal.util.Util;

/**
 * Default implementation of an {@link IApiSearchRequestor} to use with the
 * {@link ApiSearchEngine}. This requestor returns a search scope
 * composed of the dependent (visible) {@link IApiComponent}s for the given 
 * {@link IApiElement}
 * 
 * @since 1.0.0
 */
public class ApiUseSearchRequestor implements IApiSearchRequestor {

	/**
	 * The backing element to search against
	 */
	private ArrayList fElements = null;
	/**
	 * The mask to use while searching
	 */
	private int fSearchMask = 0;
	
	/**
	 * The listing of components to ignore
	 */
	private Set fExcludeList = null;
	
	/**
	 * The search scope for this requestor
	 */
	private IApiSearchScope fScope = null;
	
	/**
	 * Default comparator that orders {@link IApiComponent} by their ID 
	 */
	private static final Comparator componentsorter = new Comparator(){
		public int compare(Object o1, Object o2) {
			if(o1 instanceof IApiComponent && o2 instanceof IApiComponent) {
				try {
					return ((IApiComponent)o1).getId().compareTo(((IApiComponent)o2).getId());
				}
				catch (CoreException ce) {}
			}
			return -1;
		}
	};
	
	/**
	 * Constructor
	 * @param elements an array of {@link IApiElement}s for the search engine to use
	 * @param searchkinds the kinds of references to search for. 
	 * <br>Options include: 
	 * <ol>
	 * <li>{@link #INCLUDE_API}</li>
	 * <li>{@link #INCLUDE_INTERNAL}</li>
	 * </ol>
	 * @param excludelist an array of component ids that should be excluded from the search
	 */
	public ApiUseSearchRequestor(IApiElement[] elements, int searchkinds, String[] excludelist) {
		fSearchMask = searchkinds;
		fElements = new ArrayList(elements.length);
		for(int i = 0; i < elements.length; i++) {
			fElements.add(elements[i]);
		}
		fExcludeList = new HashSet(excludelist.length);
		for (int i = 0; i < excludelist.length; i++) {
			fExcludeList.add(excludelist[i]);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.provisional.search.IApiSearchRequestor#acceptComponent(org.eclipse.pde.api.tools.internal.provisional.model.IApiComponent)
	 */
	public boolean acceptComponent(IApiComponent component) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.provisional.search.IApiSearchRequestor#acceptMember(org.eclipse.pde.api.tools.internal.provisional.model.IApiMember)
	 */
	public boolean acceptMember(IApiMember member) {
		try {
			return getScope().encloses(member);
		}
		catch(CoreException ce) {}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.provisional.search.IApiSearchRequestor#acceptReference(org.eclipse.pde.api.tools.internal.provisional.builder.IReference)
	 */
	public boolean acceptReference(IReference reference) {
		try {
			IApiMember member = reference.getResolvedReference();
			if(member != null) {
				IApiComponent component = member.getApiComponent();
				if(component.isSystemComponent() || !fElements.contains(component)) {
					return false;
				}
				if(component.equals(reference.getMember().getApiComponent())) {
					return false;
				}
				if(fSearchMask > 0) {
					if(includesAPI() && includesInternal()) {
						return true;
					}
					IApiAnnotations annots = component.getApiDescription().resolveAnnotations(member.getHandle());
					if(annots != null) {
						int vis = annots.getVisibility();
						if(VisibilityModifiers.isAPI(vis) && includesAPI()) {
							return true;
						}
						else if(VisibilityModifiers.isPrivate(vis) && includesInternal()) {
							return true;
						}
					}
				}
			}
		}
		catch(CoreException ce) {
			ApiPlugin.log(ce);
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.provisional.search.IApiSearchRequestor#getReferenceKinds()
	 */
	public int getReferenceKinds() {
		int kinds = ReferenceModifiers.MASK_REF_ALL & ~ReferenceModifiers.REF_CONSTANTPOOL;
		return kinds;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.provisional.search.IApiSearchRequestor#getScope()
	 */
	public IApiSearchScope getScope() {
		if(fElements != null) {
			if(fScope == null) {
				try {
					TreeSet comps = new TreeSet(componentsorter);
					IApiComponent[] components = null;
					IApiComponent component = null;
					for(Iterator iter = fElements.iterator(); iter.hasNext();) {
						component = ((IApiComponent)iter.next()).getApiComponent();
						components = ((ApiBaseline)component.getBaseline()).getVisibleDependentComponents(new IApiComponent[] {component});
						for (int j = 0; j < components.length; j++) {
							if(acceptComponent0(components[j])) {
								comps.add(components[j]);
							}
						}
					}
					components = (IApiComponent[]) comps.toArray(new IApiComponent[comps.size()]);
					fScope = new ApiUseSearchScope(components);
				}
				catch(CoreException ce) {}
			}
		}
		return fScope;
	}

	private boolean acceptComponent0(IApiComponent component) throws CoreException {
		return component != null && 
				!component.isSystemComponent() && 
				!fExcludeList.contains(component.getId()) && 
				isApiComponent(component);
	}
	
	private boolean isApiComponent(IApiComponent component) {
		if(component instanceof PluginProjectApiComponent) {
			PluginProjectApiComponent comp = (PluginProjectApiComponent) component;
			return comp.hasApiDescription();
		}
		else {
			return Util.isApiToolsComponent(component);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.provisional.search.IApiSearchRequestor#includesAPI()
	 */
	public boolean includesAPI() {
		return (fSearchMask & INCLUDE_API) > 0;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.provisional.search.IApiSearchRequestor#includesInternal()
	 */
	public boolean includesInternal() {
		return (fSearchMask & INCLUDE_INTERNAL) > 0;
	}
}
