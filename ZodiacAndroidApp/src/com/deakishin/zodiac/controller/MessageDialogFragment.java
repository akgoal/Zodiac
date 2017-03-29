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
 * Dialog for displaying message.
 */
public class MessageDialogFragment extends CustomDialogFragment {

	/* Keys for arguments. */
	private static final String EXTRA_MESSAGE = "message";
	private static final String EXTRA_SEND_RESULT = "sendResult";
	private static final String EXTRA_CANCEL_ON_TOUCH_OUTSIDE = "cancelOnTouchOutside";
	private static final String EXTRA_ALIGN_CENTER = "alignCenter";

	/* Displayed message. */
	private String mMessage;
	/* True is parent needs to be notified when dialog is closed. */
	private boolean mSendResult = false;
	/* True is the dialog must close when touched outside. */
	private boolean mCancelOnTouchOutside = false;
	/* True if the text must be center aligned. */
	private boolean mAlignCenter = true;

	/* True is result is sent. */
	private boolean mResultSent = false;

	/**
	 * Creates an instance of the dialog.
	 * 
	 * @param message
	 *            Message to display.
	 */
	public static MessageDialogFragment getInstance(String message) {
		return getInstance(message, null);
	}

	/**
	 * Creates an instance of the dialog.
	 * 
	 * @param message
	 *            Message to display.
	 * @param sendResult
	 *            True if parent must be notified when the dialog is closed.
	 * @return Configured dialog instance.
	 */
	public static MessageDialogFragment getInstance(String message, Boolean sendResult) {
		return getInstance(message, sendResult, null, null);
	}

	/**
	 * Creates an instance of the dialog.
	 * 
	 * @param message
	 *            Message to display.
	 * @param sendResult
	 *            True if parent must be notified when the dialog is closed.
	 * @param cancelOnTouchOutside
	 *            True if the dialog must be closed when touched outside.
	 * @return Configured dialog instance.
	 */
	public static MessageDialogFragment getInstance(String message, Boolean sendResult, Boolean cancelOnTouchOutside) {
		return getInstance(message, sendResult, cancelOnTouchOutside, null);
	}

	/**
	 * Creates an instance of the dialog.
	 * 
	 * @param message
	 *            Message to display.
	 * @param sendResult
	 *            True if parent must be notified when the dialog is closed.
	 * @param cancelOnTouchOutside
	 *            True if the dialog must be closed when touched outside.
	 * @param alignCenter
	 *            True if the text must be center aligned.
	 * @return Configured dialog instance.
	 */
	public static MessageDialogFragment getInstance(String message, Boolean sendResult, Boolean cancelOnTouchOutside,
			Boolean alignCenter) {
		MessageDialogFragment fragment = new MessageDialogFragment();
		Bundle args = new Bundle();
		args.putString(EXTRA_MESSAGE, message);
		if (sendResult != null)
			args.putBoolean(EXTRA_SEND_RESULT, sendResult);
		if (cancelOnTouchOutside != null)
			args.putBoolean(EXTRA_CANCEL_ON_TOUCH_OUTSIDE, cancelOnTouchOutside);
		if (alignCenter != null)
			args.putBoolean(EXTRA_ALIGN_CENTER, alignCenter);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_message, null);

		Bundle args = getArguments();
		if (args.containsKey(EXTRA_MESSAGE))
			mMessage = args.getString(EXTRA_MESSAGE);
		if (args.containsKey(EXTRA_SEND_RESULT))
			mSendResult = args.getBoolean(EXTRA_SEND_RESULT);

		if (args.containsKey(EXTRA_CANCEL_ON_TOUCH_OUTSIDE))
			mCancelOnTouchOutside = args.getBoolean(EXTRA_CANCEL_ON_TOUCH_OUTSIDE);

		if (args.containsKey(EXTRA_ALIGN_CENTER))
			mAlignCenter = args.getBoolean(EXTRA_ALIGN_CENTER);

		TextView messageTextView = (TextView) v.findViewById(R.id.message_textView);

		/*
		 * if (mAlignCenter) messageTextView.setGravity(Gravity.CENTER);
		 */

		if (mMessage != null)
			messageTextView.setText(mMessage);

		Dialog dialog = new AlertDialog.Builder(getActivity()).setView(v)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if (mSendResult) {
							sendResult(Activity.RESULT_OK);
						}
					}
				}).create();
		dialog.setCanceledOnTouchOutside(mCancelOnTouchOutside);
		return dialog;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (mSendResult && !mResultSent)
			sendResult(Activity.RESULT_CANCELED);
		super.onDismiss(dialog);
	}

	/* Notify target fragment that the dialog is closed. */
	private void sendResult(int resultCode) {
		mResultSent = true;
		if (getTargetFragment() == null)
			return;
		getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, null);
	}

}
