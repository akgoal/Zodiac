package com.deakishin.zodiac.controller.settingsscreen;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.model.settings.SettingsPersistent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

/** Fragment for displaying application settings. */
public class SettingsFragment extends Fragment {

	/* Keys for dialogs. */
	private static final String DIALOG_FONT_COLOR = "fontColor";
	private static final String DIALOG_CHECKPOINT_NAME = "checkpointName";

	/* Request codes for dialogs. */
	private static final int REQUEST_DIALOG = 0;

	/* Adapter for the list of settings elements. */
	private SettingsListAdapter mAdapter;

	/* Application settings. */
	private SettingsPersistent mSettings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSettings = SettingsPersistent.getInstance(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.settings_fragment, parent, false);

		ListView listView = (ListView) v.findViewById(R.id.settings_listView);
		mAdapter = new SettingsListAdapter(getActivity());
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				switch (position) {
				case SettingsListAdapter.FONT_COLOR_SETTINGS:
					DialogFontColor dialogFontColor = new DialogFontColor();
					dialogFontColor.setTargetFragment(SettingsFragment.this, REQUEST_DIALOG);
					dialogFontColor.show(getActivity().getSupportFragmentManager(), DIALOG_FONT_COLOR);
					return;
				case SettingsListAdapter.CHECKPOINT_NAME_SETTINGS:
					DialogCheckpointName dialogCheckpointName = new DialogCheckpointName();
					dialogCheckpointName.setTargetFragment(SettingsFragment.this, REQUEST_DIALOG);
					dialogCheckpointName.show(getActivity().getSupportFragmentManager(), DIALOG_CHECKPOINT_NAME);
					return;
				case SettingsListAdapter.AUTOSAVE_SETTINGS:
					mSettings.switchAutosaveEnabled();
					mAdapter.notifyDataSetChanged();
					return;
				default:
					return;
				}
			}
		});
		return v;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;
		if (requestCode == REQUEST_DIALOG) {
			// Some settings have changed.
			mAdapter.notifyDataSetChanged();
			return;
		}
	}
}
