package com.deakishin.zodiac.model.ciphermodel;

import android.graphics.Bitmap;

/** Block that holds symbol's id and its image. */
public class Block {

	/** Empty block that has no image. */
	public static final Block EMPTY_BLOCK = new Block(null, -1);

	private Bitmap mImage;
	private int mImgId = -1;

	public Block() {
	}

	/**
	 * Constructs a block.
	 * 
	 * @param image
	 *            Symbol's image that will be held in the block.
	 * @param imgId
	 *            Symbol's id.
	 */
	public Block(Bitmap image, int imgId) {
		super();
		mImage = image;
		mImgId = imgId;
	}

	/** @return True if the block is empty, i.e. it has no symbol's image. */
	public boolean isEmpty() {
		return mImgId == -1;
	}

	/** Releases resources held by the object. */
	public void recycle() {
		if (mImage != null) {
			mImage.recycle();
			mImage = null;
		}
	}

	public Bitmap getImage() {
		return mImage;
	}

	public void setImage(Bitmap image) {
		mImage = image;
	}

	public int getImgId() {
		return mImgId;
	}

	public void setImgId(int imgId) {
		mImgId = imgId;
	}

}
