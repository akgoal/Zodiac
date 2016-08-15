package com.example.zodiac.controller.helpactivity;

import com.example.zodiac.R;
import com.example.zodiac.model.help.HelpInfoLab;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HelpActivity extends FragmentActivity {
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_help);

		mHelpPageLayout = (LinearLayout) findViewById(R.id.help_page_layout);

		mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.viewPager);
		mHelpPageLayout.addView(mViewPager);

		mPagesSize = HelpInfoLab.getInstance(HelpActivity.this).getSize();

		mPageCounterTextView = (TextView) findViewById(R.id.help_counter_textView);
		updateCounterText();

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

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	/* Обновление текста счетчика страниц. */
	private void updateCounterText() {
		mPageCounterTextView.setText(getString(R.string.help_page_counter, mCurrPage, mPagesSize));
	}
}
