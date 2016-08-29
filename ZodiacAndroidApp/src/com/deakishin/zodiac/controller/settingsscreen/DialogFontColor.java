package com.deakishin.zodiac.controller.settingsscreen;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.model.settings.FontColorProfile;
import com.deakishin.zodiac.model.settings.FontColorProfiles;
import com.deakishin.zodiac.model.settings.SettingsPersistent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RadioButton;
import android.widget.TextView;

public class DialogFontColor extends DialogList {
	/*
	 * Диалог выбора набора цвета шрифта.
	 */

	/* Настройки приложения. */
	private SettingsPersistent mSettings;

	private FontColorListAdapter mAdapter;

	public DialogFontColor(){
		super();
	}
	
	@Override
	protected ListAdapterItemClickListener getAdapter() {
		mAdapter = new FontColorListAdapter(getActivity(), FontColorProfiles.getProfiles(),
				FontColorProfiles.getProfileIndex(mSettings.getFontColorProfile()));
		return mAdapter;
	}

	@Override
	protected void onPositiveButtonClick() {
		mSettings.setFontColorProfile(FontColorProfiles.getProfileByIndex(mAdapter.getSelectedIndex()));
	}

	@Override
	protected void prepare() {
		mSettings = SettingsPersistent.getInstance(getActivity());
	}

	/* Адаптер списка вариантов цвета шрифта. */
	private class FontColorListAdapter extends ListAdapterItemClickListener {

		private LayoutInflater mLayoutInflater;

		/* Индекс выбранного элемента. */
		private int mSelectedIndex;

		/* Данные для списка. */
		private FontColorProfile[] mItems;

		public FontColorListAdapter(Context context, FontColorProfile[] fontColorProfileList, int selectedIndex) {
			super();
			mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mSelectedIndex = selectedIndex;
			mItems = fontColorProfileList;
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
				convertView = mLayoutInflater.inflate(R.layout.settings_dialog_fontcolor_listitem, null);
			}

			FontColorProfile profile = (FontColorProfile) getItem(position);

			TextView previewLeftTextView = (TextView) convertView.findViewById(R.id.settings_fontcolor_textView_left);
			previewLeftTextView.setTextColor(profile.getPrimalColor());

			TextView previewRightTextView = (TextView) convertView.findViewById(R.id.settings_fontcolor_textView_right);
			previewRightTextView.setTextColor(profile.getSecondaryColor());

			RadioButton radio = (RadioButton) convertView.findViewById(R.id.settings_fontcolor_radioButton);
			radio.setChecked(position == mSelectedIndex);

			return convertView;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			mSelectedIndex = position;
			this.notifyDataSetChanged();
		}

		public int getSelectedIndex() {
			return mSelectedIndex;
		}
	}
}
