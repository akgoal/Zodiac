package com.deakishin.zodiac.controller.mainscreen;

import java.util.Map;

import com.deakishin.zodiac.model.Model;
import com.deakishin.zodiac.model.SymbModel;
import com.deakishin.zodiac.model.drawables.Image;
import com.deakishin.zodiac.model.settings.FontColorProfile;
import com.deakishin.zodiac.model.settings.SettingsPersistent;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class MainImageView extends View {
	/* Пользовательское представление для вывода главного изображения. */

	/* Модель и выводимое фонового (исходное) изображение. */
	private Model mModel;
	private Image mImage;
	/* Слушатель клика по символу на изображении. */
	private OnSymbolClickListener mOnSymbolClickListener;

	/* Параметры отображения букв поверх изображения. */
	private Paint mFontPaint;
	private Paint mRememberedFontPaint;
	private Paint mBgPaint;
	private Paint mFontPaintToUse;

	/* Выводимые привязки символов. */
	private Map<Integer, Character> mBindingToDraw, mRememberedBindingToDraw;

	/* Настройки приложения. */
	private SettingsPersistent mSettings;

	/*
	 * Коэффициенты масштабирования и позиция выведенного изображения.
	 * Необходимы для корректного расположения символов.
	 */
	private float mScaleX = 1, mScaleY = 1;
	private int mImgX, mImgY, mImgW, mImgH;
	/* Параметры решетки, разделяющей изоражение (в исходном масштабе). */
	private int mCellH, mCellW;

	public MainImageView(Context context) {
		this(context, null);
	}

	public MainImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mModel = Model.getInstance(context);
		mImage = new Image(mModel.getModelImage());
		mCellH = mModel.getCellHeight();
		mCellW = mModel.getCellWidth();

		mSettings = SettingsPersistent.getInstance(context);
		mSettings.addOnFontColorProfileChangeListener(new SettingsPersistent.OnFontColorProfileChangeListener() {
			@Override
			public void onFontColorProfileChanged(FontColorProfile fontColorProfile) {
				createPaints(fontColorProfile);
			}
		});

		createPaints(mSettings.getFontColorProfile());
	}

	/* Создание объектов Paint для рисования букв. */
	private void createPaints(FontColorProfile fontColorProfile) {
		mFontPaint = new Paint();
		mFontPaint.setColor(fontColorProfile.getPrimalColor());
		mFontPaint.setTextAlign(Paint.Align.CENTER);
		mFontPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

		mRememberedFontPaint = new Paint(mFontPaint);
		mRememberedFontPaint.setColor(fontColorProfile.getSecondaryColor());

		mBgPaint = new Paint();
		mBgPaint.setColor(Color.WHITE);
	}

	public void setOnSymbolClickListener(OnSymbolClickListener symbolClickListener) {
		mOnSymbolClickListener = symbolClickListener;
	}

	/*
	 * Обновление. Вызов метода означает, что данные для прорисовки изменились.
	 */
	public void update() {
		invalidate();
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (mImage != null) {
			mImage.draw(canvas, 0, 0, this.getWidth(), this.getHeight(), false);
			mImgX = mImage.getX();
			mImgY = mImage.getY();
			mImgW = mImage.getWidth();
			mImgH = mImage.getHeight();
			mScaleX = mImage.getScale()[0];
			mScaleY = mImage.getScale()[1];
		}

		mBindingToDraw = mModel.getBindingManager().getBinding();
		mRememberedBindingToDraw = mModel.getBindingManager().getRememberedBinding();
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
						// drawChar(canvas, chr, l, mFontPaintToUse);
						drawChar(canvas, chr, l, mFontPaintToUse, mImgX, mImgY, mScaleX, mScaleY);
			}
		}
	}

	private int x, y, w, h;
	private String textToDraw;

	/* Прорисовка буквы в месте loc. */
	private void drawChar(Canvas canvas, Character chr, SymbModel.Location loc, Paint fontPaint) {
		x = mImgX + (int) (loc.getX() * mCellW * mScaleX);
		y = mImgY + (int) (loc.getY() * mCellH * mScaleY);
		w = (int) (mCellW * mScaleX);
		h = (int) (mCellH * mScaleY);

		canvas.drawRect(x, y, x + w, y + h, mBgPaint);
		textToDraw = Character.toString(chr);

		fontPaint.setTextSize(h);
		canvas.drawText(textToDraw, x + w / 2, y + h, fontPaint);
	}

	/*
	 * Прорисовка буквы в месте loc. imgX,imgY - позиция фонового изображение,
	 * scaleX, scaleY - коэффициенты масштабирования.
	 */
	private void drawChar(Canvas canvas, Character chr, SymbModel.Location loc, Paint fontPaint, int imgX, int imgY,
			float scaleX, float scaleY) {
		x = imgX + (int) (loc.getX() * mCellW * scaleX);
		y = imgY + (int) (loc.getY() * mCellH * scaleY);
		w = (int) (mCellW * scaleX);
		h = (int) (mCellH * scaleY);

		canvas.drawRect(x, y, x + w, y + h, mBgPaint);
		textToDraw = Character.toString(chr);

		fontPaint.setTextSize(h);
		canvas.drawText(textToDraw, x + w / 2, y + h, fontPaint);
	}

	/*
	 * Параметры для индикации выхода за пределы одного символа во время
	 * касания. В этом случае выбор символа не инициализируется.
	 */
	private boolean mDraggedTooFar;
	private float mDownX, mDownY;
	private float mMaxX, mMaxY;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDraggedTooFar = false;
			mDownX = event.getX();
			mDownY = event.getY();
			mMaxX = mCellW * mScaleX;
			mMaxY = mCellH * mScaleY;
			break;
		case MotionEvent.ACTION_MOVE:
			if (Math.abs(event.getX() - mDownX) > mMaxX || Math.abs(event.getY() - mDownY) > mMaxY)
				mDraggedTooFar = true;
			break;
		case MotionEvent.ACTION_UP:
			if (!mDraggedTooFar) {
				if (event.getX() < mImgX || event.getY() < mImgY || event.getX() > mImgX + mImgW - 1
						|| event.getY() > mImgY + mImgH - 1)
					break;
				if (mOnSymbolClickListener != null)
					mOnSymbolClickListener.onSymbolClick(getSymbolId(event.getX(), event.getY()));
			}
			break;
		}
		return true;
	}

	/* Получение id символа по координатам x,y касания. */
	private int getSymbolId(float x, float y) {
		return mModel.getSymbIdByCoord((int) ((x - mImgX) / mScaleX), (int) ((y - mImgY) / mScaleY));
	}

	/* Интерфейс слушателя клика по символу. */
	public interface OnSymbolClickListener {
		public void onSymbolClick(int symbId);
	}

	/* Изображение с нанесенными поверх буквами. */
	public Bitmap createBitmapImage() {
		Bitmap resBitmap = Bitmap.createBitmap(mImage.getImg().getWidth(), mImage.getImg().getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(resBitmap);
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
		return resBitmap;
	}

	/*
	 * Оригинальное изображение с нанесенными поверх буквами. В правом нижнем
	 * угле приписывается подпись signature.
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
