package com.deakishin.zodiac.services.userservice;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class representing a user. Has and id, a name and a markup of user's avatar.
 */
public class User implements Parcelable {

	/* User's id. */
	private Long mId = -1L;
	/* User's name. */
	private String mName;
	/* User's avatar markup. */
	private String mAvatarMarkup;

	public User() {
	}

	public User(Long id, String name) {
		this(id, name, null);
	}

	public User(Long id, String name, String avatarMarkup) {
		mId = id;
		mName = name;
		mAvatarMarkup = avatarMarkup;
	}

	public Long getId() {
		return mId;
	}

	public void setId(Long id) {
		mId = id;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getAvatarMarkup() {
		return mAvatarMarkup;
	}

	public void setAvatarMarkup(String avatarMarkup) {
		mAvatarMarkup = avatarMarkup;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeLong(mId);
		out.writeString(mName);
		out.writeString(mAvatarMarkup == null ? "" : mAvatarMarkup);
	}

	/** Creator to construct object from a parcel. */
	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {

		@Override
		public User createFromParcel(Parcel source) {
			return new User(source);
		}

		@Override
		public User[] newArray(int size) {
			return new User[size];
		}
	};

	private User(Parcel in) {
		mId = in.readLong();
		mName = in.readString();
		mAvatarMarkup = in.readString();
		if (mAvatarMarkup.equals("")) {
			mAvatarMarkup = null;
		}
	}
}
