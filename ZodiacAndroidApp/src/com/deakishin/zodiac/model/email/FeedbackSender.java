package com.deakishin.zodiac.model.email;

import com.deakishin.zodiac.R;

import android.content.Context;

/** Class for sending feedback via email. */
public class FeedbackSender {

	/* Password for the email. */
	private static final String EMAIL_PASS = "zodiacfeedback210794";

	/* Application context. */
	private Context mContext;

	/* Info for mails that get sent. */
	private String mSenderEmail;
	private String mReceiverEmail;
	private String mEmailSubject;

	public FeedbackSender(Context context) {
		mContext = context.getApplicationContext();

		mSenderEmail = mContext.getString(R.string.feedback_email_sender_name) + "@"
				+ mContext.getString(R.string.feedback_email_sender_service);
		mReceiverEmail = mContext.getString(R.string.feedback_email_receiver_name) + "@"
				+ mContext.getString(R.string.feedback_email_receiver_service);
		mEmailSubject = mContext.getString(R.string.feedback_email_subject);
	}

	/**
	 * Sends feedback via email.
	 * 
	 * @param feedbackText
	 *            Text of the feedback.
	 * @param feedbackAuthor
	 *            Author of the feedback. Can be null if the author is unknown.
	 * @return True if the feedback was successfully sent, false otherwise.
	 */
	public boolean send(String feedbackText, String feedbackAuthor) {
		if (feedbackText == null)
			return false;

		if (feedbackAuthor == null)
			feedbackAuthor = mContext.getString(R.string.feedback_email_unknown_author);

		Mail mail = new Mail(mSenderEmail, EMAIL_PASS);

		mail.set_to(new String[] { mReceiverEmail });
		mail.set_body(feedbackText);
		mail.set_from(mSenderEmail);
		mail.set_subject(mEmailSubject + " from: " + feedbackAuthor);

		try {
			return mail.send();
		} catch (Exception e) {
			return false;
		}
	}
}
