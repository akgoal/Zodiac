package com.deakishin.zodiac.controller;

import com.deakishin.zodiac.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

/**
 * Abstract class for an activity that hosts single fragment. It also enables
 * home button to navigate up the activity hierarchy (if there is a parent activity).
 */
public abstract class SingleFragmentActivity extends AppCompatActivity {

	/**
	 * Returns a fragment that the activity has to host.
	 * 
	 * @return fragment to host.
	 */
	protected abstract Fragment createFragment();

	/**
	 * Fragment that's being hosted.
	 */
	protected Fragment mFragment;

	/**
	 * @return Resource id of the activity layout.
	 */
	protected int getLayoutResId() {
		return R.layout.activity_fragment;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResId());

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FragmentManager fm = getSupportFragmentManager();
		mFragment = fm.findFragmentById(R.id.fragmentContainer);
		if (mFragment == null) {
			mFragment = createFragment();
			fm.beginTransaction().add(R.id.fragmentContainer, mFragment).commit();
		}

		if (NavUtils.getParentActivityName(this) != null)
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
