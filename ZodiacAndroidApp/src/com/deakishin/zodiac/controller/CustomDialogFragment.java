package com.deakishin.zodiac.controller;

import com.deakishin.zodiac.R;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Button;

/**
 * Custom DialogFragment with adjusted button text color and size. Also fixes
 * the bug of dialog being destroyed during device rotating.
 * 
 * To maintain consistency, must be used everywhere in the app instead of
 * {@link DialogFragment}.
 */
public class CustomDialogFragment extends DialogFragment {

	// Adjust buttons with custom design.
	@Override
	public void onStart() {
		super.onStart();

		final AlertDialog dialog = (AlertDialog) getDialog();
		if (dialog == null)
			return;
		Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		if (positiveButton != null) {
			setButtonTextColor(positiveButton);
		}
		Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		if (negativeButton != null) {
			setButtonTextColor(negativeButton);
		}
	}

	// Set buttons' custom design.
	private void setButtonTextColor(Button button) {
		int colorResId = R.color.dialog_button_text;
		button.setTextColor(ContextCompat.getColor(getActivity(), colorResId));
		button.setTextSize(18);
	}

	/* Fix bug of dialog being destroyed when the device rotates. */
	@Override
	public void onDestroyView() {
		Dialog dialog = getDialog();
		if (dialog != null && getRetainInstance()) {
			dialog.setDismissMessage(null);
		}
		super.onDestroyView();
	}
}
