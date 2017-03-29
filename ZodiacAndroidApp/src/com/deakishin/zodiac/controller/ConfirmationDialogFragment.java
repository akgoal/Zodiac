package com.deakishin.zodiac.controller;

import com.deakishin.zodiac.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

/**
 * Confirmation dialog. Displays message and, when confirmed, sends the result
 * to the host activity or the target fragment depending on who has invoked the
 * dialog.
 */
public class ConfirmationDialogFragment extends CustomDialogFragment {

	/* Keys for arguments. */
	private static final String EXTRA_MESSAGE = "message";
	private static final String EXTRA_POSITIVE_BUTTON_TEXT = "positiveBtnText";
	private static final String EXTRA_REQUEST_CODE = "reqCode";
	private static final String EXTRA_HOST_IS_ACTIVITY = "hostIsActivity";

	/* Displayed message. */
	private String mMessage;
	/* Request code. */
	private int mReqCode;
	/* Text disaplyed on the Confirm button. */
	private String mPositiveButtonText;

	/*
	 * True if the dialog was invoked by an activity.
	 */
	private boolean mHostIsActivity = true;

	/* True if result was sent to the parent. */
	private boolean mResultSent = false;

	/**
	 * The listener interface for receiving confirmation result. If the dialog
	 * is being invoked by an activity, the activity must implement this
	 * interface.
	 */
	public static interface ConfirmationListener {
		/**
		 * Invoked when user confirms the dialog's message.
		 * 
		 * @param requestCode
		 *            Request code that was passed when creating the dialog.
		 * @param resultCode
		 *            Result code. Can be either {@link Activity#RESULT_OK} or
		 *            {@link Activity#RESULT_CANCELED}.
		 */
		public void onConfirmed(int requestCode, int resultCode);
	}

	private ConfirmationListener mConfirmationListener;

	/* Check if activity implements confirmation listener. */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (mHostIsActivity) {
			try {
				mConfirmationListener = (ConfirmationListener) activity;
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Creates an instance of the dialog.
	 * 
	 * @param message
	 *            Message to display.
	 * @param positiveBtnText
	 *            Text to display on the Confirm button.
	 * @param requestCode
	 *            Request code that then will be used to send back the result.
	 * @param hostIsActivity
	 *            True if the dialog is being created by an activity, false
	 *            otherwise. If the parent is an activity, then this activity
	 *            must implement {@link ConfirmationListener}.
	 * @return Configured dialog instance.
	 */
	public static ConfirmationDialogFragment getInstance(String message, String positiveBtnText, int requestCode,
			boolean hostIsActivity) {
		ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
		Bundle args = new Bundle();
		args.putString(EXTRA_MESSAGE, message);
		args.putString(EXTRA_POSITIVE_BUTTON_TEXT, positiveBtnText);
		args.putInt(EXTRA_REQUEST_CODE, requestCode);
		args.putBoolean(EXTRA_HOST_IS_ACTIVITY, hostIsActivity);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_message, null);

		Bundle args = getArguments();
		if (args.containsKey(EXTRA_MESSAGE))
			mMessage = args.getString(EXTRA_MESSAGE);
		if (args.containsKey(EXTRA_POSITIVE_BUTTON_TEXT))
			mPositiveButtonText = args.getString(EXTRA_POSITIVE_BUTTON_TEXT);
		if (mPositiveButtonText == null || mPositiveButtonText.equals(""))
			mPositiveButtonText = getActivity().getString(R.string.yes);
		if (args.containsKey(EXTRA_REQUEST_CODE))
			mReqCode = args.getInt(EXTRA_REQUEST_CODE);
		if (args.containsKey(EXTRA_HOST_IS_ACTIVITY))
			mHostIsActivity = args.getBoolean(EXTRA_HOST_IS_ACTIVITY);

		TextView messageTextView = (TextView) v.findViewById(R.id.message_textView);
		if (mMessage != null)
			messageTextView.setText(mMessage);
		// messageTextView.setGravity(Gravity.CENTER);

		Dialog dialog = new AlertDialog.Builder(getActivity()).setView(v)
				.setPositiveButton(mPositiveButtonText, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						sendResult(Activity.RESULT_OK);
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						sendResult(Activity.RESULT_CANCELED);
					}
				}).create();
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (!mResultSent)
			sendResult(Activity.RESULT_CANCELED);
		super.onDismiss(dialog);
	}

	/* Send result to parent. */
	private void sendResult(int resultCode) {
		mResultSent = true;
		if (mHostIsActivity) {
			if (mConfirmationListener != null)
				mConfirmationListener.onConfirmed(mReqCode, resultCode);
		} else {
			if (getTargetFragment() == null)
				return;

			getTargetFragment().onActivityResult(mReqCode, resultCode, null);
		}
	}
}
