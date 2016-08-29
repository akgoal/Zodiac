package com.deakishin.zodiac.controller.settingsscreen;

import com.deakishin.zodiac.controller.SingleFragmentActivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class SettingsActivity extends SingleFragmentActivity{

	@Override
	protected Fragment createFragment() {
		return new SettingsFragment();
	}
	
	@Override 
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		if (NavUtils.getParentActivityName(this) != null)
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (NavUtils.getParentActivityName(this) != null) {
				NavUtils.navigateUpFromSameTask(this);
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
