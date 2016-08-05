package com.example.zodiac.controller;

import java.util.Map;

import com.example.zodiac.model.Model;
import com.example.zodiac.model.SymbModel;
import com.example.zodiac.model.drawables.Image;

import android.content.Context;
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
	private static final int FONT_COLOR = Color.RED;
	private static final int REMEMBERED_FONT_COLOR = Color.argb(255, 200, 0, 0);
	private Paint mFontPaint;
	private Paint mRememberedFontPaint;
	private Paint mBgPaint;

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
		createPaints();
	}

	/* Создание объектов Paint для рисования букв. */
	private void createPaints() {
		mFontPaint = new Paint();
		mFontPaint.setColor(FONT_COLOR);
		mFontPaint.setTextAlign(Paint.Align.CENTER);
		mFontPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

		mRememberedFontPaint = new Paint(mFontPaint);
		mRememberedFontPaint.setColor(REMEMBERED_FONT_COLOR);

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
		Paint fontPaint;
		Map<Integer, Character> binding = mModel.getBindingManager().getBinding();
		Map<Integer, Character> rememberedBinding = mModel.getBindingManager().getRememberedBinding();
		int id;
		for (SymbModel.SymbInfo s : mModel.getSymbols()) {
			id = s.getId();
			if (binding.containsKey(id)) {
				Character chr = binding.get(id);
				if (rememberedBinding != null && rememberedBinding.containsKey(id)
						&& rememberedBinding.get(id).equals(chr))
					fontPaint = mRememberedFontPaint;
				else
					fontPaint = mFontPaint;
				if (chr != null)
					for (SymbModel.Location l : s.getLocations())
						drawChar(canvas, chr, l, fontPaint);
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

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			if (event.getX() < mImgX || event.getY() < mImgY || event.getX() > mImgX + mImgW - 1
					|| event.getY() > mImgY + mImgH - 1)
				break;
			if (mOnSymbolClickListener != null)
				mOnSymbolClickListener.onSymbolClick(getSymbolId(event.getX(), event.getY()));
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

}
