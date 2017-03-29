package com.deakishin.zodiac.services.userservice;

/** Interface for the service to manage logged-in user's info. */
public interface UserServiceI {

	/** @return True if the user is logged in. */
	public boolean isSignedIn();

	/**
	 * Logs in a user.
	 * 
	 * @param user
	 *            User to log in.
	 */
	public void signIn(User user);

	/** Logs out current user. */
	public void signOut();

	/** @return Logged-in user. */
	public User getUser();
}
