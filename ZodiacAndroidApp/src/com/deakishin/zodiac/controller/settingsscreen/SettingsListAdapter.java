package com.deakishin.zodiac.controller.settingsscreen;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.model.settings.FontColorProfile;
import com.deakishin.zodiac.model.settings.SettingsPersistent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

/** Adapter for the list of settings elements. */
public class SettingsListAdapter extends BaseAdapter {

	/*
	 * Ids and at the same time ordinal numbers of the settings elements. If id
	 * < 0, then the element won't be displayed.
	 */
	/** Id for the Font color settings element. */
	public static final int FONT_COLOR_SETTINGS = 0;
	/** Id for the Checkpoint naming settings element. */
	public static final int CHECKPOINT_NAME_SETTINGS = 1;
	/** Id for the Autosave settings element. */
	public static final int AUTOSAVE_SETTINGS = -10;

	/** Number of settings elements. */
	private static final int SETTINGS_COUNT = 2;

	/* Application context. */
	private Context mContext;
	/* Views inflater. */
	private LayoutInflater mLayoutInflater;
	/* Application settings. */
	private SettingsPersistent mSettings;

	public SettingsListAdapter(Context context) {
		super();
		mContext = context;
		mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mSettings = SettingsPersistent.getInstance(mContext);
	}

	@Override
	public int getCount() {
		return SETTINGS_COUNT;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		switch (position) {
		case FONT_COLOR_SETTINGS:
			return getColorSettingsView(convertView);
		case CHECKPOINT_NAME_SETTINGS:
			return getCheckpointNameView(convertView);
		case AUTOSAVE_SETTINGS:
			return getAutosaveView(convertView);
		default:
			return null;
		}
	}

	/* Create view for the Font color settings element. */
	private View getColorSettingsView(View convertView) {
		convertView = mLayoutInflater.inflate(R.layout.settings_list_item_fontcolor, null);

		TextView titleTextView = (TextView) convertView.findViewById(R.id.settings_fontcolor_title_textView);
		titleTextView.setText(R.string.settings_item_fontcolor_title);

		TextView subtitleTextView = (TextView) convertView.findViewById(R.id.settings_fontcolor_subtitle_textView);
		subtitleTextView.setText(R.string.settings_item_fontcolor_subtitle);
		subtitleTextView.setVisibility(View.VISIBLE);

		FontColorProfile profile = mSettings.getFontColorProfile();
		View primalColorPreviewView = convertView.findViewById(R.id.settings_fontcolor_primal_color_view);
		primalColorPreviewView.setBackgroundColor(profile.getPrimalColor());
		View secondaryColorPreviewView = convertView.findViewById(R.id.settings_fontcolor_secondary_color_view);
		secondaryColorPreviewView.setBackgroundColor(profile.getSecondaryColor());

		return convertView;
	}

	/* Create view for the Checkpoint naming settings element. */
	private View getCheckpointNameView(View convertView) {
		convertView = mLayoutInflater.inflate(R.layout.settings_list_item_checkpointname, null);

		TextView titleTextView = (TextView) convertView.findViewById(R.id.settings_checkpointname_title_textView);
		titleTextView.setText(R.string.settings_item_checkpointname_title);

		TextView subtitleTextView = (TextView) convertView.findViewById(R.id.settings_checkpointname_subtitle_textView);
		subtitleTextView.setText(mSettings.getCheckpointNameOption().getTitleResId());

		return convertView;
	}

	/* Create view for the Autosave settings element. */
	private View getAutosaveView(View convertView) {
		convertView = mLayoutInflater.inflate(R.layout.settings_list_item_autosave, null);

		CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.settings_autosave_checkbox);
		checkBox.setChecked(mSettings.isAutosaveEnabled());

		return convertView;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
