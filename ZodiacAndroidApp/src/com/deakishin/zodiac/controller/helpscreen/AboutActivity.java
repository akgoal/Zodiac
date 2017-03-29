package com.deakishin.zodiac.controller.helpscreen;

import com.deakishin.zodiac.controller.SingleFragmentActivity;

import android.support.v4.app.Fragment;

/** Activity for the About screen. Hosts {@link AboutFragment}. */
public class AboutActivity extends SingleFragmentActivity{

	@Override
	protected Fragment createFragment() {
		return new AboutFragment();
	}
}
