package com.deakishin.zodiac.controller.helpscreen;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.model.help.HelpInfoLab;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HelpActivity extends AppCompatActivity {
	/*
	 * Активность, выводящая справку.
	 */

	/* Виджет, позволяющий листать элементы справки. */
	private ViewPager mViewPager;

	/* Виджеты. */
	private LinearLayout mHelpPageLayout;
	private TextView mPageCounterTextView;

	/* Номер текущей страницы и общее число страниц справки. */
	private int mCurrPage = 1, mPagesSize;
	
	private Toast mToast;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_help);

		if (NavUtils.getParentActivityName(this) != null)
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mHelpPageLayout = (LinearLayout) findViewById(R.id.help_page_layout);

		mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.viewPager);
		mHelpPageLayout.addView(mViewPager);

		mPagesSize = HelpInfoLab.getInstance(HelpActivity.this).getSize();

		mPageCounterTextView = (TextView) findViewById(R.id.help_counter_textView);
		updateCounterText();

		mToast = Toast.makeText(this, "Oh Hi Mark!", Toast.LENGTH_SHORT);
		FragmentManager fm = getSupportFragmentManager();
		mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
			@Override
			public Fragment getItem(int arg0) {
				return HelpFragment.newInstance(arg0);
			}

			@Override
			public int getCount() {
				return mPagesSize;
			}
		});
		mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int page) {
				mCurrPage = page + 1;
				updateCounterText();
			}

			private static final int MAX_DIRECTION_CHANGES = 9;
			private int mChangeDirectionCount = 0;
			private boolean mOffsetUp = true;
			private float mPrevOffset = 0;
			
			@Override
			public void onPageScrolled(int arg0, float positionOffset, int arg2) {
				if (mOffsetUp){
					if (positionOffset < mPrevOffset) {
						mChangeDirectionCount++;
						mOffsetUp = false;
					}
				} else {
					if (positionOffset > mPrevOffset) {
						mChangeDirectionCount++;
						mOffsetUp = true;
					}
				}
				mPrevOffset = positionOffset;
				if (mChangeDirectionCount > MAX_DIRECTION_CHANGES){
					mToast.show();
					mChangeDirectionCount = 0;
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {
				if (state == ViewPager.SCROLL_STATE_IDLE){
					mChangeDirectionCount=0;
					mOffsetUp = true;
					mPrevOffset = 0;
				}
			}
		});
	}
	
	@Override
	public void onPause(){
		super.onPause();
		if (mToast!=null)
			mToast.cancel();
	}

	/* Обновление текста счетчика страниц. */
	private void updateCounterText() {
		mPageCounterTextView.setText(getString(R.string.help_page_counter, mCurrPage, mPagesSize));
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
