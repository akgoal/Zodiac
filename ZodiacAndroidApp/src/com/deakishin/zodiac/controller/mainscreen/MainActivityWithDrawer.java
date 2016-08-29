package com.deakishin.zodiac.controller.mainscreen;

import java.util.Calendar;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.helpscreen.HelpActivity;
import com.deakishin.zodiac.controller.settingsscreen.SettingsActivity;
import com.deakishin.zodiac.model.Model;
import com.deakishin.zodiac.model.bindingmanager.BindingManager;
import com.deakishin.zodiac.model.bindingmanager.CheckPoint;
import com.deakishin.zodiac.model.framework.FileIO;
import com.deakishin.zodiac.model.settings.CheckpointNameOptions;
import com.deakishin.zodiac.model.settings.FontColorProfile;
import com.deakishin.zodiac.model.settings.SettingsPersistent;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

public class MainActivityWithDrawer extends AppCompatActivity
		implements DialogCheckpointName.OnCheckpointNamedListener, DialogSavedImageName.OnSavedImageNamedListener {
	/*
	 * Главная активность с боковым меню.
	 */

	/* Ключи для диалогов. */
	private static final String DIALOG_CHECKPOINT_NAME = "checkpointName";
	private static final String DIALOG_SAVED_IMAGE_NAME = "savedImageName";
	private static final String DIALOG_ERROR_SAVING_IMAGE = "errorSavingImage";

	/* Менеджер привязок букв к символам. */
	private BindingManager mBindingManager;

	/* Виджеты в боковом меню. */
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private ExpandableListView mExpandableListView;

	/* Адаптер для списка в боковом меню. */
	private DrawerListAdapter mDrawerListAdapter;

	/* Фрагмент с изображением. */
	private ImageFragment mImageFragment;

	/* Элементы меню. */
	private MenuItem mCheckpointMenuItem;

	/* Настройки приложения. */
	private SettingsPersistent mSettings;

	/* Иконка чекпоинта в выделенном состоянии. */
	private Drawable mCheckpointMenuItemSelectedIcon;

	/* Объект, управляющий записью/чтением файлов. */
	private FileIO mFileIO;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mBindingManager = Model.getInstance(this).getBindingManager();

		mSettings = SettingsPersistent.getInstance(this);

		mFileIO = new FileIO(this);

		setContentView(R.layout.activity_main);

		FragmentManager fm = getSupportFragmentManager();
		mImageFragment = (ImageFragment) fm.findFragmentById(R.id.fragmentContainer);
		if (mImageFragment == null) {
			mImageFragment = new ImageFragment();
			fm.beginTransaction().add(R.id.fragmentContainer, mImageFragment).commit();
		}

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mDrawerLayout.addDrawerListener(mDrawerToggle);

		mExpandableListView = (ExpandableListView) findViewById(R.id.drawer_explistView);
		mDrawerListAdapter = new DrawerListAdapter(this, mBindingManager.getCheckPoints(),
				new DrawerListAdapter.ClickCallback() {
					@Override
					public void onHelpGroupClicked() {
						Intent i = new Intent(MainActivityWithDrawer.this, HelpActivity.class);
						startActivity(i);
						mDrawerLayout.closeDrawers();
					}

					@Override
					public void onSettingsGroupClicked() {
						Intent i = new Intent(MainActivityWithDrawer.this, SettingsActivity.class);
						startActivity(i);
						mDrawerLayout.closeDrawers();
					}

					@Override
					public void onCheckpointClicked(CheckPoint checkpoint) {
						mBindingManager.setCurrBindingFromCheckPoint(checkpoint);
						mImageFragment.update();
						// mDrawerLayout.closeDrawers();
					}

					@Override
					public void onCheckpointDeleted(CheckPoint checkpoint) {
						mBindingManager.removeCheckPoint(checkpoint);
						mDrawerListAdapter.notifyDataSetChanged();
					}

					@Override
					public void onSaveImageGroupClicked() {
						mDrawerLayout.closeDrawers();
						showSavedImageNameDialog();
					}
				});
		mExpandableListView.setAdapter(mDrawerListAdapter);
		mExpandableListView.setOnGroupClickListener(mDrawerListAdapter);
		mExpandableListView.setOnChildClickListener(mDrawerListAdapter);
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

	/* Сохранение чекпоинтов во время приостановки активности. */
	@Override
	public void onPause() {
		super.onPause();
		mBindingManager.saveCheckpoints();
	}

	/* Обновление представления во время продолженния активности. */
	@Override
	public void onResume() {
		super.onResume();
		mImageFragment.updateImage();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		mCheckpointMenuItem = menu.findItem(R.id.menu_item_checkpoint);
		mBindingManager.setOnBindingStatusChangedListener(new BindingManager.OnBindingStatusChangedListener() {

			@Override
			public void onBindingStatusChanged(boolean isCheckpoint) {
				updateCheckpointMenuItemIcon(isCheckpoint);
			}
		});
		createCheckpointMenuItemIcon(mSettings.getFontColorProfile().getSecondaryColor());
		updateCheckpointMenuItemIcon(mBindingManager.isCurrBindingCheckpoint());
		mSettings.addOnFontColorProfileChangeListener(new SettingsPersistent.OnFontColorProfileChangeListener() {
			@Override
			public void onFontColorProfileChanged(FontColorProfile fontColorProfile) {
				createCheckpointMenuItemIcon(fontColorProfile.getSecondaryColor());
				updateCheckpointMenuItemIcon(mBindingManager.isCurrBindingCheckpoint());
			}
		});
		return true;
	}

	/* Обновление иконки чекпоинтов. */
	private void updateCheckpointMenuItemIcon(boolean isSelected) {
		if (isSelected) {
			if (mCheckpointMenuItemSelectedIcon == null)
				mCheckpointMenuItem.setIcon(R.drawable.ic_menu_checkpoint_selected);
			else
				mCheckpointMenuItem.setIcon(mCheckpointMenuItemSelectedIcon);
		} else {
			mCheckpointMenuItem.setIcon(R.drawable.ic_menu_checkpoint);
		}
	}

	/* Создание иконки чекпоинтов. */
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.menu_item_checkpoint:
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
				if (!mBindingManager.isCurrBindingCheckpoint())
					showCheckpointNameDialog();
				return true;
			default:
				checkpointName = "";
				break;
			}
			addCurrBindingToCheckpoints(checkpointName);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* Добавление чекпоинта и именем name. */
	private void addCurrBindingToCheckpoints(String checkpointName) {
		mBindingManager.addCurrToCheckPoints(checkpointName);
		mDrawerListAdapter.notifyDataSetChanged();
		mImageFragment.updateImage();
	}

	/*
	 * Показать диалог ввода имени чекпоинта. Диалог оборачивается в фрагмент
	 * для обработтки поворотов.
	 */
	private void showCheckpointNameDialog() {
		DialogCheckpointName dialog = new DialogCheckpointName();
		dialog.show(getSupportFragmentManager(), DIALOG_CHECKPOINT_NAME);
	}

	/* Реакция на получение результата от диалога ввода имени чекпоинта. */
	@Override
	public void onCheckpointNamed(String name) {
		addCurrBindingToCheckpoints(name);
	}

	/*
	 * Показать диалог ввода имени сохраняемого изображения. Диалог
	 * оборачивается в фрагмент для обработтки поворотов.
	 */
	private void showSavedImageNameDialog() {
		DialogSavedImageName dialog = new DialogSavedImageName();
		dialog.show(getSupportFragmentManager(), DIALOG_SAVED_IMAGE_NAME);
	}

	/*
	 * Реакция на получение результата от диалога ввода имени сохраняемого
	 * изображения.
	 */
	@Override
	public void onSavedImageNamed(String name) {
		Bitmap bitmap = mImageFragment.getBitmapImage();
		boolean saveSuccess = mFileIO.writeExtBitmap(bitmap, name);
		if (!saveSuccess)
			new DialogErrorSavingImage().show(getSupportFragmentManager(), DIALOG_ERROR_SAVING_IMAGE);
	}
}
