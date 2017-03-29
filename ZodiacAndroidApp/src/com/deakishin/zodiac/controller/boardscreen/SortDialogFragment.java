package com.deakishin.zodiac.controller.boardscreen;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.CustomDialogFragment;
import com.deakishin.zodiac.model.settings.SettingsPersistent;
import com.deakishin.zodiac.services.boardservice.BoardServiceI;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;

/**
 * Dialog for managing settings regarding the board's sorting and filtering.
 * Returns result to the target fragment: {@link Activity#RESULT_OK} if settings
 * are changed, {@link Activity#RESULT_CANCEL} otherwise.
 */
public class SortDialogFragment extends CustomDialogFragment {

	/* Application settings. */
	private SettingsPersistent mSettings;

	/* Widgets. */
	private CheckBox mHideImportedCheckBox;
	private RadioGroup mSortByOptionsRadioGroup;
	private RadioGroup mShowSolvedOptionsRadioGroup;

	public SortDialogFragment() {
		super();
		mSettings = SettingsPersistent.getInstance(getActivity());
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(R.layout.board_dialog_sortoptions, null);

		mSortByOptionsRadioGroup = (RadioGroup) v.findViewById(R.id.board_dialog_sort_sortby_radioGroup);
		switch (mSettings.getBoardSortSortByOption()) {
		case Date:
			mSortByOptionsRadioGroup.check(R.id.board_dialog_sort_sortby_date_radioButton);
			break;
		case Reward:
			mSortByOptionsRadioGroup.check(R.id.board_dialog_sort_sortby_difficulty_radioButton);
			break;
		case Popular:
			mSortByOptionsRadioGroup.check(R.id.board_dialog_sort_sortby_popular_radioButton);
			break;
		default:
			mSortByOptionsRadioGroup.check(-1);
			break;
		}

		mShowSolvedOptionsRadioGroup = (RadioGroup) v.findViewById(R.id.board_dialog_sort_filter_radioGroup);
		switch (mSettings.getBoardSortShowSolvedOption()) {
		case SolvedOnly:
			mShowSolvedOptionsRadioGroup.check(R.id.board_dialog_sort_filter_solvedOnly_radioButton);
			break;
		case UnsolvedOnly:
			mShowSolvedOptionsRadioGroup.check(R.id.board_dialog_sort_filter_unsolvedOnly_radioButton);
			break;
		case All:
			mShowSolvedOptionsRadioGroup.check(R.id.board_dialog_sort_filter_all_radioButton);
			break;
		default:
			mShowSolvedOptionsRadioGroup.check(-1);
			break;
		}

		mHideImportedCheckBox = (CheckBox) v.findViewById(R.id.board_dialog_sort_hide_imported_checkBox);
		mHideImportedCheckBox.setChecked(mSettings.isBoardSortHideImported());

		AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(v)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (changeOptions())
							sendResult(Activity.RESULT_OK);
						else
							sendResult(Activity.RESULT_CANCELED);
					}

				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						sendResult(Activity.RESULT_CANCELED);
					}

				}).create();
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	/*
	 * Change settings using values from widgets. If new values are not
	 * different from old ones, return false. Otherwise return true.
	 */
	private boolean changeOptions() {
		boolean optionsAreChanged = false;

		BoardServiceI.SortByOption selectedSortByOption = null;
		switch (mSortByOptionsRadioGroup.getCheckedRadioButtonId()) {
		case R.id.board_dialog_sort_sortby_date_radioButton:
			selectedSortByOption = BoardServiceI.SortByOption.Date;
			break;
		case R.id.board_dialog_sort_sortby_difficulty_radioButton:
			selectedSortByOption = BoardServiceI.SortByOption.Reward;
			break;
		case R.id.board_dialog_sort_sortby_popular_radioButton:
			selectedSortByOption = BoardServiceI.SortByOption.Popular;
			break;
		}
		if (selectedSortByOption != null
				&& selectedSortByOption.ordinal() != mSettings.getBoardSortSortByOption().ordinal()) {
			optionsAreChanged = true;
			mSettings.setBoardSortSortByOption(selectedSortByOption);
		}

		BoardServiceI.ShowSolvedOption selectedShowSolvedOption = null;
		switch (mShowSolvedOptionsRadioGroup.getCheckedRadioButtonId()) {
		case R.id.board_dialog_sort_filter_solvedOnly_radioButton:
			selectedShowSolvedOption = BoardServiceI.ShowSolvedOption.SolvedOnly;
			break;
		case R.id.board_dialog_sort_filter_unsolvedOnly_radioButton:
			selectedShowSolvedOption = BoardServiceI.ShowSolvedOption.UnsolvedOnly;
			break;
		case R.id.board_dialog_sort_filter_all_radioButton:
			selectedShowSolvedOption = BoardServiceI.ShowSolvedOption.All;
			break;
		}
		if (selectedShowSolvedOption != null
				&& selectedShowSolvedOption.ordinal() != mSettings.getBoardSortShowSolvedOption().ordinal()) {
			optionsAreChanged = true;
			mSettings.setBoardSortShowSolvedOption(selectedShowSolvedOption);
		}

		boolean selectedHideImportedOption = mHideImportedCheckBox.isChecked();
		if (selectedHideImportedOption != mSettings.isBoardSortHideImported()) {
			optionsAreChanged = true;
			mSettings.setBoardSortHideImported(selectedHideImportedOption);
		}
		return optionsAreChanged;
	}

	/* Send result to the target fragment. */
	private void sendResult(int resultCode) {
		if (getTargetFragment() == null)
			return;
		getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, null);
		this.dismiss();
	}
}
