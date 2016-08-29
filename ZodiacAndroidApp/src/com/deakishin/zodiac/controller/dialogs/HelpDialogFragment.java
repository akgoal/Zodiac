package com.deakishin.zodiac.controller.dialogs;

import com.deakishin.zodiac.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

public class HelpDialogFragment extends DialogFragment {
	/*
	 * Диалог поисковой справки.
	 */

	public HelpDialogFragment() {
		super();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_search_help, null);

		TextView textView = (TextView) v.findViewById(R.id.search_help_textView);
		Activity activity = getActivity();
		String text = activity.getString(R.string.help_search_text, activity.getString(R.string.keep_binding),
				activity.getString(R.string.homophonic));
		textView.setText(text);

		return new AlertDialog.Builder(getActivity()).setView(v).setPositiveButton(R.string.ok, null).create();
	}

}
