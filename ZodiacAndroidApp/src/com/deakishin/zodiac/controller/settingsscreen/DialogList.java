package com.deakishin.zodiac.controller.settingsscreen;

import com.deakishin.zodiac.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

public abstract class DialogList extends DialogFragment {
	/*
	 * Абстрактный класс диалога со списком, в котором надо выбрать один
	 * элемент.
	 */

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
		return dialog;
	}

	/* Адаптер для списка. */
	protected abstract ListAdapterItemClickListener getAdapter();

	protected abstract class ListAdapterItemClickListener extends BaseAdapter
			implements AdapterView.OnItemClickListener {
		protected abstract int getSelectedIndex();
	}

	/* Действие по нажатии кнопки подтверждения. */
	protected abstract void onPositiveButtonClick();

	/*
	 * Подготовительные действия. Выполняются до всех других операций.
	 */
	protected abstract void prepare();
}
