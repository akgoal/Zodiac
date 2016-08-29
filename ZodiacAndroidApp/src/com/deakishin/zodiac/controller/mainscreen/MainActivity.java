package com.deakishin.zodiac.controller.mainscreen;

import com.deakishin.zodiac.controller.SingleFragmentActivity;

import android.support.v4.app.Fragment;

public class MainActivity extends SingleFragmentActivity {
	/*
	 * Главная активность приложения.
	 */

	@Override
	protected Fragment createFragment() {
		return new ImageFragment();
	}
}
