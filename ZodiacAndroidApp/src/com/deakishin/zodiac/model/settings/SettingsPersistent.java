package com.deakishin.zodiac.model.settings;

import java.util.ArrayList;

import com.deakishin.zodiac.services.boardservice.BoardServiceI;
import com.deakishin.zodiac.services.userservice.User;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Singleton for application settings. These settings are stored in the memory
 * and don't change between launches of the app.
 */
public class SettingsPersistent {

	/* Application context. */
	private Context mContext;

	/* Storage for storing settings. */
	private SharedPreferences mSharedPreferences;

	/* Keys for storing settings. */
	private static final String PREF_SEARCH_WARNING_DONT_SHOW = "warningDontShow";
	private static final String PREF_FONT_COLOR_PROFILE_ID = "fontColorProfileid";
	private static final String PREF_CHECKPOINT_NAME_OPTION_CODE = "checkpointNameOption";
	private static final String PREF_AUTOSAVE_ENABLED = "autosaveEnabled";
	private static final String PREF_BOARD_SORT_SORTBY_OPTION = "boardSortSortByOption";
	private static final String PREF_BOARD_SORT_SHOW_SOLVED_OPTION = "boardSortShowSolvedOption";
	private static final String PREF_BOARD_SORT_HIDE_IMPORTED = "boardSortHideImported";
	private static final String PREF_ACCOUNT_NAME = "accountName";
	private static final String PREF_LAST_CIPHER_ID = "lastCipherId";
	private static final String PREF_USER_ID = "userId";
	private static final String PREF_USER_NAME = "userName";
	private static final String PREF_USER_AVATAR_MARKUP = "userAvatarMarkup";

	/* Default settings values. */
	private static final boolean DEFAULT_SEARCH_WARNING_DOWN_SHOW = false;

	/* Don't show warning when searching. */
	private boolean mSearchWarningDontShow;
	/* Color profile for the replacing letters when decrypting. */
	private FontColorProfile mFontColorProfile;
	/* Option for checkpoint naming. */
	private CheckpointNameOption mCheckpointNameOption;
	/* Is autosave enabled. */
	private boolean mAutosaveEnabled;
	/* Settings for sorting and filtering the board. */
	private BoardServiceI.SortByOption mBoardSortSortByOption;
	private BoardServiceI.ShowSolvedOption mBoardSortShowSolvedOption;
	private boolean mBoardSortHideImported;
	/* User's account. */
	private String mAccountName = null;
	/* Id of the last loaded cipher. */
	private long mLastCipherId;
	/* Logged-in user. */
	private User mUser;

	/* Listeners to changes of color settings. */
	private ArrayList<OnFontColorProfileChangeListener> mOnFontColorProfileChangeListeners = new ArrayList<OnFontColorProfileChangeListener>();

	private static SettingsPersistent mSettingsPersistent;

	/**
	 * @param context
	 *            Application context. Needed to store settings in the memory.
	 * @return The sole instance of the singleton to work with settings.
	 */
	public static SettingsPersistent getInstance(Context context) {
		if (mSettingsPersistent == null)
			mSettingsPersistent = new SettingsPersistent(context.getApplicationContext());
		return mSettingsPersistent;
	}

	private SettingsPersistent(Context context) {
		mContext = context;
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

		// Restore settings.

		mSearchWarningDontShow = mSharedPreferences.getBoolean(PREF_SEARCH_WARNING_DONT_SHOW,
				DEFAULT_SEARCH_WARNING_DOWN_SHOW);
		mFontColorProfile = FontColorProfiles.getProfile(mSharedPreferences.getInt(PREF_FONT_COLOR_PROFILE_ID, -1));
		if (mFontColorProfile == null)
			setFontColorProfile(FontColorProfiles.getDefaultProfile());

		mCheckpointNameOption = CheckpointNameOptions
				.getOption(mSharedPreferences.getInt(PREF_CHECKPOINT_NAME_OPTION_CODE, -1));
		if (mCheckpointNameOption == null)
			setCheckpointNameOption(CheckpointNameOptions.getDefaultOption());

		mAutosaveEnabled = mSharedPreferences.getBoolean(PREF_AUTOSAVE_ENABLED, true);

		int boardSortSortByOptionOrdinal = mSharedPreferences.getInt(PREF_BOARD_SORT_SORTBY_OPTION,
				BoardServiceI.DEFAULT_SORT_OPTION.ordinal());
		BoardServiceI.SortByOption[] sortSortByOptionValues = BoardServiceI.SortByOption.values();
		if (boardSortSortByOptionOrdinal >= sortSortByOptionValues.length || boardSortSortByOptionOrdinal < 0) {
			mBoardSortSortByOption = BoardServiceI.DEFAULT_SORT_OPTION;
		} else {
			mBoardSortSortByOption = sortSortByOptionValues[boardSortSortByOptionOrdinal];
		}
		int boardSortShowSolvedOptionOrdinal = mSharedPreferences.getInt(PREF_BOARD_SORT_SHOW_SOLVED_OPTION,
				BoardServiceI.DEFAULT_SHOW_SOLVED_OPTION.ordinal());
		BoardServiceI.ShowSolvedOption[] sortShowSolvedOptionValues = BoardServiceI.ShowSolvedOption.values();
		if (boardSortShowSolvedOptionOrdinal >= sortShowSolvedOptionValues.length
				|| boardSortShowSolvedOptionOrdinal < 0) {
			mBoardSortShowSolvedOption = BoardServiceI.DEFAULT_SHOW_SOLVED_OPTION;
		} else {
			mBoardSortShowSolvedOption = sortShowSolvedOptionValues[boardSortShowSolvedOptionOrdinal];
		}
		mBoardSortHideImported = mSharedPreferences.getBoolean(PREF_BOARD_SORT_HIDE_IMPORTED, false);

		mAccountName = mSharedPreferences.getString(PREF_ACCOUNT_NAME, null);

		mLastCipherId = mSharedPreferences.getLong(PREF_LAST_CIPHER_ID, 0);

		long userId = mSharedPreferences.getLong(PREF_USER_ID, -1);
		String userName = mSharedPreferences.getString(PREF_USER_NAME, null);
		String userAvatarMarkup = mSharedPreferences.getString(PREF_USER_AVATAR_MARKUP, null);
		if (userId >= 0 && userName != null)
			mUser = new User(userId, userName, userAvatarMarkup);
	}

	public boolean isSearchWarningDontShow() {
		return mSearchWarningDontShow;
	}

	public void setSearchWarningDontShow(boolean searchWarningDontShow) {
		mSearchWarningDontShow = searchWarningDontShow;
		mSharedPreferences.edit().putBoolean(PREF_SEARCH_WARNING_DONT_SHOW, mSearchWarningDontShow).commit();
	}

	public FontColorProfile getFontColorProfile() {
		return mFontColorProfile;
	}

	public void setFontColorProfile(FontColorProfile fontColorProfile) {
		if (fontColorProfile == null)
			return;
		mFontColorProfile = fontColorProfile;
		mSharedPreferences.edit().putInt(PREF_FONT_COLOR_PROFILE_ID, mFontColorProfile.getId()).commit();
		for (OnFontColorProfileChangeListener listener : mOnFontColorProfileChangeListeners) {
			listener.onFontColorProfileChanged(mFontColorProfile);
		}
	}

	/**
	 * Adds a listener that listens to changes of color settings.
	 * 
	 * @param onFontColorProfileChangeListener
	 *            Listener to add.
	 */
	public void addOnFontColorProfileChangeListener(OnFontColorProfileChangeListener onFontColorProfileChangeListener) {
		mOnFontColorProfileChangeListeners.add(onFontColorProfileChangeListener);
	}

	/**
	 * Remove a listener that listens to changes of color settings.
	 * 
	 * @param onFontColorProfileChangeListener
	 *            Listener to remove.
	 */
	public void removeOnFontColorProfileChangeListener(
			OnFontColorProfileChangeListener onFontColorProfileChangeListener) {
		mOnFontColorProfileChangeListeners.remove(onFontColorProfileChangeListener);
	}

	/**
	 * The listener interface for receiving callbacks when color settings are
	 * changed.
	 */
	public static interface OnFontColorProfileChangeListener {
		/**
		 * Invoked when color settings are changed.
		 * 
		 * @param fontColorProfile
		 *            New color profile.
		 */
		public void onFontColorProfileChanged(FontColorProfile fontColorProfile);
	}

	public CheckpointNameOption getCheckpointNameOption() {
		return mCheckpointNameOption;
	}

	public void setCheckpointNameOption(CheckpointNameOption checkpointNameOption) {
		if (checkpointNameOption == null)
			return;
		mCheckpointNameOption = checkpointNameOption;
		mSharedPreferences.edit().putInt(PREF_CHECKPOINT_NAME_OPTION_CODE, mCheckpointNameOption.getCode()).commit();
	}

	public boolean isAutosaveEnabled() {
		return mAutosaveEnabled;
	}

	public void setAutosaveEnabled(boolean autosaveEnabled) {
		mAutosaveEnabled = autosaveEnabled;
		mSharedPreferences.edit().putBoolean(PREF_AUTOSAVE_ENABLED, autosaveEnabled).commit();
	}

	/** Switches Autosave option. */
	public void switchAutosaveEnabled() {
		setAutosaveEnabled(!mAutosaveEnabled);
	}

	public BoardServiceI.SortByOption getBoardSortSortByOption() {
		return mBoardSortSortByOption;
	}

	public void setBoardSortSortByOption(BoardServiceI.SortByOption boardSortSortByOption) {
		if (boardSortSortByOption == null)
			return;
		mBoardSortSortByOption = boardSortSortByOption;
		mSharedPreferences.edit().putInt(PREF_BOARD_SORT_SORTBY_OPTION, mBoardSortSortByOption.ordinal()).commit();
	}

	public boolean isBoardSortHideImported() {
		return mBoardSortHideImported;
	}

	public void setBoardSortHideImported(boolean boardSortHideImported) {
		mBoardSortHideImported = boardSortHideImported;
		mSharedPreferences.edit().putBoolean(PREF_BOARD_SORT_HIDE_IMPORTED, mBoardSortHideImported).commit();
	}

	public BoardServiceI.ShowSolvedOption getBoardSortShowSolvedOption() {
		return mBoardSortShowSolvedOption;
	}

	public void setBoardSortShowSolvedOption(BoardServiceI.ShowSolvedOption boardSortShowSolvedOption) {
		if (boardSortShowSolvedOption == null)
			return;
		mBoardSortShowSolvedOption = boardSortShowSolvedOption;
		mSharedPreferences.edit().putInt(PREF_BOARD_SORT_SHOW_SOLVED_OPTION, mBoardSortShowSolvedOption.ordinal())
				.commit();
	}

	public String getAccountName() {
		return mAccountName;
	}

	public void setAccountName(String accountName) {
		mAccountName = accountName;
		mSharedPreferences.edit().putString(PREF_ACCOUNT_NAME, mAccountName).commit();
	}

	public long getLastCipherId() {
		return mLastCipherId;
	}

	public void setLastCipherId(long lastCipherId) {
		mLastCipherId = lastCipherId;
		mSharedPreferences.edit().putLong(PREF_LAST_CIPHER_ID, mLastCipherId).commit();
	}

	public User getUser() {
		return mUser;
	}

	public void setUser(User user) {
		if (user == null) {
			mSharedPreferences.edit().remove(PREF_USER_ID).remove(PREF_USER_NAME).remove(PREF_USER_AVATAR_MARKUP)
					.commit();
			return;
		}

		if (user.getId() == null || user.getName() == null)
			return;

		mUser = user;

		// Store user info.
		mSharedPreferences.edit().putLong(PREF_USER_ID, mUser.getId()).commit();
		mSharedPreferences.edit().putString(PREF_USER_NAME, mUser.getName()).commit();
		if (mUser.getAvatarMarkup() != null) {
			mSharedPreferences.edit().putString(PREF_USER_AVATAR_MARKUP, mUser.getAvatarMarkup()).commit();
		} else {
			mSharedPreferences.edit().remove(PREF_USER_AVATAR_MARKUP).commit();
		}
	}
}
