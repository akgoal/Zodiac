package com.deakishin.zodiac.controller.mainscreen.dialogs.searchdialog;

import java.util.ArrayList;
import java.util.Map;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.CustomDialogFragment;
import com.deakishin.zodiac.model.ciphermanager.CipherManager;
import com.deakishin.zodiac.model.ciphermodel.Model;
import com.deakishin.zodiac.model.settings.Settings;
import com.deakishin.zodiac.model.settings.SettingsPersistent;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

/** Dialog for managing search in the cipher. */
public class SearchDialogFragment extends CustomDialogFragment {

	/* Ids for child fragments. */
	private static final String DIALOG_WARNING = "warning";

	/* Widgets. */
	private EditText mEditText;
	private View mDirectionsPanel;
	private ToggleButton mLeftToggleButton;
	private ToggleButton mUpToggleButton;
	private ToggleButton mRightToggleButton;
	private ToggleButton mDownToggleButton;
	private CheckBox mKeepBindingCheckBox;
	private CheckBox mHomophonicCheckBox;
	private ProgressBar mProgressBar;
	private ImageButton mHelpButton;
	private TextView mHelpKeepBindingTextView;
	private TextView mHelpHomophonicTextView;
	private View mMainPanel;
	private View mErrorMemoryView;

	/* Application settings. */
	private Settings mSettings;
	private SettingsPersistent mSettingsPersistent;

	/* Objects that performs the search in the background. */
	private SearchTask mSearchTask;
	/* Search is in process. */
	private boolean mTaskIsRunning = false;

	/* Displayed dialog. */
	private Dialog mDialog;

	/* Number of query words that causes warning being shown. */
	private static final int WORDS_COUNT_TO_WARN = 3;

	/* Result must be sent on resume and then dialog has to be dismissed. */
	private boolean mSetResultAndDismissOnResume = false;

	/* Help needs to be shown. */
	private boolean mShowHelp = false;

	/* Memory error has to be shown. */
	private boolean mShowMemoryError = false;

	public SearchDialogFragment() {
		super();

		setRetainInstance(true);

		mSettings = Settings.getInstance();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_search, null);

		mSettingsPersistent = SettingsPersistent.getInstance(getActivity());

		mMainPanel = v.findViewById(R.id.search_main_panel);
		mErrorMemoryView = v.findViewById(R.id.search_memory_error_view);

		mEditText = (EditText) v.findViewById(R.id.search_editText);
		mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					startSearchActions();
					return true;
				}
				return false;
			}
		});

		mKeepBindingCheckBox = (CheckBox) v.findViewById(R.id.search_keep_binding_checkBox);
		mHomophonicCheckBox = (CheckBox) v.findViewById(R.id.search_homophonic_checkBox);

		mLeftToggleButton = (ToggleButton) v.findViewById(R.id.search_left_toggleButton);
		mUpToggleButton = (ToggleButton) v.findViewById(R.id.search_up_toggleButton);
		mRightToggleButton = (ToggleButton) v.findViewById(R.id.search_right_toggleButton);
		mDownToggleButton = (ToggleButton) v.findViewById(R.id.search_down_toggleButton);
		mDirectionsPanel = v.findViewById(R.id.search_directions_panel);
		mDirectionsPanel.setVisibility(View.GONE);

		mHelpKeepBindingTextView = (TextView) v.findViewById(R.id.search_help_keep_binding_textView);
		mHelpHomophonicTextView = (TextView) v.findViewById(R.id.search_help_homophonic_textView);

		mProgressBar = (ProgressBar) v.findViewById(R.id.search_progressBar);
		mProgressBar.setMax(Model.getMaxSearchProgress() - Model.getMinSearchProgress());

		mHelpButton = (ImageButton) v.findViewById(R.id.search_help_imageButton);

		enableWidgets(!mTaskIsRunning);

		setWidgetSettings();
		updateHelpViews();

		mHelpButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mShowHelp = !mShowHelp;
				updateHelpViews();
			}
		});

		mDialog = new AlertDialog.Builder(getActivity()).setView(v)
				.setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}
				}).create();
		showSoftKeyboard(!mTaskIsRunning);
		mDialog.setCanceledOnTouchOutside(true);
		return mDialog;
	}

	@Override
	public void onStart() {
		super.onStart();
		updateErrorState();
	}

	/* Update memory error visibility. */
	private void updateErrorState() {
		final AlertDialog d = (AlertDialog) getDialog();
		Button positiveButton = null;
		Button negativeButton = null;
		if (d != null) {
			positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
			negativeButton = (Button) d.getButton(Dialog.BUTTON_NEGATIVE);
		}
		if (mShowMemoryError) {
			mMainPanel.setVisibility(View.GONE);
			mErrorMemoryView.setVisibility(View.VISIBLE);
			if (positiveButton != null) {
				positiveButton.setVisibility(View.VISIBLE);
				positiveButton.setText(R.string.try_again);
				positiveButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mShowMemoryError = false;
						updateErrorState();
					}
				});
			}
			if (negativeButton != null) {
				negativeButton.setVisibility(View.VISIBLE);
				negativeButton.setText(R.string.cancel);
				negativeButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dismiss();
					}
				});
			}
		} else {
			mMainPanel.setVisibility(View.VISIBLE);
			mErrorMemoryView.setVisibility(View.GONE);
			if (positiveButton != null) {
				positiveButton.setVisibility(View.VISIBLE);
				if (mTaskIsRunning) {
					positiveButton.setText(R.string.cancel_search);
					positiveButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							startSearchActions();
						}
					});
				} else {
					positiveButton.setText(R.string.search);
					positiveButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							startSearchActions();
						}
					});
				}

			}
			if (negativeButton != null) {
				negativeButton.setVisibility(View.VISIBLE);
				negativeButton.setText(R.string.cancel);
				negativeButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dismiss();
					}
				});
			}
		}
	}

	/* Start searching task. */
	private void startSearchActions() {
		if (mTaskIsRunning) {
			mSearchTask.cancel(true);
			mSearchTask = null;
			mTaskIsRunning = false;
		} else {
			String text = mEditText.getText().toString();
			if (text == null || text.length() < 1)
				return;
			String[] words = text.split(" ");
			if (words.length == 0) {
				return;
			}

			/*
			 * if (words.length >= WORDS_COUNT_TO_WARN) { if
			 * (!mSettingsPersistent.isSearchWarningDontShow()) { showWarning();
			 * return; } }
			 */

			saveWidgetSettings();

			showSoftKeyboard(false);

			Model model = CipherManager.getInstance(getActivity()).getCipherModel();

			SearchParams params = new SearchParams();
			params.setWords(words);
			params.setKeepBinding(mKeepBindingCheckBox.isChecked());
			params.setHomophonic(mHomophonicCheckBox.isChecked());
			/*
			 * params.setSearchLeft(mLeftToggleButton.isChecked());
			 * params.setSearchUp(mUpToggleButton.isChecked());
			 * params.setSearchRight(mRightToggleButton.isChecked());
			 * params.setSearchDown(mDownToggleButton.isChecked());
			 */
			params.setSearchUp(false);
			params.setSearchRight(true);
			params.setSearchDown(false);
			params.setSearchLeft(false);
			params.setBinding(model.getBindingManager().getBinding());

			model.getBindingManager().setTmpSearchWords(words);

			mSearchTask = new SearchTask();
			mSearchTask.execute(params);
			mTaskIsRunning = true;
		}
		enableWidgets(!mTaskIsRunning);
		updateErrorState();
	}

	/* Show warning that there are too many words in the search query. */
	private void showWarning() {
		FragmentManager fm = getActivity().getSupportFragmentManager();
		WarningDialogFragment dialog = new WarningDialogFragment();
		dialog.show(fm, DIALOG_WARNING);
	}

	/* Set visibility of the keyboard. */
	private void showSoftKeyboard(boolean toShow) {
		int code;
		if (toShow) {
			code = LayoutParams.SOFT_INPUT_STATE_VISIBLE;
		} else {
			code = LayoutParams.SOFT_INPUT_STATE_HIDDEN;
		}
		mDialog.getWindow().setSoftInputMode(code);
	}

	/* Update widgets regarding help info. */
	private void updateHelpViews() {
		if (mShowHelp) {
			mHelpKeepBindingTextView.setVisibility(View.VISIBLE);
			mHelpHomophonicTextView.setVisibility(View.VISIBLE);
			mHelpButton.setImageResource(R.drawable.ic_questionmark_selected);
		} else {
			mHelpKeepBindingTextView.setVisibility(View.GONE);
			mHelpHomophonicTextView.setVisibility(View.GONE);
			mHelpButton.setImageResource(R.drawable.ic_questionmark);
		}
	}

	/* Set widgets enability. */
	private void enableWidgets(boolean toEnable) {
		mEditText.setEnabled(toEnable);
		mKeepBindingCheckBox.setEnabled(toEnable);
		mHomophonicCheckBox.setEnabled(toEnable);
		mLeftToggleButton.setEnabled(toEnable);
		mUpToggleButton.setEnabled(toEnable);
		mDownToggleButton.setEnabled(toEnable);
		mRightToggleButton.setEnabled(toEnable);

		if (toEnable) {
			mProgressBar.setProgress(0);
		}
	}

	/* Set widgets params from the settings. */
	private void setWidgetSettings() {
		mKeepBindingCheckBox.setChecked(mSettings.isSearchKeepBinding());
		mHomophonicCheckBox.setChecked(mSettings.isSearchHomophonic());
		mLeftToggleButton.setChecked(mSettings.isSearchLeft());
		mUpToggleButton.setChecked(mSettings.isSearchUp());
		mRightToggleButton.setChecked(mSettings.isSearchRight());
		mDownToggleButton.setChecked(mSettings.isSearchDown());
		mEditText.setText(mSettings.getSearchString());
	}

	/* Save widgets params to the settings. */
	private void saveWidgetSettings() {
		mSettings.setSearchKeepBinding(mKeepBindingCheckBox.isChecked());
		mSettings.setSearchHomophonic(mHomophonicCheckBox.isChecked());
		mSettings.setSearchLeft(mLeftToggleButton.isChecked());
		mSettings.setSearchUp(mUpToggleButton.isChecked());
		mSettings.setSearchRight(mRightToggleButton.isChecked());
		mSettings.setSearchDown(mDownToggleButton.isChecked());
		mSettings.setSearchString(mEditText.getText().toString());
	}

	/* Stop searching when the dialog is dismissed. */
	@Override
	public void onDismiss(DialogInterface dialog) {
		if (mSearchTask != null)
			mSearchTask.cancel(true);
		super.onDismiss(dialog);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mSetResultAndDismissOnResume) {
			mSetResultAndDismissOnResume = false;
			setResultAndDismiss();
		}
		updateErrorState();
	}

	/* Send result to the target fragment and dismiss. */
	private void setResultAndDismiss() {
		if (!isResumed()) {
			mSetResultAndDismissOnResume = true;
			return;
		}
		getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
		dismiss();
	}

	/* Show memory error. */
	private void showOutOfMemoryError() {
		mShowMemoryError = true;
		if (isResumed()) {
			updateErrorState();
		}
	}

	/** Search parameters. */
	private class SearchParams {
		private String[] mWords;
		private boolean mKeepBinding;
		private boolean mHomophonic;
		private boolean mSearchLeft;
		private boolean mSearchUp;
		private boolean mSearchRight;
		private boolean mSearchDown;
		private Map<Integer, Character> mBinding;

		public SearchParams() {
		}

		public String[] getWords() {
			return mWords;
		}

		public void setWords(String[] words) {
			mWords = words;
		}

		public boolean isKeepBinding() {
			return mKeepBinding;
		}

		public void setKeepBinding(boolean keepBinding) {
			mKeepBinding = keepBinding;
		}

		public boolean isHomophonic() {
			return mHomophonic;
		}

		public void setHomophonic(boolean homophonic) {
			mHomophonic = homophonic;
		}

		public boolean isSearchLeft() {
			return mSearchLeft;
		}

		public void setSearchLeft(boolean searchLeft) {
			mSearchLeft = searchLeft;
		}

		public boolean isSearchUp() {
			return mSearchUp;
		}

		public void setSearchUp(boolean searchUp) {
			mSearchUp = searchUp;
		}

		public boolean isSearchRight() {
			return mSearchRight;
		}

		public void setSearchRight(boolean searchRight) {
			mSearchRight = searchRight;
		}

		public boolean isSearchDown() {
			return mSearchDown;
		}

		public void setSearchDown(boolean searchDown) {
			mSearchDown = searchDown;
		}

		public Map<Integer, Character> getBinding() {
			return mBinding;
		}

		public void setBinding(Map<Integer, Character> binding) {
			mBinding = binding;
		}
	}

	/** {@link AsyncTask} for performing searching in the background. */
	private class SearchTask extends AsyncTask<SearchParams, Integer, ArrayList<Map<Integer, Character>>> {

		/*
		 * Minimum amount of memory in megabytes, that needs to remain free
		 * during the search.
		 */
		private static final int MIN_AVAILABLE_MEMORY_IN_MB = 2;

		/* Search ended with a memory error. */
		private boolean mRunOutOfMemory = false;

		@Override
		protected ArrayList<Map<Integer, Character>> doInBackground(SearchParams... arg0) {

			SearchParams params = arg0[0];
			try {
				return CipherManager.getInstance(getActivity()).getCipherModel().searchWords(params.getWords(),
						params.isKeepBinding() ? params.getBinding() : null, params.isHomophonic(),
						params.isSearchLeft(), params.isSearchUp(), params.isSearchRight(), params.isSearchDown(),
						new Model.SearchProgressListener() {
							@Override
							public void onProgressChanged(int progressValue) {
								publishProgress(progressValue);
							}
						}, new Model.SearchCancelIndicator() {
							@Override
							public boolean isCancelled() {
								if (outOfMemory()) {
									mRunOutOfMemory = true;
									return true;
								}
								return SearchTask.this.isCancelled();
							}
						});
			} catch (OutOfMemoryError e) {
				mRunOutOfMemory = true;
				return null;
			}
		}

		/* Indicates if there is too little memory available. */
		private boolean outOfMemory() {

			final Runtime runtime = Runtime.getRuntime();
			final long usedMem = (runtime.totalMemory() - runtime.freeMemory());
			final long maxHeapSize = runtime.maxMemory();
			final long availHeapSpaceInMb = (maxHeapSize - usedMem) / 1048576L;

			return availHeapSpaceInMb < MIN_AVAILABLE_MEMORY_IN_MB;
		}

		@Override
		protected void onProgressUpdate(Integer... params) {
			if (isCancelled())
				return;
			if (params.length < 1)
				return;
			mProgressBar.setProgress(params[0] - Model.getMinSearchProgress());
		}

		@Override
		protected void onPostExecute(ArrayList<Map<Integer, Character>> res) {
			if (isCancelled())
				return;
			mTaskIsRunning = false;
			if (mRunOutOfMemory) {
				enableWidgets(!mTaskIsRunning);
				System.gc();
				showOutOfMemoryError();
				return;
			}
			if (res != null) {
				CipherManager.getInstance(getActivity()).getCipherModel().getBindingManager().setSearchResults(res);
				setResultAndDismiss();
			} else
				enableWidgets(!mTaskIsRunning);
		}
	}

}
