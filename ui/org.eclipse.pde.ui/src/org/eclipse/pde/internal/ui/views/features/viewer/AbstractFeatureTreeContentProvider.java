/*******************************************************************************
 * Copyright (c) 2019 Ed Scadding.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ed Scadding <edscadding@secondfiddle.org.uk> - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.views.features.viewer;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.*;
import org.eclipse.pde.internal.core.*;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.progress.DeferredTreeContentManager;

public abstract class AbstractFeatureTreeContentProvider implements ITreeContentProvider, IFeatureModelListener {

	protected final FeatureModelManager fFeatureModelManager;

	protected DeferredFeatureInput fInput;

	protected DeferredTreeContentManager fDeferredTreeContentManager;

	private TreeViewer fViewer;

	public AbstractFeatureTreeContentProvider(FeatureModelManager featureModelManager) {
		fFeatureModelManager = featureModelManager;
		fFeatureModelManager.addFeatureModelListener(this);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		fViewer = (TreeViewer) viewer;
		fDeferredTreeContentManager = new DeferredTreeContentManager(fViewer);
		fDeferredTreeContentManager.addUpdateCompleteListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				resetViewerScrollPosition();
			}
		});

		if (newInput instanceof DeferredFeatureInput) {
			fInput = (DeferredFeatureInput) newInput;
		}
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof DeferredFeatureInput) {
			DeferredFeatureInput deferredFeatureInput = (DeferredFeatureInput) inputElement;
			return deferredFeatureInput.isInitialized() ? deferredFeatureInput.getChildren(inputElement)
					: fDeferredTreeContentManager.getChildren(inputElement);
		}

		return new Object[0];
	}

	@Override
	public void dispose() {
		fFeatureModelManager.removeFeatureModelListener(this);
	}

	@Override
	public void modelsChanged(IFeatureModelDelta delta) {
		refreshViewer();
	}

	private void refreshViewer() {
		runViewerTask(fViewer::refresh);
	}

	private void resetViewerScrollPosition() {
		runViewerTask(() -> {
			Tree tree = fViewer.getTree();
			if (tree.getItemCount() > 0) {
				TreeItem firstItem = tree.getItem(0);
				tree.setTopItem(firstItem);
			}
		});
	}

	private void runViewerTask(Runnable viewerTask) {
		if (fViewer.getTree().isDisposed()) {
			return;
		}

		fViewer.getTree().getDisplay().asyncExec(() -> {
			if (!fViewer.getTree().isDisposed()) {
				viewerTask.run();
			}
		});
	}

}