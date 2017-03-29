package com.deakishin.zodiac.model.ciphermodel;

import java.util.ArrayList;
import java.util.Map;

import com.deakishin.zodiac.model.ciphermanager.CipherInfo;
import com.deakishin.zodiac.model.ciphermanager.Markup;
import com.deakishin.zodiac.model.ciphermodel.bindingmanager.BindingManager;
import com.deakishin.zodiac.model.ciphermodel.search.SearchHelper;
import com.deakishin.zodiac.model.ciphermodel.search.SearchHelper2;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/** Singleton for a cipher's model. */
public class Model {

	/* Original image that was used when creating the cipher. */
	private Bitmap mOriginalImage;

	/* Marked-up image. */
	private Bitmap mModelImage;

	/* Params of the grid that is used to get symbols from the image. */
	private int mOffsetX, mOffsetY, mCellWidth, mCellHeight, mGridWidth, mGridHeight;

	/* Blocks each containing symbol's id and image. */
	private Block[][] mBlocks;

	/* Model of the symbols in the ciphers. */
	private SymbModel mSymbModel = new SymbModel();

	/* Object that manages letter-symbol mapping for decrypting process. */
	private BindingManager mBindingManager;

	/* Helper for performing searching in the cipher. */
	private SearchHelper mSearchHelper;

	private static Model sModel;

	/** @return The sole instance of the singleton. */
	public static Model getInstance() {
		return sModel;
	}

	private Model() {
	}

	/** @return List of informaion about every symbol in the cipher. */
	public ArrayList<SymbModel.SymbInfo> getSymbols() {
		return mSymbModel.getSymbInfos();
	}

	/**
	 * @return List of informaion about every symbol in the cipher. Symbols are
	 *         sorted by their frequencies from the most to the least frequent.
	 *         Symbol's frequency is defined by how many times the symbol is
	 *         encountered in the cipher.
	 */
	public ArrayList<SymbModel.SymbInfo> getSymbolsByCount() {
		return mSymbModel.getSymbInfosByCount();
	}

	/** @return Number of (different) symbols in the cipher. */
	public int getSymbolCount() {
		return getSymbols().size();
	}

	/**
	 * Returns symbol's id by coordinates x, y in the image.
	 * 
	 * @param x
	 *            X coordinate in the image.
	 * @param y
	 *            Y coordinate in the image.
	 * @return Id of the found symbol. Or -1 if the symbol is not found.
	 */
	public int getSymbIdByCoord(int x, int y) {
		if (x < 0 || y < 0) {
			return -1;
		}
		int cellX = x / mCellWidth;
		int cellY = y / mCellHeight;
		if (cellX >= mGridWidth || cellY >= mGridHeight) {
			return -1;
		}
		Block b = mBlocks[cellX][cellY];
		if (b.isEmpty())
			return -1;
		return b.getImgId();
	}

	/**
	 * Returns a rectangle that contains the area of a single symbols and
	 * coordinates x, y in the image.
	 * 
	 * @param x
	 *            X coordinate in the image.
	 * @param y
	 *            Y coordinate in the image.
	 * @return Rectangle that defines borders of the area in which a single
	 *         symbol in contained. Or null if no such area is found.
	 */
	public Rect getRectByCoord(int x, int y) {
		if (x < 0 || y < 0) {
			return null;
		}
		int cellX = x / mCellWidth;
		int cellY = y / mCellHeight;
		if (cellX >= mGridWidth || cellY >= mGridHeight) {
			return null;
		}
		int left = cellX * mCellWidth;
		int top = cellY * mCellHeight;
		return new Rect(left, top, left + mCellWidth, top + mCellHeight);
	}

	/**
	 * Returns the image of the symbol by the symbol's id.
	 * 
	 * @param symbId
	 *            Id of the symbol.
	 * @return Bitmap image of the symbol.
	 */
	public Bitmap getImageBySymbId(int symbId) {
		return mSymbModel.getSymbImageById(symbId);
	}

	/**
	 * @return True if the solution (defined by the symbol-letter mapping in the
	 *         {@link BindingManager}) is valid, i.e. every symbol is mapped to
	 *         a letter.
	 */
	public boolean isSolutionComplete() {
		return mBindingManager.getBinding().size() == getSymbolCount();
	}

	/**
	 * @return Current solution defined by the symbol-letter mapping in the
	 *         {@link BindingManager}.
	 */
	@SuppressLint("DefaultLocale")
	public String getSolutionAsString() {
		if (mBlocks == null || mBlocks.length == 0)
			return null;

		StringBuilder sb = new StringBuilder();
		Map<Integer, Character> binding = mBindingManager.getBinding();
		int len = mBlocks.length;
		int innerLen = mBlocks[0].length;
		for (int x = 0; x < innerLen; x++) {
			for (int y = 0; y < len; y++) {
				Block b = mBlocks[y][x];
				if (!b.isEmpty()) {
					if (binding.containsKey(b.getImgId()))
						sb.append(binding.get(b.getImgId()));
				}
			}
		}
		return sb.toString().toUpperCase();
	}

	/**
	 * Sets the model from the cipher's info.
	 * 
	 * @param context
	 *            Application context.
	 * @param cipher
	 *            Info of the cipher.
	 * @return True if set successfully, false otherwise.
	 */
	public static boolean setModelFromCipherInfo(Context context, CipherInfo cipher) {
		Model model = null;
		try {
			model = new Model();
			model.setOriginalImage(cipher.getOriginalImage());
			Markup markup = cipher.getMarkup();
			model.setOffsetX(markup.getOffsetX());
			model.setOffsetY(markup.getOffsetY());
			model.setCellWidth(markup.getCellWidth());
			model.setCellHeight(markup.getCellHeight());
			model.setGridWidth(markup.getGridWidth());
			model.setGridHeight(markup.getGridHeight());
			model.setBlocks(markup.getBlocks());

			model.setSearchHelper(new SearchHelper2(model.getBlocks()));
			model.createImage();

			model.setBindingManager(new BindingManager(context, cipher.getFolderName()));

			model.setSymbModel(new SymbModel());
			for (int y = 0; y < model.getGridHeight(); y++) {
				for (int x = 0; x < model.getGridWidth(); x++) {
					model.updateSymbModel(model.getBlocks()[x][y], x, y);
				}
			}
		} catch (Exception e) {
			if (model != null) {
				model.recycle();
				model = null;
			}
			return false;
		}
		if (sModel != null)
			sModel.recycle();
		sModel = model;
		return true;
	}

	/* Release resources. */
	private void recycle() {
		if (mModelImage != null) {
			mModelImage.recycle();
			mModelImage = null;
		}
	}

	/* Update symbol model with a block in a given position. */
	private void updateSymbModel(Block block, int x, int y) {
		if (!block.isEmpty())
			mSymbModel.update(block, x, y);
	}

	/* Create cipher's image. */
	private void createImage() {
		mModelImage = Bitmap.createBitmap(mGridWidth * mCellWidth, mGridHeight * mCellHeight,
				mOriginalImage.getConfig());
		Canvas canvas = new Canvas(mModelImage);
		Paint bgPaint = new Paint();
		bgPaint.setColor(Color.WHITE);
		canvas.drawRect(0, 0, mModelImage.getWidth(), mModelImage.getHeight(), bgPaint);
		Rect rect;
		for (int y = 0; y < mGridHeight; y++) {
			for (int x = 0; x < mGridWidth; x++) {
				if (!mBlocks[x][y].isEmpty()) {
					rect = new Rect(x * mCellWidth, y * mCellHeight, x * mCellWidth + mCellWidth,
							y * mCellHeight + mCellHeight);
					canvas.drawBitmap(mBlocks[x][y].getImage(), null, rect, null);
				}
			}
		}
	}

	/* Listeners to search progress. */
	private SearchProgressListener mSearchProgressListener;
	/* Search cancel indicator. */
	private SearchCancelIndicator mSearchCancelIndicator;

	/**
	 * Searches words in the cipher. Searching means looking for specific
	 * symbol-letter mappings. Each mapping maps symbols' ids to letters in such
	 * a way that replacing each symbol in the cipher with the corresponding
	 * letter makes the query words appear in the cipher.
	 * 
	 * Search can be conducted in different directions, in which case the
	 * results are compined.
	 * 
	 * @param words
	 *            Array of words, each of which has to be found.
	 * @param initBinding
	 *            Initial binding that can not be changed and only supplemented
	 *            while searching. Can be null.
	 * @param isHomophonic
	 *            Indicates if encryption is (or is suggested to be) homophonic.
	 *            If true, then mulpiple symbols can be mapped to one letter.
	 * @param searchLeft
	 *            Searching from right to left, from top to bottom.
	 * @param searchUp
	 *            Searching from bottom to top, from left to right.
	 * @param searchRight
	 *            Searching from left to right, from top to bottom.
	 * @param searchDown
	 *            Searching from top to bottom, from left to right.
	 * @param listener
	 *            Listener to search progress that is notified with every search
	 *            step.
	 * @param indicator
	 *            Search cancel indicator that can be asked if the search must
	 *            be stopped.
	 * @return List of symbol-letter mappings as a search result.
	 */
	public ArrayList<Map<Integer, Character>> searchWords(String[] words, Map<Integer, Character> initBinding,
			boolean isHomophonic, boolean searchLeft, boolean searchUp, boolean searchRight, boolean searchDown,
			SearchProgressListener listener, SearchCancelIndicator indicator) {
		if (mSearchHelper != null) {
			mSearchProgressListener = listener;
			mSearchCancelIndicator = indicator;
			ArrayList<SearchHelper.Direction> directions = new ArrayList<SearchHelper.Direction>();
			if (searchLeft) {
				directions.add(SearchHelper.Direction.LEFT);
			}
			if (searchUp) {
				directions.add(SearchHelper.Direction.UP);
			}
			if (searchRight) {
				directions.add(SearchHelper.Direction.RIGHT);
			}
			if (searchDown) {
				directions.add(SearchHelper.Direction.DOWN);
			}
			return mSearchHelper.search(words, initBinding, isHomophonic, directions,
					new SearchHelper.ProgressListener() {
						@Override
						public void onProgressChanged(int value) {
							mSearchProgressListener.onProgressChanged(value);
						}
					}, new SearchHelper.CancelIndicator() {
						@Override
						public boolean isCancelled() {
							return mSearchCancelIndicator.isCancelled();
						}
					});
		} else {
			return null;
		}
	}

	/**
	 * The listener interface for receiving notifications about search progress.
	 */
	public static interface SearchProgressListener {
		/**
		 * Invoked when search progress changes.
		 * 
		 * @param progressValue
		 *            Progress value. Minimum and maximum progress values can be
		 *            found by calling {@link Model#getMinSearchProgress()} and
		 *            {@link Model#getMaxSearchProgress()}.
		 */
		public void onProgressChanged(int progressValue);
	}

	/**
	 * Interface for indicator that can be asked if the search must be
	 * calcelled.
	 */
	public static interface SearchCancelIndicator {
		/**
		 * @return True if the search must be stopped, false otherwise.
		 */
		public boolean isCancelled();
	}

	/** @return Minimum value for the search progress. */
	public static int getMinSearchProgress() {
		return SearchHelper.MIN_PROGRESS;
	}

	/** @return Maximum value for the search progress. */
	public static int getMaxSearchProgress() {
		return SearchHelper.MAX_PROGRESS;
	}

	/** Bitmap image of the cipher. */
	public Bitmap getModelImage() {
		return mModelImage;
	}

	/**
	 * Sets cipher's image.
	 * 
	 * @param modelImage
	 *            Image to be used in the model.
	 */
	public void setModelImage(Bitmap modelImage) {
		mModelImage = modelImage;
	}

	/**
	 * @return Binding manager that manages symbol-letter mappings for
	 *         decrypting process.
	 */
	public BindingManager getBindingManager() {
		return mBindingManager;
	}

	public int getCellWidth() {
		return mCellWidth;
	}

	public int getCellHeight() {
		return mCellHeight;
	}

	public Bitmap getOriginalImage() {
		return mOriginalImage;
	}

	public int getOffsetX() {
		return mOffsetX;
	}

	public int getOffsetY() {
		return mOffsetY;
	}

	private void setOriginalImage(Bitmap originalImage) {
		mOriginalImage = originalImage;
	}

	private void setOffsetX(int offsetX) {
		mOffsetX = offsetX;
	}

	private void setOffsetY(int offsetY) {
		mOffsetY = offsetY;
	}

	private void setCellWidth(int cellWidth) {
		mCellWidth = cellWidth;
	}

	private void setCellHeight(int cellHeight) {
		mCellHeight = cellHeight;
	}

	private void setGridWidth(int gridWidth) {
		mGridWidth = gridWidth;
	}

	private void setGridHeight(int gridHeight) {
		mGridHeight = gridHeight;
	}

	private void setBlocks(Block[][] blocks) {
		mBlocks = blocks;
	}

	private void setSymbModel(SymbModel symbModel) {
		mSymbModel = symbModel;
	}

	private void setSearchHelper(SearchHelper searchHelper) {
		mSearchHelper = searchHelper;
	}

	private int getGridWidth() {
		return mGridWidth;
	}

	private int getGridHeight() {
		return mGridHeight;
	}

	private Block[][] getBlocks() {
		return mBlocks;
	}

	private void setBindingManager(BindingManager bindingManager) {
		mBindingManager = bindingManager;
	}
}
