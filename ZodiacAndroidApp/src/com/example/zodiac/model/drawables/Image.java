package com.example.zodiac.model.drawables;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Image implements Drawable {
	/*
	 * Изображение.
	 */

	/* Рисуемая картинка. */
	private Bitmap mImg;

	/* Позиция и размеры. */
	private int mX, mY, mWidth, mHeight;

	/* Размеры исходного изображения. */
	private int mImgWidth, mImgHeight;

	public Image() {
	}

	public Image(Bitmap img) {
		this.mImg = img;
		mImgWidth = img.getWidth();
		mImgHeight = img.getHeight();
	}

	@Override
	public void draw(Canvas canvas, int x, int y, int w, int h, boolean toFit) {
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

	/* Масштаб, с которым выведено изображение. */
	public float[] getScale() {
		float[] res = new float[2];
		res[0] = mWidth / (float) mImgWidth;
		res[1] = mHeight / (float) mImgHeight;
		return res;
	}

	public int getX() {
		return mX;
	}

	public int getY() {
		return mY;
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

}
