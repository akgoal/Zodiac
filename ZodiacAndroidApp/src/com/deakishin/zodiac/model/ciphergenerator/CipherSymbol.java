package com.deakishin.zodiac.model.ciphergenerator;

import android.graphics.Bitmap;

/** Cipher's encrypting symbol. Contains its id and its image. */
class CipherSymbol {

	/* Symbol's image. */
	private Bitmap mImage;
	/* Symbol's id. */
	private int mId;

	public CipherSymbol() {

	}

	public Bitmap getImage() {
		return mImage;
	}

	public void setImage(Bitmap image) {
		mImage = image;
	}

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		mId = id;
	}
}