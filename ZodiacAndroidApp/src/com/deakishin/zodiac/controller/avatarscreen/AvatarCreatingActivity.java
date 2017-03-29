package com.deakishin.zodiac.controller.avatarscreen;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.ConfirmationDialogFragment;
import com.deakishin.zodiac.controller.SingleFragmentActivity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

/**
 * Activity for the avatar creating screen. Hosts single fragment
 * {@link AvatarCreatingFragment}.
 */
public class AvatarCreatingActivity extends SingleFragmentActivity
		implements AvatarCreatingFragment.SuccessCallback, ConfirmationDialogFragment.ConfirmationListener {

	/* Keys for dialogs. */
	private static final String DIALOG_CONFIRM_DISCARD_CHANGES = "confirmDiscardChanges";

	/* Request codes. */
	private static final int REQUEST_CONFIRM_DISCARD_CHANGES = 1;

	@Override
	protected Fragment createFragment() {
		return new AvatarCreatingFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			checkChangesAndFinish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// AvatarCreatingFragment callback indicating that changes are saved
	// successfully.
	@Override
	public void onSaveSuccess() {
		setResult(Activity.RESULT_OK);
		finish();
	}

	/*
	 * Check if there are unsaved changes. If so, show a dialog to confirm
	 * discarding these changes. Otherwise, finish activity.
	 */
	public void checkChangesAndFinish() {
		if (mFragment instanceof AvatarCreatingFragment) {
			if (((AvatarCreatingFragment) mFragment).changesAreMade()) {
				showConfirmDiscardChanges();
			} else {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		} else {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
	}

	// Check for changes when the back button is pressed.
	@Override
	public void onBackPressed() {
		checkChangesAndFinish();
	}

	// Show dialog to confirm discarding changes.
	private void showConfirmDiscardChanges() {
		ConfirmationDialogFragment dialog = ConfirmationDialogFragment.getInstance(
				getString(R.string.avatar_creating_confirm_discard_changes),
				getString(R.string.avatar_creating_confirm_discard_changes_option_yes), REQUEST_CONFIRM_DISCARD_CHANGES,
				true);
		dialog.show(getSupportFragmentManager(), DIALOG_CONFIRM_DISCARD_CHANGES);
	}

	// Callback from confirmation dialog.
	@Override
	public void onConfirmed(int requestCode, int resultCode) {
		switch (requestCode) {
		case REQUEST_CONFIRM_DISCARD_CHANGES:
			if (resultCode == Activity.RESULT_OK) {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
			break;
		default:
			break;
		}
	}
}
