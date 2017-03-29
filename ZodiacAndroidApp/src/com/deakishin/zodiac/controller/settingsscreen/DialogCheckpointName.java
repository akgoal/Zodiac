package com.deakishin.zodiac.controller.settingsscreen;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.CustomDialogFragment;
import com.deakishin.zodiac.model.settings.CheckpointNameOption;
import com.deakishin.zodiac.model.settings.CheckpointNameOptions;
import com.deakishin.zodiac.model.settings.SettingsPersistent;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

/** Dialog for choosing an option regarding checkpoint naming. */
public class DialogCheckpointName extends CustomDialogFragment {

	/* Adapter for the list of options. */
	private CheckpointNameListAdapter mAdapter;

	/* Application settings. */
	private SettingsPersistent mSettings;

	public DialogCheckpointName() {
		super();

		mSettings = SettingsPersistent.getInstance(getActivity());
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		View v = getActivity().getLayoutInflater().inflate(R.layout.settings_dialog_list, null);

		TextView titleTextView = (TextView) v.findViewById(R.id.settings_dialog_title);
		titleTextView.setVisibility(View.VISIBLE);
		titleTextView.setText(R.string.settings_dialog_checkpointname_title);

		ListView fontcolorListView = (ListView) v.findViewById(R.id.settings_dialog_list_view);

		mAdapter = new CheckpointNameListAdapter(getActivity(), CheckpointNameOptions.getOptions(),
				CheckpointNameOptions.getOptionIndex(mSettings.getCheckpointNameOption()));
		fontcolorListView.setAdapter(mAdapter);
		fontcolorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				mAdapter.setSelectedIndex(position);
				mSettings.setCheckpointNameOption(CheckpointNameOptions.getOptionByIndex(mAdapter.getSelectedIndex()));

				sendResultOkAndDismiss();
			}
		});

		AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(v).create();
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	/* Send result to the target fragment and dismiss. */
	private void sendResultOkAndDismiss() {
		if (getTargetFragment() != null)
			getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
		dismiss();
	}

	/** Adapter for the list of checkpoint naming options. */
	private class CheckpointNameListAdapter extends BaseAdapter {

		/* Views inflater. */
		private LayoutInflater mLayoutInflater;

		/* Index of the selected option. */
		private int mSelectedIndex;

		/* Options to display. */
		private CheckpointNameOption[] mItems;

		/**
		 * Constructs an adapter.
		 * 
		 * @param context
		 *            Application context.
		 * @param checkpointNameOptions
		 *            Options to display.
		 * @param selectedIndex
		 *            Index of the selected option.
		 */
		public CheckpointNameListAdapter(Context context, CheckpointNameOption[] сheckpointNameOptions,
				int selectedIndex) {
			super();

			mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mSelectedIndex = selectedIndex;
			mItems = сheckpointNameOptions;
		}

		@Override
		public int getCount() {
			return mItems.length;
		}

		@Override
		public Object getItem(int position) {
			return mItems[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mLayoutInflater.inflate(R.layout.settings_dialog_checkpointname_listitem, null);
			}

			CheckpointNameOption option = (CheckpointNameOption) getItem(position);

			TextView optionTextView = (TextView) convertView.findViewById(R.id.settings_checkpointname_option_textView);
			optionTextView.setText(option.getTitleResId());

			RadioButton radio = (RadioButton) convertView.findViewById(R.id.settings_checkpointname_option_radioButton);
			radio.setChecked(position == mSelectedIndex);

			return convertView;
		}

		/**
		 * Sets the selected option.
		 * 
		 * @param index
		 *            Index of the selected option.
		 * 
		 */
		public void setSelectedIndex(int index) {
			mSelectedIndex = index;
			notifyDataSetChanged();
		}

		/** @return Index of the selected option. */
		public int getSelectedIndex() {
			return mSelectedIndex;
		}

	}
}
