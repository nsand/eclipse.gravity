/**
 * Copyright (c) 2013 Nick Sandonato
 * 
 * Released under the MIT license (http://opensource.org/licenses/MIT)
 */
package com.nsand.gravity.internal;


import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNatureDescriptor;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import com.nsand.gravity.Activator;
import com.nsand.gravity.internal.preferences.IGravityPreferences;

/**
 * @author nsand
 *
 */
public class ProjectNaturePropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

	private CheckboxTableViewer viewer;
	private IProject project;

	public ProjectNaturePropertyPage() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		project = (IProject) getElement().getAdapter(IProject.class);
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		if (project != null) {
			try {
				initializeDefaults();
				final IProjectDescription description = project.getDescription();
				final IProjectNatureDescriptor[] descriptors = ResourcesPlugin.getWorkspace().getNatureDescriptors();
				Arrays.sort(descriptors, new Comparator<IProjectNatureDescriptor>() {
					@Override
					public int compare(IProjectNatureDescriptor desc1, IProjectNatureDescriptor desc2) {
						return ((IProjectNatureDescriptor) desc1).getLabel().compareTo(((IProjectNatureDescriptor) desc2).getLabel());
					}
				});
				Label label = new Label(composite, SWT.NONE);
				label.setText("&Natures:");
				
				viewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
				viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
				viewer.setContentProvider(ArrayContentProvider.getInstance());
				viewer.setLabelProvider(new LabelProvider() {
					@Override
					public String getText(Object element) {
						return ((IProjectNatureDescriptor) element).getLabel();
					}
				});
				viewer.addCheckStateListener(new ICheckStateListener() {
					
					@Override
					public void checkStateChanged(CheckStateChangedEvent event) {
						if (event.getChecked()) {
							enableDependencies((IProjectNatureDescriptor) event.getElement(), false);
						}
					}
				});
				viewer.setInput(descriptors);

				final String[] natures = description.getNatureIds();
				for (int i = 0; i < natures.length; i++) {
					viewer.setChecked(ResourcesPlugin.getWorkspace().getNatureDescriptor(natures[i]), true);
				}
			} catch (CoreException e) { }
		}
		return composite;
	}

	private void enableDependencies(IProjectNatureDescriptor descriptor, boolean isDependency) {
		final String[] dependencies = descriptor.getRequiredNatureIds();
		for (int i = 0; i < dependencies.length; i++) {
			enableDependencies(ResourcesPlugin.getWorkspace().getNatureDescriptor(dependencies[i]), true);
		}
		if (isDependency) {
			viewer.setChecked(descriptor, true);
		}
	}

	@Override
	public boolean performOk() {
		// TODO Need validation to make sure dependencies are enabled
		if (viewer != null && !viewer.getControl().isDisposed()) {
			try {
				final IProjectDescription description = project.getDescription();
				final Object[] checked = viewer.getCheckedElements();
				final String[] ids = new String[checked.length];

				for (int i = 0; i < checked.length; i++) {
					ids[i] = ((IProjectNatureDescriptor) checked[i]).getNatureId();
				}
				description.setNatureIds(ids);
				project.setDescription(description, null);
			} catch (CoreException e) { }
		}
		return super.performOk();
	}

	private void initializeDefaults() {
		final IEclipsePreferences preferences = getProjectPreferences();
		if (preferences != null) {
			final String preference = preferences.get(IGravityPreferences.PROJECT_NATURES_KEY, null);
			if (preference == null) {
				try {
					final String[] ids = project.getDescription().getNatureIds();
					final StringBuffer buffer = new StringBuffer();
					for (int i = 0; i < ids.length; i++) {
						if (buffer.length() > 0) {
							buffer.append(',');
						}
						buffer.append(ids[i]);
					}
					preferences.put(IGravityPreferences.PROJECT_NATURES_KEY, buffer.toString());
				} catch (CoreException e) {
					e.printStackTrace();
				}
				
			}
		}
	}

	@Override
	protected void performDefaults() {
		final IEclipsePreferences preferences = getProjectPreferences();
		if (preferences != null) {
			final String preference = preferences.get(IGravityPreferences.PROJECT_NATURES_KEY, ""); //$NON-NLS-1$
			final String[] natures = preference.split(","); //$NON-NLS-1$
			for (int i = 0; i < natures.length; i++) {
				viewer.setChecked(ResourcesPlugin.getWorkspace().getNatureDescriptor(natures[i]), true);
			}
		}
		super.performDefaults();
	}

	private IEclipsePreferences getProjectPreferences() {
		IEclipsePreferences preferences = null;
		if (project != null) {
			final IScopeContext context = new ProjectScope(project);
			preferences = context.getNode(Activator.PLUGIN_ID);
		}
		return preferences;
	}
}
