package com.deakishin.zodiac.controller.boardscreen.ciphercreatingscreen;

import java.io.IOException;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.ConfirmationDialogFragment;
import com.deakishin.zodiac.controller.MessageDialogFragment;
import com.deakishin.zodiac.controller.boardscreen.CipherPreviewView;
import com.deakishin.zodiac.model.ciphergenerator.CipherGenerator;
import com.deakishin.zodiac.services.boardservice.BoardCipher;
import com.deakishin.zodiac.services.boardservice.BoardServiceI;
import com.deakishin.zodiac.services.boardservice.BoardServiceImpl;
import com.deakishin.zodiac.services.userservice.User;
import com.deakishin.zodiac.services.userservice.UserService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

/** Fragment for managing cipher creating process. */
public class CipherCreatingFragment extends Fragment {

	/* Ids for child fragments. */
	private static final String DIALOG_PARAMS_HELP = "paramsHelp";
	private static final String DIALOG_DIFFICULTY_HELP = "difficultyHelp";
	private static final String DIALOG_ERROR_UPLOAD = "errorUpload";
	private static final String DIALOG_SUCCESS_UPLOAD = "successUpload";

	/* Request codes for child fragments. */
	private static final int REQUEST_SUCCESS_UPLOAD = 10;
	private static final int REQUEST_TRY_AGAIN = 11;

	/* Max and min lengths for input values. */
	private static final int MAX_TITLE = 50;
	private static final int MAX_DESCR = 500;
	private static final int MAX_PLAINTEXT = 500;
	private static final int MIN_PLAINTEXT = 12;

	/* Padding in edit fields. */
	private static final int EDITTEXT_PADDING = 8;

	/* Widgets. */
	private View mCipherCreatingLayout;
	private TextView mTitleLabelTextView;
	private EditText mTitleEditText;
	private TextView mTitleCounterTextView;
	private TextView mDescrLabelTextView;
	private EditText mDescrEditText;
	private TextView mDescrCounterTextView;
	private TextView mPlaintextLabelTextView;
	private EditText mPlaintextEditText;
	private TextView mPlaintextErrorLenTextView;
	private TextView mPlaintextErrorLangTextView;
	private TextView mPlaintextCounterTextView;
	private CheckBox mHomophonicCheckBox;
	private ImageButton mGenerationParamsHelpImageButton;
	private Button mGenerateButton;
	private TextView mPreviewErrorTextView;
	private View mPreviewPanel;
	private TextView mDifficultyTextView;
	private ImageButton mDifficultyHelpImageButton;
	private CipherPreviewView mPreviewView;
	private Button mUploadButton;
	private ScrollView mScrollView;
	private View mGeneratingPanel;
	private SwitchCompat mSolutionSwitch;

	/* Flags for errors and statuses. */
	private boolean mTitleCounterError = false;
	private boolean mDescrCounterError = false;
	private boolean mPlaintextCounterError = false;
	private boolean mPlaintextInputLenError = false;
	private boolean mPlaintextInputLangError = false;
	private boolean mPreviewError = false;

	/* Focus indicators for widgets. */
	private boolean mTitleInFocus = false;
	private boolean mDescrInFocus = false;
	private boolean mPlaintextInFocus = false;

	/* Flag indicating that uploading is in process. */
	private boolean mTaskIsRunning = false;

	/* Flags indicating if error or success messages must be show on resume. */
	private boolean mShowUploadErrorOnResume = false;
	private boolean mShowUploadSuccessOnResume = false;

	/* Cipher generator. */
	private CipherGenerator mGenerator;

	/* Current generated cipher. */
	private CipherGenerator.GeneratedCipher mGeneratedCipher;

	/* Board service to upload the cipher. */
	private BoardServiceI mBoardService;

	/* Current logged-in user. */
	private User mUser;

	/* Flag indicating the the fragment was just created. */
	private boolean mJustCreated;

	/**
	 * The callback interface to receive a callback when a cipher is
	 * successfully uploaded.
	 */
	public static interface SuccessCallback {
		/**
		 * Invoked when a cipher is successfully uploaded.
		 */
		public void onUploadSuccess();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setHasOptionsMenu(true);

		mGenerator = CipherGenerator.getInstance(getActivity());
		mBoardService = BoardServiceImpl.getImpl(getActivity());
		mUser = UserService.getInstance(getActivity()).getUser();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.board_fragment_cipher_creating, parent, false);

		mJustCreated = true;

		mCipherCreatingLayout = v.findViewById(R.id.cipher_creating_layout);

		mTitleLabelTextView = (TextView) v.findViewById(R.id.cipher_creating_title_label_textView);
		mTitleEditText = (EditText) v.findViewById(R.id.cipher_creating_title_editText);
		mTitleCounterTextView = (TextView) v.findViewById(R.id.cipher_creating_title_counter_textView);
		mTitleEditText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if (mJustCreated)
					return;
				updateTitleViews();
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});
		mTitleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				mTitleInFocus = hasFocus;
				updateTitleViews();
			}
		});

		mDescrLabelTextView = (TextView) v.findViewById(R.id.cipher_creating_description_label_textView);
		mDescrEditText = (EditText) v.findViewById(R.id.cipher_creating_description_editText);
		mDescrCounterTextView = (TextView) v.findViewById(R.id.cipher_creating_description_counter_textView);
		mDescrEditText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if (mJustCreated)
					return;
				updateDescrViews();
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});
		mDescrEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				mDescrInFocus = hasFocus;
				updateDescrViews();
			}
		});

		mPlaintextLabelTextView = (TextView) v.findViewById(R.id.cipher_creating_plaintext_label_textView);
		mPlaintextEditText = (EditText) v.findViewById(R.id.cipher_creating_plaintext_editText);
		mPlaintextErrorLenTextView = (TextView) v.findViewById(R.id.cipher_creating_plaintext_error_len_textView);
		mPlaintextErrorLenTextView.setText(
				getActivity().getString(R.string.cipher_creating_plaintext_requirements_length, MIN_PLAINTEXT));
		mPlaintextErrorLangTextView = (TextView) v.findViewById(R.id.cipher_creating_plaintext_error_lang_textView);
		mPlaintextCounterTextView = (TextView) v.findViewById(R.id.cipher_creating_plaintext_counter_textView);
		mPlaintextEditText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if (mJustCreated)
					return;
				clearGeneratedInfo();
				mPlaintextInputLenError = false;
				mPlaintextInputLangError = !confirmPlaintextIsValid();
				updatePlaintextViews();
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});
		mPlaintextEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				mPlaintextInFocus = hasFocus;
				updatePlaintextViews();
			}
		});

		mPreviewView = (CipherPreviewView) v.findViewById(R.id.cipher_creating_previewView);
		mPreviewErrorTextView = (TextView) v.findViewById(R.id.cipher_creating_generated_error_textView);
		mPreviewPanel = v.findViewById(R.id.cipher_creating_preview_panel);

		mHomophonicCheckBox = (CheckBox) v.findViewById(R.id.cipher_creating_homophonic_checkBox);

		mGenerationParamsHelpImageButton = (ImageButton) v.findViewById(R.id.cipher_creating_params_help_imageButton);
		mGenerationParamsHelpImageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showGenerationParamsHelp();
			}
		});

		mGenerateButton = (Button) v.findViewById(R.id.cipher_creating_generate_button);
		mGenerateButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				hideKeyboard();

				if (mPlaintextCounterError || mPlaintextInputLangError || mPlaintextInputLenError)
					return;

				if (mPlaintextEditText.getText() == null || mPlaintextEditText.getText().toString() == null)
					return;

				String plaintext = getPlaintextValidString();

				if (plaintext.length() < MIN_PLAINTEXT) {
					mPlaintextInputLenError = true;
					updatePlaintextViews();
					return;
				}

				// Generate cipher and update preview.
				clearGeneratedInfo();
				mGeneratedCipher = mGenerator.generateCipher(plaintext, mHomophonicCheckBox.isChecked());
				updatePreview();
				mPreviewError = mGeneratedCipher == null;
				updatePreviewViews();
				scrollToGeneratingPanel();
			}
		});

		mDifficultyTextView = (TextView) v.findViewById(R.id.cipher_creating_difficulty_textView);
		mDifficultyHelpImageButton = (ImageButton) v.findViewById(R.id.cipher_creating_difficulty_help_imageButton);
		mDifficultyHelpImageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDifficultyHelp();
			}
		});

		mSolutionSwitch = (SwitchCompat) v.findViewById(R.id.cipher_creating_solution_switch);
		mSolutionSwitch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPreviewView.isSolutionShown()) {
					mPreviewView.setShowSolution(false);
				} else {
					mPreviewView.setShowSolution(true);
				}
				updatePreviewViews();
			}
		});

		mScrollView = (ScrollView) v.findViewById(R.id.cipher_creating_scrollView);
		mGeneratingPanel = v.findViewById(R.id.cipher_creating_generate_panel);

		updateViews();

		updatePreview();
		// mPreviewView.setCipherImage(mGeneratedCipher);

		return v;
	}

	/**
	 * Indicates if any changes has been made.
	 * 
	 * @return True if any changes has been made.
	 */
	public boolean changesAreMade() {
		return (!editTextIsEmpty(mTitleEditText) || !editTextIsEmpty(mDescrEditText)
				|| !editTextIsEmpty(mPlaintextEditText) || mGeneratedCipher != null);
	}

	/* Scroll to generating panel. */
	private void scrollToGeneratingPanel() {
		mScrollView.smoothScrollTo(0, (int) mGeneratingPanel.getY());
	}

	/* Start uploading generated cipher if everything's valid. */
	private void startUpload() {
		hideKeyboard();

		if (mUser == null)
			return;

		if (mTitleCounterError || mDescrCounterError || mPlaintextCounterError || mPlaintextInputLangError
				|| mPlaintextInputLenError)
			return;

		if (mGeneratedCipher == null) {
			mPreviewError = true;
			updatePreviewViews();
			return;
		}

		String title = null;
		if (mTitleEditText.getText() != null)
			title = mTitleEditText.getText().toString();
		if (title == null || title.equals("")) {
			title = getActivity().getString(R.string.cipher_creating_untitled);
		}

		String descr = null;
		if (mDescrEditText.getText() != null)
			descr = mDescrEditText.getText().toString();
		if (descr == null)
			descr = "";

		BoardCipher boardCipher = new BoardCipher();
		boardCipher.setTitle(title);
		boardCipher.setAuthor(mUser);
		boardCipher.setDescription(descr);
		boardCipher.setCipherMarkup(mGeneratedCipher.getMarkup());
		boardCipher.setCorrectAnswer(mGeneratedCipher.getPlainText());
		boardCipher.setDifficulty(mGeneratedCipher.getDifficulty());

		new UploadTask().execute(boardCipher);
	}

	/* Update widgets according to current status and errors. */
	private void updateViews() {
		if (mTaskIsRunning) {
			if (mUploadButton != null)
				mUploadButton.setText(R.string.cipher_creating_uploading);
			enableViews(false);
		} else {
			if (mUploadButton != null)
				mUploadButton.setText(R.string.cipher_creating_upload);
			enableViews(true);
		}
		updateTitleViews();
		updateDescrViews();
		updatePlaintextViews();
		updatePreviewViews();
	}

	/* Update widgets visibilities. */
	private void enableViews(boolean enable) {
		if (mUploadButton != null)
			mUploadButton.setEnabled(enable);
		mTitleEditText.setEnabled(enable);
		mDescrEditText.setEnabled(enable);
		mPlaintextEditText.setEnabled(enable);
		mGenerateButton.setEnabled(enable);
		mHomophonicCheckBox.setEnabled(enable);
		mGenerationParamsHelpImageButton.setEnabled(enable);
		mDifficultyHelpImageButton.setEnabled(enable);
		if (!enable) {
			mCipherCreatingLayout.requestFocus();
		}
	}

	/* Update widgets regarding cipher's title. */
	private void updateTitleViews() {
		updateViewVisibility(mTitleLabelTextView, !editTextIsEmpty(mTitleEditText));

		mTitleCounterError = updateCounterView(mTitleEditText, mTitleCounterTextView, MAX_TITLE);
		updateViewVisibility(mTitleCounterTextView, mTitleCounterError || mTitleInFocus);

		updateTextViewState(mTitleLabelTextView, mTitleCounterError, mTitleInFocus);
		updateEditTextErrorState(mTitleEditText, mTitleCounterError);
	}

	/*
	 * Update counterTextView with the symbol count in the given editText.
	 * Return true if this count exceeds its limit. isPlaintext indicates if the
	 * editText is the field for cipher's plaintext.
	 */
	private boolean updateCounterView(EditText editText, TextView counterTextView, int counterLimit,
			boolean isPlaintext) {
		int counter = 0;
		if (!editTextIsEmpty(editText)) {
			String text;
			if (isPlaintext) {
				text = getPlaintextValidString();
			} else {
				text = editText.getText().toString();
			}
			counter = text == null ? 0 : text.length();
		}
		counterTextView.setText(getActivity().getString(R.string.sign_up_counter_limit, counter, counterLimit));
		boolean counterLimitExceeded = counter > counterLimit;
		updateTextViewState(counterTextView, counterLimitExceeded, false);
		return counterLimitExceeded;
	}

	private boolean updateCounterView(EditText editText, TextView counterTextView, int counterLimit) {
		return updateCounterView(editText, counterTextView, counterLimit, false);
	}

	/* Update editText according to error presence. */
	private void updateEditTextErrorState(EditText editText, boolean error) {
		int backgroundResId = 0;
		if (error)
			backgroundResId = R.drawable.log_in_input_border_error;
		else
			backgroundResId = R.drawable.log_in_input_border;
		editText.setBackgroundResource(backgroundResId);
		editText.setPadding(0, EDITTEXT_PADDING, 0, EDITTEXT_PADDING);
	}

	/* Update textView according to error and focus presence. */
	private void updateTextViewState(TextView textView, boolean error, boolean focused) {
		int textColorResId = 0;
		if (error) {
			textColorResId = R.color.form_field_error;
		} else {
			if (focused)
				textColorResId = R.color.form_field_focused;
			else
				textColorResId = R.color.form_field_normal;
		}
		textView.setTextColor(ContextCompat.getColor(getActivity(), textColorResId));
	}

	/* Indicates if editText is empty. */
	private boolean editTextIsEmpty(EditText editText) {
		return editText.getText() == null || editText.getText().length() == 0 || editText.getText().equals("");
	}

	/* Set view visibility. */
	private void updateViewVisibility(View view, boolean visible) {
		if (visible) {
			view.setVisibility(View.VISIBLE);
		} else {
			view.setVisibility(View.INVISIBLE);
		}
	}

	/* Update widgets regarding cipher's desription. */
	private void updateDescrViews() {
		updateViewVisibility(mDescrLabelTextView, !editTextIsEmpty(mDescrEditText));

		mDescrCounterError = updateCounterView(mDescrEditText, mDescrCounterTextView, MAX_DESCR);
		updateViewVisibility(mDescrCounterTextView, mDescrCounterError || mDescrInFocus);

		updateTextViewState(mDescrLabelTextView, mDescrCounterError, mDescrInFocus);
		updateEditTextErrorState(mDescrEditText, mDescrCounterError);
	}

	/* Update widgets regarding cipher's plaintext. */
	private void updatePlaintextViews() {
		updateViewVisibility(mPlaintextLabelTextView, !editTextIsEmpty(mPlaintextEditText));

		mPlaintextCounterError = updateCounterView(mPlaintextEditText, mPlaintextCounterTextView, MAX_PLAINTEXT, true);
		updateViewVisibility(mPlaintextCounterTextView, mPlaintextCounterError || mPlaintextInFocus);

		updateTextViewState(mPlaintextErrorLangTextView, mPlaintextInputLangError, false);
		updateTextViewState(mPlaintextErrorLenTextView, mPlaintextInputLenError, false);

		boolean error = mPlaintextInputLenError || mPlaintextInputLangError || mPlaintextCounterError;
		updateTextViewState(mPlaintextLabelTextView, error, mPlaintextInFocus);
		updateEditTextErrorState(mPlaintextEditText, error);
	}

	/* Check if entered cipher's plaintext is valid. */
	private boolean confirmPlaintextIsValid() {

		if (mPlaintextEditText == null || mPlaintextEditText.getText() == null)
			return false;

		// String plaintext = mPlaintextEditText.getText().toString();
		String plaintext = getPlaintextValidString();
		if (plaintext == null || plaintext.equals(""))
			return true;
		return CipherGenerator.isStringValid(plaintext);
	}

	/* Get vaild string from entered cipher's plaintext. */
	private String getPlaintextValidString() {
		if (mPlaintextEditText == null || mPlaintextEditText.getText() == null)
			return null;

		String plaintext = mPlaintextEditText.getText().toString();
		return CipherGenerator.getValidString(plaintext);
	}

	/* Update widgets regarding generated cipher. */
	private void updatePreviewViews() {
		updateViewVisibility(mPreviewErrorTextView, mPreviewError);
		if (mPreviewError)
			mPreviewPanel.setBackgroundResource(R.drawable.cipher_creating_preview_error_border);
		else
			mPreviewPanel.setBackgroundResource(0);

		if (mGeneratedCipher == null) {
			mSolutionSwitch.setEnabled(false);
			mSolutionSwitch.setChecked(false);
		} else {
			mSolutionSwitch.setEnabled(true);
			if (mPreviewView.isSolutionShown()) {
				mSolutionSwitch.setChecked(true);
			} else {
				mSolutionSwitch.setChecked(false);
			}
		}
	}

	/* Update preview of generated cipher. */
	private void updatePreview() {
		if (mGeneratedCipher == null) {
			mPreviewView.setCipherImage(null);
			mDifficultyTextView.setText(getActivity().getString(R.string.cipher_creating_difficulty, (float) 0));
		} else {
			mPreviewView.setImages(mGeneratedCipher.getImages());
			mDifficultyTextView.setText(
					getActivity().getString(R.string.cipher_creating_difficulty, mGeneratedCipher.getDifficulty()));
		}
	}

	/* Clear generated cipher and all info about it. */
	private void clearGeneratedInfo() {
		mGeneratedCipher = null;
		updatePreview();
	}

	/* Hide keyboard. */
	private void hideKeyboard() {
		View view = getActivity().getCurrentFocus();
		if (view != null) {
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	/* Show dialog with connection error. */
	private void showConnectionError() {
		if (!this.isResumed()) {
			mShowUploadErrorOnResume = true;
			return;
		}
		DialogFragment dialog = ConfirmationDialogFragment.getInstance(
				getActivity().getString(R.string.cipher_creating_error_upload),
				getActivity().getString(R.string.try_again), REQUEST_TRY_AGAIN, false);
		dialog.setTargetFragment(this, REQUEST_TRY_AGAIN);
		dialog.show(getActivity().getSupportFragmentManager(), DIALOG_ERROR_UPLOAD);
	}

	/* Show dialog with help info about cipher generating parameters. */
	private void showGenerationParamsHelp() {
		FragmentManager fm = getActivity().getSupportFragmentManager();
		MessageDialogFragment dialog = MessageDialogFragment
				.getInstance(getActivity().getString(R.string.help_cipher_generating_params), false, true, false);
		dialog.show(fm, DIALOG_PARAMS_HELP);
	}

	/* Show dialog with help info about generated cipher's difficulty. */
	private void showDifficultyHelp() {
		FragmentManager fm = getActivity().getSupportFragmentManager();
		MessageDialogFragment dialog = MessageDialogFragment.getInstance(getActivity()
				.getString(R.string.help_cipher_generating_difficulty, CipherGenerator.MAX_HOMOPHONIC_DIFFICULTY_COEFF),
				false, true, false);
		dialog.show(fm, DIALOG_DIFFICULTY_HELP);
	}

	/* Show dialog about successful uploading. */
	private void showSuccessDialog() {
		if (!this.isResumed()) {
			mShowUploadSuccessOnResume = true;
			return;
		}
		FragmentManager fm = getActivity().getSupportFragmentManager();
		MessageDialogFragment dialog = MessageDialogFragment
				.getInstance(getActivity().getString(R.string.cipher_creating_success_upload), true);
		dialog.setTargetFragment(this, REQUEST_SUCCESS_UPLOAD);
		dialog.show(fm, DIALOG_SUCCESS_UPLOAD);
	}

	@Override
	public void onResume() {
		super.onResume();
		mJustCreated = false;
		updateViews();
		if (mShowUploadErrorOnResume) {
			showConnectionError();
			mShowUploadErrorOnResume = false;
		}
		if (mShowUploadSuccessOnResume) {
			showSuccessDialog();
			mShowUploadSuccessOnResume = false;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_SUCCESS_UPLOAD) {
			Activity activity = getActivity();
			if (activity instanceof SuccessCallback)
				((SuccessCallback) activity).onUploadSuccess();
			return;
		}
		if (requestCode == REQUEST_TRY_AGAIN) {
			if (resultCode != Activity.RESULT_OK)
				return;
			startUpload();
			return;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_cipher_creating, menu);
		MenuItem uploadItem = menu.findItem(R.id.menu_item_upload_cipher);

		View v = getActivity().getLayoutInflater().inflate(R.layout.board_fragment_cipher_creating_upload_panel, null);

		mUploadButton = (Button) v.findViewById(R.id.board_cipher_creating_upload_panel_button);
		mUploadButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startUpload();
			}
		});
		if (mTaskIsRunning) {
			mUploadButton.setText(R.string.cipher_creating_uploading);
			mUploadButton.setEnabled(false);
		} else {
			mUploadButton.setText(R.string.cipher_creating_upload);
			mUploadButton.setEnabled(true);
		}

		uploadItem.setActionView(v);
	}

	/** {@link AsyncTask} for uloading generated cipher to server in the background. */
	private class UploadTask extends AsyncTask<BoardCipher, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			mTaskIsRunning = true;
			updateViews();
		}

		@Override
		protected Boolean doInBackground(BoardCipher... arg0) {
			if (arg0.length != 1)
				return false;
			try {
				return mBoardService.addCipher(arg0[0], mUser);
			} catch (IOException e) {
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean res) {
			mTaskIsRunning = false;
			if (res == false) {
				showConnectionError();
			} else {
				showSuccessDialog();
			}
			updateViews();
		}
	}
}
