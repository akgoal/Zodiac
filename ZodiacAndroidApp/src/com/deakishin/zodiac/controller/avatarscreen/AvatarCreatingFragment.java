package com.deakishin.zodiac.controller.avatarscreen;

import java.io.IOException;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.ConfirmationDialogFragment;
import com.deakishin.zodiac.controller.MessageDialogFragment;
import com.deakishin.zodiac.model.avatargenerator.AvatarGenerator;
import com.deakishin.zodiac.model.avatargenerator.AvatarPart;
import com.deakishin.zodiac.model.avatargenerator.AvatarPartOption;
import com.deakishin.zodiac.model.avatargenerator.AvatarProfile;
import com.deakishin.zodiac.model.avatargenerator.AvatarProfile.PartProfile;
import com.deakishin.zodiac.services.boardservice.BoardServiceI;
import com.deakishin.zodiac.services.boardservice.BoardServiceImpl;
import com.deakishin.zodiac.services.userservice.User;
import com.deakishin.zodiac.services.userservice.UserService;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.SeekBar;

/**
 * Fragment for creating and editing the avatar.
 */
public class AvatarCreatingFragment extends Fragment {

	/* IDs for child fragments. */
	private static final String DIALOG_ERROR_SAVE = "errorSave";
	private static final String DIALOG_SUCCESS_SAVE = "successSave";

	/* Request codes to child fragments. */
	private static final int REQUEST_SUCCESS_SAVE = 10;
	private static final int REQUEST_TRY_AGAIN = 11;

	/* Widgets and adapters. */
	private ImageView mPreviewImageView;
	private RecyclerView mFacePartsView, mFacePartOptionsView;
	private FacePartsViewAdapter mFacePartsViewAdapter;
	private FacePartOptionsViewAdapter mPartOptionsViewAdapter;

	private SeekBar mOffsetHorizSeekBar, mScaleHorizSeekBar;
	private VerticalSeekBar mOffsetVerticSeekBar, mScaleVerticSeekBar;

	private Button mSaveButton;

	/* Maximum value for seekbar widgets. */
	private static final int MAX_SEEKBAR_PROGRESS = 100;

	// 50x50 preview image view.
	// private ImageView mPreview50ImageView;

	/* Avatar generator. */
	private AvatarGenerator mAvatarGenerator;

	/* Current avatar part that's being edited. */
	private AvatarPart mCurrentPart;
	/* Id for the chosen option for the part that's being edited. */
	private int mCurrentPartOptionId = -1;

	/* Avatar profile, containin info about its parts. */
	private AvatarProfile mAvatarProfile;

	/* Current logged-in user. */
	private User mUser;

	/* User service. */
	private UserService mUserService;

	/* Board service to upload changes. */
	private BoardServiceI mBoardService;

	/* Background color for generated avatar image. */
	private static final Integer BG_COLOR = Color.WHITE;

	/* Flag indicating if uploading is in process. */
	private boolean mTaskIsRunning = false;

	/* True if error or success messages must be shown on resume. */
	private boolean mShowSaveErrorOnResume = false;
	private boolean mShowSaveSuccessOnResume = false;

	/**
	 * Callback interface for receiving callbacks when avatar is successfully
	 * saved.
	 */
	public static interface SuccessCallback {
		/**
		 * Invoked when avatar is successfully saved.
		 */
		public void onSaveSuccess();
	}

	/* Part id for the default avatar. */
	private static final int DEF_AVATAR_ID = -100;

	/* indicates if the default avatar is chosen. */
	private boolean mDefAvatarChosen;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);

		mAvatarGenerator = AvatarGenerator.getInstance(getActivity());

		mFacePartsViewAdapter = new FacePartsViewAdapter(getActivity(), mAvatarGenerator.getParts());
		AvatarPart defAvatarPart = new AvatarPart(DEF_AVATAR_ID, mAvatarGenerator.getDefaultAvatar(), null);
		mFacePartsViewAdapter.setFirstItem(defAvatarPart);

		mPartOptionsViewAdapter = new FacePartOptionsViewAdapter(getActivity());

		mUser = UserService.getInstance(getActivity()).getUser();

		if (mUser == null || mUser.getAvatarMarkup() == null) {
			mAvatarProfile = mAvatarGenerator.getDefaultProfile();
			mDefAvatarChosen = true;
			mFacePartsViewAdapter.setSelectedId(DEF_AVATAR_ID);
			mCurrentPart = defAvatarPart;
		} else {
			mAvatarProfile = new AvatarProfile(mUser.getAvatarMarkup());
			mDefAvatarChosen = false;
		}

		mBoardService = BoardServiceImpl.getImpl(getActivity());
		mUserService = UserService.getInstance(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater layoutInflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = layoutInflater.inflate(R.layout.avatar_creating_fragment, null);

		mPreviewImageView = (ImageView) v.findViewById(R.id.avatar_creating_preview_imageView);
		mPreviewImageView.setScaleType(ScaleType.FIT_XY);

		/*
		 * mPreview50ImageView = (ImageView)
		 * v.findViewById(R.id.avatar_creating_preview50_imageView);
		 * mPreview50ImageView.setScaleType(ScaleType.FIT_XY);
		 */

		View verticalIndicator = v.findViewById(R.id.avatar_creating_vertical_indicator);
		int linearLayoutOrientation = verticalIndicator == null ? LinearLayoutManager.HORIZONTAL
				: LinearLayoutManager.VERTICAL;

		mFacePartsView = (RecyclerView) v.findViewById(R.id.avatar_creating_face_parts_recyclerView);
		mFacePartsView.setLayoutManager(new LinearLayoutManager(getActivity(), linearLayoutOrientation, false));
		mFacePartsView.setAdapter(mFacePartsViewAdapter);
		mFacePartsViewAdapter.setOnItemClickListener(new FacePartsViewAdapter.OnItemClickListener() {
			@Override
			public void onItemClicked(AvatarPart avatarPart) {
				if (!mFacePartsView.isEnabled())
					return;

				if (isPartDefAvatar(avatarPart)) {
					mDefAvatarChosen = true;
					mPartOptionsViewAdapter.setDataset(null);
					mCurrentPart = avatarPart;
					updatePreview();
				} else {
					mDefAvatarChosen = false;
					mPartOptionsViewAdapter.setDataset(avatarPart.getOptions());
					mCurrentPart = avatarPart;
				}

				mFacePartsViewAdapter.setSelectedId(mCurrentPart.getId());

				resetPartSpecificViews();
			}
		});

		mFacePartOptionsView = (RecyclerView) v.findViewById(R.id.avatar_creating_parts_options_recyclerView);
		mFacePartOptionsView.setLayoutManager(new LinearLayoutManager(getActivity(), linearLayoutOrientation, false));
		mFacePartOptionsView.setAdapter(mPartOptionsViewAdapter);
		mPartOptionsViewAdapter.setOnItemClickListener(new FacePartOptionsViewAdapter.OnItemClickListener() {
			@Override
			public void onItemClicked(AvatarPartOption avatarPartOption) {
				if (!mFacePartOptionsView.isEnabled())
					return;

				mCurrentPartOptionId = avatarPartOption.getId();
				mPartOptionsViewAdapter.setSelectedId(mCurrentPartOptionId);
				updatePreview();
			}
		});

		SeekBar.OnSeekBarChangeListener seekBarProgressListener = new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				updatePreview();
			}
		};

		mOffsetHorizSeekBar = (SeekBar) v.findViewById(R.id.avatar_creating_offset_horiz_seekBar);
		mOffsetHorizSeekBar.setMax(MAX_SEEKBAR_PROGRESS);
		mOffsetHorizSeekBar.setOnSeekBarChangeListener(seekBarProgressListener);
		mScaleHorizSeekBar = (SeekBar) v.findViewById(R.id.avatar_creating_scale_horiz_seekBar);
		mScaleHorizSeekBar.setMax(MAX_SEEKBAR_PROGRESS);
		mScaleHorizSeekBar.setOnSeekBarChangeListener(seekBarProgressListener);

		mOffsetVerticSeekBar = (VerticalSeekBar) v.findViewById(R.id.avatar_creating_offset_vertic_seekBar);
		mOffsetVerticSeekBar.setMaximum(MAX_SEEKBAR_PROGRESS);
		mOffsetVerticSeekBar.setOnSeekBarChangeListener(seekBarProgressListener);
		mScaleVerticSeekBar = (VerticalSeekBar) v.findViewById(R.id.avatar_creating_scale_vertic_seekBar);
		mScaleVerticSeekBar.setMaximum(MAX_SEEKBAR_PROGRESS);
		mScaleVerticSeekBar.setOnSeekBarChangeListener(seekBarProgressListener);

		resetPartSpecificViews();
		updatePreview();
		updateViewsEnability();

		return v;
	}

	/* Indicates if given part is a flag for the default avatar. */
	private boolean isPartDefAvatar(AvatarPart part) {
		return part.getId() == DEF_AVATAR_ID;
	}

	/* Indicates if changes are made. */
	public boolean changesAreMade() {
		if (mUser == null)
			return false;

		if (mUser.getAvatarMarkup() == null) {
			return !mDefAvatarChosen;
		} else {
			if (mDefAvatarChosen) {
				return true;
			} else {
				return !mUser.getAvatarMarkup().equals(mAvatarProfile.toMarkup());
			}
		}
	}

	/* Update widgets enability according to current state. */
	private void updateViewsEnability() {
		if (mTaskIsRunning) {
			if (mSaveButton != null) {
				mSaveButton.setText(R.string.avatar_creating_menu_item_saving);
				mSaveButton.setEnabled(false);
			}
		} else {
			if (mSaveButton != null) {
				mSaveButton.setText(R.string.avatar_creating_menu_item_save);
				mSaveButton.setEnabled(true);
			}
		}
		mFacePartOptionsView.setEnabled(!mTaskIsRunning);
		mFacePartsView.setEnabled(!mTaskIsRunning);
		mOffsetHorizSeekBar.setEnabled(!mTaskIsRunning);
		mOffsetVerticSeekBar.setEnabled(!mTaskIsRunning);
		mScaleHorizSeekBar.setEnabled(!mTaskIsRunning);
		mScaleVerticSeekBar.setEnabled(!mTaskIsRunning);
	}

	/* Update avatar image preview. */
	private void updatePreview() {
		if (mCurrentPart != null && !mDefAvatarChosen) {
			float offsetHoriz = convertProgressToFloat(mOffsetHorizSeekBar.getProgress());
			float offsetVertic = convertProgressToFloat(MAX_SEEKBAR_PROGRESS - mOffsetVerticSeekBar.getProgress());
			float scaleHoriz = convertProgressToFloat(mScaleHorizSeekBar.getProgress());
			float scaleVertic = convertProgressToFloat(mScaleVerticSeekBar.getProgress());

			PartProfile partProfile = new PartProfile(mCurrentPartOptionId, offsetHoriz, offsetVertic, scaleHoriz,
					scaleVertic);
			mAvatarProfile.setPartProfile(mCurrentPart.getId(), partProfile);
		}

		Bitmap bitmap = null;
		if (mCurrentPart != null && mDefAvatarChosen) {
			bitmap = mAvatarGenerator.generateAvatarBitmap(null, BG_COLOR);
		} else {
			bitmap = mAvatarGenerator.generateAvatarBitmap(mAvatarProfile, BG_COLOR);
		}
		mPreviewImageView.setImageBitmap(bitmap);
	}

	/* Convert progress in [0,MAX_SEEKBAR_PROGRESS] to a float number between [-1,1]. */
	private float convertProgressToFloat(int progress) {
		return 2 * progress / (float) MAX_SEEKBAR_PROGRESS - 1;
	}

	/* Convert a float number between [-1,1] to progress in [0,MAX_SEEKBAR_PROGRESS]. */
	private int convertFloatToProgress(float number) {
		return (int) ((1 + number) * MAX_SEEKBAR_PROGRESS / 2);
	}

	/* Reset widgets that depend on current avatar part. */
	private void resetPartSpecificViews() {
		int visibility;
		if (mCurrentPart == null || mDefAvatarChosen) {
			visibility = View.INVISIBLE;
		} else {
			visibility = View.VISIBLE;
		}
		mOffsetHorizSeekBar.setVisibility(visibility);
		mScaleHorizSeekBar.setVisibility(visibility);
		mOffsetVerticSeekBar.setVisibility(visibility);
		mScaleVerticSeekBar.setVisibility(visibility);

		if (mCurrentPart == null || mDefAvatarChosen)
			return;

		PartProfile partProfile = mAvatarProfile.getOptionForPart(mCurrentPart.getId());
		if (partProfile != null) {
			mCurrentPartOptionId = partProfile.getOptionId();

			mOffsetHorizSeekBar.setProgress(convertFloatToProgress(partProfile.getOffsetX()));
			mScaleHorizSeekBar.setProgress(convertFloatToProgress(partProfile.getScaleX()));
			mOffsetVerticSeekBar
					.setProgressAndThumb(MAX_SEEKBAR_PROGRESS - convertFloatToProgress(partProfile.getOffsetY()));
			mScaleVerticSeekBar.setProgressAndThumb(convertFloatToProgress(partProfile.getScaleY()));
		} else {
			mCurrentPartOptionId = -1;

			mOffsetHorizSeekBar.setProgress(MAX_SEEKBAR_PROGRESS / 2);
			mScaleHorizSeekBar.setProgress(MAX_SEEKBAR_PROGRESS / 2);
			mOffsetVerticSeekBar.setProgressAndThumb(MAX_SEEKBAR_PROGRESS / 2);
			mScaleVerticSeekBar.setProgressAndThumb(MAX_SEEKBAR_PROGRESS / 2);
		}
		mPartOptionsViewAdapter.setSelectedId(mCurrentPartOptionId);
		mFacePartOptionsView.scrollToPosition(mPartOptionsViewAdapter.getPositionForId(mCurrentPartOptionId));
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Callback for successfull save.
		if (requestCode == REQUEST_SUCCESS_SAVE) {
			Activity activity = getActivity();
			if (activity instanceof SuccessCallback)
				((SuccessCallback) activity).onSaveSuccess();
			return;
		}
		// Callback for try-again.
		if (requestCode == REQUEST_TRY_AGAIN) {
			if (resultCode != Activity.RESULT_OK)
				return;
			startSave();
			return;
		}
	}

	/* Initiate saving avatar and uploading it to server. */
	private void startSave() {
		if (mUser == null)
			return;

		String newMarkup = mDefAvatarChosen ? "" : mAvatarProfile.toMarkup();
		new SaveTask().execute(newMarkup);
	}

	/* Show connection error message. */
	private void showConnectionError() {
		if (!this.isResumed()) {
			mShowSaveErrorOnResume = true;
			return;
		}
		DialogFragment dialog = ConfirmationDialogFragment.getInstance(
				getActivity().getString(R.string.avatar_creating_error_save),
				getActivity().getString(R.string.try_again), REQUEST_TRY_AGAIN, false);
		dialog.setTargetFragment(this, REQUEST_TRY_AGAIN);
		dialog.show(getActivity().getSupportFragmentManager(), DIALOG_ERROR_SAVE);
	}

	/* Show message about successful save. */
	private void showSuccessDialog() {
		if (!this.isResumed()) {
			mShowSaveSuccessOnResume = true;
			return;
		}
		FragmentManager fm = getActivity().getSupportFragmentManager();
		MessageDialogFragment dialog = MessageDialogFragment
				.getInstance(getActivity().getString(R.string.avatar_creating_success_save), true);
		dialog.setTargetFragment(this, REQUEST_SUCCESS_SAVE);
		dialog.show(fm, DIALOG_SUCCESS_SAVE);
	}

	@Override
	public void onResume() {
		super.onResume();
		// Show message dialogs if necessary.
		if (mShowSaveErrorOnResume) {
			showConnectionError();
			mShowSaveErrorOnResume = false;
		}
		if (mShowSaveSuccessOnResume) {
			showSuccessDialog();
			mShowSaveSuccessOnResume = false;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_avatar_creating, menu);
		MenuItem saveItem = menu.findItem(R.id.avatar_creating_menu_item_save);

		View v = getActivity().getLayoutInflater().inflate(R.layout.avatar_creating_save_panel, null);

		mSaveButton = (Button) v.findViewById(R.id.avatar_creating_save_panel_button);
		mSaveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startSave();
			}
		});
		if (mTaskIsRunning) {
			mSaveButton.setText(R.string.avatar_creating_menu_item_saving);
			mSaveButton.setEnabled(false);
		} else {
			mSaveButton.setText(R.string.avatar_creating_menu_item_save);
			mSaveButton.setEnabled(true);
		}

		saveItem.setActionView(v);
	}

	/* {@link AsyncTask} for uploading created avatar in the background. */
	private class SaveTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			mTaskIsRunning = true;
			updateViewsEnability();
		}

		@Override
		protected Boolean doInBackground(String... arg0) {
			if (arg0.length != 1)
				return false;
			try {
				String newAvatarMarkup = arg0[0];
				if (newAvatarMarkup.equals(""))
					newAvatarMarkup = null;
				boolean success = mBoardService.changeUserAvatar(mUser, newAvatarMarkup);
				if (success) {
					mUser.setAvatarMarkup(newAvatarMarkup);
					mUserService.signIn(mUser);
				}
				return success;
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
			updateViewsEnability();
		}
	}
}
