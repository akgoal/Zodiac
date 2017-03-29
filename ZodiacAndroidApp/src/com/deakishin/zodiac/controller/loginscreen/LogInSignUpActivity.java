package com.deakishin.zodiac.controller.loginscreen;

import com.deakishin.zodiac.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Activity for logging in and signing up. Consists of and manages two tabs, one
 * hosting {@link LogInFragment}, the other hosting {@link SignUpFragment}.
 */
public class LogInSignUpActivity extends AppCompatActivity
		implements LogInFragment.OnLogInListener, SignUpFragment.OnSignUpListener {

	/* Keys for storing state. */
	private static final String INDEX_POSITION = "position";
	private static final String INDEX_TABS_ENABLED = "tabsEnabled";

	/* Widgets. */
	private LinearLayout mPageLayout;
	private CustomViewPager mViewPager;
	private TextView mTabLogInTextView;
	private View mTabIndicatorLogInView;
	private TextView mTabSignUpTextView;
	private View mTabIndicatorSignUpView;

	/* Current tab position. */
	private int mCurrentTabPosition = 0;
	/* Flag indicating that navigation between tabs is enabled. */
	private boolean mTabsEnabled = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(INDEX_POSITION))
				mCurrentTabPosition = savedInstanceState.getInt(INDEX_POSITION);
			if (savedInstanceState.containsKey(INDEX_TABS_ENABLED))
				mTabsEnabled = savedInstanceState.getBoolean(INDEX_TABS_ENABLED);
		}
		setContentView(R.layout.log_in_sign_up_activity);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		mPageLayout = (LinearLayout) findViewById(R.id.login_signup_page_layout);

		mViewPager = new CustomViewPager(this);
		mViewPager.setId(R.id.login_signup_viewPager);
		mPageLayout.addView(mViewPager);

		FragmentManager fm = getSupportFragmentManager();
		mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
			@Override
			public Fragment getItem(int position) {
				switch (position) {
				case 0:
					return new LogInFragment();
				case 1:
					return new SignUpFragment();
				default:
					return null;
				}
			}

			@Override
			public int getCount() {
				return 2;
			}
		});
		mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				mCurrentTabPosition = position;
				updateTabsHeaders();
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

		mTabLogInTextView = (TextView) findViewById(R.id.login_signup_tab_login_textView);
		mTabIndicatorLogInView = findViewById(R.id.login_signup_tab_indicator_login);
		mTabLogInTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mTabsEnabled)
					mViewPager.setCurrentItem(0);
			}
		});

		mTabSignUpTextView = (TextView) findViewById(R.id.login_signup_tab_signup_textView);
		mTabIndicatorSignUpView = findViewById(R.id.login_signup_tab_indicator_signup);
		mTabSignUpTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mTabsEnabled)
					mViewPager.setCurrentItem(1);
			}
		});

		updateTabsHeaders();
		updateTabsEnability(mTabsEnabled);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setResult(Activity.RESULT_CANCELED);
	}

	/* Update tabs' headers. */
	private void updateTabsHeaders() {
		boolean logInTabSelected = (mCurrentTabPosition == 0);
		updateTabHeader(mTabLogInTextView, mTabIndicatorLogInView, logInTabSelected, false);
		updateTabHeader(mTabSignUpTextView, mTabIndicatorSignUpView, !logInTabSelected, false);
	}

	/* Update looks of widgets regarding a single tab and its state. */
	private void updateTabHeader(TextView labelTextView, View indicatorView, boolean selected, boolean disabled) {
		int textColorResId = 0;
		if (selected) {
			textColorResId = R.color.log_in_tab_text;
			indicatorView.setVisibility(View.VISIBLE);
		} else {
			indicatorView.setVisibility(View.INVISIBLE);
			if (disabled)
				textColorResId = R.color.log_in_tab_text_disabled;
			else
				textColorResId = R.color.log_in_tab_text_unfocused;
		}
		labelTextView.setTextColor(ContextCompat.getColor(this, textColorResId));
	}

	/* Update tabs enability. */
	private void updateTabsEnability(boolean enabled) {
		mTabsEnabled = enabled;
		if (mViewPager != null)
			mViewPager.setPagingEnabled(mTabsEnabled);

		boolean logInTabSelected = (mCurrentTabPosition == 0);
		if (logInTabSelected)
			updateTabHeader(mTabSignUpTextView, mTabIndicatorSignUpView, false, !mTabsEnabled);
		else
			updateTabHeader(mTabLogInTextView, mTabIndicatorLogInView, false, !mTabsEnabled);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putInt(INDEX_POSITION, mCurrentTabPosition);
		savedInstanceState.putBoolean(INDEX_TABS_ENABLED, mTabsEnabled);
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

	/* Settings result to parent activity and finishing. */
	private void setResultAndFinish() {
		setResult(Activity.RESULT_OK);
		finish();
	}

	// Signing-up is in process.
	@Override
	public void onSigningUpStarted() {
		updateTabsEnability(false);
	}

	// Logging-in is in process.
	@Override
	public void onLoggingInStarted() {
		updateTabsEnability(false);
	}

	// Signing up finished.
	@Override
	public void onSigningUpFinished(boolean signedUp) {
		if (signedUp) {
			setResultAndFinish();
		} else {
			updateTabsEnability(true);
		}
	}

	// Logging in finished.
	@Override
	public void onLoggingInFinished(boolean loggedIn) {
		if (loggedIn) {
			setResultAndFinish();
		} else {
			updateTabsEnability(true);
		}
	}

	/**
	 * Custom {@link ViewPager} that provides the option to disable paging.
	 */
	public class CustomViewPager extends ViewPager {

		private boolean mPagingEnabled = true;

		public CustomViewPager(Context context) {
			super(context);
		}

		public CustomViewPager(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			if (mPagingEnabled) {
				return super.onTouchEvent(event);
			}

			return false;
		}

		@Override
		public boolean onInterceptTouchEvent(MotionEvent event) {
			if (mPagingEnabled) {
				return super.onInterceptTouchEvent(event);
			}

			return false;
		}

		/**
		 * Sets paging enability.
		 * 
		 * @param enabled
		 *            True if paging has to be enabled, false otherwise.
		 */
		public void setPagingEnabled(boolean enabled) {
			mPagingEnabled = enabled;
		}
	}
}
