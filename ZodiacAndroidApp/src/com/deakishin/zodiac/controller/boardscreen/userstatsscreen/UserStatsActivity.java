package com.deakishin.zodiac.controller.boardscreen.userstatsscreen;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.ConfirmationDialogFragment;
import com.deakishin.zodiac.controller.avatarscreen.AvatarCreatingActivity;
import com.deakishin.zodiac.services.userservice.User;
import com.deakishin.zodiac.services.userservice.UserService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Activity for displaying user stats.
 * 
 * if stats are displayed for currently logged-in user then it also provides
 * options to log out and to change profile. Returns {@link Activity#RESULT_OK} if
 * user's status or profile are changed.
 */
public class UserStatsActivity extends AppCompatActivity implements ConfirmationDialogFragment.ConfirmationListener {

	/** Key for passing user as a parameter when starting the activity. */
	public static final String EXTRA_USER = "extra_user";

	/* Keys for dialogs. */
	private static final String DIALOG_CONFIRM_LOG_OUT = "confirmSignOut";

	/* Request codes for dialogs. */
	private static final int REQUEST_AVATAR_EDITING = 1;
	private static final int REQUEST_CONFIRM_LOG_OUT = 2;

	/* User service. */
	private UserService mUserService;

	/* Hosted fragment for displaying user stats. */
	private Fragment mFragment;

	/* User whose stats are displayed. */
	private User mUser;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mUserService = UserService.getInstance(this);

		setContentView(R.layout.activity_fragment);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		if (getIntent().getExtras() != null) {
			mUser = getIntent().getExtras().getParcelable(EXTRA_USER);
		}
		if (mUser == null) {
			mUser = mUserService.getUser();
		}

		FragmentManager fm = getSupportFragmentManager();
		mFragment = fm.findFragmentById(R.id.fragmentContainer);
		if (mFragment == null) {
			mFragment = UserStatsFragment.getInstance(mUser);
			fm.beginTransaction().add(R.id.fragmentContainer, mFragment).commit();
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (checkUser()) {
			getMenuInflater().inflate(R.menu.activity_userstats, menu);
			if (getSupportActionBar() != null)
				getSupportActionBar().setTitle(R.string.userstats_screen_title_my_profile);
		}
		return true;
	}

	/*
	 * Indicates if the displayed user is the one that is currently logged in.
	 */
	private boolean checkUser() {
		if (mUser == null)
			return false;

		User loggedInUser = UserService.getInstance(this).getUser();
		if (loggedInUser == null)
			return false;

		return loggedInUser.getId().equals(mUser.getId());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.userstats_menu_item_logout:
			if (mUserService.isSignedIn()) {
				showConfirmSignOutDialog();
			}
			return true;
		case R.id.userstats_menu_item_edit_avatar:
			if (mUserService.isSignedIn()) {
				Intent i = new Intent(this, AvatarCreatingActivity.class);
				startActivityForResult(i, REQUEST_AVATAR_EDITING);
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* Show confirmation dialog to confirm logging out. */
	private void showConfirmSignOutDialog() {
		ConfirmationDialogFragment dialog = ConfirmationDialogFragment.getInstance(getString(R.string.log_out_confirm),
				getString(R.string.log_out_confirm_option_yes), REQUEST_CONFIRM_LOG_OUT, true);
		dialog.show(getSupportFragmentManager(), DIALOG_CONFIRM_LOG_OUT);
	}

	@Override
	public void onConfirmed(int requestCode, int resultCode) {
		switch (requestCode) {
		case REQUEST_CONFIRM_LOG_OUT:
			if (resultCode == Activity.RESULT_OK) {
				// Log out confirmed.
				mUserService.signOut();
				setResult(Activity.RESULT_OK);
				finish();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_AVATAR_EDITING:
			if (resultCode == Activity.RESULT_OK) {
				// Avatar is changed.
				setResult(Activity.RESULT_OK);
				if (mFragment instanceof UserStatsFragment)
					((UserStatsFragment) mFragment).update();
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

}
