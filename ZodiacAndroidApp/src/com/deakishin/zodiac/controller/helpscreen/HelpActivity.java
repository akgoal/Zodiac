package com.deakishin.zodiac.controller.helpscreen;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.SingleFragmentActivity;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

/** Activity for displaying Help information. Hosts {@link HelpFragment}. */
public class HelpActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new HelpFragment();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.activity_help, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_help_about:
			Intent i = new Intent(this, AboutActivity.class);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
