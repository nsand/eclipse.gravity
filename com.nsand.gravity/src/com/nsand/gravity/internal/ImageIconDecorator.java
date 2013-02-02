/**
 * Copyright (c) 2013 Nick Sandonato
 * 
 * Released under the MIT license (http://opensource.org/licenses/MIT)
 */
package com.nsand.gravity.internal;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

/**
 * Decorator that replaces the default explorer icon with the actual
 * resource image (if it's icon-sized).
 * 
 * @author nsand
 *
 */
public class ImageIconDecorator implements ILabelDecorator {

	/** Header for GIF87 */
	private static final byte[] GIF87 = new byte[] { 0x47, 0x49, 0x46, 0x38, 0x37, 0x61 };
	/** Header for GIF89 */
	private static final byte[] GIF89 = new byte[] { 0x47, 0x49, 0x46, 0x38, 0x39, 0x61 };
	/** Header for PNG */
	private static final byte[] PNG = new byte[] { (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a };

	/** The header length */
	private static final int HEADER_LENGTH = 6;

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image decorateImage(Image image, Object element) {
		final IFile file = (IFile) element;
		BufferedInputStream contents = null;
		try {
			contents = new BufferedInputStream(file.getContents());
			final byte[] header = new byte[HEADER_LENGTH];
			int read = 0, offset = 0;
			contents.mark(HEADER_LENGTH);
			while (offset < HEADER_LENGTH && (read = contents.read(header, offset, HEADER_LENGTH)) > 0) {
				offset += read;
			}
			// Check the first series of bytes to see if it matches any of the image headers
			if (Arrays.equals(header, GIF89) || Arrays.equals(header, PNG) || Arrays.equals(header, GIF87)) {
				contents.reset();
				final ImageData data = new ImageData(contents);
				// Only provide an Image for 16x16-or-less data
				if (data.height <= 16 && data.width <= 16) {
					return new Image(image.getDevice(), data);
				}
			}
		} catch (CoreException e) {
		} catch (IOException e) {
		}
		finally {
			if (contents != null) {
				try {
					contents.close();
				} catch (IOException e) {}
			}
		}
		return null;
	}

	@Override
	public String decorateText(String text, Object element) {
		return null;
	}

}
