package com.deakishin.zodiac.model.ciphermodel.bindingmanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import android.content.Context;
import android.util.Log;

/**
 * Binding manager that manages symbol-letter mapping for decoding process. Also
 * contains search results and checkpoints.
 * 
 * Each binding is a mapping between symbol ids and letters. So each such
 * mapping is a decryption of the cipher.
 */
public class BindingManager {

	/* Current binding. */
	private Map<Integer, Character> mBinding = new HashMap<Integer, Character>();

	/* Remembered binding. It is the last checkpoint. */
	private Map<Integer, Character> mRememberedBinding;

	/* List of checkpoints - remembered bindings with titles. */
	private ArrayList<CheckPoint> mCheckPoints = new ArrayList<CheckPoint>();

	/* Bindings - search results. */
	private ArrayList<Map<Integer, Character>> mSearchResults;
	/* Current index in search results. */
	private int mCurrSearchIndex;

	/* Search query for which search results are stored. */
	private String mSearchLine;
	/*
	 * Temporal search query. Search query is stored here until search is
	 * completed.
	 */
	private String mTmpSearchLine;

	/* Application context. */
	private Context mContext;

	/* Helper for saving/loading checkpoints. */
	private BindingManagerStorageHelper mCheckpointStorageHelper;

	/* Checkpoint counter for their naming. */
	private int mCheckpointCount = 1;

	/*
	 * Flag indicating that current binding is a checkpoint, i.e. no changes are
	 * made after the checkpoint.
	 */
	private boolean mBindingIsCheckpoint = false;

	/*
	 * Listener to current binding status changes (status is whether the binding
	 * is a checkpoint or not).
	 */
	private OnBindingStatusChangedListener mOnBindingStatusChangedListener;

	/*
	 * Listener to revert option availability changes (revert option is an
	 * option to go back to the last checkpoint).
	 */
	private RevertAvailabilityListener mRevertAvailabilityListener;

	/**
	 * Constructs Binding manager.
	 * 
	 * @param context
	 *            Application context.
	 * @param checkpointsFolderName
	 *            Name of the folder whether files containin checkpoints info
	 *            must be stored.
	 */
	public BindingManager(Context context, String checkpointsFolderName) {
		mContext = context;
		mCheckpointStorageHelper = new BindingManagerStorageHelper(mContext, checkpointsFolderName);

		loadData();
	}

	/**
	 * Updates mapping for a specific symbol.
	 * 
	 * @param id
	 *            Id of the symbol that must be mapped to new letter.
	 * @param chr
	 *            New letter to be mapped to the symbol.
	 */
	public void updateBinding(Integer id, Character chr) {
		if (chr == null) {
			if (mBinding.containsKey(id)) {
				mBinding.remove(id);
				setBindingIsCheckpoint(false);
			}
			return;
		}
		if (mBinding.containsKey(id)) {
			if (!mBinding.get(id).equals(chr)) {
				setBindingIsCheckpoint(false);
			}
		} else {
			setBindingIsCheckpoint(false);
		}
		mBinding.put(id, chr);
	}

	/**
	 * Returns letter mapped to a specific symbol.
	 * 
	 * @param id
	 *            Id of the symbol.
	 * @return Letter mapped to the symbol.
	 */
	public Character getCharacter(int id) {
		if (mBinding.containsKey(id))
			return mBinding.get(id);
		return null;
	}

	/* Remember given binding. The given binding becomes remembered. */
	private void rememberBinding(Map<Integer, Character> binding) {
		mRememberedBinding = new HashMap<Integer, Character>(binding);
		setBindingIsCheckpoint(true);

		if (mRevertAvailabilityListener != null)
			mRevertAvailabilityListener.onRevertAvailable();
	}

	/* Revert to remembered binding. Remembered binding becomes current. */
	private void retrieveRememberedBinding() {
		if (mRememberedBinding == null)
			return;

		mBinding = new HashMap<Integer, Character>(mRememberedBinding);
		setBindingIsCheckpoint(true);
	}

	/** Clears current binding. All the mapping gets erased. */
	public void clearBinding() {
		if (!mBinding.isEmpty()) {
			mBinding.clear();
			setBindingIsCheckpoint(false);
		}
	}

	/**
	 * @return Current binding as a mapping between symbols' ids and letters.
	 */
	public Map<Integer, Character> getBinding() {
		return mBinding;
	}

	/**
	 * @return Remembered binding as a mapping between symbols' ids and letters.
	 */
	public Map<Integer, Character> getRememberedBinding() {
		return mRememberedBinding;
	}

	/** @return True if there is a remembered binding, false otherwise. */
	public boolean hasRememberedBinding() {
		return mRememberedBinding != null;
	}

	/**
	 * Sets current binding by copying the given one. Future changes in the
	 * given binding won't affect current binding.
	 * 
	 * @param binding
	 *            Binding to set as current.
	 */
	public void setBindingAsCopy(Map<Integer, Character> binding) {
		mBinding = new HashMap<Integer, Character>(binding);
		setBindingIsCheckpoint(false);
	}

	/**
	 * @return True if there is search info (search results), false otherwise.
	 */
	public boolean hasSearchInfo() {
		return mSearchResults != null;
	}

	/**
	 * @return True if there are search results (if the search has given
	 *         results), false otherwise.
	 */
	public boolean hasSearchResults() {
		if (mSearchResults == null)
			return false;
		return !mSearchResults.isEmpty();
	}

	/**
	 * Sets search results. Current search results index resets to 0. Search
	 * query that was held as temporary gets remembered.
	 * 
	 * @param searchResults
	 *            List of binding - mappings between symbols' ids and letters.
	 */
	public void setSearchResults(ArrayList<Map<Integer, Character>> searchResults) {
		mSearchResults = searchResults;
		mCurrSearchIndex = 0;
		mSearchLine = mTmpSearchLine;
	}

	/** Deletes search results and search info. */
	public void deleteSearchResults() {
		mSearchResults = null;
		mSearchLine = null;
	}

	/** @return Number of search results. */
	public int getSearchResSize() {
		if (mSearchResults != null)
			return mSearchResults.size();
		return 0;
	}

	/**
	 * Sets current binding from the first search result (if there are any
	 * results).
	 */
	public void goToFirstSearchRes() {
		mCurrSearchIndex = 0;
		if (mSearchResults != null && !mSearchResults.isEmpty())
			setBindingAsCopy(mSearchResults.get(0));
	}

	/** Sets current binding to the previous search result. */
	public void goToPrevSearchRes() {
		if (!isFirstRes()) {
			mCurrSearchIndex--;
			if (mSearchResults != null && !mSearchResults.isEmpty())
				setBindingAsCopy(mSearchResults.get(mCurrSearchIndex));
		}
	}

	/** Sets current binding to the next search result. */
	public void goToNextSearchRes() {
		if (!isLastRes()) {
			mCurrSearchIndex++;
			setBindingAsCopy(mSearchResults.get(mCurrSearchIndex));
		}
	}

	/**
	 * @return True if current binding is at the first search result, false
	 *         otherwise.
	 */
	public boolean isFirstRes() {
		return mCurrSearchIndex == 0;
	}

	/**
	 * @return True if current binding is at the last search result, false
	 *         otherwise.
	 */
	public boolean isLastRes() {
		if (mSearchResults == null || mSearchResults.isEmpty())
			return true;
		if (mCurrSearchIndex >= mSearchResults.size() - 1)
			return true;
		return false;
	}

	public int getCurrSearchIndex() {
		return mCurrSearchIndex;
	}

	/**
	 * Sets search query words. The words are put to the temporary line until
	 * the search is completed.
	 * 
	 * @param words
	 *            Array of words in the search query.
	 */
	public void setTmpSearchWords(String[] words) {
		if (words.length < 1)
			return;
		StringBuilder sb = new StringBuilder();
		int i;
		for (i = 0; i < words.length - 1; i++)
			sb.append(words[i] + " ");
		sb.append(words[i]);
		mTmpSearchLine = sb.toString();
	}

	/** @return Search line that contains the query words. */
	public String getSearchLine() {
		if (mSearchLine == null)
			return "";
		return mSearchLine;
	}

	/* Following methods are checkpoints related. */

	/**
	 * @return List of checkpoints from new to old ones. So the first checkpoint
	 *         is the most recent one.
	 */
	public ArrayList<CheckPoint> getCheckPoints() {
		return mCheckPoints;
	}

	/** @return Number of checkpoints. */
	public int getCheckpointsSize() {
		return mCheckPoints.size();
	}

	/**
	 * Adds current binding to the checkpoints.
	 * 
	 * @param title
	 *            Title for the new checkpoint.
	 */
	public void addCurrToCheckPoints(String title) {
		if (mBindingIsCheckpoint)
			return;

		addCheckPoint(title, mBinding);
		rememberBinding(mBinding);
	}

	/**
	 * Adds given checkpoint to the begining of the checkpoints list.
	 * 
	 * @param checkpoint
	 *            Checkpoint to add.
	 */
	public void addCheckPoint(CheckPoint checkpoint) {
		mCheckpointCount++;
		mCheckPoints.add(0, checkpoint);
		saveData();
	}

	/**
	 * Creates new checkpoint with given title and binding and adds it to the
	 * beginning of the checkpoints list.
	 * 
	 * @param title
	 *            Checkpoint's title.
	 * @param binding
	 *            Checkpoint's binding.
	 */
	public void addCheckPoint(String title, Map<Integer, Character> binding) {
		addCheckPoint(new CheckPoint(binding, title));
	}

	/** @return Number of checkpoints. */
	public int getCheckpointCount() {
		return mCheckpointCount;
	}

	/**
	 * Sets current binding from a checkpoint with given title.
	 * 
	 * @param title
	 *            Checkpoint's title.
	 */
	public void setCurrCheckpoint(String title) {
		if (mCheckPoints == null || title == null)
			return;
		for (CheckPoint checkpoint : mCheckPoints) {
			if (checkpoint.getTitle().equals(title)) {
				setCurrBindingFromCheckPoint(checkpoint);
				return;
			}
		}
	}

	/**
	 * Sets current binding from a checkpoint.
	 * 
	 * @param checkPoint
	 *            Checkpoint to get binding from.
	 */
	public void setCurrBindingFromCheckPoint(CheckPoint checkPoint) {
		setBindingAsCopy(checkPoint.getBinding());
		rememberBinding(checkPoint.getBinding());
	}

	/**
	 * Deletes checkpoint from the list of checkpoints.
	 * 
	 * @param checkPoint
	 *            Checkpoint to delete.
	 */
	public void removeCheckPoint(CheckPoint checkPoint) {
		mCheckPoints.remove(checkPoint);
	}

	/** Sets current binding from the last checkpoint. */
	public void revertToCheckPoint() {
		retrieveRememberedBinding();
	}

	/** Saves data to the file system so it can be restored later. */
	public void saveData() {
		try {
			BindingManagerStorageHelper.StorageUnit storageUnit = new BindingManagerStorageHelper.StorageUnit();
			storageUnit.setCheckpoints(mCheckPoints);
			storageUnit.setCheckpointCount(mCheckpointCount);

			storageUnit.setСurrBinding(mBinding);
			storageUnit.setRememberedBinding(mRememberedBinding);
			storageUnit.setCheckpoint(mBindingIsCheckpoint);

			mCheckpointStorageHelper.saveCheckpoints(storageUnit);
		} catch (JSONException e) {
			Log.e("BindingManager", "Failed to save checkpoints " + e);
		} catch (IOException e) {
			Log.e("BindingManager", "Failed to save checkpoints " + e);
		}
	}

	/** Loads data from the file system. */
	public void loadData() {
		try {
			BindingManagerStorageHelper.StorageUnit storageUnit = mCheckpointStorageHelper.loadCheckpoints();
			mCheckPoints = storageUnit.getCheckpoints();
			mCheckpointCount = storageUnit.getCheckpointCount();

			mBindingIsCheckpoint = storageUnit.isCheckpoint();
			if (storageUnit.getСurrBinding() != null) {
				mBinding = storageUnit.getСurrBinding();
			}
			if (storageUnit.getRememberedBinding() != null) {
				mRememberedBinding = storageUnit.getRememberedBinding();
			}
		} catch (JSONException e) {
			Log.e("BindingManager", "Failed to load checkpoints: " + e);
			mCheckPoints = new ArrayList<CheckPoint>();
		} catch (IOException e) {
			Log.e("BindingManager", "Failed to load checkpoints: " + e);
			mCheckPoints = new ArrayList<CheckPoint>();
		}
	}

	/**
	 * The callback interface for receiving a callback when checkpoints are
	 * loaded.
	 */
	public static interface CheckpointsLoadCallback {
		/** Invoked when checkpoints are loaded. */
		public void action();
	}

	/**
	 * @return True if current binding is a checkpoint, i.e. no changes were
	 *         made since last checkpoint. False otherwise.
	 */
	public boolean isCurrBindingCheckpoint() {
		return mBindingIsCheckpoint;
	}

	/* Set flag indicating that current binding is a checkpoint. */
	private void setBindingIsCheckpoint(boolean bindingIsCheckpoint) {
		mBindingIsCheckpoint = bindingIsCheckpoint;
		if (mOnBindingStatusChangedListener != null)
			mOnBindingStatusChangedListener.onBindingStatusChanged(mBindingIsCheckpoint);
	}

	/**
	 * @return True if if there is an option to go back to the last checkpoint.
	 */
	public boolean isRevertAvailable() {
		return mRememberedBinding != null;
	}

	/**
	 * The listener interface receiving callbacks when status of the current
	 * binding changes. The status means whether current binding is a checkpoint
	 * or not.
	 */
	public static interface OnBindingStatusChangedListener {
		/**
		 * Invoked when current binding becomes or stops being a checkpoint. It
		 * stops being a checkpoint when a change is made.
		 * 
		 * @param isCheckpoint
		 *            True if current binding became a checkpoint, false if if
		 *            stops being a checkpoint.
		 */
		public void onBindingStatusChanged(boolean isCheckpoint);
	}

	/**
	 * Sets listener to current binding status being changed.
	 * 
	 * @param onBindingStatusChangedListener
	 *            Listener to set.
	 */
	public void setOnBindingStatusChangedListener(OnBindingStatusChangedListener onBindingStatusChangedListener) {
		mOnBindingStatusChangedListener = onBindingStatusChangedListener;
	}

	/**
	 * The listener interface for receiving callbacks when the revert option becomes
	 * available. The revert option is an option to go back to the last checkpoint.
	 */
	public static interface RevertAvailabilityListener {
		/** Invoked when the revert option becomes available. */
		public void onRevertAvailable();
	}

	/**
	 * Sets listener to the revert option becoming available.
	 * 
	 * @param revertAvailabilityListener
	 *            Listener to set.
	 */
	public void setRevertAvailabilityListener(RevertAvailabilityListener revertAvailabilityListener) {
		mRevertAvailabilityListener = revertAvailabilityListener;
	}
}
