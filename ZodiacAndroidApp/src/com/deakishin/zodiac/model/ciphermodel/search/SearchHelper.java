package com.deakishin.zodiac.model.ciphermodel.search;

import java.util.ArrayList;
import java.util.Map;

import com.deakishin.zodiac.model.ciphermodel.Block;

/**
 * Abstract class for searching words in a cipher. The cipher is represented by
 * a two-dimensional array of {@link Block} objects, each block containing info
 * a symbol in it.
 */
public abstract class SearchHelper {

	/**
	 * Blocks in cipher's model. Each block contains symbol's image and its id.
	 */
	protected Block[][] blocks;
	/** Parameters of the array of blocks. */
	protected int blocksWidth, blocksHeight, blocksSize;

	/** Search direction. */
	public static enum Direction {
		LEFT, UP, RIGHT, DOWN
	}

	/** Maximum value for search progress. */
	public static final int MAX_PROGRESS = 100;
	/** Minimum value for search progress. */
	public static final int MIN_PROGRESS = 0;

	/**
	 * The listener interface for receiving callbacks when search progress
	 * changes.
	 */
	public interface ProgressListener {
		/**
		 * Invoked when search progress changes.
		 * 
		 * @param value
		 *            Progress value between {@link SearchHelper#MIN_PROGRESS}
		 *            and {@link SearchHelper#MAX_PROGRESS}.
		 */
		public void onProgressChanged(int value);
	}

	/**
	 * Interface for a search cancel indicator that can be asked if the search
	 * has to be stopped.
	 */
	public interface CancelIndicator {
		/** @return True if the search has to be stopped, false otherwise. */
		public boolean isCancelled();
	}

	/** Search progress listener. */
	protected ProgressListener progressListener;

	/**
	 * Search cancel indicator. During the search the indicator gets asked
	 * whether to stop the search or to continue it.
	 */
	protected CancelIndicator cancelIndicator;

	/* Parameters for calculating progress. */
	/** Progress every direction searched. */
	protected float progressPerDirection = 0;
	/** Progress every word searched. */
	protected float progressPerWord = 0;
	/** Progress for every search result. */
	protected float progressPerSearchResult = 0;
	/** Current progress as a float number. */
	protected float currProgress;
	/** Current progress as an int number. */
	protected int currIntegerProgress;

	/**
	 * Constructs search helper for a specific cipher.
	 * 
	 * @param blocks
	 *            Two-dimensional array of blocks that represent the cipher.
	 *            Each symbol in the cipher is represented by a block that
	 *            contains info about this symbols.
	 */
	public SearchHelper(Block[][] blocks) {
		this.blocks = blocks;
		blocksWidth = blocks.length;
		blocksHeight = blocks[0].length;
		blocksSize = blocksWidth * blocksHeight;
	}

	/**
	 * Searches words in the cipher (in the array of blocks that represent the
	 * cipher and that are passed when constructing the helper). Searching means
	 * looking for specific symbol-letter mappings. Each mapping maps symbols'
	 * ids to letters in such a way that replacing each symbol in the cipher
	 * with the corresponding letter makes the query words appear in the cipher.
	 * 
	 * Search can be conducted in different directions, in which case the
	 * results are compined.
	 * 
	 * @param words
	 *            Array of words, each of which has to be found.
	 * @param initBinding
	 *            Initial binding that can not be changed and only supplemented
	 *            while searching. In other words, the results must no
	 *            contradict the binding. Initial binding can be null if no such
	 *            restrictions are needed.
	 * @param isHomophonic
	 *            Indicates if encryption is (or is suggested to be) homophonic.
	 *            If true, then mulpiple symbols can be mapped to one letter.
	 * @param directions
	 *            List of directions in which the search must be conducted. The
	 *            search results will contain results for every direction (if
	 *            found).
	 * @param listener
	 *            Listener to search progress that is notified with every search
	 *            step.
	 * @param indicator
	 *            Search cancel indicator that can be asked if the search must
	 *            be stopped.
	 * @return List of symbol-letter mappings as a search result.
	 */
	public ArrayList<Map<Integer, Character>> search(String[] words, Map<Integer, Character> initBinding,
			boolean isHomophonic, ArrayList<Direction> directions, ProgressListener progressListener,
			CancelIndicator cancelIndicator) {
		if (words.length == 0) {
			return null;
		}
		this.progressListener = progressListener;
		this.cancelIndicator = cancelIndicator;

		return search(words, initBinding, isHomophonic, directions);
	}

	/**
	 * Searches words in the cipher (in the array of blocks that represent the
	 * cipher and that are passed when constructing the helper). Searching means
	 * looking for specific symbol-letter mappings. Each mapping maps symbols'
	 * ids to letters in such a way that replacing each symbol in the cipher
	 * with the corresponding letter makes the query words appear in the cipher.
	 * 
	 * Search can be conducted in different directions, in which case the
	 * results are compined.
	 * 
	 * @param words
	 *            Array of words, each of which has to be found.
	 * @param initBinding
	 *            Initial binding that can not be changed and only supplemented
	 *            while searching. In other words, the results must no
	 *            contradict the binding. Initial binding can be null if no such
	 *            restrictions are needed.
	 * @param isHomophonic
	 *            Indicates if encryption is (or is suggested to be) homophonic.
	 *            If true, then mulpiple symbols can be mapped to one letter.
	 * @param directions
	 *            List of directions in which the search must be conducted. The
	 *            search results will contain results for every direction (if
	 *            found).
	 * @return List of symbol-letter mappings as a search result.
	 */
	protected abstract ArrayList<Map<Integer, Character>> search(String[] words, Map<Integer, Character> initBinding,
			boolean isHomophonic, ArrayList<Direction> directions);

	/**
	 * Updates search progress and notifies listener about its change.
	 * 
	 * @param value
	 *            New progress value.
	 */
	protected void updateProgress(float value) {
		currProgress = value;
		notifyProgressListener();
	}

	/**
	 * Increases search progress and notifies listener about its change.
	 * 
	 * @param value
	 *            Value to increase current value by.
	 */
	protected void increaseProgress(float value) {
		currProgress += value;
		notifyProgressListener();
	}

	/*
	 * Notify listener that progress has changed. Notify only if int value of
	 * the progress has changed.
	 */
	private void notifyProgressListener() {
		if (currIntegerProgress != (int) currProgress) {
			currIntegerProgress = (int) currProgress;
			progressListener.onProgressChanged(currIntegerProgress);
		}
	}

	/** @return True if the search has to be stopped, false otherwise. */
	protected boolean isCancelled() {
		if (cancelIndicator == null)
			return false;
		return cancelIndicator.isCancelled();
	}

	/**
	 * Returns id of the block in the given position from the start. Position is
	 * being counted in the given direction.
	 * 
	 * @param pos
	 *            Position of the block starting from 0.
	 * @param direction
	 *            Direction in which conduct the counting.
	 * @return Id of the found block.
	 */
	protected int getBlockId(int pos, Direction direction) {
		int posX = 0, posY = 0;
		switch (direction) {
		case LEFT:
			posY = pos / blocksWidth;
			posX = (blocksWidth - 1) - (pos - posY * blocksWidth);
			break;
		case UP:
			posX = pos / blocksHeight;
			posY = (blocksHeight - 1) - (pos - posX * blocksHeight);
			break;
		case RIGHT:
			posY = pos / blocksWidth;
			posX = pos - posY * blocksWidth;
			break;
		case DOWN:
			posX = pos / blocksHeight;
			posY = pos - posX * blocksHeight;
			break;
		}
		return blocks[posX][posY].getImgId();
	}
}
