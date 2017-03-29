package com.deakishin.zodiac.model.ciphermanager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Short information about a cipher. Contains cipher's id, title and name of the
 * folder in which files for the cipher are saved. It also contains info about
 * whether the cipher can be deleted or not.
 */
public class CipherShortInfo {

	/* Keys for storing in a JSON object. */
	private static final String JSON_ID = "id";
	private static final String JSON_TITLE = "title";
	private static final String JSON_FOLDERNAME = "folderName";

	/* Id. */
	private long mId = -1;

	/* Id reserved for the default Zodiac-340 cipher. */
	public static final int ZODIAC340_ID = 0;

	/* Title. */
	private String mTitle;

	/* Name of the folder that contains info about the cipher. */
	private String mFolderName;

	/* Indicator that cipher can be deleted. */
	private boolean mDeletable = true;

	public CipherShortInfo() {

	}

	/**
	 * Constructs object from JSON object that contains all the info.
	 * 
	 * @param json
	 *            JSON Object containing all the info.
	 * @throws JSONException
	 *             If The given JSON object has incorrect format.
	 */
	public CipherShortInfo(JSONObject json) throws JSONException {
		mId = json.getLong(JSON_ID);
		mTitle = json.getString(JSON_TITLE);
		mFolderName = json.getString(JSON_FOLDERNAME);
	}

	/**
	 * @return JSON Object containing all the info. This object can then be used
	 *         to restore the the short info of the cipher.
	 * @throws JSONException
	 *             If the object cannot be stored in a JSON object.
	 */
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put(JSON_ID, mId);
		json.put(JSON_TITLE, mTitle);
		json.put(JSON_FOLDERNAME, mFolderName);
		return json;
	}

	/**
	 * Constructs short info of a cipher.
	 * 
	 * @param id
	 *            Cipher's id.
	 * @param title
	 *            Cipher's title.
	 * @param folderName
	 *            Name of the folder in which files for the cipher are stored.
	 * @param canBeDeleted
	 *            True if the cipher can be deleted, false otherwise.
	 */
	public CipherShortInfo(long id, String title, String folderName, boolean canBeDeleted) {
		this();
		mId = id;
		mTitle = title;
		mFolderName = folderName;
		mDeletable = canBeDeleted;
	}

	/**
	 * @return True if the cipher is default Zodiac-340 cipher, false otherwise.
	 */
	public boolean isZodiac340() {
		return mId == ZODIAC340_ID;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public String getFolderName() {
		return mFolderName;
	}

	public void setFolderName(String folderName) {
		mFolderName = folderName;
	}

	public boolean isDeletable() {
		return mDeletable;
	}

	public void setDeletable(boolean deletable) {
		mDeletable = deletable;
	}
}
