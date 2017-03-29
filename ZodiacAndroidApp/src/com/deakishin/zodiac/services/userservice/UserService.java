package com.deakishin.zodiac.services.userservice;

import com.deakishin.zodiac.model.settings.SettingsPersistent;

import android.content.Context;

/** Singleton implementation of the user service. */
public class UserService implements UserServiceI {

	/* Logged-in user. */
	private User mUser;

	/* Application context. */
	private Context mContext;

	/* Application settings to save and load user. */
	private SettingsPersistent mSettings;

	private static UserService sUserService;

	/**
	 * Provides access to the user service implementation.
	 * 
	 * @param context
	 *            Application context.
	 * @return The sole instance of the singleton.
	 */
	public static UserService getInstance(Context context) {
		if (sUserService == null)
			sUserService = new UserService(context.getApplicationContext());
		return sUserService;
	}

	private UserService(Context context) {
		mContext = context;
		mSettings = SettingsPersistent.getInstance(mContext);

		User user = mSettings.getUser();
		if (user != null)
			mUser = user;
	}

	@Override
	public boolean isSignedIn() {
		return mUser != null;
	}

	@Override
	public void signIn(User user) {
		mUser = user;
		mSettings.setUser(mUser);
	}

	@Override
	public void signOut() {
		mUser = null;
		mSettings.setUser(null);
	}

	@Override
	public User getUser() {
		return mUser;
	}
}
