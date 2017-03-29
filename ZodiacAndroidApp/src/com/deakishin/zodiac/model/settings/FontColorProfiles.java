package com.deakishin.zodiac.model.settings;

import android.graphics.Color;

/**
 * Holds and provides access to different color profiles.
 */
public class FontColorProfiles {

	/* Default profle. */
	private static final FontColorProfile DEF_PROFILE;
	/* Profiles. */
	private static final FontColorProfile[] PROFILES = {
			new FontColorProfile(1, Color.parseColor("#e74c3c"), Color.parseColor("#c0392b")),
			new FontColorProfile(2, Color.parseColor("#e67e22"), Color.parseColor("#d35400")),
			new FontColorProfile(3, Color.parseColor("#f1c40f"), Color.parseColor("#f39c12")),
			new FontColorProfile(4, Color.parseColor("#2ecc71"), Color.parseColor("#27ae60")),
			new FontColorProfile(5, Color.parseColor("#1abc9c"), Color.parseColor("#16a085")),
			DEF_PROFILE = new FontColorProfile(6, Color.parseColor("#3498db"), Color.parseColor("#2980b9")),
			new FontColorProfile(7, Color.parseColor("#9b59b6"), Color.parseColor("#8e44ad")),
			new FontColorProfile(8, Color.parseColor("#34495e"), Color.parseColor("#2c3e50")), };

	/*
	 * private static final FontColorProfile[] PROFILES = { new
	 * FontColorProfile(1, Color.parseColor("#FF1744"),
	 * Color.parseColor("#D50000")), new FontColorProfile(2,
	 * Color.parseColor("#D500F9"), Color.parseColor("#AA00FF")), new
	 * FontColorProfile(3, Color.parseColor("#651FFF"),
	 * Color.parseColor("#6200EA")), new FontColorProfile(4,
	 * Color.parseColor("#2979FF"), Color.parseColor("#2962FF")), new
	 * FontColorProfile(5, Color.parseColor("#00B0FF"),
	 * Color.parseColor("#0091EA")), new FontColorProfile(8,
	 * Color.parseColor("#1DE9B6"), Color.parseColor("#00BFA5")), DEF_PROFILE =
	 * new FontColorProfile(6, Color.parseColor("#00E676"),
	 * Color.parseColor("#00C853")), new FontColorProfile(7,
	 * Color.parseColor("#FFC400"), Color.parseColor("#FFAB00")), new
	 * FontColorProfile(8, Color.parseColor("#FF9100"),
	 * Color.parseColor("#FF6D00")), new FontColorProfile(8,
	 * Color.parseColor("#FF3D00"), Color.parseColor("#DD2C00")) };
	 */

	/** @return Array of available profiles. */
	public static FontColorProfile[] getProfiles() {
		return PROFILES;
	}

	/**
	 * @param profile
	 *            Color profile.
	 * @return Index of the profile in the list of profiles, or -1 if the profile
	 *         was not found.
	 */
	public static int getProfileIndex(FontColorProfile profile) {
		if (profile == null)
			return -1;
		int id = profile.getId();
		for (int i = 0; i < PROFILES.length; i++) {
			if (PROFILES[i].getId() == id)
				return i;
		}
		return -1;
	}

	/** @return Default color profile. */
	public static FontColorProfile getDefaultProfile() {
		return DEF_PROFILE;
	}

	/**
	 * @param id
	 *            Id of the profile.
	 * @return Profile with the given id, or null if no such profile was found.
	 */
	public static FontColorProfile getProfile(int id) {
		for (FontColorProfile cp : PROFILES) {
			if (cp.getId() == id)
				return cp;
		}
		return null;
	}

	/**
	 * @param index
	 *            Index in the list of profiles.
	 * @return Profile in the given place in the list of profiles, or the default
	 *         profile if the index is out of bounds.
	 */
	public static FontColorProfile getProfileByIndex(int index) {
		if (index < 0 || index >= PROFILES.length)
			return DEF_PROFILE;
		return PROFILES[index];
	}
}
