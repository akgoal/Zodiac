package com.deakishin.zodiac.model.drawables;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Image that can be drawn and that has limitations for its width and height
 * when being drawn. These limitations affect how much space the drawn image can
 * take.
 */
public class ImageWithLimits extends Image {

	/* Maximum width and height for the drawn image. */
	private int mMaxWidth;
	private int mMaxHeight;

	/**
	 * Creates an an object that holds an image and that can draw it.
	 * 
	 * @param img
	 *            Bitmap image to draw.
	 * @param maxWidth
	 *            Maximum width of the drawn image.
	 * @param maxHeight
	 *            Maximum height of the drawn image.
	 */
	public ImageWithLimits(Bitmap bitmap, int maxWidth, int maxHeight) {
		super(bitmap);
		mMaxWidth = maxWidth;
		mMaxHeight = maxHeight;
	}

	@Override
	public void draw(Canvas canvas, int x, int y, int w, int h, boolean toFit) {
		if (mMaxWidth > 0 && w > mMaxWidth) {
			x += (w - mMaxWidth) / 2;
			w = mMaxWidth;
		}
		if (mMaxHeight > 0 && h > mMaxHeight) {
			y += (h - mMaxHeight) / 2;
			h = mMaxHeight;
		}
		super.draw(canvas, x, y, w, h, toFit);
	}
}
