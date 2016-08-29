package com.deakishin.zodiac.controller.mainscreen;

import java.util.Calendar;

import com.deakishin.zodiac.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;

public class DialogSavedImageName extends DialogFragment {
	/*
	 * Диалога ввода имени сохраняемого изображения.
	 */

	/*
	 * Интерфейс слушателя результата диалога. Вызывающая диалог активность
	 * должна реализовывать этот интерфейс.
	 */
	public static interface OnSavedImageNamedListener {
		public void onSavedImageNamed(String name);
	}

	private OnSavedImageNamedListener mOnSavedImageNamedListener;

	public DialogSavedImageName() {
		super();
	}

	/* Проверка того, что активность реализовала интерфейс слушателя. */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mOnSavedImageNamedListener = (OnSavedImageNamedListener) activity;
		} catch (Exception e) {
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final EditText input = new EditText(getActivity());
		input.setHint(R.string.saved_image_name_hint);
		input.setId(R.id.saved_image_name_editText);
		builder.setView(input);
		builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				String result = input.getText().toString();
				if (result.length() == 0)
					result = createDefaultName();
				if (mOnSavedImageNamedListener != null)
					mOnSavedImageNamedListener.onSavedImageNamed(result);
			}
		});
		AlertDialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return dialog;
	}

	/* Конструирование имени по умолчанию. */
	private String createDefaultName() {
		char[] dateChars = DateFormat.getDateFormatOrder(getActivity());
		boolean isTime24 = DateFormat.is24HourFormat(getActivity());
		StringBuilder builder = new StringBuilder();
		builder.append(dateChars[0] + "-" + dateChars[1] + "-" + dateChars[2] + "_");
		builder.append(isTime24 ? "kk" : "hh");
		builder.append("-mm-ss");
		return DateFormat.format(builder.toString(), Calendar.getInstance()).toString();
	}

}
