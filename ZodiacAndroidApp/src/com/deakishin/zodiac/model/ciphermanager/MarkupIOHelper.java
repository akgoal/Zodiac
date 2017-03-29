package com.deakishin.zodiac.model.ciphermanager;

import java.io.BufferedReader;
import java.io.IOException;

import com.deakishin.zodiac.model.ciphermodel.Block;

import android.graphics.Bitmap;

/** Helper class for reading/writing markups. */
public class MarkupIOHelper {

	/* Constants for reading and writing info. */
	/** Indicator of a beginning of a comment. */
	public static final String COMMENT_BEGIN = "/*";
	/** Indicator of an end of a comment. */
	public static final String COMMENT_END = "*/";
	/** Delimiter between grid parameters. */
	public static final String GRID_PARAMS_DELIMITER = ",";
	/** Delimiter between symbols' positions. */
	public static final String POSITIONS_DELIMITER = "-";
	/**
	 * Delimiter between a block with grid parameters and a block with symbols'
	 * positions.
	 */
	public static final String GRID_PARAMS_VS_POSITIONS_DELIMITER = ";";

	public MarkupIOHelper() {
	}

	/**
	 * Reads markup from a buffered reader.
	 * 
	 * @param BufferedReader
	 *            Reader to get info from.
	 * @param originalImage
	 *            Original bitmap image that was used when creating the cipher.
	 * @return Markup constructed from provided data.
	 * @throws IOException
	 *             If data in the reader has incorrect format.
	 */
	public Markup readMarkup(BufferedReader br, Bitmap originalImage) throws IOException {
		String gridLine = readNonComm(br);
		String posLine = readNonComm(br);
		return readMarkupFromLines(gridLine, posLine, originalImage);
	}

	/**
	 * Reads markup from a String.
	 * 
	 * @param line
	 *            String to read info from.
	 * @param originalImage
	 *            Original bitmap image that was used when creating the cipher.
	 * @return Markup constructed from provided data.
	 * @throws IOException
	 *             If data in the line has incorrect format.
	 */
	public Markup readMarkupFromString(String line, Bitmap originalImage) throws IOException {
		String[] parts = line.split(GRID_PARAMS_VS_POSITIONS_DELIMITER);
		return readMarkupFromLines(parts[0], parts[1], originalImage);
	}

	/* Read markup from separated lines. */
	private Markup readMarkupFromLines(String gridParamsLine, String positionsLine, Bitmap originalImage)
			throws IOException {
		Markup markup = new Markup();

		String[] gridParams = gridParamsLine.split(GRID_PARAMS_DELIMITER);

		int offsetX = Integer.parseInt(gridParams[0]);
		int offsetY = Integer.parseInt(gridParams[1]);
		int cellWidth = Integer.parseInt(gridParams[2]);
		int cellHeight = Integer.parseInt(gridParams[3]);
		int origGridWidth = Integer.parseInt(gridParams[4]);
		int origGridHeight = Integer.parseInt(gridParams[5]);
		int gridWidth, gridHeight;
		if (gridParams.length > 6) {
			gridWidth = Integer.parseInt(gridParams[6]);
			gridHeight = Integer.parseInt(gridParams[7]);
		} else {
			gridWidth = origGridWidth;
			gridHeight = origGridHeight;
		}

		String[] markedPositions = positionsLine.split(POSITIONS_DELIMITER);
		int len = markedPositions.length;

		Block[][] blocks = new Block[gridWidth][gridHeight];
		Block block;
		int pos, posX, posY;
		int i = 0;
		for (int y = 0; y < gridHeight; y++) {
			for (int x = 0; x < gridWidth; x++, i++) {
				if (i < len) {
					pos = Integer.parseInt(markedPositions[i]);
					block = new Block();
					block.setImgId(pos);
					posY = pos / origGridWidth;
					posX = pos - posY * origGridWidth;
					block.setImage(getSubimage(originalImage, offsetX + posX * cellWidth, offsetY + posY * cellHeight,
							cellWidth, cellHeight));
					blocks[x][y] = block;
				} else {
					blocks[x][y] = Block.EMPTY_BLOCK;
				}
			}
		}

		markup.setOffsetX(offsetX);
		markup.setOffsetY(offsetY);
		markup.setCellWidth(cellWidth);
		markup.setCellHeight(cellHeight);
		markup.setGridWidth(gridWidth);
		markup.setGridHeight(gridHeight);
		markup.setBlocks(blocks);
		return markup;
	}

	/* Get an area with specified location and size from the image. */
	private Bitmap getSubimage(Bitmap image, int x, int y, int w, int h) {
		return Bitmap.createBitmap(image, x, y, w, h);
	}

	/* Read next line that is not a part of a comment. */
	private String readNonComm(BufferedReader br) throws IOException {
		String line = br.readLine();
		if (line == null) {
			return null;
		}
		if (!line.equals(COMMENT_BEGIN)) {
			return line;
		} else {
			line = br.readLine();
			if (line == null) {
				return null;
			}
			while (!line.equals(COMMENT_END)) {
				line = br.readLine();
				if (line == null) {
					return null;
				}
			}
			return br.readLine();
		}
	}

	/**
	 * Convert info of a markup to its String representation.
	 * 
	 * @param gridParam
	 *            Grid parameters.
	 * @param blockIds
	 *            Array of block ids.
	 * @return Constructed String that contains info about the markup and can be
	 *         then used to read the markup back.
	 */
	public String convertToString(int[] gridParams, int[] blockIds) {
		StringBuilder sb = new StringBuilder();
		for (int param : gridParams) {
			sb.append(param + GRID_PARAMS_DELIMITER);
		}
		sb.append(GRID_PARAMS_VS_POSITIONS_DELIMITER);
		for (int blockId : blockIds) {
			sb.append(blockId + POSITIONS_DELIMITER);
		}
		return sb.toString();
	}

	/*
	 * Перевод строки в данные разметки. Выбрасывается исключение если строка не
	 * правильная.
	 */
	/**
	 * Converts String line to markup's info.
	 * 
	 * @param line
	 *            Source String that must be properly formatted.
	 * @return Markup's data in two arrays. Data[][0] is an array of grid
	 *         parameters, data[][1] is an array of symbols' positions.
	 * @throws Exception
	 *             If the line has incorrect format.
	 */
	public int[][] convertFromStringToArray(String line) throws Exception {
		String[] parts = line.split(GRID_PARAMS_VS_POSITIONS_DELIMITER);
		String[] gridString = parts[0].split(GRID_PARAMS_DELIMITER);
		int[] gridParams = new int[8];
		for (int i = 0; i < 6; i++)
			gridParams[i] = Integer.parseInt(gridString[i]);
		int gridWidth, gridHeight;
		if (gridParams.length > 6) {
			gridParams[6] = Integer.parseInt(gridString[6]);
			gridParams[7] = Integer.parseInt(gridString[7]);
		} else {
			gridParams[6] = gridParams[4];
			gridParams[7] = gridParams[5];
		}

		String[] markedPositionsString = parts[1].split(POSITIONS_DELIMITER);
		int[] markedPositions = new int[markedPositionsString.length];
		for (int i = 0; i < markedPositionsString.length; i++) {
			markedPositions[i] = Integer.parseInt(markedPositionsString[i]);
		}
		int[][] res = { gridParams, markedPositions };
		return res;
	}
}
