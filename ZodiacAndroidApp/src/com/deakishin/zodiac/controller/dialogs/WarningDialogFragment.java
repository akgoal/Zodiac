package com.deakishin.zodiac.controller.dialogs;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.model.settings.SettingsPersistent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.CheckBox;

public class WarningDialogFragment extends DialogFragment{
	/*
	 * Диалог предупреждения о поиске многих слов.
	 * */
	
	/* "Устойчивые" настройки приложения. */
	private SettingsPersistent mSettingsPersistent;
	
	/* Виджет. */
	private CheckBox mDontShowAgainCheckBox;
	
	public WarningDialogFragment() {
		super();

		mSettingsPersistent = SettingsPersistent.getInstance(getActivity());
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_search_warning, null);

		mDontShowAgainCheckBox = (CheckBox)v.findViewById(R.id.warning_dontshowagain_checkBox);
		
		return new AlertDialog.Builder(getActivity()).setView(v).setTitle(R.string.warning)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mSettingsPersistent.setSearchWarningDontShow(mDontShowAgainCheckBox.isChecked());
					}
				}).create();
	}

}
