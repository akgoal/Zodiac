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

/** Fragment for displaying and managing controls for logging in. */
public class LogInFragment extends Fragment {

	/* Keys for dialogs. */
	private static final String DIALOG_CONNECTION_ERROR = "connectionError";

	/* Request codes for child fragments. */
	private static final int REQUEST_TRY_AGAIN = 10;

	/* Padding for input fields. */
	private static final int EDITTEXT_PADDING = 8;

	/* Widgets. */
	private View mLogInLayout;
	private TextView mUsernameLabelTextView;
	private EditText mUsernameEditText;
	private TextView mUsernameErrorTextView;
	private TextView mPasswordLabelTextView;
	private EditText mPasswordEditText;
	private TextView mPasswordErrorTextView;
	private Button mLogInButton;
	private TextView mLogInFailTextView;

	/* Flags indicating that errors have to be shown. */
	private boolean mShowUsernameError = false;
	private boolean mShowPasswordError = false;
	private boolean mShowLogInFailError = false;

	/* Flags indicating if fields have focus. */
	private boolean mUsernameInFocus = false;
	private boolean mPasswordInFocus = false;

	/* Flag indicating that connection error has to shown on resume. */
	private boolean mShowConnectionErrorOnResume = false;

	/* Flag indicating that logging in is in process. */
	private boolean mTaskIsRunning = false;

	/* Board service to perform logging in. */
	private BoardServiceI mBoardService;

	/* User service to manage current user status. */
	private UserService mUserService;

	/* Object that performs logging in. */
	private LogInTask mLogInTask;

	/* Flag indicating that the fragment was just created. */
	private boolean mJustCreated;

	/**
	 * The listener interface regarding logging-in process.
	 */
	public static interface OnLogInListener {
		/**
		 * Invoked when logging-in is in process.
		 */
		public void onLoggingInStarted();

		/**
		 * Invoked when logging-in process has finished.
		 * 
		 * @param loggedIn
		 *            True if user logged in successfully, false otherwise.
		 */
		public void onLoggingInFinished(boolean loggedIn);
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
		View v = layoutInflater.inflate(R.layout.log_in_fragment, null);

		mJustCreated = true;

		mLogInLayout = v.findViewById(R.id.log_in_layout);

		mUsernameLabelTextView = (TextView) v.findViewById(R.id.log_in_username_label_textView);
		mUsernameEditText = (EditText) v.findViewById(R.id.log_in_username_editText);
		mUsernameErrorTextView = (TextView) v.findViewById(R.id.log_in_username_error_textView);
		mUsernameEditText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if (mJustCreated)
					return;
				mShowUsernameError = false;
				mShowLogInFailError = false;
				updateUsernameViews();
				updateFailView();
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

		mPasswordLabelTextView = (TextView) v.findViewById(R.id.log_in_password_label_textView);
		mPasswordEditText = (EditText) v.findViewById(R.id.log_in_password_editText);
		mPasswordErrorTextView = (TextView) v.findViewById(R.id.log_in_password_error_textView);
		mPasswordEditText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if (mJustCreated)
					return;
				mShowPasswordError = false;
				mShowLogInFailError = false;
				updatePasswordViews();
				updateFailView();
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

		mLogInButton = (Button) v.findViewById(R.id.log_in_button);
		mLogInButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideKeyboard();
				boolean invalidFieldValues = false;
				if (editTextIsEmpty(mUsernameEditText)) {
					mShowUsernameError = true;
					invalidFieldValues = true;
				}
				if (editTextIsEmpty(mPasswordEditText)) {
					mShowPasswordError = true;
					invalidFieldValues = true;
				}
				if (invalidFieldValues) {
					updateViews();
					return;
				}

				mLogInTask = new LogInTask();
				mLogInTask.execute(new String[] { mUsernameEditText.getText().toString(),
						mPasswordEditText.getText().toString() });
			}
		});

		mLogInFailTextView = (TextView) v.findViewById(R.id.log_in_fail_textView);

		updateViews();

		return v;
	}

	/* Update widgets. */
	private void updateViews() {
		if (mTaskIsRunning) {
			mLogInButton.setText(R.string.loggin_in);
			enableViews(false);
		} else {
			mLogInButton.setText(R.string.log_in);
			enableViews(true);
		}
		updateUsernameViews();
		updatePasswordViews();
		updateFailView();
	}

	/* Update enability of widgets. */
	private void enableViews(boolean enable) {
		mLogInButton.setEnabled(enable);
		mUsernameEditText.setEnabled(enable);
		mPasswordEditText.setEnabled(enable);
		if (!enable) {
			mLogInLayout.requestFocus();
		}
	}

	/* Update widgets regarding username. */
	private void updateUsernameViews() {
		updateViewVisibility(mUsernameLabelTextView, !editTextIsEmpty(mUsernameEditText), false);
		updateViewVisibility(mUsernameErrorTextView, mShowUsernameError, false);
		updateTextViewState(mUsernameLabelTextView, mShowUsernameError, mUsernameInFocus);
		updateEditTextErrorState(mUsernameEditText, mShowUsernameError);
	}

	/* Update widgets regarding password. */
	private void updatePasswordViews() {
		updateViewVisibility(mPasswordLabelTextView, !editTextIsEmpty(mPasswordEditText), false);
		updateViewVisibility(mPasswordErrorTextView, mShowPasswordError, false);
		updateTextViewState(mPasswordLabelTextView, mShowPasswordError, mPasswordInFocus);
		updateEditTextErrorState(mPasswordEditText, mShowPasswordError);
	}

	/* Update editText input field depending on error presence. */
	private void updateEditTextErrorState(EditText editText, boolean error) {
		int backgroundResId = 0;
		if (error)
			backgroundResId = R.drawable.log_in_input_border_error;
		else
			backgroundResId = R.drawable.log_in_input_border;
		editText.setBackgroundResource(backgroundResId);
		editText.setPadding(0, EDITTEXT_PADDING, 0, EDITTEXT_PADDING);
	}

	/* Update editText input field depending on error presence. */
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

	/* Update widgets indicating failed logging-in. */
	private void updateFailView() {
		updateViewVisibility(mLogInFailTextView, mShowLogInFailError, true);
	}

	/* Indicates that editText is empty. */
	private boolean editTextIsEmpty(EditText editText) {
		return editText.getText() == null || editText.getText().length() == 0 || editText.getText().equals("");
	}

	/* Update view's visibility. */
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

	/* Show dialog displaying connection error. */
	private void showConnectionError() {
		DialogFragment dialog = ConfirmationDialogFragment.getInstance(
				getActivity().getString(R.string.sign_up_connection_error), getActivity().getString(R.string.try_again),
				REQUEST_TRY_AGAIN, false);
		dialog.setTargetFragment(this, REQUEST_TRY_AGAIN);
		dialog.show(getActivity().getSupportFragmentManager(), DIALOG_CONNECTION_ERROR);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;
		switch (requestCode) {
		case REQUEST_TRY_AGAIN:
			mLogInTask = new LogInTask();
			mLogInTask.execute(
					new String[] { mUsernameEditText.getText().toString(), mPasswordEditText.getText().toString() });
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}

	/*
	 * Notify activity that logging-in process has started (if the activity
	 * implements coresponding interface).
	 */
	private void notifyActivityLoggingInStarted() {
		Activity activity = getActivity();
		if (activity != null && activity instanceof OnLogInListener)
			((OnLogInListener) activity).onLoggingInStarted();
	}

	/*
	 * Notify activity that logging-in process has finished (if the activity
	 * implements coresponding interface).
	 */
	private void notifyActivityLoggingInFinished(boolean loggedIn) {
		Activity activity = getActivity();
		if (activity != null && activity instanceof OnLogInListener)
			((OnLogInListener) activity).onLoggingInFinished(loggedIn);
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
		if (mLogInTask != null)
			mLogInTask.cancel(true);
	}

	/** {@link AsyncTask} for performing logging-in in the background. */
	private class LogInTask extends AsyncTask<String[], Void, User> {

		/* Was there an error in process. */
		private boolean mErrorWhileLoggingIn = false;

		@Override
		protected void onPreExecute() {
			mTaskIsRunning = true;
			mShowLogInFailError = false;
			updateViews();
			notifyActivityLoggingInStarted();
		}

		@Override
		protected User doInBackground(String[]... arg0) {
			if (arg0.length != 1) {
				mErrorWhileLoggingIn = true;
				return null;
			}
			String username = arg0[0][0];
			String password = arg0[0][1];

			try {
				User user = mBoardService.getUser(username, password);
				mErrorWhileLoggingIn = false;
				return user;
			} catch (IOException e) {
				mErrorWhileLoggingIn = true;
				return null;
			}
		}

		@Override
		protected void onPostExecute(User loggedInUser) {
			if (isCancelled())
				return;
			mTaskIsRunning = false;
			if (loggedInUser != null) {
				mUserService.signIn(loggedInUser);
				notifyActivityLoggingInFinished(true);
				return;
			} else {
				notifyActivityLoggingInFinished(false);
				if (mErrorWhileLoggingIn) {
					if (!LogInFragment.this.isResumed()) {
						mShowConnectionErrorOnResume = true;
					} else {
						showConnectionError();
					}
				} else {
					mShowLogInFailError = true;
				}
			}
			updateViews();
		}

	}
}
