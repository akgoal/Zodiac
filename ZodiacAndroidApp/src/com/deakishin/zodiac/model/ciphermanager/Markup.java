package com.deakishin.zodiac.model.ciphermanager;

import com.deakishin.zodiac.model.ciphermodel.Block;

/**
 * Markup that defines a cipher. Contains various parameters of the grid that is
 * used to get symbols from the original image. For symbols in the cipher the
 * markup holds a two-dimensional array {@link Block} objects the same way the
 * symbols are located in the cipher.
 */
public class Markup {

	/*
	 * Params of the grid that is "put" upon the original image to get
	 * encrypting symbols.
	 */
	private int mOffsetX, mOffsetY, mCellWidth, mCellHeight, mGridWidth, mGridHeight;

	/*
	 * Blocks each of which contains symbol's image and its id. Each block
	 * correspond to a symbol in the cipher.
	 */
	private Block[][] mBlocks;

	public Markup() {

	}

	/** Releases resources held by the object. */
	public void recycle() {
		if (mBlocks != null) {
			for (Block[] blockLine : mBlocks) {
				if (blockLine != null)
					for (Block block : blockLine) {
						if (block != null)
							block.recycle();
					}
			}
			mBlocks = null;
		}
	}

	public int getOffsetX() {
		return mOffsetX;
	}

	public void setOffsetX(int offsetX) {
		mOffsetX = offsetX;
	}

	public int getOffsetY() {
		return mOffsetY;
	}

	public void setOffsetY(int offsetY) {
		mOffsetY = offsetY;
	}

	public int getCellWidth() {
		return mCellWidth;
	}

	public void setCellWidth(int cellWidth) {
		mCellWidth = cellWidth;
	}

	public int getCellHeight() {
		return mCellHeight;
	}

	public void setCellHeight(int cellHeight) {
		mCellHeight = cellHeight;
	}

	public int getGridWidth() {
		return mGridWidth;
	}

	public void setGridWidth(int gridWidth) {
		mGridWidth = gridWidth;
	}

	public int getGridHeight() {
		return mGridHeight;
	}

	public void setGridHeight(int gridHeight) {
		mGridHeight = gridHeight;
	}

	public Block[][] getBlocks() {
		return mBlocks;
	}

	public void setBlocks(Block[][] blocks) {
		mBlocks = blocks;
	}
}