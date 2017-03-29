package com.deakishin.zodiac.controller.mainscreen;

import java.util.Map;

import com.deakishin.zodiac.model.ciphermanager.CipherManager;
import com.deakishin.zodiac.model.ciphermodel.Model;
import com.deakishin.zodiac.model.ciphermodel.SymbModel;
import com.deakishin.zodiac.model.drawables.Image;
import com.deakishin.zodiac.model.settings.FontColorProfile;
import com.deakishin.zodiac.model.settings.SettingsPersistent;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Custom view for displaying main image.
 * 
 * It draws cipher's image, then on top of it letters from the current and
 * remembered bindings (symbol-character mappings that it gets from
 * {@link BindingManager}), and finally highlighted area of the symbol that's
 * being touched.
 * 
 * It also registers symbols being clicked on and notify its listener
 * {@link OnSymbolClickListener} about it.
 */
public class MainImageView extends View {

	/* Cipher's model. */
	private Model mModel;
	/* Cipher's image. */
	private Image mImage;
	/* Listener to clicks on symbols. */
	private OnSymbolClickListener mOnSymbolClickListener;

	/* Paints to paint letter on top of the image. */
	private Paint mFontPaint;
	private Paint mRememberedFontPaint;
	private Paint mBgPaint;
	private Paint mFontPaintToUse;

	/*
	 * Displayed bindings. Binding is a mapping between symbols (their ids) and
	 * characters to replace the symbols with. There are two drawn bindings: one
	 * is remembered binding, i.e. last checkpoint, the other one is current
	 * binding that user creates.
	 */
	private Map<Integer, Character> mBindingToDraw, mRememberedBindingToDraw;

	/* Application settings. */
	private SettingsPersistent mSettings;

	/*
	 * Scale and position of the displayed image. Necessary to draw letter on
	 * top of it correctly.
	 */
	private float mScaleX = 1, mScaleY = 1;
	private int mImgX, mImgY, mImgW, mImgH;
	/* Grid params, separating symbols in the image (in the original scale). */
	private int mCellH, mCellW;

	/* Rectangle defining area of the selected (highlighted) symbol. */
	private Rect mHighlightedSymbRect;
	/* Paint to highlight selected area. */
	private Paint mHighlightPaint;

	public MainImageView(Context context) {
		this(context, null);
	}

	public MainImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mModel = CipherManager.getInstance(context).getCipherModel();
		mImage = new Image(mModel.getModelImage());
		mCellH = mModel.getCellHeight();
		mCellW = mModel.getCellWidth();

		mSettings = SettingsPersistent.getInstance(context);

		createPaints(mSettings.getFontColorProfile());
	}

	/* Create paints to perform drawing on top of the image. */
	private void createPaints(FontColorProfile fontColorProfile) {
		mFontPaint = new Paint();
		mFontPaint.setColor(fontColorProfile.getPrimalColor());
		mFontPaint.setTextAlign(Paint.Align.CENTER);
		mFontPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

		mRememberedFontPaint = new Paint(mFontPaint);
		mRememberedFontPaint.setColor(fontColorProfile.getSecondaryColor());

		mBgPaint = new Paint();
		mBgPaint.setColor(Color.WHITE);

		mHighlightPaint = new Paint();
		mHighlightPaint.setColor(Color.BLACK);
		mHighlightPaint.setAlpha(40);
	}

	/**
	 * Sets listener to clicks on symbols.
	 * 
	 * @param symbolClickListener
	 *            Listener that listens to symbols being clicked on.
	 */
	public void setOnSymbolClickListener(OnSymbolClickListener symbolClickListener) {
		mOnSymbolClickListener = symbolClickListener;
	}

	/**
	 * Updates the view. Needs to invoked when data for drawing has changed.
	 */
	public void update() {
		invalidate();
	}

	/**
	 * Update colors accoring to app settings.
	 */
	public void updateColors() {
		if (mSettings != null) {
			createPaints(mSettings.getFontColorProfile());
		}
	}

	/**
	 * Update model being used. Current model is switched to one that is held in
	 * Cipher Manager.
	 */
	public void updateModelInUse() {
		mModel = CipherManager.getInstance(getContext()).getCipherModel();
		mImage = new Image(mModel.getModelImage());
		update();
	}

	@Override
	public void onDraw(Canvas canvas) {
		// Draw cipher image.
		if (mImage != null) {
			mImage.draw(canvas, 0, 0, this.getWidth(), this.getHeight(), false);
			mImgX = mImage.getX();
			mImgY = mImage.getY();
			mImgW = mImage.getWidth();
			mImgH = mImage.getHeight();
			mScaleX = mImage.getScale()[0];
			mScaleY = mImage.getScale()[1];
		}

		// Draw characters that replace symbols.
		mBindingToDraw = getBindingToDraw();
		mRememberedBindingToDraw = getRememberedBindingToDraw();
		int id;
		for (SymbModel.SymbInfo s : mModel.getSymbols()) {
			id = s.getId();
			if (mBindingToDraw.containsKey(id)) {
				Character chr = mBindingToDraw.get(id);
				if (mRememberedBindingToDraw != null && mRememberedBindingToDraw.containsKey(id)
						&& mRememberedBindingToDraw.get(id).equals(chr))
					mFontPaintToUse = mRememberedFontPaint;
				else
					mFontPaintToUse = mFontPaint;
				if (chr != null)
					for (SymbModel.Location l : s.getLocations())
						drawChar(canvas, chr, l, mFontPaintToUse, mImgX, mImgY, mScaleX, mScaleY);
			}
		}

		// Draw highlighted area
		if (mHighlightedSymbRect != null) {
			drawHighlight(canvas, mHighlightedSymbRect, mHighlightPaint, mImgX, mImgY, mScaleX, mScaleY);
		}
	}

	/** @return Binding that is being drawn. */
	protected Map<Integer, Character> getBindingToDraw() {
		return mModel.getBindingManager().getBinding();
	}

	/** @return Remembered Binding that is being drawn. */
	protected Map<Integer, Character> getRememberedBindingToDraw() {
		return mModel.getBindingManager().getRememberedBinding();
	}

	private int x, y, w, h, charY;
	private int x1, y1;
	private String textToDraw;

	/* Draw character in the specified location of the image. */
	private void drawChar(Canvas canvas, Character chr, SymbModel.Location loc, Paint fontPaint, int imgX, int imgY,
			float scaleX, float scaleY) {
		x = imgX + (int) (loc.getX() * mCellW * scaleX);
		y = imgY + (int) (loc.getY() * mCellH * scaleY);
		w = (int) (mCellW * scaleX);
		h = (int) (mCellH * scaleY);

		canvas.drawRect(x, y, x + w, y + h, mBgPaint);
		textToDraw = Character.toString(chr);

		fontPaint.setTextSize(h);
		charY = (int) (y + 0.9 * h);
		canvas.drawText(textToDraw, x + w / 2, charY, fontPaint);
	}

	/* Draw highlighted area. */
	private void drawHighlight(Canvas canvas, Rect rect, Paint paint, int imgX, int imgY, float scaleX, float scaleY) {
		x = imgX + (int) (rect.left * scaleX);
		y = imgY + (int) (rect.top * scaleY);
		x1 = imgX + (int) (rect.right * scaleX);
		y1 = imgY + (int) (rect.bottom * scaleY);
		canvas.drawRect(x, y, x1, y1, paint);
	}

	/*
	 * Params for determining if touch event was dragged to far. In this case
	 * click on a symbol is not registered.
	 */
	private boolean mDraggedTooFar;
	private float mDownX, mDownY;
	private float mMaxX, mMaxY;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mHighlightedSymbRect = getRectByCoord(event.getX(), event.getY());
			invalidate();
			mDraggedTooFar = false;
			mDownX = event.getX();
			mDownY = event.getY();
			mMaxX = mCellW * mScaleX;
			mMaxY = mCellH * mScaleY;
			break;
		case MotionEvent.ACTION_MOVE:
			if (Math.abs(event.getX() - mDownX) > mMaxX || Math.abs(event.getY() - mDownY) > mMaxY) {
				mDraggedTooFar = true;
				mHighlightedSymbRect = null;
			} else {
				if (!mDraggedTooFar)
					mHighlightedSymbRect = getRectByCoord(event.getX(), event.getY());
			}
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			mHighlightedSymbRect = null;
			invalidate();
			if (!mDraggedTooFar) {
				if (event.getX() < mImgX || event.getY() < mImgY || event.getX() > mImgX + mImgW - 1
						|| event.getY() > mImgY + mImgH - 1)
					break;
				if (mOnSymbolClickListener != null) {
					int symbId = getSymbolId(event.getX(), event.getY());
					if (symbId >= 0)
						mOnSymbolClickListener.onSymbolClick(symbId);
				}
			}
			break;
		}
		return true;
	}

	/* Get symbols id from click coordinates x, y. */
	private int getSymbolId(float x, float y) {
		int[] modelCoords = convertCoord(x, y);
		return mModel.getSymbIdByCoord(modelCoords[0], modelCoords[1]);
	}

	/*
	 * Get rectangle of the symbol's area on the image from click coordinates x,
	 * y.
	 */
	private Rect getRectByCoord(float x, float y) {
		int[] modelCoords = convertCoord(x, y);
		Rect rect = mModel.getRectByCoord(modelCoords[0], modelCoords[1]);
		return rect;
	}

	/*
	 * Convert touch coordinates on the view to the coordinates on the model's
	 * image.
	 */
	private int[] convertCoord(float x, float y) {
		int[] converted = { (int) ((x - mImgX) / mScaleX), (int) ((y - mImgY) / mScaleY) };
		return converted;
	}

	/**
	 * The listener interface for receiving click events on symbols.
	 */
	public interface OnSymbolClickListener {
		/**
		 * Invoked when a symbol's are is clicked.
		 * 
		 * @param symbId
		 *            Id of the symbol.
		 */
		public void onSymbolClick(int symbId);
	}

	/**
	 * Creates {@link Bitmap} image by combining cipher's image with characters
	 * from the bindings.
	 * 
	 * @param signature
	 *            Signature string that is added to the bottom of the result
	 *            image.
	 * 
	 * @return Cipher's image with letters drawn on it.
	 */
	public Bitmap createBitmapImage(String signature) {
		int signatureHeight = mCellH / 2;
		int imgW = mImage.getImg().getWidth();
		int imgH = mImage.getImg().getHeight();
		if (signatureHeight * signature.length() > imgW) {
			signatureHeight = imgW / signature.length();
		}
		/*
		 * Bitmap resBitmap = Bitmap.createBitmap(imgW, imgH + signatureHeight +
		 * signatureHeight / 2, Bitmap.Config.ARGB_8888);
		 */
		Bitmap resBitmap = Bitmap.createBitmap(imgW, imgH, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(resBitmap);
		canvas.drawPaint(mBgPaint);
		if (mImage != null)
			canvas.drawBitmap(mImage.getImg(), 0, 0, null);
		mBindingToDraw = mModel.getBindingManager().getBinding();
		int id;
		mFontPaintToUse = mFontPaint;
		for (SymbModel.SymbInfo s : mModel.getSymbols()) {
			id = s.getId();
			if (mBindingToDraw.containsKey(id)) {
				Character chr = mBindingToDraw.get(id);
				if (chr != null)
					for (SymbModel.Location l : s.getLocations())
						drawChar(canvas, chr, l, mFontPaintToUse, 0, 0, 1, 1);
			}
		}
		/*
		 * Paint signPaint = new Paint();
		 * signPaint.setTextSize(signatureHeight);
		 * signPaint.setColor(Color.BLACK);
		 * signPaint.setTextAlign(Paint.Align.RIGHT);
		 * signPaint.setTypeface(Typeface.create(Typeface.DEFAULT,
		 * Typeface.NORMAL));
		 * 
		 * canvas.drawText(signature, imgW - signatureHeight / 2, imgH +
		 * signatureHeight, signPaint);
		 */

		return resBitmap;
	}

	/**
	 * Creates {@link Bitmap} image by combining original Zodiac-340 cipher's
	 * image with characters from the bindings.
	 * 
	 * @param signature
	 *            Signature string that is added to the bottom of the result
	 *            image.
	 * 
	 * @return Zodiac-340 cipher's image with letters drawn on it.
	 */
	public Bitmap createOriginalBitmapImage(String signature) {
		Bitmap originalBitmap = mModel.getOriginalImage();
		final int signatureHeight = mCellH / 2;
		Bitmap resBitmap = Bitmap.createBitmap(originalBitmap.getWidth(),
				originalBitmap.getHeight() + signatureHeight + signatureHeight / 2, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(resBitmap);
		canvas.drawPaint(mBgPaint);
		if (mImage != null)
			canvas.drawBitmap(originalBitmap, 0, 0, null);
		mBindingToDraw = mModel.getBindingManager().getBinding();
		int id;
		int offsetX = mModel.getOffsetX();
		int offsetY = mModel.getOffsetY();
		for (SymbModel.SymbInfo s : mModel.getSymbols()) {
			id = s.getId();
			if (mBindingToDraw.containsKey(id)) {
				Character chr = mBindingToDraw.get(id);
				if (chr != null)
					for (SymbModel.Location l : s.getLocations())
						drawChar(canvas, chr, l, mFontPaint, offsetX, offsetY, 1, 1);
			}
		}
		Paint signPaint = new Paint();
		signPaint.setTextSize(signatureHeight);
		signPaint.setColor(Color.BLACK);
		signPaint.setTextAlign(Paint.Align.RIGHT);
		signPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
		canvas.drawText(signature, originalBitmap.getWidth() - signatureHeight / 2,
				originalBitmap.getHeight() + signatureHeight, signPaint);
		return resBitmap;
	}
}
