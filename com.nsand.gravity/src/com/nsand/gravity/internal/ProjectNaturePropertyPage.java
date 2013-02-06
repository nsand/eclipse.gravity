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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

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
				viewer.setInput(descriptors);
			} catch (CoreException e) { }
		}
		return composite;
	}

	@Override
	public boolean performOk() {
		if (viewer != null && !viewer.getControl().isDisposed()) {
			try {
				final IProjectDescription description = project.getDescription();
				final Object[] checked = viewer.getCheckedElements();
				final String[] ids = new String[checked.length];
				// TODO Add required natures
				for (int i = 0; i < checked.length; i++) {
					ids[i] = ((IProjectNatureDescriptor) checked[i]).getNatureId();
				}
				description.setNatureIds(ids);
				project.setDescription(description, null);
			} catch (CoreException e) { }
		}
		return super.performOk();
	}
}
