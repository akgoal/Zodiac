package com.deakishin.zodiac.controller.settingsscreen;

import com.deakishin.zodiac.controller.SingleFragmentActivity;

import android.support.v4.app.Fragment;

/** Activity for the Settings screen. Hosts {@link SettingsFragment}. */
public class SettingsActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new SettingsFragment();
	}
}
