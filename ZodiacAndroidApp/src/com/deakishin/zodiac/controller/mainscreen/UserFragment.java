package com.deakishin.zodiac.controller.mainscreen;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.ConfirmationDialogFragment;
import com.deakishin.zodiac.controller.boardscreen.userstatsscreen.UserStatsActivity;
import com.deakishin.zodiac.controller.loginscreen.LogInSignUpActivity;
import com.deakishin.zodiac.model.avatargenerator.AvatarGenerator;
import com.deakishin.zodiac.model.avatargenerator.AvatarProfile;
import com.deakishin.zodiac.services.userservice.User;
import com.deakishin.zodiac.services.userservice.UserService;
import com.deakishin.zodiac.services.userservice.UserServiceI;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Fragment that displays current logged-in user's info and manages logging
 * in/out. If an activity want to receive notifications about user's status
 * being changed (if user logged in/out), then the activity must implement
 * {@link OnUserChangedListener}.
 */
public class UserFragment extends Fragment {

	/* Ids for dialogs. */
	private static final String DIALOG_CONFIRM_LOG_OUT = "confirmSignOut";

	/* Request codes for child fragment/dialogs. */
	private static final int REQUEST_CONFIRM_LOG_OUT = 10;
	private static final int REQUEST_LOG_IN = 11;

	/* Widgets. */
	private ImageButton mSignOutButton;
	private TextView mLoginTextView;
	private TextView mUsernameTextView;
	private ImageView mAvatarImageView;
	private View mLoginPanel;
	private View mUserInfoPanel;

	/* User service for getting current user's info. */
	private UserServiceI mUserService;

	/* Avatar generator to generate user's avatar. */
	private AvatarGenerator mAvatarGenerator;

	/**
	 * The listener interface for receiving notifications if user has changed.
	 */
	public static interface OnUserChangedListener {
		/**
		 * Invoked when current user has changed (logged in or out).
		 */
		public void onUserChanged();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUserService = UserService.getInstance(getActivity());
		mAvatarGenerator = AvatarGenerator.getInstance(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_user, parent, false);

		mLoginPanel = v.findViewById(R.id.user_login_panel);
		mLoginTextView = (TextView) v.findViewById(R.id.login_textView);
		mLoginTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mUserService.isSignedIn()) {
					mLoginTextView.setEnabled(false);
					Intent i = new Intent(getActivity(), LogInSignUpActivity.class);
					startActivityForResult(i, REQUEST_LOG_IN);
				} else {
					update();
				}
			}
		});

		mUserInfoPanel = v.findViewById(R.id.user_info_panel);
		mUsernameTextView = (TextView) v.findViewById(R.id.user_username_textView);
		View.OnClickListener onUsernameClickedListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), UserStatsActivity.class);
				startActivity(i);
			}
		};
		mUsernameTextView.setOnClickListener(onUsernameClickedListener);
		mAvatarImageView = (ImageView) v.findViewById(R.id.user_avatar_imageView);
		mAvatarImageView.setOnClickListener(onUsernameClickedListener);

		mSignOutButton = (ImageButton) v.findViewById(R.id.signout_imageButton);

		mSignOutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showConfirmSignOutDialog();
			}
		});

		update();

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		update();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONFIRM_LOG_OUT:
			if (resultCode == Activity.RESULT_OK) {
				mUserService.signOut();
				update();
				notifyActivity();
			}
			break;
		case REQUEST_LOG_IN:
			mLoginTextView.setEnabled(true);
			if (resultCode == Activity.RESULT_OK) {
				update();
				notifyActivity();
			}
			break;
		}
	}

	/**
	 * Notify activity that user has changed (if the activity implements
	 * corresponding interface).
	 */
	private void notifyActivity() {
		Activity activity = getActivity();
		if (activity != null && activity instanceof OnUserChangedListener) {
			OnUserChangedListener listener = (OnUserChangedListener) activity;
			listener.onUserChanged();
		}
	}

	/** Updates its views according to current user's info and status. */
	public void update() {
		User user = mUserService.getUser();
		if (user == null) {
			mLoginPanel.setVisibility(View.VISIBLE);
			mUserInfoPanel.setVisibility(View.INVISIBLE);
		} else {
			mLoginPanel.setVisibility(View.INVISIBLE);
			mUserInfoPanel.setVisibility(View.VISIBLE);

			if (user.getName() != null)
				mUsernameTextView.setText(user.getName());
			else
				mUsernameTextView.setText(R.string.unknown_user);

			AvatarProfile avatarProfile = new AvatarProfile(user.getAvatarMarkup());
			Bitmap avatar = mAvatarGenerator.generateAvatarBitmap(avatarProfile);

			// Освобождение текущего изображения аватара.
			if (mAvatarImageView.getDrawable() != null) {
				Bitmap oldBitmap = ((BitmapDrawable) mAvatarImageView.getDrawable()).getBitmap();
				mAvatarImageView.setImageDrawable(null);
				oldBitmap.recycle();
			}

			mAvatarImageView.setImageBitmap(avatar);
		}
	}

	/* Show dialog for confirming logging out. */
	private void showConfirmSignOutDialog() {
		ConfirmationDialogFragment dialog = ConfirmationDialogFragment.getInstance(getString(R.string.log_out_confirm),
				getString(R.string.log_out_confirm_option_yes), REQUEST_CONFIRM_LOG_OUT, false);
		dialog.setTargetFragment(this, REQUEST_CONFIRM_LOG_OUT);
		dialog.show(getActivity().getSupportFragmentManager(), DIALOG_CONFIRM_LOG_OUT);
	}

	@Override
	public void onPause() {
		super.onPause();
		// mUsernameTextView.setEnabled(true);
	}
}
