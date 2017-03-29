package com.deakishin.zodiac.controller.loginscreen;

import java.io.IOException;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.ConfirmationDialogFragment;
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
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Fragment for displaying and managing controls for signing up (registering).
 */
public class SignUpFragment extends Fragment {

	/* Keys for dialogs. */
	private static final String DIALOG_CONNECTION_ERROR = "connectionError";

	/* Request codes for dialogs. */
	private static final int REQUEST_TRY_AGAIN = 20;

	/* Maximum length for input values. */
	private static final int USERNAME_LIMIT = 20;
	private static final int PASSWORD_MINIMUM = 6;
	private static final int PASSWORD_LIMIT = 20;

	/* Padding for input fields. */
	private static final int EDITTEXT_PADDING = 8;

	/* Widgets. */
	private View mSignUpLayout;
	private TextView mUsernameLabelTextView;
	private EditText mUsernameEditText;
	private TextView mUsernameErrorTextView;
	private TextView mUsernameCounterTextView;
	private TextView mPasswordLabelTextView;
	private EditText mPasswordEditText;
	private TextView mPasswordErrorTextView;
	private TextView mPasswordCounterTextView;
	private TextView mConfirmPasswordLabelTextView;
	private EditText mConfirmPasswordEditText;
	private TextView mConfirmPasswordErrorTextView;
	private Button mSignUpButton;

	/* Flags indicating that error have to be shown. */
	private boolean mShowEmptyUsernameError = false;
	private boolean mShowPasswordError = false;
	private boolean mShowConfirmPasswordError = false;
	private boolean mShowUsernameTakenError = false;
	private boolean mUsernameCounterError = false;
	private boolean mPasswordCounterError = false;

	/* Flags indicating that fields are in focus. */
	private boolean mUsernameInFocus = false;
	private boolean mPasswordInFocus = false;
	private boolean mConfirmPasswordInFocus = false;

	/* Flag indicating that connection error has to shown on resume. */
	private boolean mShowConnectionErrorOnResume = false;

	/* Flag indicating that signing-up is in process. */
	private boolean mTaskIsRunning = false;

	/* Board service for performing signing up. */
	private BoardServiceI mBoardService;

	/* User service for managing current user's status. */
	private UserService mUserService;

	/* Object that performs signing up. */
	private SignUpTask mSignUpTask;

	/* Flag indicating that the fragment was just created. */
	private boolean mJustCreated;

	/**
	 * The listener interface regarding signing-up process.
	 */
	public static interface OnSignUpListener {
		/**
		 * Invoked when signing-up is in process.
		 */
		public void onSigningUpStarted();

		/**
		 * Invoked when signing-up process has finished.
		 * 
		 * @param signedUp
		 *            True if user signed up successfully, false otherwise.
		 */
		public void onSigningUpFinished(boolean signedUp);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		mBoardService = BoardServiceImpl.getImpl(getActivity());
		mUserService = UserService.getInstance(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater layoutInflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = layoutInflater.inflate(R.layout.sign_up_fragment, null);

		mJustCreated = true;

		mSignUpLayout = v.findViewById(R.id.sign_up_layout);

		mUsernameLabelTextView = (TextView) v.findViewById(R.id.sign_up_username_label_textView);
		mUsernameEditText = (EditText) v.findViewById(R.id.sign_up_username_editText);
		mUsernameErrorTextView = (TextView) v.findViewById(R.id.sign_up_username_error_textView);
		mUsernameCounterTextView = (TextView) v.findViewById(R.id.sign_up_username_counter_textView);
		mUsernameEditText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if (mJustCreated)
					return;
				mShowEmptyUsernameError = false;
				mShowUsernameTakenError = false;
				updateUsernameViews();
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});
		mUsernameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				mUsernameInFocus = hasFocus;
				updateUsernameViews();
			}
		});

		mPasswordLabelTextView = (TextView) v.findViewById(R.id.sign_up_password_label_textView);
		mPasswordEditText = (EditText) v.findViewById(R.id.sign_up_password_editText);
		mPasswordErrorTextView = (TextView) v.findViewById(R.id.sign_up_password_error_textView);
		mPasswordErrorTextView
				.setText(getActivity().getString(R.string.sign_up_password_minimum_characters, PASSWORD_MINIMUM));
		mPasswordCounterTextView = (TextView) v.findViewById(R.id.sign_up_password_counter_textView);
		mPasswordEditText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if (mJustCreated)
					return;
				mShowPasswordError = false;
				updatePasswordViews();
				mShowConfirmPasswordError = false;
				updateConfirmPasswordViews();
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});
		mPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				mPasswordInFocus = hasFocus;
				updatePasswordViews();
			}
		});

		mConfirmPasswordLabelTextView = (TextView) v.findViewById(R.id.sign_up_confirm_password_label_textView);
		mConfirmPasswordEditText = (EditText) v.findViewById(R.id.sign_up_confirm_password_editText);
		mConfirmPasswordErrorTextView = (TextView) v.findViewById(R.id.sign_up_confirm_password_error_textView);
		mConfirmPasswordEditText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if (mJustCreated)
					return;
				mShowConfirmPasswordError = false;
				updateConfirmPasswordViews();
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});
		mConfirmPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				mConfirmPasswordInFocus = hasFocus;
				updateConfirmPasswordViews();
			}
		});

		mSignUpButton = (Button) v.findViewById(R.id.sign_up_button);
		mSignUpButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideKeyboard();
				if (mShowUsernameTakenError || mUsernameCounterError || mPasswordCounterError) {
					updateViews();
					return;
				}
				boolean invalidFieldValues = false;
				if (editTextIsEmpty(mUsernameEditText)) {
					mShowEmptyUsernameError = true;
					invalidFieldValues = true;
				}
				if (editTextIsEmpty(mPasswordEditText)
						|| mPasswordEditText.getText().toString().length() < PASSWORD_MINIMUM) {
					mShowPasswordError = true;
					invalidFieldValues = true;
				}
				if (!confirmPasswordIsValid()) {
					mShowConfirmPasswordError = true;
					invalidFieldValues = true;
				}
				if (invalidFieldValues) {
					updateViews();
					return;
				}

				mSignUpTask = new SignUpTask();
				mSignUpTask.execute(new String[] { mUsernameEditText.getText().toString(),
						mPasswordEditText.getText().toString() });
			}
		});

		updateViews();

		return v;
	}

	/* Update widgets. */
	private void updateViews() {
		if (mTaskIsRunning) {
			mSignUpButton.setText(R.string.signing_up);
			enableViews(false);
		} else {
			mSignUpButton.setText(R.string.sign_up);
			enableViews(true);
		}
		updateUsernameViews();
		updatePasswordViews();
		updateConfirmPasswordViews();
	}

	/* Update enability of widgets. */
	private void enableViews(boolean enable) {
		mSignUpButton.setEnabled(enable);
		mUsernameEditText.setEnabled(enable);
		mPasswordEditText.setEnabled(enable);
		mConfirmPasswordEditText.setEnabled(enable);
		if (!enable) {
			mSignUpLayout.requestFocus();
		}
	}

	/* Update widgets regarding username. */
	private void updateUsernameViews() {
		updateViewVisibility(mUsernameLabelTextView, !editTextIsEmpty(mUsernameEditText), false);

		boolean error = false;

		mUsernameCounterError = updateCounterView(mUsernameEditText, mUsernameCounterTextView, USERNAME_LIMIT);
		updateViewVisibility(mUsernameCounterTextView, mUsernameCounterError, false);
		error = mUsernameCounterError;

		if (mShowEmptyUsernameError || mShowUsernameTakenError) {
			updateViewVisibility(mUsernameErrorTextView, true, false);
			if (mShowEmptyUsernameError) {
				mUsernameErrorTextView.setText(R.string.sign_up_username_required);
			} else {
				if (mShowUsernameTakenError)
					mUsernameErrorTextView.setText(R.string.sign_up_username_is_taken);
			}
			error = true;
		} else {
			updateViewVisibility(mUsernameErrorTextView, false, false);
		}
		updateTextViewState(mUsernameLabelTextView, error, mUsernameInFocus);
		updateEditTextErrorState(mUsernameEditText, error);
	}

	/*
	 * Update counterTextView with symbol count from the editText. Returns true
	 * if the count exceed counterLimit.
	 */
	private boolean updateCounterView(EditText editText, TextView counterTextView, int counterLimit) {
		int counter = 0;
		if (!editTextIsEmpty(editText))
			counter = editText.getText().toString().length();
		counterTextView.setText(getActivity().getString(R.string.sign_up_counter_limit, counter, counterLimit));
		boolean counterLimitExceeded = counter > counterLimit;
		updateTextViewState(counterTextView, counterLimitExceeded, false);
		return counterLimitExceeded;
	}

	/* Update editText depending on error presence. */
	private void updateEditTextErrorState(EditText editText, boolean error) {
		int backgroundResId = 0;
		if (error)
			backgroundResId = R.drawable.log_in_input_border_error;
		else
			backgroundResId = R.drawable.log_in_input_border;
		editText.setBackgroundResource(backgroundResId);
		editText.setPadding(0, EDITTEXT_PADDING, 0, EDITTEXT_PADDING);
	}

	/*
	 * Update textView depending on error and focus presence.
	 */
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

	/* Update widgets regarding password. */
	private void updatePasswordViews() {
		updateViewVisibility(mPasswordLabelTextView, !editTextIsEmpty(mPasswordEditText), false);

		mPasswordCounterError = updateCounterView(mPasswordEditText, mPasswordCounterTextView, PASSWORD_LIMIT);
		updateViewVisibility(mPasswordCounterTextView, mPasswordCounterError, false);

		boolean error = mPasswordCounterError || mShowPasswordError;
		updateTextViewState(mPasswordLabelTextView, error, mPasswordInFocus);
		updateEditTextErrorState(mPasswordEditText, error);
		updateTextViewState(mPasswordErrorTextView, mShowPasswordError, false);
	}

	/* Update widgets regarding confirming password. */
	private void updateConfirmPasswordViews() {
		updateViewVisibility(mConfirmPasswordLabelTextView, !editTextIsEmpty(mConfirmPasswordEditText), false);
		updateViewVisibility(mConfirmPasswordErrorTextView, mShowConfirmPasswordError, false);
		updateEditTextErrorState(mConfirmPasswordEditText, mShowConfirmPasswordError);
		updateTextViewState(mConfirmPasswordLabelTextView, mShowConfirmPasswordError, mConfirmPasswordInFocus);
	}

	/* Checks if entered password and confirm-password are equal. */
	private boolean confirmPasswordIsValid() {
		if (mPasswordEditText.getText() == null || mConfirmPasswordEditText.getText() == null) {
			return false;
		}

		return mPasswordEditText.getText().toString().equals(mConfirmPasswordEditText.getText().toString());
	}

	/* Indicates that editText is empty. */
	private boolean editTextIsEmpty(EditText editText) {
		return editText.getText() == null || editText.getText().length() == 0 || editText.getText().equals("");
	}

	/* Sets view's visibility. */
	private void updateViewVisibility(View view, boolean visible, boolean goneIfInvisible) {
		if (visible)
			view.setVisibility(View.VISIBLE);
		else {
			if (goneIfInvisible)
				view.setVisibility(View.GONE);
			else
				view.setVisibility(View.INVISIBLE);
		}
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
		DialogFragment dialog = ConfirmationDialogFragment.getInstance(
				getActivity().getString(R.string.sign_up_connection_error), getActivity().getString(R.string.try_again),
				REQUEST_TRY_AGAIN, false);
		dialog.setTargetFragment(this, REQUEST_TRY_AGAIN);
		dialog.show(getActivity().getSupportFragmentManager(), DIALOG_CONNECTION_ERROR);
	}

	/*
	 * Notify activity that signing-up process has started (if the activity
	 * implements coresponding interface).
	 */
	private void notifyActivitySigningUpStarted() {
		Activity activity = getActivity();
		if (activity != null && activity instanceof OnSignUpListener)
			((OnSignUpListener) activity).onSigningUpStarted();
	}

	/*
	 * Notify activity that signing-up process has finished (if the activity
	 * implements coresponding interface).
	 */
	private void notifyActivitySigningUpFinished(boolean signedUp) {
		Activity activity = getActivity();
		if (activity != null && activity instanceof OnSignUpListener)
			((OnSignUpListener) activity).onSigningUpFinished(signedUp);
	}

	@Override
	public void onResume() {
		super.onResume();
		mJustCreated = false;
		updateViews();
		if (mShowConnectionErrorOnResume) {
			mShowConnectionErrorOnResume = false;
			showConnectionError();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mSignUpTask != null)
			mSignUpTask.cancel(true);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;
		switch (requestCode) {
		case REQUEST_TRY_AGAIN:
			mSignUpTask = new SignUpTask();
			mSignUpTask.execute(
					new String[] { mUsernameEditText.getText().toString(), mPasswordEditText.getText().toString() });
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}

	/** {@link AsyncTask} for performing signing-up in the background. */
	private class SignUpTask extends AsyncTask<String[], Void, User> {

		/* Was there an error in process. */
		private boolean mErrorWhileSigningUp = false;

		@Override
		protected void onPreExecute() {
			mTaskIsRunning = true;
			updateViews();
			notifyActivitySigningUpStarted();
		}

		@Override
		protected User doInBackground(String[]... arg0) {
			if (arg0.length != 1) {
				mErrorWhileSigningUp = true;
				return null;
			}
			String username = arg0[0][0];
			String password = arg0[0][1];

			try {
				User user = mBoardService.addUser(username, password);
				mErrorWhileSigningUp = false;
				return user;
			} catch (IOException e) {
				mErrorWhileSigningUp = true;
				return null;
			}
		}

		@Override
		protected void onPostExecute(User signedUpInUser) {
			if (isCancelled())
				return;
			mTaskIsRunning = false;
			if (signedUpInUser != null) {
				mUserService.signIn(signedUpInUser);
				notifyActivitySigningUpFinished(true);
				return;
			} else {
				notifyActivitySigningUpFinished(false);
				if (mErrorWhileSigningUp) {
					if (!SignUpFragment.this.isResumed()) {
						mShowConnectionErrorOnResume = true;
					} else {
						showConnectionError();
					}
				} else {
					mShowUsernameTakenError = true;
				}
			}
			updateViews();
		}
	}
}
