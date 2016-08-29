package com.deakishin.zodiac.model.settings;

public class Settings {
	/*
	 * Класс-синглетон для хранения настроек.
	 * */
	
	/* Настройки. */
	/* Настройки поиска. */
	/* Поиск с сохранением привязки символов. */
	private boolean mSearchKeepBinding = false;
	/* Гомофонический поиск. */
	private boolean mSearchHomophonic = true;
	/* Направления поиска - влево, вверх, вправо, вниз. */
	private boolean mSearchLeft = false;
	private boolean mSearchUp = false;
	private boolean mSearchRight = true;
	private boolean mSearchDown = false;
	/* Строка поиска. */
	private String mSearchString = "";
	

	
	private static Settings mSettings;
	
	public static Settings getInstance(){
		if (mSettings==null)
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
