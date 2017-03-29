package com.deakishin.zodiac.controller.mainscreen.dialogs;

import java.io.IOException;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.CustomDialogFragment;
import com.deakishin.zodiac.model.ciphermanager.CipherManager;
import com.deakishin.zodiac.model.ciphermodel.Model;
import com.deakishin.zodiac.services.boardservice.BoardServiceI;
import com.deakishin.zodiac.services.boardservice.BoardServiceImpl;
import com.deakishin.zodiac.services.userservice.User;
import com.deakishin.zodiac.services.userservice.UserService;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/** Dialog that displays solution checking process. */
public class DialogSolutionChecking extends CustomDialogFragment {

	/* Request codes for child fragments. */
	private static final int REQUEST_TRY_AGAIN = 30;

	/* Cipher's model. */
	private Model mModel;

	/* Board service to perform solution checking. */
	private BoardServiceI mBoardService;

	/* Current logged-in user. */
	private User mUser;

	/* Widgets. */
	private TextView mNotCompleteTextView;
	private TextView mResultTextView;
	private TextView mResultDescrTextView;
	private View mResultPanel;
	private ImageView mResultImageView;
	private View mLoadingPanel;
	private View mLoadingProgressBar;
	private TextView mLoadingTextView;

	/* Object that performs solution checking. */
	private CheckingTask mCheckingTask;

	/* Solution checking is in process. */
	private boolean mTaskIsRunning = false;

	/* The fragment was just created. */
	private boolean mFirstCreated = true;

	/* The solution is valid and can be checked. */
	private boolean mSolutionIsValid = false;

	/* There is a connection error. */
	private boolean mErrorConnecting = false;

	/* The solution is correct. */
	private boolean mSolutionIsCorrect = false;

	/* The solution was the first correct solution. */
	private boolean mFirstToSolve = false;

	/* How many correct solution are there. */
	private int mSolveNumber = 0;

	/* The cipher was created by current user. */
	private boolean mOwnCipher = false;

	/* The cipher was already solved by current user before. */
	private boolean mAlreadySolved = false;

	/*
	 * Number of points as a reward for correct solution. If the solution is not
	 * correct, number of reward points that can be obtained by solving the
	 * cipher.
	 */
	private Float mReward;

	/* Application context. */
	private Context mContext;

	public DialogSolutionChecking() {
		super();
		setRetainInstance(true);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_solution_checking, null);

		mModel = CipherManager.getInstance(getActivity()).getCipherModel();
		mContext = getActivity().getApplicationContext();
		mUser = UserService.getInstance(mContext).getUser();
		mBoardService = BoardServiceImpl.getImpl(mContext);

		mNotCompleteTextView = (TextView) v.findViewById(R.id.solution_checking_solution_not_complete_textView);
		mResultPanel = v.findViewById(R.id.solution_checking_result_panel);
		mResultDescrTextView = (TextView) v.findViewById(R.id.solution_checking_result_descr_textView);
		mResultTextView = (TextView) v.findViewById(R.id.solution_checking_result_textView);
		mResultImageView = (ImageView) v.findViewById(R.id.solution_checking_result_imageView);
		mLoadingPanel = v.findViewById(R.id.solution_checking_loading_panel);
		mLoadingProgressBar = v.findViewById(R.id.solution_checking_loading_panel_progressBar);
		mLoadingTextView = (TextView) v.findViewById(R.id.solution_checking_loading_panel_textView);

		if (mFirstCreated) {
			if (mModel.isSolutionComplete()) {
				mSolutionIsValid = true;
				startChecking();
			} else {
				mSolutionIsValid = false;
			}
		}
		mFirstCreated = false;

		updatePanels();

		Dialog dialog = new AlertDialog.Builder(getActivity())
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).setView(v).create();
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	@Override
	public void onStart() {
		super.onStart();

		updatePanels();
	}

	/* Start solution checking. */
	private void startChecking() {
		String solution = mModel.getSolutionAsString();
		mCheckingTask = new CheckingTask(CipherManager.getInstance(getActivity()).getCurrentCipherInfo().getId(),
				solution);
		mCheckingTask.execute();
	}

	/* Stop solution checking task when the dialog is dismissed. */
	@Override
	public void onDismiss(DialogInterface dialog) {
		if (mCheckingTask != null)
			mCheckingTask.cancel(true);
		super.onDismiss(dialog);
	}

	/* Update panels. */
	private void updatePanels() {
		mResultDescrTextView.setVisibility(View.GONE);
		if (mTaskIsRunning) {
			// Solution checking is in process
			mLoadingPanel.setVisibility(View.VISIBLE);
			mLoadingProgressBar.setVisibility(View.VISIBLE);
			mLoadingTextView.setText(R.string.solution_checking_loading);
			mResultPanel.setVisibility(View.GONE);
			updateButtons(false, true);
		} else {
			mLoadingPanel.setVisibility(View.GONE);
			if (mSolutionIsValid) {
				// Solution is valid and can be checked.
				mNotCompleteTextView.setVisibility(View.GONE);
				if (mErrorConnecting) {
					// Connection error
					mLoadingPanel.setVisibility(View.VISIBLE);
					mLoadingTextView.setText(R.string.solution_checking_error_connecting_to_server);
					mLoadingProgressBar.setVisibility(View.GONE);
					mResultPanel.setVisibility(View.GONE);
					updateButtons(true, false);
				} else {
					mResultPanel.setVisibility(View.VISIBLE);
					mResultDescrTextView.setVisibility(View.VISIBLE);
					int resultTextViewColorRes = 0;
					if (mSolutionIsCorrect) {
						// Solution is correct.
						mResultTextView.setText(R.string.solution_checking_result_correct);
						resultTextViewColorRes = R.color.solution_checking_result_correct;
						mResultImageView.setImageResource(R.drawable.ic_correct);
					} else {
						// Solution is incorrect.
						mResultTextView.setText(R.string.solution_checking_result_incorrect);
						resultTextViewColorRes = R.color.solution_checking_result_incorrect;
						mResultImageView.setImageResource(R.drawable.ic_correct_false);
					}
					mResultTextView.setTextColor(ContextCompat.getColor(getActivity(), resultTextViewColorRes));

					int resultDescrTextViewColorRes = R.color.solution_checking_result_neutral;
					int resultDescrTextViewTypeface = Typeface.NORMAL;
					if (mOwnCipher) {
						// Cipher was created by user
						mResultDescrTextView.setText(R.string.solution_checking_result_own_cipher);
					} else {
						if (mAlreadySolved) {
							// User has already solved the cipher before
							mResultDescrTextView.setText(R.string.solution_checking_result_already_solved);
						} else {
							if (mReward <= 0) {
								// No reward available
								mResultDescrTextView.setText(R.string.solution_checking_result_no_reward);
							} else {
								if (mSolutionIsCorrect) {
									mResultDescrTextView.setText(
											getActivity().getString(R.string.solution_checking_result_correct_reward,
													ordinalNumberToString(mSolveNumber), mReward));
									resultDescrTextViewColorRes = R.color.solution_checking_result_correct;
									resultDescrTextViewTypeface = Typeface.BOLD;
								} else {
									mResultDescrTextView.setText(getActivity()
											.getString(R.string.solution_checking_result_potential_reward, mReward));
								}
							}
						}
					}
					mResultDescrTextView
							.setTextColor(ContextCompat.getColor(getActivity(), resultDescrTextViewColorRes));
					mResultDescrTextView.setTypeface(mResultDescrTextView.getTypeface(), resultDescrTextViewTypeface);
					updateButtons(false, false);
				}
			} else {
				// Solution is not valid.
				updateButtons(false, false);
				mNotCompleteTextView.setVisibility(View.VISIBLE);
				mResultPanel.setVisibility(View.GONE);
				mLoadingPanel.setVisibility(View.GONE);
			}
		}

	}

	/* Convert ordinal number to the string representation. */
	private String ordinalNumberToString(int number) {
		int resId = 0;
		if (number == 0) {
			resId = R.string.solution_checking_ordinal_number_zero;
		} else {
			int r = Math.abs(number) % 10;
			int hundR = Math.abs(number) % 100;
			boolean teen = hundR > 10 && hundR < 20;
			if (teen) {
				resId = R.string.solution_checking_ordinal_number;
			} else {
				switch (r) {
				case 1:
					resId = R.string.solution_checking_ordinal_number_one;
					break;
				case 2:
					resId = R.string.solution_checking_ordinal_number_two;
					break;
				case 3:
					resId = R.string.solution_checking_ordinal_number_three;
					break;
				default:
					resId = R.string.solution_checking_ordinal_number;
					break;
				}
			}
		}
		return getActivity().getString(resId, number);
	}

	/* Update dialog control buttons. */
	private void updateButtons(boolean error, boolean cancel) {
		final AlertDialog d = (AlertDialog) getDialog();
		if (d != null) {
			Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
			Button negativeButton = (Button) d.getButton(Dialog.BUTTON_NEGATIVE);
			if (error) {
				positiveButton.setText(R.string.try_again);
				positiveButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						startChecking();
					}
				});
				negativeButton.setText(R.string.cancel);
				negativeButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dismiss();
					}
				});
				negativeButton.setVisibility(View.VISIBLE);
			} else {
				negativeButton.setVisibility(View.GONE);
				if (cancel) {
					positiveButton.setText(R.string.cancel);
					positiveButton.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							d.dismiss();
						}
					});
				} else {
					positiveButton.setText(R.string.ok);
					positiveButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							d.dismiss();
						}
					});
				}

			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_TRY_AGAIN:
			if (resultCode == Activity.RESULT_OK) {
				startChecking();
			} else {
				this.dismiss();
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}

	/** {@link AsyncTask} for performing solution checking in the background. */
	private class CheckingTask extends AsyncTask<Void, Void, BoardServiceI.SolutionCheckResult> {

		private long mCipherId;
		private String mSolution;

		/**
		 * Constructs solution-checker.
		 * 
		 * @param cipherId
		 *            Id of the cipher to check.
		 * @param solution
		 *            Solution to check.
		 */
		public CheckingTask(long cipherId, String solution) {
			super();
			mCipherId = cipherId;
			mSolution = solution;
		}

		@Override
		protected void onPreExecute() {
			mTaskIsRunning = true;
			updatePanels();
		}

		@Override
		protected BoardServiceI.SolutionCheckResult doInBackground(Void... arg0) {
			try {
				return mBoardService.checkSolution(mCipherId, mSolution, mUser);
			} catch (IOException e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(BoardServiceI.SolutionCheckResult result) {
			mTaskIsRunning = false;
			if (result == null) {
				mErrorConnecting = true;
			} else {
				mErrorConnecting = false;
				mSolutionIsCorrect = result.isCorrect();
				mSolveNumber = result.getSolveNumber();
				mFirstToSolve = mSolveNumber == 1;
				mOwnCipher = result.isOwnCipher();
				mAlreadySolved = result.isAlreadySolved();
				mReward = result.getReward();
			}
			updatePanels();
		}

	}
}
