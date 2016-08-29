package com.deakishin.zodiac.model.settings;

public class CheckpointNameOption {
	/* Вариант именования чекпоинтов. */

	/* Код. */
	private int mCode;

	/* Ресурс названия варианта. */
	private int mTitleResId;

	public CheckpointNameOption(int code, int titleResId) {
		super();
		mCode = code;
		mTitleResId = titleResId;
	}

	public int getCode() {
		return mCode;
	}

	public int getTitleResId() {
		return mTitleResId;
	}
}
