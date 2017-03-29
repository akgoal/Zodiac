package com.deakishin.zodiac.controller;

import com.deakishin.zodiac.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Dialog for inputing single line. Sends the entered line to the host activity
 * or the target fragment depending on who has invoked the dialog.
 */
public class InputSingleLineDialogFragment extends CustomDialogFragment {

	/* Keys for arguments. */
	private static final String EXTRA_HINT = "hint";
	private static final String EXTRA_POSITIVE_BUTTON_TEXT = "positiveBtnText";
	private static final String EXTRA_REQUEST_CODE = "reqCode";
	private static final String EXTRA_HOST_IS_ACTIVITY = "hostIsActivity";

	/* Widgets. */
	private EditText mEditText;

	/* Hint in the text field. */
	private String mHint;
	/* Request code. */
	private int mReqCode;
	/* Text displayed on the Confirm button. */
	private String mPositiveButtonText;

	/*
	 * True if the dialog was invoked from an activity, false otherwise.
	 */
	private boolean mHostIsActivity = true;

	/* True if the result was to parent. */
	private boolean mResultSent = false;

	/**
	 * The listener interface for receiving the result. If the dialog is being
	 * invoked by an activity, the activity must implement this interface.
	 */
	public static interface InputListener {
		/**
		 * Invoked when the line is entered.
		 * 
		 * @param requestCode
		 *            Request code that was passed when the dialog was being
		 *            created.
		 * @param resultCode
		 *            Result code. Can be either {@link Activity#RESULT_OK} or
		 *            {@link Activity#RESULT_CANCELED}.
		 * @param inputString
		 *            Line that was entered.
		 */
		public void onInput(int requestCode, int resultCode, String inputString);
	}

	private InputListener mInputListener;

	// Check if the host activity implements listener interface.
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (mHostIsActivity) {
			try {
				mInputListener = (InputListener) activity;
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Creates an instance of the dialog.
	 * 
	 * @param hint
	 *            Hint to displayed in the input field.
	 * @param positiveBtnText
	 *            Text to display on the OK button.
	 * @param requestCode
	 *            Request code that then will be used to send back the result.
	 * @param hostIsActivity
	 *            True if the dialog is being created by an activity, false
	 *            otherwise. If the parent is an activity, then this activity
	 *            must implement {@link InputListener}.
	 * @return Configured dialog instance.
	 */
	public static InputSingleLineDialogFragment getInstance(String hint, String positiveBtnText, int requestCode,
			boolean hostIsActivity) {
		InputSingleLineDialogFragment fragment = new InputSingleLineDialogFragment();
		Bundle args = new Bundle();
		args.putString(EXTRA_HINT, hint);
		args.putString(EXTRA_POSITIVE_BUTTON_TEXT, positiveBtnText);
		args.putInt(EXTRA_REQUEST_CODE, requestCode);
		args.putBoolean(EXTRA_HOST_IS_ACTIVITY, hostIsActivity);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_input_single_line, null);

		Bundle args = getArguments();
		if (args.containsKey(EXTRA_HINT))
			mHint = args.getString(EXTRA_HINT);
		if (args.containsKey(EXTRA_POSITIVE_BUTTON_TEXT))
			mPositiveButtonText = args.getString(EXTRA_POSITIVE_BUTTON_TEXT);
		if (mPositiveButtonText == null || mPositiveButtonText.equals(""))
			mPositiveButtonText = getActivity().getString(R.string.yes);
		if (args.containsKey(EXTRA_REQUEST_CODE))
			mReqCode = args.getInt(EXTRA_REQUEST_CODE);
		if (args.containsKey(EXTRA_HOST_IS_ACTIVITY))
			mHostIsActivity = args.getBoolean(EXTRA_HOST_IS_ACTIVITY);

		mEditText = (EditText) v.findViewById(R.id.input_singleline_editText);
		if (mHint != null)
			mEditText.setHint(mHint);
		mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					returnResult();
					return true;
				}
				return false;
			}
		});

		Dialog dialog = new AlertDialog.Builder(getActivity()).setView(v)
				.setPositiveButton(mPositiveButtonText, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						returnResult();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						sendResult(Activity.RESULT_CANCELED, null);
					}
				}).create();
		// Display keyboard.
		dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	// Return result to parent with the line from the input field.
	private void returnResult() {
		String resultString = null;
		if (mEditText.getText() != null) {
			resultString = mEditText.getText().toString();
		}
		sendResult(Activity.RESULT_OK, resultString);
		dismiss();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (!mResultSent)
			sendResult(Activity.RESULT_CANCELED, null);
		super.onDismiss(dialog);
	}

	/* Send result to parent. */
	private void sendResult(int resultCode, String resultString) {
		mResultSent = true;
		if (mHostIsActivity) {
			if (mInputListener != null)
				mInputListener.onInput(mReqCode, resultCode, resultString);
		} else {
			if (getTargetFragment() == null)
				return;

			getTargetFragment().onActivityResult(mReqCode, resultCode, null);
		}
	}
}
