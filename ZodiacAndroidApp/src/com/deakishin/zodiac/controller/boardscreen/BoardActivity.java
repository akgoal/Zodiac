package com.deakishin.zodiac.controller.boardscreen;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.ConfirmationDialogFragment;
import com.deakishin.zodiac.controller.boardscreen.ciphercreatingscreen.CipherCreatingActivity;
import com.deakishin.zodiac.controller.boardscreen.topusersscreen.TopUsersActivity;
import com.deakishin.zodiac.controller.boardscreen.userstatsscreen.UserStatsActivity;
import com.deakishin.zodiac.controller.loginscreen.LogInSignUpActivity;
import com.deakishin.zodiac.services.userservice.UserService;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

/**
 * Activity for displaying Cipher Board. Hosts BoardFragment that displays
 * CipherBoard. Also manages search invocation and user logging in/out.
 */
public class BoardActivity extends AppCompatActivity
		implements ConfirmationDialogFragment.ConfirmationListener, BoardFragment.OnNonLoggedLikeListener {

	/* Keys for dialogs. */
	private static final String DIALOG_MUST_LOG_IN_TO_CREATE = "mustLogInToCreate";
	private static final String DIALOG_MUST_LOG_IN_TO_LIKE = "mustLogInToLike";
	private static final String DIALOG_CONFIRM_LOG_OUT = "confirmSignOut";

	/* Request codes for activities and fragments. */
	private static final int REQUEST_CONFIRM_LOG_IN = 0;
	private static final int REQUEST_LOG_IN = 1;
	private static final int REQUEST_CONFIRM_LOG_OUT = 2;
	private static final int REQUEST_PROFILE_CHANGED = 3;

	/* Keys for saving current state when configuration changes. */
	private static final String INDEX_SEARCH_EXPANDED = "searchExpanded";
	private static final String INDEX_SEARCH_QUERY = "searchQuery";
	private static final String INDEX_SEARCH_IN_FOCUS = "searchInFocus";

	/* Fragment for displaying CipherBoard. */
	private BoardFragment mBoardFragment;

	/* Menu item for managing user logging in/out. */
	private MenuItem mLogInMenuItem;

	/* Widgets. */
	private ImageButton mCreateCipherButton;
	private SearchView mSearchView;

	/* Indicates if search view is expanded.. */
	private boolean mSearchActionExpanded = false;
	/* Search queary in the search view. */
	private String mSearchQuery;
	/* Indicates if the search view is in focus. */
	private boolean mSearchInFocus = false;

	/* User service for managing user's logging in/out. */
	private UserService mUserService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mSearchActionExpanded = savedInstanceState.getBoolean(INDEX_SEARCH_EXPANDED, false);
			mSearchQuery = savedInstanceState.getString(INDEX_SEARCH_QUERY);
			mSearchInFocus = savedInstanceState.getBoolean(INDEX_SEARCH_IN_FOCUS, false);
		}

		mUserService = UserService.getInstance(this);

		setContentView(R.layout.board_activity);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FragmentManager fm = getSupportFragmentManager();
		mBoardFragment = (BoardFragment) fm.findFragmentById(R.id.fragmentContainer);
		if (mBoardFragment == null) {
			mBoardFragment = new BoardFragment();
			fm.beginTransaction().add(R.id.fragmentContainer, mBoardFragment).commit();
		}

		mCreateCipherButton = (ImageButton) findViewById(R.id.board_create_cipher_floating_button);
		mCreateCipherButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mUserService.isSignedIn()) {
					showMustLogInMessage(getString(R.string.board_dialog_must_login_to_create_cipher),
							DIALOG_MUST_LOG_IN_TO_CREATE);
				} else {
					Intent i = new Intent(BoardActivity.this, CipherCreatingActivity.class);
					startActivity(i);
				}
			}
		});

		if (NavUtils.getParentActivityName(this) != null)
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	/* Show confirmation dialog that prompts user to log in. */
	private void showMustLogInMessage(String message, String dialogTag) {
		ConfirmationDialogFragment dialog = ConfirmationDialogFragment.getInstance(message, getString(R.string.log_in),
				REQUEST_CONFIRM_LOG_IN, true);
		dialog.show(getSupportFragmentManager(), dialogTag);
	}

	@Override
	public void onResume() {
		super.onResume();
		updateLogInMenuItem();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_board, menu);
		mLogInMenuItem = menu.findItem(R.id.menu_item_log_in);
		updateLogInMenuItem();

		MenuItem searchItem = menu.findItem(R.id.menu_item_search);
		mSearchView = (SearchView) searchItem.getActionView();

		if (mSearchActionExpanded) {
			MenuItemCompat.expandActionView(searchItem);
			if (mSearchQuery != null)
				mSearchView.setQuery(mSearchQuery, false);
		} else
			MenuItemCompat.collapseActionView(searchItem);

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		ComponentName name = getComponentName();
		SearchableInfo searchInfo = searchManager.getSearchableInfo(name);
		mSearchView.setSearchableInfo(searchInfo);

		if (mSearchInFocus)
			mSearchView.requestFocus();
		else if (mSearchView.hasFocus())
			mSearchView.clearFocus();

		MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {

			@Override
			public boolean onMenuItemActionCollapse(MenuItem arg0) {
				mBoardFragment.clearSearhQuery();
				mSearchActionExpanded = false;
				return true;
			}

			@Override
			public boolean onMenuItemActionExpand(MenuItem arg0) {
				mSearchActionExpanded = true;
				return true;
			}
		});

		return true;
	}

	/* Update menu item for user logging in/out according to user's current status. */
	private void updateLogInMenuItem() {
		if (mLogInMenuItem != null) {
			if (mUserService.isSignedIn())
				mLogInMenuItem.setTitle(R.string.board_menu_my_profile);
			else
				mLogInMenuItem.setTitle(R.string.menu_log_in);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (NavUtils.getParentActivityName(this) != null) {
				NavUtils.navigateUpFromSameTask(this);
			}
			return true;
		case R.id.menu_item_log_in:
			if (mUserService.isSignedIn()) {
				Intent i = new Intent(this, UserStatsActivity.class);
				startActivityForResult(i, REQUEST_PROFILE_CHANGED);
			} else {
				Intent i = new Intent(this, LogInSignUpActivity.class);
				startActivityForResult(i, REQUEST_LOG_IN);
			}
			return true;
		case R.id.menu_item_search:
			// onSearchRequested();
			return true;
		case R.id.menu_item_top_users:
			Intent i = new Intent(this, TopUsersActivity.class);
			startActivity(i);
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_LOG_IN:
			if (resultCode == Activity.RESULT_OK) {
				mBoardFragment.update();
				updateLogInMenuItem();
			}
			break;
		case REQUEST_PROFILE_CHANGED:
			if (resultCode == Activity.RESULT_OK) {
				// User profile changes so board needs to be updated.
				mBoardFragment.update();
				updateLogInMenuItem();
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			mBoardFragment.setSearchQuery(query);
			if (mSearchView != null && mSearchView.hasFocus())
				mSearchView.clearFocus();
		}
	}

	@Override
	public void onConfirmed(int requestCode, int resultCode) {
		switch (requestCode) {
		case REQUEST_CONFIRM_LOG_IN:
			if (resultCode == Activity.RESULT_OK) {
				Intent i = new Intent(this, LogInSignUpActivity.class);
				startActivityForResult(i, REQUEST_LOG_IN);
			}
			break;
		case REQUEST_CONFIRM_LOG_OUT:
			if (resultCode == Activity.RESULT_OK) {
				mUserService.signOut();
				mBoardFragment.update();
				updateLogInMenuItem();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onNonLoggedLiked() {
		showMustLogInMessage(getString(R.string.board_dialog_must_login_to_like_cipher), DIALOG_MUST_LOG_IN_TO_LIKE);
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		// Save SearchView state
		if (mSearchView != null) {
			if (mSearchView.getQuery() != null) {
				String query = mSearchView.getQuery().toString();
				savedInstanceState.putString(INDEX_SEARCH_QUERY, query);
			}
			savedInstanceState.putBoolean(INDEX_SEARCH_IN_FOCUS, mSearchView.hasFocus());
		}
		savedInstanceState.putBoolean(INDEX_SEARCH_EXPANDED, mSearchActionExpanded);
	}
}
