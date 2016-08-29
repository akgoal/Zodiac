package com.deakishin.zodiac.controller.mainscreen;

import com.deakishin.zodiac.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

public class DialogErrorSavingImage extends DialogFragment {
	/*
	 * Диалог ошибки сохранения изображения.
	 */

	public DialogErrorSavingImage() {
		super();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_error_saving_image, null);

		return new AlertDialog.Builder(getActivity()).setView(v).setPositiveButton(R.string.ok, null).create();
	}

}
