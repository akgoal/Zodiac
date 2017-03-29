package com.deakishin.zodiac.model.settings;

/**
 * Option for checkpoint naming. Has a code and an id of the resource to use
 * when naming a checkpoint.
 */
public class CheckpointNameOption {

	/* Code. */
	private int mCode;

	/* Id of the resource to use to name a checkpoint. */
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
