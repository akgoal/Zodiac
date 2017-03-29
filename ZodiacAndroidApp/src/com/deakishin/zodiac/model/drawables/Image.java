package com.deakishin.zodiac.model.drawables;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

/** Image that can be drawn in a {@link Canvas} object. */
public class Image implements Drawable {

	/* Drawn image. */
	private Bitmap mImg;

	/* Position and size. */
	private int mX, mY, mWidth, mHeight;

	/* Size of the source image. */
	private int mImgWidth, mImgHeight;

	public Image() {
	}

	/**
	 * Creates an an object that holds an image and that can draw it.
	 * 
	 * @param img
	 *            Bitmap image to draw.
	 */
	public Image(Bitmap img) {
		this.mImg = img;
		mImgWidth = img.getWidth();
		mImgHeight = img.getHeight();
	}

	@Override
	public void draw(Canvas canvas, int x, int y, int w, int h, boolean toFit) {
		if (mImg == null)
			return;

		if (toFit) {
			this.mX = x;
			this.mY = y;
			this.mWidth = w;
			this.mHeight = h;
		} else {
			float ratio = w / (float) h;
			float imgRatio = mImg.getWidth() / (float) mImg.getHeight();
			if (ratio > imgRatio) {
				// Окно шире чем изображение
				this.mY = y;
				this.mHeight = h;
				this.mWidth = (int) (imgRatio * this.mHeight);
				this.mX = x + (w - this.mWidth) / 2;
			} else {
				this.mX = x;
				this.mWidth = w;
				this.mHeight = (int) (this.mWidth / imgRatio);
				this.mY = y + (h - this.mHeight) / 2;
			}
		}
		canvas.drawBitmap(mImg, null, new Rect(mX, mY, mX + mWidth, mY + mHeight), null);
	}

	/**
	 * @return Scale with which the image was drawn. Returns an array of two
	 *         numbers: the first is for width, the second is for height. For
	 *         example, if the image is 100x100 and the area it was drawn on is
	 *         50x75 then {0.5, 0.75} will be returned.
	 */
	public float[] getScale() {
		float[] res = new float[2];
		res[0] = mWidth / (float) mImgWidth;
		res[1] = mHeight / (float) mImgHeight;
		return res;
	}

	@Override
	public void recycle() {
		if (mImg != null) {
			mImg.recycle();
			mImg = null;
		}
	}

	/** @return Bitmap image that the object holds. */
	public Bitmap getImg() {
		return mImg;
	}

	/** @return X-coordinate of the top-left corner of the drawn image. */
	public int getX() {
		return mX;
	}

	/** @return Y-coordinate of the top-left corner of the drawn image. */
	public int getY() {
		return mY;
	}

	/** @return Width of the drawn image. */
	public int getWidth() {
		return mWidth;
	}

	/** @return Height of the drawn image. */
	public int getHeight() {
		return mHeight;
	}

}
