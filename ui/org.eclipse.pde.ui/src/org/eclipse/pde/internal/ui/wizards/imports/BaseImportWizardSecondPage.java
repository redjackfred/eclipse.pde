package org.eclipse.pde.internal.ui.wizards.imports;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.core.plugin.*;
import org.eclipse.pde.core.plugin.IFragment;
import org.eclipse.pde.core.plugin.IFragmentModel;
import org.eclipse.pde.core.plugin.IPlugin;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.elements.DefaultContentProvider;
import org.eclipse.pde.internal.ui.wizards.ListUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public abstract class BaseImportWizardSecondPage extends WizardPage {
	
	protected static final String SETTINGS_IMPLICIT = "implicit";
	protected static final String SETTINGS_ADD_FRAGMENTS = "addFragments";
	
	protected PluginImportWizardFirstPage page1;
	protected IPluginModelBase[] models = new IPluginModelBase[0];
	private String location;
	protected Button implicitButton;
	protected Button addFragmentsButton;
	protected TableViewer importListViewer;

	class ContentProvider
		extends DefaultContentProvider
		implements IStructuredContentProvider {
		public Object[] getElements(Object element) {
			return new Object[0];
		}
	}
	
	public BaseImportWizardSecondPage(String pageName, PluginImportWizardFirstPage page) {
		super(pageName);
		this.page1 = page;
		PDEPlugin.getDefault().getLabelProvider().connect(this);
	}

	protected Composite createImportList(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label label = new Label(container, SWT.NONE);
		label.setText(PDEPlugin.getResourceString("ImportWizard.DetailedPage.importList"));

		Table table = new Table(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 180;
		gd.heightHint = 250;
		table.setLayoutData(gd);

		importListViewer = new TableViewer(table);
		importListViewer.setLabelProvider(PDEPlugin.getDefault().getLabelProvider());
		importListViewer.setContentProvider(new ContentProvider());
		importListViewer.setInput(PDECore.getDefault().getExternalModelManager());
		importListViewer.setSorter(ListUtil.PLUGIN_SORTER);
		return container;
	}
	
	protected Composite createComputationsOption(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		
		Label label = new Label(composite, SWT.NONE);
		label.setText(PDEPlugin.getResourceString("ImportWizard.SecondPage.optionsLabel"));
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		implicitButton = new Button(composite, SWT.CHECK);
		implicitButton.setText(PDEPlugin.getResourceString("ImportWizard.SecondPage.implicit"));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = 15;
		implicitButton.setLayoutData(gd);
		if (getDialogSettings().get(SETTINGS_IMPLICIT) != null)
			implicitButton.setSelection(getDialogSettings().getBoolean(SETTINGS_IMPLICIT));
		else 
			implicitButton.setSelection(true);
				
		addFragmentsButton = new Button(composite, SWT.CHECK);
		addFragmentsButton.setText(PDEPlugin.getResourceString("ImportWizard.SecondPage.addFragments"));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = 15;
		addFragmentsButton.setLayoutData(gd);
		if (getDialogSettings().get(SETTINGS_ADD_FRAGMENTS) != null)
			addFragmentsButton.setSelection(getDialogSettings().getBoolean(SETTINGS_ADD_FRAGMENTS));
		else 
			addFragmentsButton.setSelection(true);
			
		return composite;
		
	}

	public void dispose() {
		PDEPlugin.getDefault().getLabelProvider().disconnect(this);
	}
	
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible && isRefreshNeeded()) {
			models = page1.getModels();
			refreshPage();
		}
	}

	protected abstract void refreshPage();

	protected boolean isRefreshNeeded() {
		String currLocation = page1.getDropLocation();
		if (location == null || !location.equals(currLocation)) {
			location = page1.getDropLocation();
			return true;
		}
		return false;	
	}
	
	private IPluginModelBase findModel(String id) {
		for (int i = 0; i < models.length; i++) {
			if (models[i].getPluginBase().getId().equals(id))
				return models[i];
		}
		return null;
	}

	private IFragmentModel[] findFragments(IPlugin plugin) {
		ArrayList result = new ArrayList();
		for (int i = 0; i < models.length; i++) {
			if (models[i] instanceof IFragmentModel) {
				IFragment fragment = ((IFragmentModel) models[i]).getFragment();
				if (plugin.getId().equalsIgnoreCase(fragment.getPluginId())) {
					result.add(models[i]);
				}
			}
		}
		return (IFragmentModel[]) result.toArray(new IFragmentModel[result.size()]);
	}

	protected void addPluginAndDependencies(
		IPluginModelBase model,
		ArrayList selected,
		boolean addFragments) {
			
		if (!selected.contains(model)) {
			selected.add(model);
			IPluginExtension[] extensions = model.getPluginBase().getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				if (extensions[i].getPoint().equals("org.eclipse.ant.core.extraClasspathEntries")) {
					IPluginModelBase antModel = findModel("org.apache.ant");
					if (antModel != null && !selected.contains(antModel))
						selected.add(antModel);
					break;
				}
			}
			addDependencies(model, selected, addFragments);
		}
	}
	
	protected void addDependencies(
		IPluginModelBase model,
		ArrayList selected,
		boolean addFragments) {
		if (model instanceof IPluginModel) {
			IPlugin plugin = ((IPluginModel) model).getPlugin();
			IPluginImport[] required = plugin.getImports();
			if (required.length > 0) {
				for (int i = 0; i < required.length; i++) {
					IPluginModelBase found = findModel(required[i].getId());
					if (found != null) {
						addPluginAndDependencies(found, selected, addFragments);
					}
				}
			}
			if (addFragments) {
				IFragmentModel[] fragments = findFragments(plugin);
				for (int i = 0; i < fragments.length; i++) {
					addPluginAndDependencies(fragments[i], selected, addFragments);
				}
			}
		}
		if (addFragments && model instanceof IFragmentModel) {
			IFragment fragment = ((IFragmentModel) model).getFragment();
			IPluginModelBase found = findModel(fragment.getPluginId());
			if (found != null) {
				addPluginAndDependencies(found, selected, addFragments);
			}
		}

	}
	
	protected void addImplicitDependencies(ArrayList selected, boolean addFragment) {
		for (int i = 0; i < models.length; i++) {
			String id = models[i].getPluginBase().getId();
			if (id.equals("org.eclipse.core.boot") || id.equals("org.eclipse.core.runtime")) {
				addPluginAndDependencies(models[i], selected, addFragment);
			}			
		}
	}
	
	public IPluginModelBase[] getModelsToImport() {
		TableItem[] items = importListViewer.getTable().getItems();
		ArrayList result = new ArrayList();
		for (int i = 0; i < items.length; i++) {
			result.add(items[i].getData());
		}
		return (IPluginModelBase[]) result.toArray(new IPluginModelBase[result.size()]);
	}
	
	public void storeSettings() {
		IDialogSettings settings = getDialogSettings();
		settings.put(SETTINGS_IMPLICIT, implicitButton.getSelection());
		settings.put(SETTINGS_ADD_FRAGMENTS, addFragmentsButton.getSelection());
	}

}
