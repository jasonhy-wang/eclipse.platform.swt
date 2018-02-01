/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.graphics;


import java.io.*;

import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.cairo.*;
import org.eclipse.swt.internal.gtk.*;

/**
 * Instances of this class are graphics which have been prepared
 * for display on a specific device. That is, they are ready
 * to paint using methods such as <code>GC.drawImage()</code>
 * and display on widgets with, for example, <code>Button.setImage()</code>.
 * <p>
 * If loaded from a file format that supports it, an
 * <code>Image</code> may have transparency, meaning that certain
 * pixels are specified as being transparent when drawn. Examples
 * of file formats that support transparency are GIF and PNG.
 * </p><p>
 * There are two primary ways to use <code>Images</code>.
 * The first is to load a graphic file from disk and create an
 * <code>Image</code> from it. This is done using an <code>Image</code>
 * constructor, for example:
 * <pre>
 *    Image i = new Image(device, "C:\\graphic.bmp");
 * </pre>
 * A graphic file may contain a color table specifying which
 * colors the image was intended to possess. In the above example,
 * these colors will be mapped to the closest available color in
 * SWT. It is possible to get more control over the mapping of
 * colors as the image is being created, using code of the form:
 * <pre>
 *    ImageData data = new ImageData("C:\\graphic.bmp");
 *    RGB[] rgbs = data.getRGBs();
 *    // At this point, rgbs contains specifications of all
 *    // the colors contained within this image. You may
 *    // allocate as many of these colors as you wish by
 *    // using the Color constructor Color(RGB), then
 *    // create the image:
 *    Image i = new Image(device, data);
 * </pre>
 * <p>
 * Applications which require even greater control over the image
 * loading process should use the support provided in class
 * <code>ImageLoader</code>.
 * </p><p>
 * Application code must explicitly invoke the <code>Image.dispose()</code>
 * method to release the operating system resources managed by each instance
 * when those instances are no longer required.
 * </p>
 *
 * @see Color
 * @see ImageData
 * @see ImageLoader
 * @see <a href="http://www.eclipse.org/swt/snippets/#image">Image snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Examples: GraphicsExample, ImageAnalyzer</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public final class Image extends Resource implements Drawable {

	/**
	 * specifies whether the receiver is a bitmap or an icon
	 * (one of <code>SWT.BITMAP</code>, <code>SWT.ICON</code>)
	 * <p>
	 * <b>IMPORTANT:</b> This field is <em>not</em> part of the SWT
	 * public API. It is marked public only so that it can be shared
	 * within the packages provided by SWT. It is not available on all
	 * platforms and should never be accessed from application code.
	 * </p>
	 *
	 * @noreference This field is not intended to be referenced by clients.
	 */
	public int type;

	/**
	 * The handle to the OS pixmap resource.
	 * (Warning: This field is platform dependent)
	 * <p>
	 * <b>IMPORTANT:</b> This field is <em>not</em> part of the SWT
	 * public API. It is marked public only so that it can be shared
	 * within the packages provided by SWT. It is not available on all
	 * platforms and should never be accessed from application code.
	 * </p>
	 *
	 * @noreference This field is not intended to be referenced by clients.
	 */
	public long /*int*/ pixmap;

	/**
	 * The handle to the OS mask resource.
	 * (Warning: This field is platform dependent)
	 * <p>
	 * <b>IMPORTANT:</b> This field is <em>not</em> part of the SWT
	 * public API. It is marked public only so that it can be shared
	 * within the packages provided by SWT. It is not available on all
	 * platforms and should never be accessed from application code.
	 * </p>
	 *
	 * @noreference This field is not intended to be referenced by clients.
	 */
	public long /*int*/ mask;

	/**
	 * The handle to the OS cairo surface resource.
	 * (Warning: This field is platform dependent)
	 * <p>
	 * <b>IMPORTANT:</b> This field is <em>not</em> part of the SWT
	 * public API. It is marked public only so that it can be shared
	 * within the packages provided by SWT. It is not available on all
	 * platforms and should never be accessed from application code.
	 * </p>
	 *
	 * @noreference This field is not intended to be referenced by clients.
	 */
	public long /*int*/ surface;

	/**
	 * specifies the transparent pixel
	 */
	int transparentPixel = -1;

	/**
	 * The GC the image is currently selected in.
	 */
	GC memGC;

	/**
	 * The alpha data of the image.
	 */
	byte[] alphaData;

	/**
	 * The global alpha value to be used for every pixel.
	 */
	int alpha = -1;

	/**
	 * The width of the image.
	 */
	int width = -1;

	/**
	 * The height of the image.
	 */
	int height = -1;

	/**
	 * Specifies the default scanline padding.
	 */
	static final int DEFAULT_SCANLINE_PAD = 4;

	/**
	 * ImageFileNameProvider to provide file names at various Zoom levels
	 */
	private ImageFileNameProvider imageFileNameProvider;

	/**
	 * ImageDataProvider to provide ImageData at various Zoom levels
	 */
	private ImageDataProvider imageDataProvider;

	/**
	 * Attribute to cache current device zoom level
	 */
	private int currentDeviceZoom = 100;

Image(Device device) {
	super(device);
	currentDeviceZoom = DPIUtil.getDeviceZoom();
}

/**
 * Constructs an empty instance of this class with the
 * specified width and height. The result may be drawn upon
 * by creating a GC and using any of its drawing operations,
 * as shown in the following example:
 * <pre>
 *    Image i = new Image(device, width, height);
 *    GC gc = new GC(i);
 *    gc.drawRectangle(0, 0, 50, 50);
 *    gc.dispose();
 * </pre>
 * <p>
 * Note: Some platforms may have a limitation on the size
 * of image that can be created (size depends on width, height,
 * and depth). For example, Windows 95, 98, and ME do not allow
 * images larger than 16M.
 * </p>
 * <p>
 * You must dispose the image when it is no longer required.
 * </p>
 *
 * @param device the device on which to create the image
 * @param width the width of the new image
 * @param height the height of the new image
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
 *    <li>ERROR_INVALID_ARGUMENT - if either the width or height is negative or zero</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
 * </ul>
 *
 * @see #dispose()
 */
public Image(Device device, int width, int height) {
	super(device);
	Point size = DPIUtil.autoScaleUp(new Point(width, height));
	currentDeviceZoom = DPIUtil.getDeviceZoom();
	init(size.x, size.y);
	init();
}

/**
 * Constructs a new instance of this class based on the
 * provided image, with an appearance that varies depending
 * on the value of the flag. The possible flag values are:
 * <dl>
 * <dt><b>{@link SWT#IMAGE_COPY}</b></dt>
 * <dd>the result is an identical copy of srcImage</dd>
 * <dt><b>{@link SWT#IMAGE_DISABLE}</b></dt>
 * <dd>the result is a copy of srcImage which has a <em>disabled</em> look</dd>
 * <dt><b>{@link SWT#IMAGE_GRAY}</b></dt>
 * <dd>the result is a copy of srcImage which has a <em>gray scale</em> look</dd>
 * </dl>
 * <p>
 * You must dispose the image when it is no longer required.
 * </p>
 *
 * @param device the device on which to create the image
 * @param srcImage the image to use as the source
 * @param flag the style, either <code>IMAGE_COPY</code>, <code>IMAGE_DISABLE</code> or <code>IMAGE_GRAY</code>
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
 *    <li>ERROR_NULL_ARGUMENT - if srcImage is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the flag is not one of <code>IMAGE_COPY</code>, <code>IMAGE_DISABLE</code> or <code>IMAGE_GRAY</code></li>
 *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or an icon, or is otherwise in an invalid state</li>
 *    <li>ERROR_UNSUPPORTED_DEPTH - if the depth of the image is not supported</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
 * </ul>
 *
 * @see #dispose()
 */
public Image(Device device, Image srcImage, int flag) {
	super(device);
	if (srcImage == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (srcImage.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	switch (flag) {
		case SWT.IMAGE_COPY:
		case SWT.IMAGE_DISABLE:
		case SWT.IMAGE_GRAY:
			break;
		default:
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	}
	device = this.device;
	this.type = srcImage.type;
	this.imageDataProvider = srcImage.imageDataProvider;
	this.imageFileNameProvider = srcImage.imageFileNameProvider;
	this.currentDeviceZoom = srcImage.currentDeviceZoom;

	if (flag != SWT.IMAGE_DISABLE) transparentPixel = srcImage.transparentPixel;
	alpha = srcImage.alpha;
	if (srcImage.alphaData != null) {
		alphaData = new byte[srcImage.alphaData.length];
		System.arraycopy(srcImage.alphaData, 0, alphaData, 0, alphaData.length);
	}

	long /*int*/ imageSurface = srcImage.surface;
	int width = this.width = srcImage.width;
	int height = this.height = srcImage.height;
	int format = Cairo.cairo_surface_get_content(imageSurface) == Cairo.CAIRO_CONTENT_COLOR ? Cairo.CAIRO_FORMAT_RGB24 : Cairo.CAIRO_FORMAT_ARGB32;
	boolean hasAlpha = format == Cairo.CAIRO_FORMAT_ARGB32;
	surface = Cairo.cairo_image_surface_create(format, width, height);
	if (surface == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	long /*int*/ cairo = Cairo.cairo_create(surface);
	if (cairo == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	Cairo.cairo_set_operator(cairo, Cairo.CAIRO_OPERATOR_SOURCE);
	Cairo.cairo_set_source_surface (cairo, imageSurface, 0, 0);
	Cairo.cairo_paint (cairo);
	Cairo.cairo_destroy(cairo);
	if (flag != SWT.IMAGE_COPY) {
		int stride = Cairo.cairo_image_surface_get_stride(surface);
		long /*int*/ data = Cairo.cairo_image_surface_get_data(surface);
		int oa, or, og, ob;
		if (OS.BIG_ENDIAN) {
			oa = 0; or = 1; og = 2; ob = 3;
		} else {
			oa = 3; or = 2; og = 1; ob = 0;
		}
		switch (flag) {
			case SWT.IMAGE_DISABLE: {
				Color zeroColor = device.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
				RGB zeroRGB = zeroColor.getRGB();
				int zeroRed = zeroRGB.red;
				int zeroGreen = zeroRGB.green;
				int zeroBlue = zeroRGB.blue;
				Color oneColor = device.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
				RGB oneRGB = oneColor.getRGB();
				int oneRed = oneRGB.red;
				int oneGreen = oneRGB.green;
				int oneBlue = oneRGB.blue;
				byte[] line = new byte[stride];
				for (int y=0; y<height; y++) {
					C.memmove(line, data + (y * stride), stride);
					for (int x=0, offset=0; x<width; x++, offset += 4) {
						int a = line[offset + oa] & 0xFF;
						int r = line[offset + or] & 0xFF;
						int g = line[offset + og] & 0xFF;
						int b = line[offset + ob] & 0xFF;
						if (hasAlpha && a != 0) {
							r = ((r * 0xFF) + a / 2) / a;
							g = ((g * 0xFF) + a / 2) / a;
							b = ((b * 0xFF) + a / 2) / a;
						}
						int intensity = r * r + g * g + b * b;
						if (intensity < 98304) {
							r = zeroRed;
							g = zeroGreen;
							b = zeroBlue;
						} else {
							r = oneRed;
							g = oneGreen;
							b = oneBlue;
						}
						if (hasAlpha) {
							/* pre-multiplied alpha */
							r = (r * a) + 128;
							r = (r + (r >> 8)) >> 8;
							g = (g * a) + 128;
							g = (g + (g >> 8)) >> 8;
							b = (b * a) + 128;
							b = (b + (b >> 8)) >> 8;
						}
						line[offset + or] = (byte)r;
						line[offset + og] = (byte)g;
						line[offset + ob] = (byte)b;
					}
					C.memmove(data + (y * stride), line, stride);
				}
				break;
			}
			case SWT.IMAGE_GRAY: {
				byte[] line = new byte[stride];
				for (int y=0; y<height; y++) {
					C.memmove(line, data + (y * stride), stride);
					for (int x=0, offset = 0; x<width; x++, offset += 4) {
						int a = line[offset + oa] & 0xFF;
						int r = line[offset + or] & 0xFF;
						int g = line[offset + og] & 0xFF;
						int b = line[offset + ob] & 0xFF;
						if (hasAlpha && a != 0) {
							r = ((r * 0xFF) + a / 2) / a;
							g = ((g * 0xFF) + a / 2) / a;
							b = ((b * 0xFF) + a / 2) / a;
						}
						int intensity = (r+r+g+g+g+g+g+b) >> 3;
						if (hasAlpha) {
							/* pre-multiplied alpha */
							intensity = (intensity * a) + 128;
							intensity = (intensity + (intensity >> 8)) >> 8;
						}
						line[offset+or] = line[offset+og] = line[offset+ob] = (byte)intensity;
					}
					C.memmove(data + (y * stride), line, stride);
				}
				break;
			}
		}
	}
	init();
}

/**
 * Constructs an empty instance of this class with the
 * width and height of the specified rectangle. The result
 * may be drawn upon by creating a GC and using any of its
 * drawing operations, as shown in the following example:
 * <pre>
 *    Image i = new Image(device, boundsRectangle);
 *    GC gc = new GC(i);
 *    gc.drawRectangle(0, 0, 50, 50);
 *    gc.dispose();
 * </pre>
 * <p>
 * Note: Some platforms may have a limitation on the size
 * of image that can be created (size depends on width, height,
 * and depth). For example, Windows 95, 98, and ME do not allow
 * images larger than 16M.
 * </p>
 * <p>
 * You must dispose the image when it is no longer required.
 * </p>
 *
 * @param device the device on which to create the image
 * @param bounds a rectangle specifying the image's width and height (must not be null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
 *    <li>ERROR_NULL_ARGUMENT - if the bounds rectangle is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if either the rectangle's width or height is negative</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
 * </ul>
 *
 * @see #dispose()
 */
public Image(Device device, Rectangle bounds) {
	super(device);
	if (bounds == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	currentDeviceZoom = DPIUtil.getDeviceZoom();
	Rectangle bounds1 = DPIUtil.autoScaleUp (bounds);
	init(bounds1.width, bounds1.height);
	init();
}

/**
 * Constructs an instance of this class from the given
 * <code>ImageData</code>.
 * <p>
 * You must dispose the image when it is no longer required.
 * </p>
 *
 * @param device the device on which to create the image
 * @param data the image data to create the image from (must not be null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
 *    <li>ERROR_NULL_ARGUMENT - if the image data is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_UNSUPPORTED_DEPTH - if the depth of the ImageData is not supported</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
 * </ul>
 *
 * @see #dispose()
 */
public Image(Device device, ImageData data) {
	super(device);
	if (data == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	currentDeviceZoom = DPIUtil.getDeviceZoom();
	data = DPIUtil.autoScaleUp (device, data);
	init(data);
	init();
}

/**
 * Constructs an instance of this class, whose type is
 * <code>SWT.ICON</code>, from the two given <code>ImageData</code>
 * objects. The two images must be the same size. Pixel transparency
 * in either image will be ignored.
 * <p>
 * The mask image should contain white wherever the icon is to be visible,
 * and black wherever the icon is to be transparent. In addition,
 * the source image should contain black wherever the icon is to be
 * transparent.
 * </p>
 * <p>
 * You must dispose the image when it is no longer required.
 * </p>
 *
 * @param device the device on which to create the icon
 * @param source the color data for the icon
 * @param mask the mask data for the icon
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
 *    <li>ERROR_NULL_ARGUMENT - if either the source or mask is null </li>
 *    <li>ERROR_INVALID_ARGUMENT - if source and mask are different sizes</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
 * </ul>
 *
 * @see #dispose()
 */
public Image(Device device, ImageData source, ImageData mask) {
	super(device);
	if (source == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (mask == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (source.width != mask.width || source.height != mask.height) {
		SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	}
	currentDeviceZoom = DPIUtil.getDeviceZoom();
	source = DPIUtil.autoScaleUp (device, source);
	mask = DPIUtil.autoScaleUp (device, mask);
	mask = ImageData.convertMask (mask);
	ImageData image = new ImageData(source.width, source.height, source.depth, source.palette, source.scanlinePad, source.data);
	image.maskPad = mask.scanlinePad;
	image.maskData = mask.data;
	init(image);
	init();
}

/**
 * Constructs an instance of this class by loading its representation
 * from the specified input stream. Throws an error if an error
 * occurs while loading the image, or if the result is an image
 * of an unsupported type.  Application code is still responsible
 * for closing the input stream.
 * <p>
 * This constructor is provided for convenience when loading a single
 * image only. If the stream contains multiple images, only the first
 * one will be loaded. To load multiple images, use
 * <code>ImageLoader.load()</code>.
 * </p><p>
 * This constructor may be used to load a resource as follows:
 * </p>
 * <pre>
 *     static Image loadImage (Display display, Class clazz, String string) {
 *          InputStream stream = clazz.getResourceAsStream (string);
 *          if (stream == null) return null;
 *          Image image = null;
 *          try {
 *               image = new Image (display, stream);
 *          } catch (SWTException ex) {
 *          } finally {
 *               try {
 *                    stream.close ();
 *               } catch (IOException ex) {}
 *          }
 *          return image;
 *     }
 * </pre>
 * <p>
 * You must dispose the image when it is no longer required.
 * </p>
 *
 * @param device the device on which to create the image
 * @param stream the input stream to load the image from
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
 *    <li>ERROR_NULL_ARGUMENT - if the stream is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_IO - if an IO error occurs while reading from the stream</li>
 *    <li>ERROR_INVALID_IMAGE - if the image stream contains invalid data </li>
 *    <li>ERROR_UNSUPPORTED_DEPTH - if the image stream describes an image with an unsupported depth</li>
 *    <li>ERROR_UNSUPPORTED_FORMAT - if the image stream contains an unrecognized format</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
 * </ul>
 *
 * @see #dispose()
 */
public Image(Device device, InputStream stream) {
	super(device);
	ImageData data = new ImageData(stream);
	currentDeviceZoom = DPIUtil.getDeviceZoom();
	data = DPIUtil.autoScaleUp (device, data);
	init(data);
	init();
}

/**
 * Constructs an instance of this class by loading its representation
 * from the file with the specified name. Throws an error if an error
 * occurs while loading the image, or if the result is an image
 * of an unsupported type.
 * <p>
 * This constructor is provided for convenience when loading
 * a single image only. If the specified file contains
 * multiple images, only the first one will be used.
 * <p>
 * You must dispose the image when it is no longer required.
 * </p>
 *
 * @param device the device on which to create the image
 * @param filename the name of the file to load the image from
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
 *    <li>ERROR_NULL_ARGUMENT - if the file name is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_IO - if an IO error occurs while reading from the file</li>
 *    <li>ERROR_INVALID_IMAGE - if the image file contains invalid data </li>
 *    <li>ERROR_UNSUPPORTED_DEPTH - if the image file describes an image with an unsupported depth</li>
 *    <li>ERROR_UNSUPPORTED_FORMAT - if the image file contains an unrecognized format</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
 * </ul>
 *
 * @see #dispose()
 */
public Image(Device device, String filename) {
	super(device);
	if (filename == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);

	ImageData data = new ImageData(filename);
	currentDeviceZoom = DPIUtil.getDeviceZoom();
	data = DPIUtil.autoScaleUp (device, data);
	init(data);
	init();
}

/**
 * Constructs an instance of this class by loading its representation
 * from the file retrieved from the ImageFileNameProvider. Throws an
 * error if an error occurs while loading the image, or if the result
 * is an image of an unsupported type.
 * <p>
 * This constructor is provided for convenience for loading image as
 * per DPI level.
 *
 * @param device the device on which to create the image
 * @param imageFileNameProvider the ImageFileNameProvider object that is
 * to be used to get the file name
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
 *    <li>ERROR_NULL_ARGUMENT - if the ImageFileNameProvider is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the fileName provided by ImageFileNameProvider is null at 100% zoom</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_IO - if an IO error occurs while reading from the file</li>
 *    <li>ERROR_INVALID_IMAGE - if the image file contains invalid data </li>
 *    <li>ERROR_UNSUPPORTED_DEPTH - if the image file describes an image with an unsupported depth</li>
 *    <li>ERROR_UNSUPPORTED_FORMAT - if the image file contains an unrecognized format</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
 * </ul>
 * @since 3.104
 */
public Image(Device device, ImageFileNameProvider imageFileNameProvider) {
	super(device);
	this.imageFileNameProvider = imageFileNameProvider;
	currentDeviceZoom = DPIUtil.getDeviceZoom();
	boolean[] found = new boolean[1];
	String filename = DPIUtil.validateAndGetImagePathAtZoom (imageFileNameProvider, currentDeviceZoom, found);
	if (found[0]) {
		initNative (filename);
		if (this.pixmap == 0 && this.surface == 0) {
			ImageData data = new ImageData(filename);
			init(data);
		}
	} else {
		ImageData imageData = new ImageData (filename);
		ImageData resizedData = DPIUtil.autoScaleUp (device, imageData);
		init(resizedData);
	}
	init ();
}

/**
 * Constructs an instance of this class by loading its representation
 * from the ImageData retrieved from the ImageDataProvider. Throws an
 * error if an error occurs while loading the image, or if the result
 * is an image of an unsupported type.
 * <p>
 * This constructor is provided for convenience for loading image as
 * per DPI level.
 *
 * @param device the device on which to create the image
 * @param imageDataProvider the ImageDataProvider object that is
 * to be used to get the ImageData
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
 *    <li>ERROR_NULL_ARGUMENT - if the ImageDataProvider is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the ImageData provided by ImageDataProvider is null at 100% zoom</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_IO - if an IO error occurs while reading from the file</li>
 *    <li>ERROR_INVALID_IMAGE - if the image file contains invalid data </li>
 *    <li>ERROR_UNSUPPORTED_DEPTH - if the image file describes an image with an unsupported depth</li>
 *    <li>ERROR_UNSUPPORTED_FORMAT - if the image file contains an unrecognized format</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
 * </ul>
 * @since 3.104
 */
public Image(Device device, ImageDataProvider imageDataProvider) {
	super(device);
	this.imageDataProvider = imageDataProvider;
	currentDeviceZoom = DPIUtil.getDeviceZoom();
	boolean[] found = new boolean[1];
	ImageData data =  DPIUtil.validateAndGetImageDataAtZoom(imageDataProvider, currentDeviceZoom, found);
	if (found[0]) {
		init (data);
	} else {
		ImageData resizedData = DPIUtil.autoScaleUp (device, data);
		init (resizedData);
	}
	init ();
}

/**
 * Refresh the Image based on the zoom level, if required.
 *
 * @return true if image is refreshed
 */
boolean refreshImageForZoom () {
	boolean refreshed = false;
	int deviceZoom = DPIUtil.getDeviceZoom();
	if (imageFileNameProvider != null) {
		int deviceZoomLevel = deviceZoom;
		if (deviceZoomLevel != currentDeviceZoom) {
			boolean[] found = new boolean[1];
			String filename = DPIUtil.validateAndGetImagePathAtZoom (imageFileNameProvider, deviceZoomLevel, found);
			/* Avoid re-creating the fall-back image, when current zoom is already 100% */
			if (found[0] || currentDeviceZoom != 100) {
				/* Release current native resources */
				destroy ();
				initNative(filename);
				if (this.pixmap == 0 && this.surface == 0) {
					ImageData data = new ImageData(filename);
					init(data);
				}
				init ();
				refreshed = true;
			}
			if (!found[0]) {
				/* Release current native resources */
				destroy ();
				ImageData imageData = new ImageData (filename);
				ImageData resizedData = DPIUtil.autoScaleUp (device, imageData);
				init(resizedData);
				init ();
				refreshed = true;
			}
			currentDeviceZoom = deviceZoomLevel;
		}
	} else if (imageDataProvider != null) {
		int deviceZoomLevel = deviceZoom;
		if (deviceZoomLevel != currentDeviceZoom) {
			boolean[] found = new boolean[1];
			ImageData data = DPIUtil.validateAndGetImageDataAtZoom (imageDataProvider, deviceZoomLevel, found);
			/* Avoid re-creating the fall-back image, when current zoom is already 100% */
			if (found[0] || currentDeviceZoom != 100) {
				/* Release current native resources */
				destroy ();
				init(data);
				init();
				refreshed = true;
			}
			if (!found[0]) {
				/* Release current native resources */
				destroy ();
				ImageData resizedData = DPIUtil.autoScaleImageData(device, data, deviceZoomLevel, 100);
				init(resizedData);
				init();
				refreshed = true;
			}
			currentDeviceZoom = deviceZoomLevel;
		}
	} else {
		int deviceZoomLevel = deviceZoom;
		if (deviceZoomLevel != currentDeviceZoom) {
			ImageData data = getImageDataAtCurrentZoom();
			destroy ();
			ImageData resizedData = DPIUtil.autoScaleImageData(device, data, deviceZoomLevel, currentDeviceZoom);
			init(resizedData);
			init();
			refreshed = true;
			currentDeviceZoom = deviceZoomLevel;
		}
	}
	return refreshed;
}

void initNative(String filename) {
	try {
		int length = filename.length ();
		char [] chars = new char [length];
		filename.getChars (0, length, chars, 0);
		byte [] buffer = Converter.wcsToMbcs(chars, true);
		long /*int*/ pixbuf = GDK.gdk_pixbuf_new_from_file(buffer, null);
		if (pixbuf != 0) {
			try {
				createFromPixbuf (SWT.BITMAP, pixbuf);
			} finally {
				if (pixbuf != 0) OS.g_object_unref (pixbuf);
			}
		}
	} catch (SWTException e) {}
}

void createFromPixbuf(int type, long /*int*/ pixbuf) {
	this.type = type;
	boolean hasAlpha = GDK.gdk_pixbuf_get_has_alpha(pixbuf);
	int width = this.width = GDK.gdk_pixbuf_get_width(pixbuf);
	int height = this.height = GDK.gdk_pixbuf_get_height(pixbuf);
	int stride = GDK.gdk_pixbuf_get_rowstride(pixbuf);
	long /*int*/ pixels = GDK.gdk_pixbuf_get_pixels(pixbuf);
	int format = hasAlpha ? Cairo.CAIRO_FORMAT_ARGB32 : Cairo.CAIRO_FORMAT_RGB24;
	surface = Cairo.cairo_image_surface_create(format, width, height);
	if (surface == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	long /*int*/ data = Cairo.cairo_image_surface_get_data(surface);
	int cairoStride = Cairo.cairo_image_surface_get_stride(surface);
	int oa = 0, or = 0, og = 0, ob = 0;
	if (OS.BIG_ENDIAN) {
		oa = 0; or = 1; og = 2; ob = 3;
	} else {
		oa = 3; or = 2; og = 1; ob = 0;
	}
	byte[] line = new byte[stride];
	if (hasAlpha) {
		alphaData = new byte[width * height];
		for (int y = 0, alphaOffset = 0; y < height; y++) {
			C.memmove(line, pixels + (y * stride), stride);
			for (int x = 0, offset = 0; x < width; x++, offset += 4) {
				int a = line[offset + 3] & 0xFF;
				int r = ((line[offset + 0] & 0xFF) * a) + 128;
				r = (r + (r >> 8)) >> 8;
				int g = ((line[offset + 1] & 0xFF) * a) + 128;
				g = (g + (g >> 8)) >> 8;
				int b = ((line[offset + 2] & 0xFF) * a) + 128;
				b = (b + (b >> 8)) >> 8;
				line[offset + oa] = (byte)a;
				line[offset + or] = (byte)r;
				line[offset + og] = (byte)g;
				line[offset + ob] = (byte)b;
				alphaData[alphaOffset++] = (byte)a;
			}
			C.memmove(data + (y * stride), line, stride);
		}
	} else {
		byte[] cairoLine = new byte[cairoStride];
		for (int y = 0; y < height; y++) {
			C.memmove(line, pixels + (y * stride), stride);
			for (int x = 0, offset = 0, cairoOffset = 0; x < width; x++, offset += 3, cairoOffset += 4) {
				int r = line[offset + 0] & 0xFF;
				int g = line[offset + 1] & 0xFF;
				int b = line[offset + 2] & 0xFF;
				cairoLine[cairoOffset + or] = (byte)r;
				cairoLine[cairoOffset + og] = (byte)g;
				cairoLine[cairoOffset + ob] = (byte)b;
			}
			C.memmove(data + (y * cairoStride), cairoLine, cairoStride);
		}
	}
	Cairo.cairo_surface_mark_dirty(surface);
}

/**
 * Create the receiver's mask if necessary.
 */
void createMask() {
	int width = this.width;
	int height = this.height;
	int stride = Cairo.cairo_image_surface_get_stride(surface);
	long /*int*/ surfaceData = Cairo.cairo_image_surface_get_data(surface);
	int oa, or, og, ob, tr, tg, tb;
	if (OS.BIG_ENDIAN) {
		oa = 0; or = 1; og = 2; ob = 3;
		tr = (transparentPixel >> 24) & 0xFF;
		tg = (transparentPixel >> 16) & 0xFF;
		tb = (transparentPixel >> 8) & 0xFF;
	} else {
		oa = 3; or = 2; og = 1; ob = 0;
		tr = (transparentPixel >> 16) & 0xFF;
		tg = (transparentPixel >> 8) & 0xFF;
		tb = (transparentPixel >> 0) & 0xFF;
	}
	byte[] srcData = new byte[stride * height];
	C.memmove(srcData, surfaceData, srcData.length);
	int offset = 0;
	for (int y = 0; y < height; y++) {
		for (int x = 0; x < width; x++, offset += 4) {
			int a = srcData[offset + oa] & 0xFF;
			int r = srcData[offset + or] & 0xFF;
			int g = srcData[offset + og] & 0xFF;
			int b = srcData[offset + ob] & 0xFF;
			if (r == tr && g == tg && b == tb) {
				a = r = g = b = 0;
			} else {
				a = 0xff;
			}
			srcData[offset + oa] = (byte)a;
			srcData[offset + or] = (byte)r;
			srcData[offset + og] = (byte)g;
			srcData[offset + ob] = (byte)b;
		}
	}
	C.memmove(surfaceData, srcData, srcData.length);
}

void createSurface() {
	if (surface != 0) return;
	/* There is no pixmaps in GTK 3. */
	if (GTK.GTK3) return;
	/* Generate the mask if necessary. */
	if (transparentPixel != -1) createMask();
	int[] w = new int[1], h = new int[1];
	GDK.gdk_pixmap_get_size(pixmap, w, h);
	int width = w[0], height = h[0];
	this.width = width;
	this.height = height;
	if (mask != 0 || alpha != -1 || alphaData != null) {
	 	long /*int*/ pixbuf = GDK.gdk_pixbuf_new(GDK.GDK_COLORSPACE_RGB, true, 8, width, height);
		if (pixbuf == 0) SWT.error(SWT.ERROR_NO_HANDLES);
		long /*int*/ colormap = GDK.gdk_colormap_get_system();
		GDK.gdk_pixbuf_get_from_drawable(pixbuf, pixmap, colormap, 0, 0, 0, 0, width, height);
		int stride = GDK.gdk_pixbuf_get_rowstride(pixbuf);
		long /*int*/ pixels = GDK.gdk_pixbuf_get_pixels(pixbuf);
		byte[] line = new byte[stride];
		int oa, or, og, ob;
		if (OS.BIG_ENDIAN) {
			oa = 0; or = 1; og = 2; ob = 3;
		} else {
			oa = 3; or = 2; og = 1; ob = 0;
		}
		if (mask != 0 && GDK.gdk_drawable_get_depth(mask) == 1) {
			long /*int*/ maskPixbuf = GDK.gdk_pixbuf_new(GDK.GDK_COLORSPACE_RGB, false, 8, width, height);
			if (maskPixbuf == 0) SWT.error(SWT.ERROR_NO_HANDLES);
			GDK.gdk_pixbuf_get_from_drawable(maskPixbuf, mask, 0, 0, 0, 0, 0, width, height);
			int maskStride = GDK.gdk_pixbuf_get_rowstride(maskPixbuf);
			long /*int*/ maskPixels = GDK.gdk_pixbuf_get_pixels(maskPixbuf);
			byte[] maskLine = new byte[maskStride];
			long /*int*/ offset = pixels, maskOffset = maskPixels;
			for (int y=0; y<height; y++) {
				C.memmove(line, offset, stride);
				C.memmove(maskLine, maskOffset, maskStride);
				for (int x=0, offset1=0; x<width; x++, offset1 += 4) {
					if (maskLine[x * 3] == 0) {
						line[offset1 + 0] = line[offset1 + 1] = line[offset1 + 2] = line[offset1 + 3] = 0;
					} else {
						byte r = line[offset1 + 0];
						byte g = line[offset1 + 1];
						byte b = line[offset1 + 2];
						line[offset1 + oa] = (byte)0xFF;
						line[offset1 + or] = r;
						line[offset1 + og] = g;
						line[offset1 + ob] = b;
					}
				}
				C.memmove(offset, line, stride);
				offset += stride;
				maskOffset += maskStride;
			}
			OS.g_object_unref(maskPixbuf);
		} else if (alpha != -1) {
			long /*int*/ offset = pixels;
			for (int y=0; y<height; y++) {
				C.memmove(line, offset, stride);
				for (int x=0, offset1=0; x<width; x++, offset1 += 4) {
					/* pre-multiplied alpha */
					int r = ((line[offset1 + 0] & 0xFF) * alpha) + 128;
					r = (r + (r >> 8)) >> 8;
					int g = ((line[offset1 + 1] & 0xFF) * alpha) + 128;
					g = (g + (g >> 8)) >> 8;
					int b = ((line[offset1 + 2] & 0xFF) * alpha) + 128;
					b = (b + (b >> 8)) >> 8;
					line[offset1 + oa] = (byte)alpha;
					line[offset1 + or] = (byte)r;
					line[offset1 + og] = (byte)g;
					line[offset1 + ob] = (byte)b;
				}
				C.memmove(offset, line, stride);
				offset += stride;
			}
		} else if (alphaData != null) {
			long /*int*/ offset = pixels;
			for (int y = 0; y < h [0]; y++) {
				C.memmove (line, offset, stride);
				for (int x=0, offset1=0; x<width; x++, offset1 += 4) {
					int alpha = alphaData [y*w [0]+x] & 0xFF;
					/* pre-multiplied alpha */
					int r = ((line[offset1 + 0] & 0xFF) * alpha) + 128;
					r = (r + (r >> 8)) >> 8;
					int g = ((line[offset1 + 1] & 0xFF) * alpha) + 128;
					g = (g + (g >> 8)) >> 8;
					int b = ((line[offset1 + 2] & 0xFF) * alpha) + 128;
					b = (b + (b >> 8)) >> 8;
					line[offset1 + oa] = (byte)alpha;
					line[offset1 + or] = (byte)r;
					line[offset1 + og] = (byte)g;
					line[offset1 + ob] = (byte)b;
				}
				C.memmove (offset, line, stride);
				offset += stride;
			}
		} else {
			long /*int*/ offset = pixels;
			for (int y = 0; y < h [0]; y++) {
				C.memmove (line, offset, stride);
				for (int x=0, offset1=0; x<width; x++, offset1 += 4) {
					byte r = line[offset1 + 0];
					byte g = line[offset1 + 1];
					byte b = line[offset1 + 2];
					line[offset1 + oa] = (byte)0xFF;
					line[offset1 + or] = r;
					line[offset1 + og] = g;
					line[offset1 + ob] = b;
				}
				C.memmove (offset, line, stride);
				offset += stride;
			}
		}
		surface = Cairo.cairo_image_surface_create(Cairo.CAIRO_FORMAT_ARGB32, width, height);
		long /*int*/ data = Cairo.cairo_image_surface_get_data(surface);
		C.memmove(data, pixels, stride * height);
		Cairo.cairo_surface_mark_dirty(surface);
		OS.g_object_unref(pixbuf);
	} else {
		long /*int*/ xDisplay = GDK.gdk_x11_display_get_xdisplay(GDK.gdk_display_get_default());
		long /*int*/ xDrawable = GDK.GDK_PIXMAP_XID(pixmap);
		long /*int*/ xVisual = GDK.gdk_x11_visual_get_xvisual(GDK.gdk_visual_get_system());
		surface = Cairo.cairo_xlib_surface_create(xDisplay, xDrawable, xVisual, width, height);
	}
	/* Destroy the image mask if the there is a GC created on the image */
	if (transparentPixel != -1 && memGC != null) destroyMask();
}

/**
 * Destroy the receiver's mask if it exists.
 */
void destroyMask() {
	if (mask == 0) return;
	OS.g_object_unref(mask);
	mask = 0;
}

@Override
void destroy() {
	if (memGC != null) memGC.dispose();
	if (pixmap != 0) OS.g_object_unref(pixmap);
	if (mask != 0) OS.g_object_unref(mask);
	if (surface != 0) Cairo.cairo_surface_destroy(surface);
	surface = pixmap = mask = 0;
	memGC = null;
}

/**
 * Compares the argument to the receiver, and returns true
 * if they represent the <em>same</em> object using a class
 * specific comparison.
 *
 * @param object the object to compare with this object
 * @return <code>true</code> if the object is the same as this object and <code>false</code> otherwise
 *
 * @see #hashCode
 */
@Override
public boolean equals (Object object) {
	if (object == this) return true;
	if (!(object instanceof Image)) return false;
	Image image = (Image)object;
	if (device != image.device || transparentPixel != image.transparentPixel) return false;
	if (imageDataProvider != null && image.imageDataProvider != null) {
		return imageDataProvider.equals (image.imageDataProvider);
	} else if (imageFileNameProvider != null && image.imageFileNameProvider != null) {
		return imageFileNameProvider.equals (image.imageFileNameProvider);
	} else {
		return surface == image.surface;
	}
}

/**
 * Returns the color to which to map the transparent pixel, or null if
 * the receiver has no transparent pixel.
 * <p>
 * There are certain uses of Images that do not support transparency
 * (for example, setting an image into a button or label). In these cases,
 * it may be desired to simulate transparency by using the background
 * color of the widget to paint the transparent pixels of the image.
 * Use this method to check which color will be used in these cases
 * in place of transparency. This value may be set with setBackground().
 * <p>
 *
 * @return the background color of the image, or null if there is no transparency in the image
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public Color getBackground() {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (transparentPixel == -1) return null;
	//NOT DONE
	return null;
}
/**
 * Returns the bounds of the receiver. The rectangle will always
 * have x and y values of 0, and the width and height of the
 * image.
 *
 * @return a rectangle specifying the image's bounds in points.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or an icon</li>
 * </ul>
 */
public Rectangle getBounds() {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	return DPIUtil.autoScaleBounds(getBoundsInPixels(), 100, currentDeviceZoom);
}

/**
 * Returns the bounds of the receiver. The rectangle will always
 * have x and y values of 0, and the width and height of the
 * image in pixels.
 *
 * @return a rectangle specifying the image's bounds in pixels.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or an icon</li>
 * </ul>
 * @since 3.105
 * @deprecated This API doesn't serve the purpose in an environment having
 *             multiple monitors with different DPIs, hence deprecated.
 */
@Deprecated
public Rectangle getBoundsInPixels() {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (width != -1 && height != -1) {
		return new Rectangle(0, 0, width, height);
	}
	int[] w = new int[1]; int[] h = new int[1];
	GDK.gdk_pixmap_get_size(pixmap, w, h);
	return new Rectangle(0, 0, width = w[0], height = h[0]);
}

/**
 * Returns an <code>ImageData</code> based on the receiver.
 * Modifications made to this <code>ImageData</code> will not
 * affect the Image.
 *
 * @return an <code>ImageData</code> containing the image's data and
 *         attributes at 100% zoom level.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or an icon</li>
 * </ul>
 *
 * @see ImageData
 */
public ImageData getImageData () {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	return getImageData(100);

}

/**
 * Returns an <code>ImageData</code> based on the receiver.
 * Modifications made to this <code>ImageData</code> will not
 * affect the Image.
 *
 * @return an <code>ImageData</code> containing the image's data
 * and attributes at the current zoom level.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or an icon</li>
 * </ul>
 *
 * @see ImageData
 * @since 3.105
 * @deprecated This API doesn't serve the purpose in an environment having
 *             multiple monitors with different DPIs, hence deprecated. Use
 *             {@link #getImageData(int)} instead.
 */
@Deprecated
public ImageData getImageDataAtCurrentZoom () {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);

	long /*int*/ surface = ImageList.convertSurface(this);
	int format = Cairo.cairo_image_surface_get_format(surface);
	int width = Cairo.cairo_image_surface_get_width(surface);
	int height = Cairo.cairo_image_surface_get_height(surface);
	int stride = Cairo.cairo_image_surface_get_stride(surface);
	long /*int*/ surfaceData = Cairo.cairo_image_surface_get_data(surface);
	boolean hasAlpha = format == Cairo.CAIRO_FORMAT_ARGB32;
	int oa, or, og, ob;
	if (OS.BIG_ENDIAN) {
		oa = 0; or = 1; og = 2; ob = 3;
	} else {
		oa = 3; or = 2; og = 1; ob = 0;
	}
	byte[] srcData = new byte[stride * height];
	C.memmove(srcData, surfaceData, srcData.length);
	PaletteData palette = new PaletteData(0xFF0000, 0xFF00, 0xFF);
	ImageData data = new ImageData(width, height, 32, palette, 4, srcData);
	if (hasAlpha) {
		byte[] alphaData = data.alphaData = new byte[width * height];
		for (int y = 0, offset = 0, alphaOffset = 0; y < height; y++) {
			for (int x = 0; x < width; x++, offset += 4) {
				int a = srcData[offset + oa] & 0xFF;
				int r = srcData[offset + or] & 0xFF;
				int g = srcData[offset + og] & 0xFF;
				int b = srcData[offset + ob] & 0xFF;
				srcData[offset + 0] = 0;
				alphaData[alphaOffset++] = (byte)a;
				if (a != 0) {
					srcData[offset + 1] = (byte)(((r * 0xFF) + a / 2) / a);
					srcData[offset + 2] = (byte)(((g * 0xFF) + a / 2) / a);
					srcData[offset + 3] = (byte)(((b * 0xFF) + a / 2) / a);
				}
			}
		}
	} else {
		for (int y = 0, offset = 0; y < height; y++) {
			for (int x = 0; x < width; x++, offset += 4) {
				byte r = srcData[offset + or];
				byte g = srcData[offset + og];
				byte b = srcData[offset + ob];
				srcData[offset + 0] = 0;
				srcData[offset + 1] = r;
				srcData[offset + 2] = g;
				srcData[offset + 3] = b;
			}
		}
	}
	Cairo.cairo_surface_destroy(surface);
	return data;
}

/**
 * Returns an {@link ImageData} for the given zoom level based on the
 * receiver.
 * <p>
 * Note that this method is mainly intended to be used by custom
 * implementations of {@link ImageDataProvider} that draw a composite image
 * at the requested zoom level based on other images. For custom zoom
 * levels, the image data may be an auto-scaled version of the native image
 * and may look more blurred or mangled than expected.
 * </p>
 * <p>
 * Modifications made to the returned {@code ImageData} will not affect this
 * {@code Image}.
 * </p>
 *
 * @param zoom
 *            The zoom level in % of the standard resolution (which is 1
 *            physical monitor pixel == 1 SWT logical pixel). Typically 100,
 *            150, or 200.
 * @return an <code>ImageData</code> containing the image's data and
 *         attributes at the given zoom level
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or an icon</li>
 * </ul>
 *
 * @since 3.106
 */
public ImageData getImageData (int zoom) {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);

	if (zoom == currentDeviceZoom) {
		return getImageDataAtCurrentZoom();
	} else if (imageDataProvider != null) {
		boolean[] found = new boolean[1];
		ImageData data = DPIUtil.validateAndGetImageDataAtZoom (imageDataProvider, zoom, found);
		// exact image found
		if (found[0]) {
			return data;
		}
		// AutoScale the image at 100% zoom
		return DPIUtil.autoScaleUp (device, data);
	} else if (imageFileNameProvider != null) {
		boolean[] found = new boolean[1];
		String fileName = DPIUtil.validateAndGetImagePathAtZoom (imageFileNameProvider, zoom, found);
		// exact image found
		if (found[0]) {
			return new ImageData (fileName);
		}
		// AutoScale the image at 100% zoom
		return DPIUtil.autoScaleUp (device, new ImageData (fileName));
	} else {
		return DPIUtil.autoScaleImageData (device, getImageDataAtCurrentZoom (), zoom, currentDeviceZoom);
	}
}

/**
 * Invokes platform specific functionality to allocate a new image.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Image</code>. It is marked public only so that it
 * can be shared within the packages provided by SWT. It is not
 * available on all platforms, and should never be called from
 * application code.
 * </p>
 *
 * @param device the device on which to allocate the color
 * @param type the type of the image (<code>SWT.BITMAP</code> or <code>SWT.ICON</code>)
 * @param imageHandle the OS handle for the image
 * @param mask the OS handle for the image mask
 *
 * @noreference This method is not intended to be referenced by clients.
 */
public static Image gtk_new(Device device, int type, long /*int*/ imageHandle, long /*int*/ mask) {
	Image image = new Image(device);
	image.type = type;
	if (GTK.GTK3) {
		image.surface = imageHandle;
	} else {
		image.pixmap = imageHandle;
		image.createSurface();
	}
	image.mask = mask;
	return image;
}

/**
 * Invokes platform specific functionality to allocate a new image.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Image</code>. It is marked public only so that it
 * can be shared within the packages provided by SWT. It is not
 * available on all platforms, and should never be called from
 * application code.
 * </p>
 *
 * @param device the device on which to allocate the color
 * @param type the type of the image (<code>SWT.BITMAP</code> or <code>SWT.ICON</code>)
 * @param pixbuf an GdkPixbuf
 *
 * @noreference This method is not intended to be referenced by clients.
 */
public static Image gtk_new_from_pixbuf(Device device, int type, long /*int*/ pixbuf) {
	Image image = new Image(device);
	image.createFromPixbuf(type, pixbuf);
	image.type = type;
	return image;
}

/**
 * Returns an integer hash code for the receiver. Any two
 * objects that return <code>true</code> when passed to
 * <code>equals</code> must return the same value for this
 * method.
 *
 * @return the receiver's hash
 *
 * @see #equals
 */
@Override
public int hashCode () {
	if (imageDataProvider != null) {
		return imageDataProvider.hashCode();
	} else if (imageFileNameProvider != null) {
		 return imageFileNameProvider.hashCode();
	} else {
		return (int)/*64*/surface;
	}
}

void init(int width, int height) {
	if (width <= 0 || height <= 0) {
		SWT.error (SWT.ERROR_INVALID_ARGUMENT);
	}
	this.type = SWT.BITMAP;

	/* Create the pixmap */
	surface = GDK.gdk_window_create_similar_surface(GDK.gdk_get_default_root_window(), Cairo.CAIRO_CONTENT_COLOR, width, height);
	if (surface == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	long /*int*/ cairo = Cairo.cairo_create(surface);
	if (cairo == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	Cairo.cairo_set_source_rgb(cairo, 1, 1, 1);
	Cairo.cairo_rectangle(cairo, 0, 0, width, height);
	Cairo.cairo_fill(cairo);
	Cairo.cairo_destroy(cairo);
	this.width = width;
	this.height = height;
}

void init(ImageData image) {
	if (image == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	int width = this.width = image.width;
	int height = this.height = image.height;
	PaletteData palette = image.palette;
	if (!(((image.depth == 1 || image.depth == 2 || image.depth == 4 || image.depth == 8) && !palette.isDirect) ||
		((image.depth == 8) || (image.depth == 16 || image.depth == 24 || image.depth == 32) && palette.isDirect)))
			SWT.error (SWT.ERROR_UNSUPPORTED_DEPTH);
	boolean hasAlpha = image.transparentPixel != -1 || image.alpha != -1 || image.maskData != null || image.alphaData != null;
	int format = hasAlpha ? Cairo.CAIRO_FORMAT_ARGB32 : Cairo.CAIRO_FORMAT_RGB24;
	surface = Cairo.cairo_image_surface_create(format, width, height);
	if (surface == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	int stride = Cairo.cairo_image_surface_get_stride(surface);
	long /*int*/ data = Cairo.cairo_image_surface_get_data(surface);
	int oa = 0, or = 0, og = 0, ob = 0;
	int redMask, greenMask, blueMask, destDepth = 32, destOrder;
	if (OS.BIG_ENDIAN) {
		oa = 0; or = 1; og = 2; ob = 3;
		redMask = 0xFF00;
		greenMask = 0xFF0000;
		blueMask = 0xFF000000;
		destOrder = ImageData.MSB_FIRST;
	} else {
		oa = 3; or = 2; og = 1; ob = 0;
		redMask = 0xFF0000;
		greenMask = 0xFF00;
		blueMask = 0xFF;
		destOrder = ImageData.LSB_FIRST;
	}
	byte[] buffer = image.data;
	if (!palette.isDirect || image.depth != destDepth || stride != image.bytesPerLine || palette.redMask != redMask || palette.greenMask != greenMask || palette.blueMask != blueMask || destOrder != image.getByteOrder()) {
		buffer = new byte[stride * height];
		if (palette.isDirect) {
			ImageData.blit(ImageData.BLIT_SRC,
				image.data, image.depth, image.bytesPerLine, image.getByteOrder(), 0, 0, width, height, palette.redMask, palette.greenMask, palette.blueMask,
				ImageData.ALPHA_OPAQUE, null, 0, 0, 0,
				buffer, destDepth, stride, destOrder, 0, 0, width, height, redMask, greenMask, blueMask,
				false, false);
		} else {
			RGB[] rgbs = palette.getRGBs();
			int length = rgbs.length;
			byte[] srcReds = new byte[length];
			byte[] srcGreens = new byte[length];
			byte[] srcBlues = new byte[length];
			for (int i = 0; i < rgbs.length; i++) {
				RGB rgb = rgbs[i];
				if (rgb == null) continue;
				srcReds[i] = (byte)rgb.red;
				srcGreens[i] = (byte)rgb.green;
				srcBlues[i] = (byte)rgb.blue;
			}
			ImageData.blit(ImageData.BLIT_SRC,
				image.data, image.depth, image.bytesPerLine, image.getByteOrder(), 0, 0, width, height, srcReds, srcGreens, srcBlues,
				ImageData.ALPHA_OPAQUE, null, 0, 0, 0,
				buffer, destDepth, stride, destOrder, 0, 0, width, height, redMask, greenMask, blueMask,
				false, false);
		}
	}
	boolean isIcon = image.getTransparencyType() == SWT.TRANSPARENCY_MASK;
	this.type = isIcon ? SWT.ICON : SWT.BITMAP;
	if (isIcon || image.transparentPixel != -1) {
		if (image.transparentPixel != -1) {
			RGB rgb = null;
			if (palette.isDirect) {
				rgb = palette.getRGB(image.transparentPixel);
			} else {
				if (image.transparentPixel < palette.colors.length) {
					rgb = palette.getRGB(image.transparentPixel);
				}
			}
			if (rgb != null) {
				transparentPixel = rgb.red << 16 | rgb.green << 8 | rgb.blue;
			}
		}
		ImageData mask = image.getTransparencyMask();
		for (int y = 0, offset = 0; y < height; y++) {
			for (int x=0; x<width; x++, offset += 4) {
				int alpha = mask.getPixel(x, y) == 0 ? 0 : 0xff;
				/* pre-multiplied alpha */
				int r = ((buffer[offset + or] & 0xFF) * alpha) + 128;
				r = (r + (r >> 8)) >> 8;
				int g = ((buffer[offset + og] & 0xFF) * alpha) + 128;
				g = (g + (g >> 8)) >> 8;
				int b = ((buffer[offset + ob] & 0xFF) * alpha) + 128;
				b = (b + (b >> 8)) >> 8;
				buffer[offset + oa] = (byte)alpha;
				buffer[offset + or] = (byte)r;
				buffer[offset + og] = (byte)g;
				buffer[offset + ob] = (byte)b;
			}
		}
	} else {
		this.alpha = image.alpha;
		if (image.alpha == -1 && image.alphaData != null) {
			this.alphaData = new byte[image.alphaData.length];
			System.arraycopy(image.alphaData, 0, this.alphaData, 0, alphaData.length);
		}
		if (this.alpha != -1) {
			for (int y = 0, offset = 0; y < height; y++) {
				for (int x=0; x<width; x++, offset += 4) {
					int alpha = this.alpha;
					/* pre-multiplied alpha */
					int r = ((buffer[offset + or] & 0xFF) * alpha) + 128;
					r = (r + (r >> 8)) >> 8;
					int g = ((buffer[offset + og] & 0xFF) * alpha) + 128;
					g = (g + (g >> 8)) >> 8;
					int b = ((buffer[offset + ob] & 0xFF) * alpha) + 128;
					b = (b + (b >> 8)) >> 8;
					buffer[offset + oa] = (byte)alpha;
					buffer[offset + or] = (byte)r;
					buffer[offset + og] = (byte)g;
					buffer[offset + ob] = (byte)b;
				}
			}
		} else if (this.alphaData != null) {
			for (int y = 0, offset = 0; y < height; y++) {
				for (int x=0; x<width; x++, offset += 4) {
					int alpha = alphaData [y*width+x] & 0xFF;
					/* pre-multiplied alpha */
					int r = ((buffer[offset + or] & 0xFF) * alpha) + 128;
					r = (r + (r >> 8)) >> 8;
					int g = ((buffer[offset + og] & 0xFF) * alpha) + 128;
					g = (g + (g >> 8)) >> 8;
					int b = ((buffer[offset + ob] & 0xFF) * alpha) + 128;
					b = (b + (b >> 8)) >> 8;
					buffer[offset + oa] = (byte)alpha;
					buffer[offset + or] = (byte)r;
					buffer[offset + og] = (byte)g;
					buffer[offset + ob] = (byte)b;
				}
			}
		}
	}
	C.memmove(data, buffer, stride * height);
	Cairo.cairo_surface_mark_dirty(surface);
}

/**
 * Invokes platform specific functionality to allocate a new GC handle.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Image</code>. It is marked public only so that it
 * can be shared within the packages provided by SWT. It is not
 * available on all platforms, and should never be called from
 * application code.
 * </p>
 *
 * @param data the platform specific GC data
 * @return the platform specific GC handle
 *
 * @noreference This method is not intended to be referenced by clients.
 */
@Override
public long /*int*/ internal_new_GC (GCData data) {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (type != SWT.BITMAP || memGC != null) {
		SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	}
	long /*int*/ gc = Cairo.cairo_create(surface);
	if (data != null) {
		int mask = SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;
		if ((data.style & mask) == 0) {
			data.style |= SWT.LEFT_TO_RIGHT;
		} else {
			if ((data.style & SWT.RIGHT_TO_LEFT) != 0) {
				data.style |= SWT.MIRRORED;
			}
		}
		data.device = device;
		data.drawable = pixmap;
		if (GTK.GTK3) {
			data.foregroundRGBA = device.COLOR_BLACK.handleRGBA;
			data.backgroundRGBA = device.COLOR_WHITE.handleRGBA;
		} else {
			data.background = device.COLOR_WHITE.handle;
			data.foreground = device.COLOR_BLACK.handle;
		}
		data.font = device.systemFont;
		data.image = this;
	}
	return gc;
}

/**
 * Invokes platform specific functionality to dispose a GC handle.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Image</code>. It is marked public only so that it
 * can be shared within the packages provided by SWT. It is not
 * available on all platforms, and should never be called from
 * application code.
 * </p>
 *
 * @param hDC the platform specific GC handle
 * @param data the platform specific GC data
 *
 * @noreference This method is not intended to be referenced by clients.
 */
@Override
public void internal_dispose_GC (long /*int*/ hDC, GCData data) {
	Cairo.cairo_destroy(hDC);
}

/**
 * Returns <code>true</code> if the image has been disposed,
 * and <code>false</code> otherwise.
 * <p>
 * This method gets the dispose state for the image.
 * When an image has been disposed, it is an error to
 * invoke any other method (except {@link #dispose()}) using the image.
 *
 * @return <code>true</code> when the image is disposed and <code>false</code> otherwise
 */
@Override
public boolean isDisposed() {
	return surface == 0;
}

/**
 * Sets the color to which to map the transparent pixel.
 * <p>
 * There are certain uses of <code>Images</code> that do not support
 * transparency (for example, setting an image into a button or label).
 * In these cases, it may be desired to simulate transparency by using
 * the background color of the widget to paint the transparent pixels
 * of the image. This method specifies the color that will be used in
 * these cases. For example:
 * <pre>
 *    Button b = new Button();
 *    image.setBackground(b.getBackground());
 *    b.setImage(image);
 * </pre>
 * </p><p>
 * The image may be modified by this operation (in effect, the
 * transparent regions may be filled with the supplied color).  Hence
 * this operation is not reversible and it is not legal to call
 * this function twice or with a null argument.
 * </p><p>
 * This method has no effect if the receiver does not have a transparent
 * pixel value.
 * </p>
 *
 * @param color the color to use when a transparent pixel is specified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the color is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the color has been disposed</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void setBackground(Color color) {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (color == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (color.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	if (transparentPixel == -1) return;
	//NOT DONE
}

/**
 * Returns a string containing a concise, human-readable
 * description of the receiver.
 *
 * @return a string representation of the receiver
 */
@Override
public String toString () {
	if (isDisposed()) return "Image {*DISPOSED*}";
	return "Image {" + surface + "}";
}

}
