package com.deakishin.zodiac.model.settings;

/* Color profile. Has id, primary color and secondary color. */
public class FontColorProfile {
	private int mId;
	private int mPrimalColor;
	private int mSecondaryColor;

	public FontColorProfile(int id, int primalColor, int secondaryColor) {
		super();
		mId = id;
		mPrimalColor = primalColor;
		mSecondaryColor = secondaryColor;
	}

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		mId = id;
	}

	public int getPrimalColor() {
		return mPrimalColor;
	}

	public void setPrimalColor(int primalColor) {
		mPrimalColor = primalColor;
	}

	public int getSecondaryColor() {
		return mSecondaryColor;
	}

	public void setSecondaryColor(int secondaryColor) {
		mSecondaryColor = secondaryColor;
	}
}