package com.deakishin.zodiac.controller.boardscreen;

import com.deakishin.zodiac.model.drawables.Drawable;
import com.deakishin.zodiac.model.drawables.Image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/** View for previewing cipher's image. */
public class CipherPreviewView extends View {

	/* Displayed cipher's image. */
	private Drawable mCipherImage;
	/* Solution image. */
	private Drawable mSolutionImage;

	/* Flag indicating if the solution must be shown. */
	private boolean mShowSolution = false;

	/* Background color. */
	private static final int BG_COLOR = Color.WHITE;

	/* Paint for painting background. */
	private Paint mBgPaint;

	public CipherPreviewView(Context context) {
		this(context, null);
	}

	public CipherPreviewView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mBgPaint = new Paint();
		mBgPaint.setColor(BG_COLOR);
	}

	/* Make view square using its width as the square's side length. */
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, widthMeasureSpec);
	}

	/**
	 * Sets displayed images for cipher and its solution.
	 * 
	 * @param images
	 *            Two images. First is cipher itself, second is its solution.
	 */
	public void setImages(Bitmap[] images) {
		if (mCipherImage != null) {
			mCipherImage.recycle();
			mCipherImage = null;
		}
		if (mSolutionImage != null) {
			mSolutionImage.recycle();
			mSolutionImage = null;
		}
		mShowSolution = false;

		if (images == null || images.length != 2)
			return;

		if (images[0] != null)
			mCipherImage = new Image(images[0]);

		if (images[1] != null)
			mSolutionImage = new Image(images[1]);

		invalidate();
	}

	/**
	 * Sets displayed cipher's image. Solution image gets deleted.
	 * 
	 * @param image
	 *            Cipher's image to display.
	 */
	public void setCipherImage(Bitmap image) {
		setImages(new Bitmap[] { image, null });
	}

	@Override
	public void onDraw(Canvas canvas) {
		canvas.drawRect(0, 0, this.getWidth(), this.getHeight(), mBgPaint);
		if (mShowSolution && mSolutionImage != null) {
			mSolutionImage.draw(canvas, 0, 0, this.getWidth(), this.getHeight(), false);
			return;
		} else {
			if (mCipherImage != null) {
				mCipherImage.draw(canvas, 0, 0, this.getWidth(), this.getHeight(), false);
			}
		}
	}

	/**
	 * Sets solution visibility and updates the view.
	 * 
	 * @param showSolution
	 *            Indicates whether to show solution or not.
	 */
	public void setShowSolution(boolean showSolution) {
		mShowSolution = showSolution;
		invalidate();
	}

	/**
	 * Indicates solution visibility.
	 * 
	 * @return True if the solution is shown, false otherwise.
	 */
	public boolean isSolutionShown() {
		return mShowSolution;
	}
}
