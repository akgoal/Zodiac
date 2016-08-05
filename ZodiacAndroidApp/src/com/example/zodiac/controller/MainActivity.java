package com.example.zodiac.controller;

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
