/**
 * Copyright (c) 2013 Nick Sandonato
 * 
 * Released under the MIT license (http://opensource.org/licenses/MIT)
 */
package com.nsand.gravity.internal.handlers;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class FileExplorerHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Only execute if the platform supports this feature
		if(Desktop.isDesktopSupported()) {
			final IResource resource = getResource(event);
			if (resource != null) {
				final IPath location = getContainerLocation(resource);
				if (location != null) {
					try {
						Desktop.getDesktop().open(new File(location.toString()));
					}
					catch (IOException e) {
					}
				}
			}
		}
		return null;
	}

	/**
	 * Gets the appropriate folder path to open in the file explorer
	 * @param resource the selected {@link IResource} to navigate to
	 * @return the local file system path to the container for the resource 
	 */
	private IPath getContainerLocation(IResource resource) {
		final IContainer container = resource.getType() == IResource.FOLDER ? (IContainer) resource : resource.getParent();
		return container != null ? container.getLocation() : null;
	}

	/**
	 * Get the {@link IResource} from the execution event
	 * @param event the handler's execution event
	 * @return the selected {@link IResource}, or null if one is not selected
	 */
	private IResource getResource(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		IResource resource = null;
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof IResource) {
				resource = (IResource) element;
			}
			else if (element instanceof IAdaptable) {
				// Try to adapt the selection to an IResource
				resource = (IResource) ((IAdaptable) element).getAdapter(IResource.class);
			}
		}
		return resource;
	}
}
