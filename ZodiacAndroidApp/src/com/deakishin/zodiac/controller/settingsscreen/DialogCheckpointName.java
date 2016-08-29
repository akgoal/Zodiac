package com.deakishin.zodiac.controller.settingsscreen;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.model.settings.CheckpointNameOption;
import com.deakishin.zodiac.model.settings.CheckpointNameOptions;
import com.deakishin.zodiac.model.settings.SettingsPersistent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RadioButton;
import android.widget.TextView;

public class DialogCheckpointName extends DialogList {
	/* Диалог выбора варианта именования чекпоинтов. */

	/* Адаптер списка. */
	private ListAdapterItemClickListener mAdapter;

	private SettingsPersistent mSettings;

	public DialogCheckpointName(){
		super();
	}
	
	@Override
	protected void prepare() {
		mSettings = SettingsPersistent.getInstance(getActivity());
	}

	@Override
	protected ListAdapterItemClickListener getAdapter() {
		mAdapter = new CheckpointNameListAdapter(getActivity(), CheckpointNameOptions.getOptions(),
				CheckpointNameOptions.getOptionIndex(mSettings.getCheckpointNameOption()));
		return mAdapter;
	}

	@Override
	protected void onPositiveButtonClick() {
		mSettings.setCheckpointNameOption(CheckpointNameOptions.getOptionByIndex(mAdapter.getSelectedIndex()));
	}

	/* Класс адаптера списка. */
	private class CheckpointNameListAdapter extends ListAdapterItemClickListener {
		private LayoutInflater mLayoutInflater;

		/* Индекс выбранного элемента. */
		private int mSelectedIndex;

		/* Данные списка. */
		private CheckpointNameOption[] mItems;

		public CheckpointNameListAdapter(Context context, CheckpointNameOption[] сheckpointNameOptions,
				int selectedIndex) {
			super();

			mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mSelectedIndex = selectedIndex;
			mItems = сheckpointNameOptions;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			mSelectedIndex = position;
			this.notifyDataSetChanged();
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

		@Override
		protected int getSelectedIndex() {
			return mSelectedIndex;
		}

	}
}
