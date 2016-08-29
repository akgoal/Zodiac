package com.deakishin.zodiac.model.settings;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingsPersistent {
	/*
	 * Класс-синглетон для постоянных настроек, способных храниться в файловой
	 * системе. Отличается от класса обычных настроек наличием контекста
	 * приложения.
	 */

	/* Контекст приложения. */
	private Context mContext;

	/* Хранилице общих настроек. */
	private SharedPreferences mSharedPreferences;

	/* Ключи для хранения настроек в файловой системе. */
	private static final String PREF_SEARCH_WARNING_DONT_SHOW = "warningDontShow";
	private static final String PREF_FONT_COLOR_PROFILE_ID = "fontColorProfileId";
	private static final String PREF_CHECKPOINT_NAME_OPTION_CODE = "checkpointNameOption";

	/* Значения настроек по умолчанию. */
	private static final boolean DEFAULT_SEARCH_WARNING_DOWN_SHOW = false;

	/* Настройки. */
	/* Не показывать предупреждение в диалоге поиска. */
	private boolean mSearchWarningDontShow;
	/* Цветовой профиль шрифта. */
	private FontColorProfile mFontColorProfile;
	/* Вариант именования чекпоинтов. */
	private CheckpointNameOption mCheckpointNameOption;

	/* Слушатели изменения профиля цвета шрифта. */
	private ArrayList<OnFontColorProfileChangeListener> mOnFontColorProfileChangeListeners = new ArrayList<OnFontColorProfileChangeListener>();

	private static SettingsPersistent mSettingsPersistent;

	public static SettingsPersistent getInstance(Context context) {
		if (mSettingsPersistent == null)
			mSettingsPersistent = new SettingsPersistent(context.getApplicationContext());
		return mSettingsPersistent;
	}

	private SettingsPersistent(Context context) {
		mContext = context;
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

		mSearchWarningDontShow = mSharedPreferences.getBoolean(PREF_SEARCH_WARNING_DONT_SHOW,
				DEFAULT_SEARCH_WARNING_DOWN_SHOW);
		mFontColorProfile = FontColorProfiles.getProfile(mSharedPreferences.getInt(PREF_FONT_COLOR_PROFILE_ID, -1));
		if (mFontColorProfile == null)
			setFontColorProfile(FontColorProfiles.getDefaultProfile());

		mCheckpointNameOption = CheckpointNameOptions
				.getOption(mSharedPreferences.getInt(PREF_CHECKPOINT_NAME_OPTION_CODE, -1));
		if (mCheckpointNameOption == null)
			setCheckpointNameOption(CheckpointNameOptions.getDefaultOption());
	}

	public boolean isSearchWarningDontShow() {
		return mSearchWarningDontShow;
	}

	public void setSearchWarningDontShow(boolean searchWarningDontShow) {
		mSearchWarningDontShow = searchWarningDontShow;
		mSharedPreferences.edit().putBoolean(PREF_SEARCH_WARNING_DONT_SHOW, searchWarningDontShow).commit();
	}

	public FontColorProfile getFontColorProfile() {
		return mFontColorProfile;
	}

	public void setFontColorProfile(FontColorProfile fontColorProfile) {
		mFontColorProfile = fontColorProfile;
		mSharedPreferences.edit().putInt(PREF_FONT_COLOR_PROFILE_ID, fontColorProfile.getId()).commit();
		for (OnFontColorProfileChangeListener listener : mOnFontColorProfileChangeListeners) {
			listener.onFontColorProfileChanged(mFontColorProfile);
		}
	}

	public void addOnFontColorProfileChangeListener(OnFontColorProfileChangeListener onFontColorProfileChangeListener) {
		mOnFontColorProfileChangeListeners.add(onFontColorProfileChangeListener);
	}

	/* Интерфейс слушателя изменения профиля цвета шрифта. */
	public static interface OnFontColorProfileChangeListener {
		public void onFontColorProfileChanged(FontColorProfile fontColorProfile);
	}

	public CheckpointNameOption getCheckpointNameOption() {
		return mCheckpointNameOption;
	}

	public void setCheckpointNameOption(CheckpointNameOption checkpointNameOption) {
		mCheckpointNameOption = checkpointNameOption;
		mSharedPreferences.edit().putInt(PREF_CHECKPOINT_NAME_OPTION_CODE, checkpointNameOption.getCode()).commit();
	}
}
