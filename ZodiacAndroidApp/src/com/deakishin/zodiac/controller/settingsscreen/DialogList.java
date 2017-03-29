package com.deakishin.zodiac.controller.settingsscreen;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.CustomDialogFragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

/** Abstract dialog that displays a list of options. */
public abstract class DialogList extends CustomDialogFragment {

	/* Adapter for accessing options. */
	private ListAdapterItemClickListener mAdapter;

	public DialogList() {
		super();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		prepare();

		View v = getActivity().getLayoutInflater().inflate(R.layout.settings_dialog_list, null);

		ListView fontcolorListView = (ListView) v.findViewById(R.id.settings_dialog_list_view);

		mAdapter = getAdapter();
		fontcolorListView.setAdapter(mAdapter);
		fontcolorListView.setOnItemClickListener(mAdapter);

		AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(v)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						onPositiveButtonClick();
						if (getTargetFragment() != null)
							getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
					}
				}).create();
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	/** @return Adapter that provides options. */
	protected abstract ListAdapterItemClickListener getAdapter();

	/** Adapter that provides options and also handles its items being clicked on. */
	protected abstract class ListAdapterItemClickListener extends BaseAdapter
			implements AdapterView.OnItemClickListener {
		/** @return Index of the selected option. */
		protected abstract int getSelectedIndex();
	}

	/** Handles a click on the positive button of the dialog. */
	protected abstract void onPositiveButtonClick();

	/** Prepares the dialog. Invoked before any other operations. */
	protected abstract void prepare();
}
