package com.deakishin.zodiac.controller;

import com.deakishin.zodiac.model.drawables.Image;
import com.deakishin.zodiac.model.drawables.ImageWithLimits;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * ImageView widget that can recycle its image when the image is replaced by new
 * one.
 */
public class RecyclingImageView extends ImageView {
	
	/* Displayed image. */
	private Image mImage;

	/* Paint for painting background. */
	private static final Paint BG_PAINT = new Paint();

	// Set background color and transparency.
	{
		BG_PAINT.setColor(Color.WHITE);
		BG_PAINT.setAlpha(235);
	}

	/* Maximum image size. */
	private static final int MAX_WIDTH = 300, MAX_HEIGHT = 300;

	public RecyclingImageView(Context context) {
		this(context, null);
	}

	public RecyclingImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// Make view square.
		/*
		 * int dim = Math.min(getMeasuredWidth(), getMeasuredHeight());
		 * setMeasuredDimension(dim, dim);
		 */
	}

	@Override
	public void setImageBitmap(Bitmap bitmap) {
		super.setImageBitmap(bitmap);

		// Recycle image.
		if (mImage != null)
			mImage.recycle();
		mImage = new ImageWithLimits(bitmap, MAX_WIDTH, MAX_HEIGHT);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawPaint(BG_PAINT);

		if (mImage == null)
			return;

		// Draw the image in the center.
		mImage.draw(canvas, 0, 0, getWidth(), getHeight(), false);
	}
}
