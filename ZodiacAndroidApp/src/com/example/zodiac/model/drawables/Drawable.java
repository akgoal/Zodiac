package com.example.zodiac.model.drawables;

import android.graphics.Canvas;

public interface Drawable {
	/*
	 * Интерфейс описывает объекты, которые можно нарисовать
	 */

	/*
	 * Рисование объекта. x,y - позиции левого верхнего угла, w - ширина, h -
	 * высота, toFit определяет, должен ли объект заполнить выделенное ему
	 * место.
	 */
	void draw(Canvas canvas, int x, int y, int w, int h, boolean toFit);
}
