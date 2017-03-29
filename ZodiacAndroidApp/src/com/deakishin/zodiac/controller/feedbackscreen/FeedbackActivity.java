package com.deakishin.zodiac.controller.feedbackscreen;

import com.deakishin.zodiac.controller.SingleFragmentActivity;

import android.support.v4.app.Fragment;

/** Activity for Feedback screen. Hosts {@link FeedbackFragment}. */
public class FeedbackActivity extends SingleFragmentActivity implements FeedbackFragment.SuccessCallback {
	
	@Override
	protected Fragment createFragment() {
		return new FeedbackFragment();
	}

	// Callback for a successful sending of the feedback.
	@Override
	public void onSendingSuccess() {
		finish();
	}
}
