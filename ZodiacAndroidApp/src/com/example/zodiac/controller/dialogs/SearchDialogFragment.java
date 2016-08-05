package com.example.zodiac.controller.dialogs;

import java.util.ArrayList;
import java.util.Map;

import com.example.zodiac.R;
import com.example.zodiac.model.Model;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ToggleButton;

public class SearchDialogFragment extends DialogFragment {
	/*
	 * Диалог поиска.
	 */

	/* Модель. */
	private Model mModel;

	/* Виджеты. */
	private Button mSearchButton;
	private EditText mEditText;
	private ToggleButton mLeftToggleButton;
	private ToggleButton mUpToggleButton;
	private ToggleButton mRightToggleButton;
	private ToggleButton mDownToggleButton;
	private CheckBox mKeepBindingCheckBox;
	private CheckBox mHomophonicCheckBox;
	private ProgressBar mProgressBar;

	/* Задание поиска, выполняемого асинхронно, и флаг его выполнения. */
	private SearchTask mSearchTask;
	private boolean mTaskIsRunning = false;

	/* Выводимый диалог. */
	private Dialog mDialog;

	public SearchDialogFragment() {
		super();

		setRetainInstance(true);

		mModel = Model.getInstance(getActivity());
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_search, null);

		mEditText = (EditText) v.findViewById(R.id.search_editText);

		mKeepBindingCheckBox = (CheckBox) v.findViewById(R.id.search_keep_binding_checkBox);
		mHomophonicCheckBox = (CheckBox) v.findViewById(R.id.search_homophonic_checkBox);

		mLeftToggleButton = (ToggleButton) v.findViewById(R.id.search_left_toggleButton);
		mUpToggleButton = (ToggleButton) v.findViewById(R.id.search_up_toggleButton);
		mRightToggleButton = (ToggleButton) v.findViewById(R.id.search_right_toggleButton);
		mDownToggleButton = (ToggleButton) v.findViewById(R.id.search_down_toggleButton);

		mProgressBar = (ProgressBar) v.findViewById(R.id.search_progressBar);
		mProgressBar.setMax(Model.getMaxSearchProgress() - Model.getMinSearchProgress());

		mSearchButton = (Button) v.findViewById(R.id.search_button);

		enableWidgets(!mTaskIsRunning);

		mSearchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mTaskIsRunning) {
					mSearchTask.cancel(true);
					mTaskIsRunning = false;
				} else {
					String text = mEditText.getText().toString();
					if (text == null || text.length() < 1)
						return;
					String[] words = text.split(" ");
					if (words.length == 0)
						return;

					showSoftKeyboard(false);

					SearchParams params = new SearchParams();
					params.setWords(words);
					params.setKeepBinding(mKeepBindingCheckBox.isChecked());
					params.setHomophonic(mHomophonicCheckBox.isChecked());
					params.setSearchLeft(mLeftToggleButton.isChecked());
					params.setSearchUp(mUpToggleButton.isChecked());
					params.setSearchRight(mRightToggleButton.isChecked());
					params.setSearchDown(mDownToggleButton.isChecked());
					params.setBinding(mModel.getBindingManager().getBinding());

					mModel.getBindingManager().setTmpSearchWords(words);

					mSearchTask = new SearchTask();
					mSearchTask.execute(params);
					mTaskIsRunning = true;
				}
				enableWidgets(!mTaskIsRunning);
			}
		});

		mDialog = new AlertDialog.Builder(getActivity()).setView(v).create();
		showSoftKeyboard(true);
		return mDialog;
	}

	/* Показ/скрытие клавиатуры. */
	private void showSoftKeyboard(boolean toShow) {
		int code;
		if (toShow) {
			code = LayoutParams.SOFT_INPUT_STATE_VISIBLE;
		} else {
			code = LayoutParams.SOFT_INPUT_STATE_HIDDEN;
		}
		mDialog.getWindow().setSoftInputMode(code);
	}

	/* Метод, закрывающий/открывающий виджеты для редактирования. */
	private void enableWidgets(boolean toEnable) {
		mEditText.setEnabled(toEnable);
		mKeepBindingCheckBox.setEnabled(toEnable);
		mHomophonicCheckBox.setEnabled(toEnable);
		mLeftToggleButton.setEnabled(toEnable);
		mUpToggleButton.setEnabled(toEnable);
		mDownToggleButton.setEnabled(toEnable);
		mRightToggleButton.setEnabled(toEnable);

		if (toEnable) {
			mSearchButton.setText(R.string.search);
			mProgressBar.setProgress(0);
		} else
			mSearchButton.setText(R.string.cancel_search);
	}

	/* При закрытии диалога останавливаем задание, если оно выполняется. */
	@Override
	public void onDismiss(DialogInterface dialog) {
		if (mSearchTask != null)
			mSearchTask.cancel(true);
		super.onDismiss(dialog);
	}

	/* Отправка результата родительскому фрагменту и закрытие диалога. */
	private void setResultAndDismiss() {
		getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
		dismiss();
	}

	/* Класс, содержащий параметры поиска. */
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

	/* Класс, создающий фоновый поток и выполняющий в нем операцию поиска. */
	private class SearchTask extends AsyncTask<SearchParams, Integer, ArrayList<Map<Integer, Character>>> {
		@Override
		protected ArrayList<Map<Integer, Character>> doInBackground(SearchParams... arg0) {

			SearchParams params = arg0[0];
			return mModel.searchWords(params.getWords(), params.isKeepBinding() ? params.getBinding() : null,
					params.isHomophonic(), params.isSearchLeft(), params.isSearchUp(), params.isSearchRight(),
					params.isSearchDown(), new Model.SearchProgressListener() {
						@Override
						public void onProgressChanged(int progressValue) {
							publishProgress(progressValue);
						}
					}, new Model.SearchCancelIndicator() {
						@Override
						public boolean isCancelled() {
							return SearchTask.this.isCancelled();
						}
					});

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
			if (res != null) {
				mModel.getBindingManager().setSearchResults(res);
			}
			mTaskIsRunning = false;
			enableWidgets(!mTaskIsRunning);
			setResultAndDismiss();
		}
	}

}
