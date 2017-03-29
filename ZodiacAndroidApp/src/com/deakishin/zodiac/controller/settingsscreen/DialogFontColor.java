package com.deakishin.zodiac.controller.settingsscreen;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.CustomDialogFragment;
import com.deakishin.zodiac.model.settings.FontColorProfile;
import com.deakishin.zodiac.model.settings.FontColorProfiles;
import com.deakishin.zodiac.model.settings.SettingsPersistent;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

/**
 * Dialog for choosing font colors. These colors are used to draw characters on
 * the cipher image on the main screen.
 */
public class DialogFontColor extends CustomDialogFragment {

	/* Keys for storing state. */
	private static final String INDEX_POSITION = "position";

	/* Application settings. */
	private SettingsPersistent mSettings;

	/* Widgets. */
	private View mPreviewPanel;
	private GridView mGridView;
	private TextView mLeftPreviewTextView;
	private TextView mRightPreviewTextView;

	/* Adapter for the grid of options. */
	private ColorImageAdapter mAdapter;

	/* Index of the selected option. */
	private int mSelectedPosition = -1;

	public DialogFontColor() {
		super();

		mSettings = SettingsPersistent.getInstance(getActivity());
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (savedInstanceState != null)
			mSelectedPosition = savedInstanceState.getInt(INDEX_POSITION, -1);

		View v = getActivity().getLayoutInflater().inflate(R.layout.settings_dialog_fontcolor, null);

		mPreviewPanel = v.findViewById(R.id.settings_dialog_fontcolor_preview_panel);
		mPreviewPanel.setBackgroundColor(Color.WHITE);
		mGridView = (GridView) v.findViewById(R.id.settings_dialog_fontcolor_gridView);
		mLeftPreviewTextView = (TextView) v.findViewById(R.id.settings_dialog_fontcolor_textView_left);
		mRightPreviewTextView = (TextView) v.findViewById(R.id.settings_dialog_fontcolor_textView_right);

		if (mSelectedPosition < 0 || mSelectedPosition >= FontColorProfiles.getProfiles().length)
			mSelectedPosition = FontColorProfiles.getProfileIndex(mSettings.getFontColorProfile());
		updatePreview(FontColorProfiles.getProfileByIndex(mSelectedPosition));
		mAdapter = new ColorImageAdapter(getActivity(), FontColorProfiles.getProfiles(), mSelectedPosition);
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(mAdapter);
		mAdapter.setOnItemSelectedListener(new ColorImageAdapter.OnItemSelectedListener() {
			@Override
			public void onItemSelected(FontColorProfile profile) {
				updatePreview(profile);
			}
		});

		AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(v)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						mSettings.setFontColorProfile(FontColorProfiles.getProfileByIndex(mAdapter.getSelectedIndex()));
						if (getTargetFragment() != null)
							getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
					}
				}).setNegativeButton(R.string.cancel, null).create();
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	/* Update preview panel according to selected option. */
	private void updatePreview(FontColorProfile profile) {
		if (profile == null)
			return;

		mLeftPreviewTextView.setTextColor(profile.getPrimalColor());

		mRightPreviewTextView.setTextColor(profile.getSecondaryColor());

	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		mSelectedPosition = mAdapter.getSelectedIndex();
		savedInstanceState.putInt(INDEX_POSITION, mSelectedPosition);
		super.onSaveInstanceState(savedInstanceState);
	}

	/** Adapter that provides color options. */
	private static class ColorImageAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

		/* Views inflater. */
		private LayoutInflater mLayoutInflater;

		/* Index of the selected option. */
		private int mSelectedIndex;

		/* Color options. */
		private FontColorProfile[] mItems;

		/* Listener to option selection. */
		private OnItemSelectedListener mOnItemSelectedListener;

		/**
		 * The listener interface to receive callbacks when an option is
		 * selected.
		 */
		public static interface OnItemSelectedListener {
			/**
			 * Invoked when an option is selected.
			 * 
			 * @param item
			 *            Selected color option.
			 */
			public void onItemSelected(FontColorProfile item);
		}

		/**
		 * Constructs an adapter.
		 * 
		 * @param context
		 *            Application context.
		 * @param fontColorProfileList
		 *            List of options.
		 * @param selectedIndex
		 *            Index of the selected option.
		 */
		public ColorImageAdapter(Context context, FontColorProfile[] fontColorProfileList, int selectedIndex) {
			super();
			mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mSelectedIndex = selectedIndex;
			mItems = fontColorProfileList;
		}

		/**
		 * Sets a listener to option selection.
		 * 
		 * @param onItemSelectedListener
		 *            Listener that will be notified when an option is selected.
		 */
		public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
			mOnItemSelectedListener = onItemSelectedListener;
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
				convertView = mLayoutInflater.inflate(R.layout.settings_dialog_fontcolor_item, null);
			}

			FontColorProfile profile = (FontColorProfile) getItem(position);

			View colorPreviewLeftHalf = convertView.findViewById(R.id.settings_dialog_fontcolor_item_lefthalf);
			colorPreviewLeftHalf.setBackgroundColor(profile.getPrimalColor());

			View colorPreviewRightHalf = convertView.findViewById(R.id.settings_dialog_fontcolor_item_righthalf);
			colorPreviewRightHalf.setBackgroundColor(profile.getSecondaryColor());

			View colorPreviewPanel = convertView.findViewById(R.id.settings_dialog_fontcolor_item_panel);

			// View colorPreviewBorder =
			// convertView.findViewById(R.id.settings_dialog_fontcolor_item_border);
			int borderResId = 0;
			if (position == mSelectedIndex)
				borderResId = R.drawable.settings_fontcolor_preview_border_selected;
			colorPreviewPanel.setBackgroundResource(borderResId);

			return convertView;
		}

		// Handle clicks on the options.
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			mSelectedIndex = position;
			this.notifyDataSetChanged();
			if (mOnItemSelectedListener != null)
				mOnItemSelectedListener.onItemSelected((FontColorProfile) getItem(mSelectedIndex));
		}

		/** @return Index of the selected option. */
		public int getSelectedIndex() {
			return mSelectedIndex;
		}

	}
}
