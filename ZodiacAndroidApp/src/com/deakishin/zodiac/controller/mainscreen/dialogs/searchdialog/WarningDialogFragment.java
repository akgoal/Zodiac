package com.deakishin.zodiac.controller.mainscreen.dialogs.searchdialog;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.CustomDialogFragment;
import com.deakishin.zodiac.model.settings.SettingsPersistent;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;

/** Dialog displaying search warning about too many words being searched. */
public class WarningDialogFragment extends CustomDialogFragment{
	
	/* Application settings. */
	private SettingsPersistent mSettingsPersistent;
	
	/* Widgets. */
	private CheckBox mDontShowAgainCheckBox;
	
	public WarningDialogFragment() {
		super();

		mSettingsPersistent = SettingsPersistent.getInstance(getActivity());
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_search_warning, null);

		mDontShowAgainCheckBox = (CheckBox)v.findViewById(R.id.warning_dontshowagain_checkBox);
		
		Dialog dialog = new AlertDialog.Builder(getActivity()).setView(v).setTitle(R.string.warning)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mSettingsPersistent.setSearchWarningDontShow(mDontShowAgainCheckBox.isChecked());
					}
				}).create();
		dialog.setCanceledOnTouchOutside(false);
		return dialog;
	}

}
