package com.deakishin.zodiac.controller.boardscreen.ciphercreatingscreen;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.ConfirmationDialogFragment;
import com.deakishin.zodiac.controller.SingleFragmentActivity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

/**
 * Activity for cipher creating screen. Hosts {@link CipherCreatingFragment}.
 */
public class CipherCreatingActivity extends SingleFragmentActivity
		implements CipherCreatingFragment.SuccessCallback, ConfirmationDialogFragment.ConfirmationListener {

	/* Keys for dialogs. */
	private static final String DIALOG_CONFIRM_DISCARD_CHANGES = "confirmDiscardChanges";

	/* Request codes for dialogs. */
	private static final int REQUEST_CONFIRM_DISCARD_CHANGES = 1;

	@Override
	protected Fragment createFragment() {
		return new CipherCreatingFragment();
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

	/* Finish activity. If there is a parent activity, go up to it. */
	private void finishActivity() {
		if (NavUtils.getParentActivityName(this) != null) {
			NavUtils.navigateUpFromSameTask(this);
		} else {
			this.finish();
		}
	}

	// Callback for successful uploading.
	@Override
	public void onUploadSuccess() {
		finishActivity();
	}

	/*
	 * Check if there are changes made. If so, show confirmation dialog to
	 * confirm discarding changes. Otherwise, finish activity.
	 */
	public void checkChangesAndFinish() {
		if (mFragment instanceof CipherCreatingFragment) {
			if (((CipherCreatingFragment) mFragment).changesAreMade()) {
				showConfirmDiscardChanges();
			} else {
				finishActivity();
			}
		} else {
			finishActivity();
		}
	}

	// Callback for the back button being clicked.
	@Override
	public void onBackPressed() {
		checkChangesAndFinish();
	}

	/* Show confirmation dialog to confirm discarding changes. */
	private void showConfirmDiscardChanges() {
		ConfirmationDialogFragment dialog = ConfirmationDialogFragment.getInstance(
				getString(R.string.cipher_creating_confirm_discard_changes),
				getString(R.string.cipher_creating_confirm_discard_changes_option_yes), REQUEST_CONFIRM_DISCARD_CHANGES,
				true);
		dialog.show(getSupportFragmentManager(), DIALOG_CONFIRM_DISCARD_CHANGES);
	}

	@Override
	public void onConfirmed(int requestCode, int resultCode) {
		switch (requestCode) {
		case REQUEST_CONFIRM_DISCARD_CHANGES:
			if (resultCode == Activity.RESULT_OK) {
				// Discarding changes confirmed.
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
			break;
		default:
			break;
		}
	}

}
