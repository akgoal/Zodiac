package com.deakishin.zodiac.controller.mainscreen.dialogs;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.CustomDialogFragment;
import com.deakishin.zodiac.model.ciphermanager.CipherInfo;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

/** Dialog displaying cipher info. */
public class DialogCipherInfo extends CustomDialogFragment {

	/* Keys for arguments. */
	private static final String EXTRA_TITLE = "title";
	private static final String EXTRA_AUTHOR = "author";
	private static final String EXTRA_DESCRIPTION = "description";
	private static final String EXTRA_DIFFICULTY = "difficulty";

	/**
	 * Creates and returns configured dialog.
	 * 
	 * @param cipher
	 *            Cipher info to display.
	 * @return Configured dialog.
	 */
	public static DialogCipherInfo getInstance(CipherInfo cipher) {
		return getInstance(cipher.getTitle(), cipher.getAuthor(), cipher.getDescription(), cipher.getDifficulty());
	}

	/**
	 * Creates and returns configured dialog.
	 * 
	 * @param cipherTitle
	 *            Title of the cipher.
	 * @param cipherAuthor
	 *            Author of the cipher.
	 * @param cipherDescr
	 *            Cipher's description.
	 * @param cipherDifficulty
	 *            Difficulty of the cipher.
	 * @return Configured dialog.
	 */
	public static DialogCipherInfo getInstance(String cipherTitle, String cipherAuthor, String cipherDescr,
			Float cipherDifficulty) {
		DialogCipherInfo fragment = new DialogCipherInfo();
		Bundle args = new Bundle();
		args.putString(EXTRA_TITLE, cipherTitle);
		if (cipherDescr != null)
			args.putString(EXTRA_DESCRIPTION, cipherDescr);
		if (cipherAuthor != null)
			args.putString(EXTRA_AUTHOR, cipherAuthor);
		if (cipherDifficulty != null)
			args.putDouble(EXTRA_DIFFICULTY, cipherDifficulty);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_cipher_info, null);

		Bundle args = getArguments();
		String title;
		if (args.containsKey(EXTRA_TITLE))
			title = args.getString(EXTRA_TITLE);
		else
			title = "";

		String author = null;
		if (args.containsKey(EXTRA_AUTHOR))
			author = args.getString(EXTRA_AUTHOR);
		if (author == null || author.equals(""))
			author = getActivity().getString(R.string.cipher_unknown_author);
		String createdBy = getActivity().getString(R.string.cipher_info_created_by, author);

		Float difficulty = null;
		if (args.containsKey(EXTRA_DIFFICULTY))
			difficulty = (float) args.getDouble(EXTRA_DIFFICULTY);

		String description = null;
		if (args.containsKey(EXTRA_DESCRIPTION))
			description = args.getString(EXTRA_DESCRIPTION);
		if (description == null || description.equals(""))
			description = getActivity().getString(R.string.cipher_info_no_description);

		String descriptionText = getActivity().getString(R.string.cipher_info_description, description);

		TextView titleTextView = (TextView) v.findViewById(R.id.dialog_cipher_info_title_textView);
		titleTextView.setText(title);

		TextView createdByTextView = (TextView) v.findViewById(R.id.dialog_cipher_info_created_by_textView);
		createdByTextView.setText(createdBy);

		TextView difficultyTextView = (TextView) v.findViewById(R.id.dialog_cipher_info_difficulty_textView);
		if (difficulty == null) {
			difficultyTextView.setVisibility(View.GONE);
		} else {
			difficultyTextView.setVisibility(View.VISIBLE);
			difficultyTextView.setText(getActivity().getString(R.string.cipher_info_difficulty, difficulty));
		}

		TextView descrTextView = (TextView) v.findViewById(R.id.dialog_cipher_info_description_textView);
		descrTextView.setText(descriptionText);

		Dialog dialog = new AlertDialog.Builder(getActivity()).setView(v).setPositiveButton(R.string.ok, null).create();
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

}
