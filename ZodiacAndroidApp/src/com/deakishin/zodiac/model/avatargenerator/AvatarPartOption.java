package com.deakishin.zodiac.model.avatargenerator;

import android.graphics.Bitmap;

/** Specific option for an avatar part. Has id and an image. */
public class AvatarPartOption {

	/* Id. */
	private int mId;

	/* Image. */
	private Bitmap mBitmap;

	/**
	 * Contructs a specific option for a part.
	 * 
	 * @param id
	 *            Option's id.
	 * @param bitmap
	 *            Bitmap image of the option.
	 */
	public AvatarPartOption(int id, Bitmap bitmap) {
		super();
		mId = id;
		mBitmap = bitmap;
	}

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		mId = id;
	}

	public Bitmap getBitmap() {
		return mBitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		mBitmap = bitmap;
	}
}
