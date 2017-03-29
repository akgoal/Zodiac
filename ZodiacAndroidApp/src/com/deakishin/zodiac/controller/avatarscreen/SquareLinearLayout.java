package com.deakishin.zodiac.controller.avatarscreen;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/** Square {@link LinearLayout}. */
public class SquareLinearLayout extends LinearLayout {

	public SquareLinearLayout(Context context) {
		this(context, null);
	}

	public SquareLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	// Make view square. The side length is the minimum between view's original width
	// and height.
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int dim = Math.min(getMeasuredWidth(), getMeasuredHeight());
		setMeasuredDimension(dim, dim);
		super.onMeasure(dim, dim);
	}
}
