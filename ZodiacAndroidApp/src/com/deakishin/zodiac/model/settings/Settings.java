package com.deakishin.zodiac.model.settings;

/**
 * Singleton for application settings. These settings are not stored in the
 * memory and get reset with every launch of the app.
 */
public class Settings {

	/* Settings for the search. */
	private boolean mSearchKeepBinding = false;
	private boolean mSearchHomophonic = true;
	private boolean mSearchLeft = false;
	private boolean mSearchUp = false;
	private boolean mSearchRight = true;
	private boolean mSearchDown = false;
	/* Search query. */
	private String mSearchString = "";

	private static Settings mSettings;

	/** @return The sole instance of the singleton. */
	public static Settings getInstance() {
		if (mSettings == null)
			mSettings = new Settings();
		return mSettings;
	}

	private Settings() {
	}

	public boolean isSearchKeepBinding() {
		return mSearchKeepBinding;
	}

	public void setSearchKeepBinding(boolean searchKeepBinding) {
		mSearchKeepBinding = searchKeepBinding;
	}

	public boolean isSearchHomophonic() {
		return mSearchHomophonic;
	}

	public void setSearchHomophonic(boolean searchHomophonic) {
		mSearchHomophonic = searchHomophonic;
	}

	public boolean isSearchLeft() {
		return mSearchLeft;
	}

	public void setSearchLeft(boolean searchLeft) {
		mSearchLeft = searchLeft;
	}

	public boolean isSearchUp() {
		return mSearchUp;
	}

	public void setSearchUp(boolean searchUp) {
		mSearchUp = searchUp;
	}

	public boolean isSearchRight() {
		return mSearchRight;
	}

	public void setSearchRight(boolean searchRight) {
		mSearchRight = searchRight;
	}

	public boolean isSearchDown() {
		return mSearchDown;
	}

	public void setSearchDown(boolean searchDown) {
		mSearchDown = searchDown;
	}

	public String getSearchString() {
		return mSearchString;
	}

	public void setSearchString(String searchString) {
		mSearchString = searchString;
	}
}
