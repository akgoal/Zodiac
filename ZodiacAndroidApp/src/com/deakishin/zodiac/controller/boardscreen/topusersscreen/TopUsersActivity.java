package com.deakishin.zodiac.controller.boardscreen.topusersscreen;

import com.deakishin.zodiac.controller.SingleFragmentActivity;

import android.support.v4.app.Fragment;

/**
 * Activity for Top users screen. All it does is host {@link TopUsersFragment}.
 */
public class TopUsersActivity extends SingleFragmentActivity {
	@Override
	protected Fragment createFragment() {
		return new TopUsersFragment();
	}
}
