/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.pde.internal.core.icheatsheet.simple;

/**
 * ISimpleCSCommand
 *
 */
public interface ISimpleCSCommand extends ISimpleCSRunObject {

	/**
	 * Attribute: serialization
	 * @see org.eclipse.core.commands.ParameterizedCommand#serialize()
	 * @return
	 */
	public String getSerialization();
	
	/**
	 * Attribute: serialization
	 * @param serialization
	 */
	public void setSerialization(String serialization);			

	/**
	 * Attribute: returns
	 * @return
	 */
	public boolean getReturns();
	
	/**
	 * Attribute: returns
	 * @param returns
	 */
	public void setReturns(boolean returns);		
	
	/**
	 * Attribute: confirm
	 * @return
	 */
	public boolean getConfirm();
	
	/**
	 * Attribute: confirm
	 * @param confirm
	 */
	public void setConfirm(boolean confirm);
	
	/**
	 * Attribute: when
	 * @return
	 */
	public String getWhen();
	
	/**
	 * Attribute: when
	 * @param when
	 */
	public void setWhen(String when);			
	
}
