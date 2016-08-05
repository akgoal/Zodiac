package com.example.zodiac.model;

import android.graphics.Bitmap;

public class Block {
	/*
	 * Блок, содержащий id и изображения.
	 */
	private Bitmap mImage;
	private int mImgId = -1;

	public Block() {
	}

	public Block(Bitmap image, int imgId) {
		super();
		mImage = image;
		mImgId = imgId;
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
