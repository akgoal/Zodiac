package com.deakishin.zodiac.controller.boardscreen;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.CustomDialogFragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;

/** Dialog that displays help information. */
public class DialogBoardHelp extends CustomDialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(R.layout.board_dialog_help, null);

		Dialog dialog = new AlertDialog.Builder(getActivity()).setView(v).setPositiveButton(R.string.ok, null).create();
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}
}
