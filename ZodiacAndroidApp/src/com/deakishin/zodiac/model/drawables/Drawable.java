package com.deakishin.zodiac.model.drawables;

import android.graphics.Canvas;

/** Interface for objects that can be drawn on a {@link Canvas} object. */
public interface Drawable {

	/**
	 * Draws the object.
	 * 
	 * @param canvas
	 *            Canvas to draw on.
	 * @param x
	 *            X-coordinate of the top-left corner.
	 * @param y
	 *            Y-coordinate of the top-left corner.
	 * @param w
	 *            Width.
	 * @param h
	 *            Height.
	 * @param toFit
	 *            True if the object has to fill the area, false otherwise.
	 */
	void draw(Canvas canvas, int x, int y, int w, int h, boolean toFit);

	/** Releases resources that are being held. */
	void recycle();
}
