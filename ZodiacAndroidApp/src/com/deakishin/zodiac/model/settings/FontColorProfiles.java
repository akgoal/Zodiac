package com.deakishin.zodiac.model.settings;

import android.graphics.Color;

public class FontColorProfiles {
	/*
	 * Варианты цвета шрифта.
	 */

	/* Наборы. */
	private static final FontColorProfile DEF_PROFILE;
	private static final FontColorProfile[] PROFILES = {
			DEF_PROFILE = new FontColorProfile(1, Color.argb(255, 255, 0, 0), Color.argb(255, 210, 0, 0)),
			new FontColorProfile(2, Color.argb(255, 7, 240, 61), Color.argb(255, 14, 153, 46)),
			new FontColorProfile(3, Color.argb(255, 0, 136, 222), Color.argb(255, 17, 93, 173)),
			new FontColorProfile(4, Color.argb(255, 212, 85, 203), Color.argb(255, 176, 32, 166))};

	public static FontColorProfile[] getProfiles() {
		return PROFILES;
	}

	/* Индекс профиля в массиве. -1, если профиль не найден. */
	public static int getProfileIndex(FontColorProfile profile) {
		int id = profile.getId();
		for (int i = 0; i < PROFILES.length; i++) {
			if (PROFILES[i].getId() == id)
				return i;
		}
		return -1;
	}

	public static FontColorProfile getDefaultProfile() {
		return DEF_PROFILE;
	}

	/* Профиль по его идентификатору. */
	public static FontColorProfile getProfile(int id) {
		for (FontColorProfile cp : PROFILES) {
			if (cp.getId() == id)
				return cp;
		}
		return null;
	}

	/*
	 * Профиль по его индексу в массиве. Если не найден, профиль по умолчанию.
	 */
	public static FontColorProfile getProfileByIndex(int index) {
		if (index < 0 || index >= PROFILES.length)
			return DEF_PROFILE;
		return PROFILES[index];
	}
}
