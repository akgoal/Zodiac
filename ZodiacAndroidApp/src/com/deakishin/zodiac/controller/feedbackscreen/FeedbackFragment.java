package com.deakishin.zodiac.controller.feedbackscreen;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.ConfirmationDialogFragment;
import com.deakishin.zodiac.controller.MessageDialogFragment;
import com.deakishin.zodiac.model.email.FeedbackSender;
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
import android.widget.EditText;
import android.widget.TextView;

/** Fragment for managing feedback. */
public class FeedbackFragment extends Fragment {

	/* Ids for child fragments. */
	private static final String DIALOG_ERROR_SEND = "errorSend";
	private static final String DIALOG_SUCCESS_SEND = "successSend";

	/* Request codes for child fragment. */
	private static final int REQUEST_SUCCESS_SEND = 10;
	private static final int REQUEST_TRY_AGAIN = 11;

	/* Max feedback length. */
	private static final int MAX_FEEDBACK = 5000;

	/* Padding for input fields. */
	private static final int EDITTEXT_PADDING = 8;

	/* Widgets. */
	private View mFeedbackLayout;
	private TextView mFeedbackLabelTextView;
	private EditText mFeedbackEditText;
	private TextView mFeedbackErrorTextView;
	private TextView mFeedbackCounterTextView;
	private Button mSendButton;

	/* Flags indicating if errors must be shown. */
	private boolean mFeedbackCounterError = false;
	private boolean mFeedbackEmptyError = false;

	/* Indicates if feedback field has focus. */
	private boolean mFeedbackInFocus = false;

	/* Flag indicating that sending is in process. */
	private boolean mTaskIsRunning = false;

	/* Flags indicating if sending result must be shown. */
	private boolean mShowSendErrorOnResume = false;
	private boolean mShowSendSuccessOnResume = false;

	/* Current logged-in user. */
	private User mUser;

	/* Feedback sender. */
	private FeedbackSender mFeedbackSender;

	/* Flag indicating that the fragment was just created. */
	private boolean mJustCreated;

	/**
	 * The listener interface for receiving an event of a successful sending.
	 */
	public static interface SuccessCallback {
		/**
		 * Invoked when the feedback is sent successfully.
		 */
		public void onSendingSuccess();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setHasOptionsMenu(true);

		mUser = UserService.getInstance(getActivity()).getUser();
		mFeedbackSender = new FeedbackSender(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.feedback_fragment, parent, false);

		mJustCreated = true;

		mFeedbackLayout = v.findViewById(R.id.feedback_layout);

		mFeedbackLabelTextView = (TextView) v.findViewById(R.id.feedback_text_label_textView);
		mFeedbackEditText = (EditText) v.findViewById(R.id.feedback_text_editText);
		mFeedbackErrorTextView = (TextView) v.findViewById(R.id.feedback_text_error_textView);
		mFeedbackCounterTextView = (TextView) v.findViewById(R.id.feedback_text_counter_textView);
		mFeedbackEditText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if (mJustCreated)
					return;
				mFeedbackEmptyError = false;
				updateFeedbackViews();
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});
		mFeedbackEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				mFeedbackInFocus = hasFocus;
				updateFeedbackViews();
			}
		});

		updateViews();

		return v;
	}

	/* Update widgets according to current status. */
	private void updateViews() {
		if (mTaskIsRunning) {
			if (mSendButton != null)
				mSendButton.setText(R.string.feedback_sending);
			enableViews(false);
		} else {
			if (mSendButton != null)
				mSendButton.setText(R.string.feedback_send);
			enableViews(true);
		}
		updateFeedbackViews();
	}

	/* Update widgets enability. */
	private void enableViews(boolean enable) {
		if (mSendButton != null)
			mSendButton.setEnabled(enable);
		mFeedbackEditText.setEnabled(enable);
		if (!enable) {
			mFeedbackLayout.requestFocus();
		}
	}

	/* Update widgets regarding feedback text. */
	private void updateFeedbackViews() {
		updateViewVisibility(mFeedbackLabelTextView, !editTextIsEmpty(mFeedbackEditText));

		mFeedbackCounterError = updateCounterView(mFeedbackEditText, mFeedbackCounterTextView, MAX_FEEDBACK);
		updateViewVisibility(mFeedbackCounterTextView, mFeedbackCounterError);

		updateViewVisibility(mFeedbackErrorTextView, mFeedbackEmptyError);
		updateTextViewState(mFeedbackErrorTextView, mFeedbackEmptyError, false);

		boolean error = mFeedbackEmptyError || mFeedbackCounterError;
		updateTextViewState(mFeedbackLabelTextView, error, mFeedbackInFocus);
		updateEditTextErrorState(mFeedbackEditText, error);
	}

	/*
	 * Update countTextView with symbol count in editText. Returns true if the
	 * count exceeds counterLimit.
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

	/* Update textView depending on error and focus presence. */
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

	/* Indicates if editText is Empty. */
	private boolean editTextIsEmpty(EditText editText) {
		return editText.getText() == null || editText.getText().length() == 0 || editText.getText().equals("");
	}

	/* Set view's visibility. */
	private void updateViewVisibility(View view, boolean visible) {
		if (visible) {
			view.setVisibility(View.VISIBLE);
		} else {
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

	/* Start sending the feedback by executing sender. */
	private void startSending() {
		hideKeyboard();

		if (mFeedbackCounterError || mFeedbackEmptyError)
			return;

		if (editTextIsEmpty(mFeedbackEditText)) {
			mFeedbackEmptyError = true;
			updateFeedbackViews();
			return;
		}

		String author = null;
		if (mUser != null)
			author = mUser.getName();

		String feedbackText = mFeedbackEditText.getText().toString();

		new SendTask().execute(feedbackText, author);
	}

	/* Show dialog about connection error. */
	private void showConnectionError() {
		if (!this.isResumed()) {
			mShowSendErrorOnResume = true;
			return;
		}
		DialogFragment dialog = ConfirmationDialogFragment.getInstance(
				getActivity().getString(R.string.feedback_error_sending), getActivity().getString(R.string.try_again),
				REQUEST_TRY_AGAIN, false);
		dialog.setTargetFragment(this, REQUEST_TRY_AGAIN);
		dialog.show(getActivity().getSupportFragmentManager(), DIALOG_ERROR_SEND);
	}

	/* Show dialog about the feedback being successfully sent. */
	private void showSuccessDialog() {
		if (!this.isResumed()) {
			mShowSendSuccessOnResume = true;
			return;
		}
		FragmentManager fm = getActivity().getSupportFragmentManager();
		MessageDialogFragment dialog = MessageDialogFragment
				.getInstance(getActivity().getString(R.string.feedback_success_sending), true);
		dialog.setTargetFragment(this, REQUEST_SUCCESS_SEND);
		dialog.show(fm, DIALOG_SUCCESS_SEND);
	}

	@Override
	public void onResume() {
		super.onResume();
		mJustCreated = false;
		updateViews();
		if (mShowSendErrorOnResume) {
			showConnectionError();
			mShowSendErrorOnResume = false;
		}
		if (mShowSendSuccessOnResume) {
			showSuccessDialog();
			mShowSendSuccessOnResume = false;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_SUCCESS_SEND) {
			Activity activity = getActivity();
			if (activity instanceof SuccessCallback)
				((SuccessCallback) activity).onSendingSuccess();
			return;
		}
		if (requestCode == REQUEST_TRY_AGAIN) {
			if (resultCode != Activity.RESULT_OK)
				return;
			startSending();
			return;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_feedback, menu);
		MenuItem sendItem = menu.findItem(R.id.menu_item_send_feedback);

		View v = getActivity().getLayoutInflater().inflate(R.layout.feedback_send_panel, null);

		mSendButton = (Button) v.findViewById(R.id.feedback_send_panel_button);
		mSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startSending();
			}
		});
		if (mTaskIsRunning) {
			mSendButton.setText(R.string.feedback_sending);
			mSendButton.setEnabled(false);
		} else {
			mSendButton.setText(R.string.feedback_send);
			mSendButton.setEnabled(true);
		}

		sendItem.setActionView(v);
	}

	/** {@link AsyncTask} for sending the feedback in the background. */
	private class SendTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			mTaskIsRunning = true;
			updateViews();
		}

		@Override
		protected Boolean doInBackground(String... args) {
			if (args.length != 2)
				return false;

			return mFeedbackSender.send(args[0], args[1]);
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
