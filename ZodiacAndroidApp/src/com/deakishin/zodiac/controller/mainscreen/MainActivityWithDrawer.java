package com.deakishin.zodiac.controller.mainscreen;

import java.util.Calendar;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.AnimatedExpandableListView;
import com.deakishin.zodiac.controller.ConfirmationDialogFragment;
import com.deakishin.zodiac.controller.InputSingleLineDialogFragment;
import com.deakishin.zodiac.controller.MessageDialogFragment;
import com.deakishin.zodiac.controller.boardscreen.BoardActivity;
import com.deakishin.zodiac.controller.feedbackscreen.FeedbackActivity;
import com.deakishin.zodiac.controller.helpscreen.HelpActivity;
import com.deakishin.zodiac.controller.loginscreen.LogInSignUpActivity;
import com.deakishin.zodiac.controller.mainscreen.dialogs.DialogCipherInfo;
import com.deakishin.zodiac.controller.mainscreen.dialogs.DialogSolutionChecking;
import com.deakishin.zodiac.controller.settingsscreen.SettingsActivity;
import com.deakishin.zodiac.model.ciphermanager.CipherInfo;
import com.deakishin.zodiac.model.ciphermanager.CipherManager;
import com.deakishin.zodiac.model.ciphermanager.CipherShortInfo;
import com.deakishin.zodiac.model.ciphermodel.bindingmanager.BindingManager;
import com.deakishin.zodiac.model.ciphermodel.bindingmanager.CheckPoint;
import com.deakishin.zodiac.model.framework.FileIO;
import com.deakishin.zodiac.model.settings.CheckpointNameOptions;
import com.deakishin.zodiac.model.settings.FontColorProfile;
import com.deakishin.zodiac.model.settings.SettingsPersistent;
import com.deakishin.zodiac.services.userservice.UserService;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

/**
 * Activity for application's main screen.It also manages the navigation drawer
 * on the side.
 */
public class MainActivityWithDrawer extends AppCompatActivity
		implements SettingsPersistent.OnFontColorProfileChangeListener, ConfirmationDialogFragment.ConfirmationListener,
		UserFragment.OnUserChangedListener, InputSingleLineDialogFragment.InputListener {

	/* Keys for dialogs. */
	private static final String DIALOG_CHECKPOINT_NAME = "checkpointName";
	private static final String DIALOG_SAVED_IMAGE_NAME = "savedImageName";
	private static final String DIALOG_ERROR_SAVING_IMAGE = "errorSavingImage";
	private static final String DIALOG_ERROR_LOADING_CIPHER = "errorLoadingCipher";
	private static final String DIALOG_CONFIRM_CIPHER_LOAD = "confirmCipherLoad";
	private static final String DIALOG_CONFIRM_CIPHER_DELETE = "confirmCipherDelete";
	private static final String DIALOG_CIPHER_INFO = "cipherInfo";
	private static final String DIALOG_SOLUTION_CHECKING = "checkSolution";
	private static final String DIALOG_MUST_LOG_IN_TO_CHECK = "mustLogInToCheck";
	private static final String DIALOG_CONFIRM_ASK_PERMISSIONS = "confirmAskPermissions";

	/* Request codes. */
	private static final int REQUEST_BOARD = 0;
	private static final int REQUEST_CONFIRM_CIPHER_LOAD = 1;
	private static final int REQUEST_CONFIRM_CIPHER_DELETE = 2;
	private static final int REQUEST_INPUT_CHECKPOINT_NAME = 3;
	private static final int REQUEST_INPUT_SAVED_IMAGE_FILENAME = 4;
	private static final int REQUEST_CONFIRM_LOG_IN = 5;
	private static final int REQUEST_LOG_IN = 6;
	private static final int REQUEST_CONFIRM_PERMISSIONS = 7;
	private static final int REQUEST_ASK_PERMISSIONS = 8;

	/* Binding manager that manages symbol-letter mapping. */
	private BindingManager mBindingManager;

	/* Widgets in the navigation drawer. */
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private AnimatedExpandableListView mExpandableListView;
	private TextView mCipherTitleTextView;
	private View mCipherInfoPanel;

	/* Adapter fot the list in the navigation drawer. */
	private DrawerListAdapter mDrawerListAdapter;

	/* Fragment with the main cipher's image. */
	private ImageFragment mImageFragment;

	/* Fragment that displays and manages user's info. */
	private UserFragment mUserFragment;

	/* Menu items. */
	private MenuItem mCheckpointMenuItem;

	/* Application settings. */
	private SettingsPersistent mSettings;

	/* Checkpoint-makings icon when selected. */
	private Drawable mCheckpointMenuItemSelectedIcon;

	/* Object that manages file input-output. */
	private FileIO mFileIO;

	/* Cipher manager. */
	private CipherManager mCipherManager;

	private CipherShortInfo mTmpCipherShortInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSettings = SettingsPersistent.getInstance(this);

		mCipherManager = CipherManager.getInstance(this);

		mBindingManager = mCipherManager.getCipherModel().getBindingManager();

		mFileIO = FileIO.getInstance(this);

		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		toolbar.setLogo(R.drawable.zodiaccrypt_logo);

		FragmentManager fm = getSupportFragmentManager();
		mImageFragment = (ImageFragment) fm.findFragmentById(R.id.fragmentContainer);
		if (mImageFragment == null) {
			mImageFragment = new ImageFragment();
			fm.beginTransaction().add(R.id.fragmentContainer, mImageFragment).commit();
		}
		mUserFragment = (UserFragment) fm.findFragmentById(R.id.drawerHeaderFragmentContainer);
		if (mUserFragment == null) {
			mUserFragment = new UserFragment();
			fm.beginTransaction().add(R.id.drawerHeaderFragmentContainer, mUserFragment).commit();
		}

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mDrawerLayout.addDrawerListener(mDrawerToggle);

		mExpandableListView = (AnimatedExpandableListView) findViewById(R.id.drawer_explistView);
		View v = getLayoutInflater().inflate(R.layout.drawer_main_header, null);
		mCipherTitleTextView = (TextView) v.findViewById(R.id.drawer_main_header_textView);
		mCipherTitleTextView.setText(mCipherManager.getCurrentCipherInfo().getTitle());
		mCipherInfoPanel = v.findViewById(R.id.drawer_main_header_panel);
		mCipherInfoPanel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showCurrentCipherInfo(mCipherManager.getCurrentCipherInfo());
			}
		});
		mExpandableListView.addHeaderView(v);
		mDrawerListAdapter = new DrawerListAdapter(this, mBindingManager.getCheckPoints(),
				mCipherManager.getOtherCipherInfos(), new DrawerListAdapter.ClickCallback() {
					@Override
					public void onHelpGroupClicked() {
						Intent i = new Intent(MainActivityWithDrawer.this, HelpActivity.class);
						startActivity(i);
					}

					@Override
					public void onSettingsGroupClicked() {
						Intent i = new Intent(MainActivityWithDrawer.this, SettingsActivity.class);
						startActivity(i);
					}

					@Override
					public void onCheckpointClicked(CheckPoint checkpoint) {
						mBindingManager.setCurrBindingFromCheckPoint(checkpoint);
						mImageFragment.update();
					}

					@Override
					public void onCheckpointDeleted(CheckPoint checkpoint) {
						mBindingManager.removeCheckPoint(checkpoint);
						mDrawerListAdapter.notifyDataSetChanged();
					}

					@Override
					public void onSaveImageGroupClicked() {
						showSavedImageNameDialog();
					}

					@Override
					public void onBoardGroupSelected() {
						Intent i = new Intent(MainActivityWithDrawer.this, BoardActivity.class);
						startActivityForResult(i, REQUEST_BOARD);
					}

					@Override
					public void onCipherClicked(CipherShortInfo cipherInfo) {
						mTmpCipherShortInfo = cipherInfo;
						showCipherLoadConfirmDialog(cipherInfo.getTitle());
					}

					@Override
					public void onCipherDeleted(CipherShortInfo cipherInfo) {
						mTmpCipherShortInfo = cipherInfo;
						showCipherDeleteConfirmDialog(cipherInfo.getTitle());
					}

					@Override
					public void onCheckSolutionClicked() {
						checkSolution();
					}

					@Override
					public void onFeedbackGroupClicked() {
						Intent i = new Intent(MainActivityWithDrawer.this, FeedbackActivity.class);
						startActivity(i);
					}
				});
		mExpandableListView.setAdapter(mDrawerListAdapter);
		mExpandableListView.setOnGroupClickListener(mDrawerListAdapter);
		mExpandableListView.setOnChildClickListener(mDrawerListAdapter);

		checkPermissions();
	}

	/* Handle cipher being switched. */
	private void handleCipherChanging(CipherInfo cipher) {
		mBindingManager = CipherManager.getInstance(MainActivityWithDrawer.this).getCipherModel().getBindingManager();
		setWorkWithBindingManager();
		mCipherTitleTextView.setText(mCipherManager.getCurrentCipherInfo().getTitle());
		mDrawerListAdapter.updateData(mBindingManager.getCheckPoints(), mCipherManager.getOtherCipherInfos());
		mImageFragment.updateModelInUse();
	}

	/* Show dialog for confirming changing cipher. */
	private void showCipherLoadConfirmDialog(String cipherName) {
		ConfirmationDialogFragment
				.getInstance(getString(R.string.confirm_cipher_load, cipherName), getString(R.string.cipher_load),
						REQUEST_CONFIRM_CIPHER_LOAD, true)
				.show(getSupportFragmentManager(), DIALOG_CONFIRM_CIPHER_LOAD);
	}

	/* Show dialog for confirming cipher deleting. */
	private void showCipherDeleteConfirmDialog(String cipherName) {
		ConfirmationDialogFragment
				.getInstance(getString(R.string.confirm_cipher_delete, cipherName), getString(R.string.cipher_delete),
						REQUEST_CONFIRM_CIPHER_DELETE, true)
				.show(getSupportFragmentManager(), DIALOG_CONFIRM_CIPHER_DELETE);
	}

	/* Show dialog for checking current solution. */
	private void checkSolution() {
		if (mCipherManager.getCurrentCipherInfo().isZodiac340()) {
			// If current cipher is Zodiac-340 then there is no corrent solution
			// and this has to be told to user.
			MessageDialogFragment
					.getInstance(getString(R.string.solution_checking_zodiac340, getString(R.string.board)), false,
							true, false)
					.show(getSupportFragmentManager(), DIALOG_SOLUTION_CHECKING);
		} else if (UserService.getInstance(this).isSignedIn())
			new DialogSolutionChecking().show(getSupportFragmentManager(), DIALOG_SOLUTION_CHECKING);
		else {
			showMustLogInToCheckMessage();
		}
	}

	/*
	 * Show dialog that says that user has to be logged in to check the
	 * solution.
	 */
	private void showMustLogInToCheckMessage() {
		ConfirmationDialogFragment dialog = ConfirmationDialogFragment.getInstance(
				getString(R.string.solution_checking_must_log_in), getString(R.string.log_in), REQUEST_CONFIRM_LOG_IN,
				true);
		dialog.show(getSupportFragmentManager(), DIALOG_MUST_LOG_IN_TO_CHECK);
	}

	// Handle responses from confirmation dialogs.
	@Override
	public void onConfirmed(int requestCode, int resultCode) {
		switch (requestCode) {
		case REQUEST_CONFIRM_CIPHER_LOAD:
			if (resultCode == Activity.RESULT_OK) {
				CipherInfo cipher = mCipherManager.setCurrentCipher(mTmpCipherShortInfo);
				if (cipher != null) {
					handleCipherChanging(cipher);
				} else {
					showCipherLoadingError();
				}
			}
			break;
		case REQUEST_CONFIRM_CIPHER_DELETE:
			if (resultCode == Activity.RESULT_OK) {
				mCipherManager.deleteCipher(mTmpCipherShortInfo);
				mDrawerListAdapter.updateData(mCipherManager.getOtherCipherInfos());
			}
			break;
		case REQUEST_CONFIRM_LOG_IN:
			if (resultCode == Activity.RESULT_OK) {
				Intent i = new Intent(this, LogInSignUpActivity.class);
				startActivityForResult(i, REQUEST_LOG_IN);
			}
			break;
		case REQUEST_CONFIRM_PERMISSIONS:
			if (resultCode == Activity.RESULT_OK) {
				requestPermissions();
			}
			break;
		}
	}

	/* Show cipher loading error dialog. */
	private void showCipherLoadingError() {
		MessageDialogFragment.getInstance(getString(R.string.error_loading_cipher)).show(getSupportFragmentManager(),
				DIALOG_ERROR_LOADING_CIPHER);
	}

	/* Show dialog with the info about current cipher. */
	private void showCurrentCipherInfo(CipherInfo cipher) {
		DialogCipherInfo.getInstance(cipher).show(getSupportFragmentManager(), DIALOG_CIPHER_INFO);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	/* Invoke saving checkpoints when the activity is paused. */
	@Override
	public void onPause() {
		super.onPause();

		/*
		 * if (mSettings.isAutosaveEnabled() &&
		 * !mBindingManager.isCurrBindingCheckpoint()) { String checkpointName =
		 * getString(R.string.checkpointname_autosave,
		 * mBindingManager.getCheckpointCount());
		 * addCurrBindingToCheckpoints(checkpointName);
		 * mSettings.setLastCheckpointName(checkpointName); }
		 */

		if (mBindingManager != null)
			mBindingManager.saveData();
	}

	/* Update views when the activity is resumed. */
	@Override
	public void onResume() {
		super.onResume();
		if (mImageFragment != null) {
			mImageFragment.updateImage();
		}
		if (mDrawerListAdapter != null && mCipherManager != null)
			mDrawerListAdapter.updateData(mCipherManager.getOtherCipherInfos());
	}

	/* Stop listening to color settings changes to avoid memory leak. */
	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mSettings != null) {
			mSettings.removeOnFontColorProfileChangeListener(this);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_BOARD:
			if (resultCode == Activity.RESULT_OK)
				mDrawerListAdapter.notifyDataSetChanged();
			break;
		case REQUEST_LOG_IN:
			if (resultCode == Activity.RESULT_OK) {
				mUserFragment.update();
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	/* Checkpoint-making menu item image button. */
	private ImageButton mCheckpointMenuItemImageButton;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		mCheckpointMenuItem = menu.findItem(R.id.menu_item_checkpoint);

		mCheckpointMenuItemImageButton = new ImageButton(this);
		mCheckpointMenuItemImageButton.setBackgroundResource(0);
		mCheckpointMenuItemImageButton.setScaleType(ScaleType.CENTER);
		mCheckpointMenuItemImageButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				performCheckpointMenuItemActions(true);
				return true;
			}
		});
		mCheckpointMenuItemImageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				performCheckpointMenuItemActions(false);
			}
		});
		mCheckpointMenuItem.setActionView(mCheckpointMenuItemImageButton);

		createCheckpointMenuItemIcon(mSettings.getFontColorProfile().getSecondaryColor());
		setWorkWithBindingManager();
		mSettings.addOnFontColorProfileChangeListener(this);
		return true;
	}

	/* Configure work with the Binding manager. */
	private void setWorkWithBindingManager() {
		mBindingManager.setOnBindingStatusChangedListener(new BindingManager.OnBindingStatusChangedListener() {

			@Override
			public void onBindingStatusChanged(boolean isCheckpoint) {
				updateCheckpointMenuItemIcon(isCheckpoint);
			}
		});
		updateCheckpointMenuItemIcon(mBindingManager.isCurrBindingCheckpoint());
	}

	/* Handle color settings changes. */
	@Override
	public void onFontColorProfileChanged(FontColorProfile fontColorProfile) {
		createCheckpointMenuItemIcon(fontColorProfile.getSecondaryColor());
		updateCheckpointMenuItemIcon(mBindingManager.isCurrBindingCheckpoint());
		mImageFragment.updateColors();
	}

	/* Update checkpoint-making menu item icon. */
	private void updateCheckpointMenuItemIcon(boolean isSelected) {
		if (isSelected) {
			if (mCheckpointMenuItemSelectedIcon == null) {
				mCheckpointMenuItemImageButton.setImageResource(R.drawable.ic_menu_checkpoint_selected);
				// mCheckpointMenuItem.setIcon(R.drawable.ic_menu_checkpoint_selected);
			} else {
				mCheckpointMenuItemImageButton.setImageDrawable(mCheckpointMenuItemSelectedIcon);
				// mCheckpointMenuItem.setIcon(mCheckpointMenuItemSelectedIcon);
			}
		} else {
			mCheckpointMenuItemImageButton.setImageResource(R.drawable.ic_menu_checkpoint_add);
			// mCheckpointMenuItem.setIcon(R.drawable.ic_menu_checkpoint);
		}
	}

	/* Create checkpoint-making menu item icon. */
	private void createCheckpointMenuItemIcon(int color) {
		Bitmap baseBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_menu_checkpoint);
		Bitmap newBitmap = Bitmap.createBitmap(baseBitmap.getWidth(), baseBitmap.getHeight(), Bitmap.Config.ARGB_8888);
		for (int x = 0; x < baseBitmap.getWidth(); x++) {
			for (int y = 0; y < baseBitmap.getHeight(); y++) {
				if (Color.alpha(baseBitmap.getPixel(x, y)) != 0)
					newBitmap.setPixel(x, y, color);
			}
		}
		mCheckpointMenuItemSelectedIcon = new BitmapDrawable(this.getResources(), newBitmap);
	}

	/*
	 * Handle clicks on checkpoint-making menu item. toShowCheckpointNameDialog
	 * is true if a dialog must be shown to enter checkpoint name.
	 */
	private void performCheckpointMenuItemActions(boolean toShowCheckpointNameDialog) {
		if (toShowCheckpointNameDialog) {
			showCheckpointNameDialog();
			return;
		}
		String checkpointName;
		switch (mSettings.getCheckpointNameOption().getCode()) {
		case CheckpointNameOptions.SERIAL_NUMBER:
			checkpointName = getString(R.string.checkpointname_serialnumber, mBindingManager.getCheckpointCount());
			break;
		case CheckpointNameOptions.CREATION_TIME:
			Calendar calendar = Calendar.getInstance();
			checkpointName = getString(R.string.checkpointname_time, calendar.get(Calendar.HOUR_OF_DAY),
					calendar.get(Calendar.MINUTE));
			break;
		case CheckpointNameOptions.CUSTOM:
			showCheckpointNameDialog();
			return;
		default:
			checkpointName = "";
			break;
		}
		addCurrBindingToCheckpoints(checkpointName);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.menu_item_checkpoint:
			// performCheckpointMenuItemActions();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* Add checkpoint with the given name to the Binding manager. */
	private void addCurrBindingToCheckpoints(String checkpointName) {
		mBindingManager.addCurrToCheckPoints(checkpointName);
		mDrawerListAdapter.notifyDataSetChanged();
		mImageFragment.updateImage();
	}

	/*
	 * Show dialog prompting to enter checkpoint name. If current binding is
	 * already a checkpoint, then don't show dialog.
	 */
	private void showCheckpointNameDialog() {
		if (mBindingManager.isCurrBindingCheckpoint()) {
			return;
		}
		InputSingleLineDialogFragment
				.getInstance(getString(R.string.checkpointname_custom_dialog_title),
						getString(R.string.checkpointname_custom_dialog_create), REQUEST_INPUT_CHECKPOINT_NAME, true)
				.show(getSupportFragmentManager(), DIALOG_CHECKPOINT_NAME);
	}

	/*
	 * Show dialog prompting to enter saved image name.
	 */
	private void showSavedImageNameDialog() {
		InputSingleLineDialogFragment
				.getInstance(getString(R.string.saved_image_name_hint), getString(R.string.saved_image_name_save),
						REQUEST_INPUT_SAVED_IMAGE_FILENAME, true)
				.show(getSupportFragmentManager(), DIALOG_SAVED_IMAGE_NAME);
	}

	// Handle user being changed.
	@Override
	public void onUserChanged() {
		mDrawerListAdapter.notifyDataSetChanged();
	}

	/* Receive results from line input dialogs. */
	@Override
	public void onInput(int requestCode, int resultCode, String inputString) {
		switch (requestCode) {
		case REQUEST_INPUT_CHECKPOINT_NAME:
			if (resultCode == Activity.RESULT_OK) {
				if (inputString == null || inputString.equals(""))
					inputString = getString(R.string.checkpointname_custom_noname);
				addCurrBindingToCheckpoints(inputString);
				mBindingManager.saveData();
			}
			break;
		case REQUEST_INPUT_SAVED_IMAGE_FILENAME:
			if (resultCode == Activity.RESULT_OK) {
				if (inputString == null || inputString.equals("")) {

					char[] dateChars = DateFormat.getDateFormatOrder(this);
					boolean isTime24 = DateFormat.is24HourFormat(this);
					StringBuilder builder = new StringBuilder();
					builder.append(dateChars[0] + "-" + dateChars[1] + "-" + dateChars[2] + "_");
					builder.append(isTime24 ? "kk" : "hh");
					builder.append("-mm-ss");
					inputString = DateFormat.format(builder.toString(), Calendar.getInstance()).toString();
				}
				Bitmap bitmap = mImageFragment.getBitmapImage();
				boolean saveSuccess = mFileIO.writeExtBitmap(bitmap, inputString);
				if (!saveSuccess)
					MessageDialogFragment.getInstance(getString(R.string.error_saving_image))
							.show(getSupportFragmentManager(), DIALOG_ERROR_SAVING_IMAGE);
				if (bitmap != null) {
					bitmap.recycle();
					bitmap = null;
				}
			}
			break;
		}
	}

	/*
	 * Check granted permissions. This is necessary on Android 6.0 and higher
	 * due to introduction of Runtime Permissions.
	 */
	private void checkPermissions() {
		int hasWriteExtStoragePermission = ContextCompat.checkSelfPermission(MainActivityWithDrawer.this,
				Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (hasWriteExtStoragePermission != PackageManager.PERMISSION_GRANTED) {
			if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivityWithDrawer.this,
					Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				showPermissionRationale();
				return;
			}
			requestPermissions();
		}
	}

	/* Request permisssions. */
	private void requestPermissions() {
		ActivityCompat.requestPermissions(MainActivityWithDrawer.this,
				new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_ASK_PERMISSIONS);
	}

	/* Show explanation to why permissions need to be granted. */
	private void showPermissionRationale() {
		ConfirmationDialogFragment dialog = ConfirmationDialogFragment.getInstance(
				getString(R.string.permissions_rationale), getString(R.string.permissions_ok),
				REQUEST_CONFIRM_PERMISSIONS, true);
		dialog.show(getSupportFragmentManager(), DIALOG_CONFIRM_ASK_PERMISSIONS);
	}

	@TargetApi(23)
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
		case REQUEST_ASK_PERMISSIONS:
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				FileIO.rebuild(this);
			}
		default:
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
			return;
		}
	}
}
