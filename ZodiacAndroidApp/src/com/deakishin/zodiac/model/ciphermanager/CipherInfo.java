package com.deakishin.zodiac.model.ciphermanager;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;

/**
 * Information about a cipher. This info contains the cipher's id, title,
 * description, author, difficulty, markup (and its String representation), name
 * of the folder in which files for the cipher are saved, and an original image
 * that was used to construct the cipher.
 */
public class CipherInfo {

	/* Keys for storing in a JSON object. */
	private static final String JSON_ID = "id";
	private static final String JSON_DESCRIPTION = "description";
	private static final String JSON_AUTHOR = "author";
	private static final String JSON_DIFFICULTY = "difficulty";

	/* Id. */
	private long mId = -1;

	/* Title. */
	private String mTitle;

	/* Description. */
	private String mDescription;

	/* Author's name. */
	private String mAuthor;

	/* Cipher's difficulty. */
	private Float mDifficulty;

	/* Cipher's markup. */
	private Markup mMarkup;

	/* Markup in a String form. */
	private String mMarkupAsString;

	/* Name of the folder that contains info about the cipher. */
	private String mFolderName;

	/* Original image that was used to construct the cipher. */
	private Bitmap mOriginalImage;

	public CipherInfo() {

	}

	/**
	 * Sets short info.
	 * 
	 * @param cipherShortInfo
	 *            Cipher's short info.
	 */
	public void setShortInfo(CipherShortInfo cipherShortInfo) {
		mId = cipherShortInfo.getId();
		mTitle = cipherShortInfo.getTitle();
		mFolderName = cipherShortInfo.getFolderName();
	}

	/** Releases resources held by the object. */
	public void recycle() {
		if (mMarkup != null) {
			mMarkup.recycle();
			mMarkup = null;
		}
	}

	/**
	 * Reads and sets info from a JSON object.
	 * 
	 * @param json
	 *            JSON object to read info from.
	 * @return True if read and set successfully, false otherwise.
	 */
	public boolean readInfoFromJson(JSONObject json) {
		try {
			long id = json.getLong(JSON_ID);
			if (id != mId) {
				return false;
			}
			mId = id;
			if (json.has(JSON_DESCRIPTION))
				mDescription = json.getString(JSON_DESCRIPTION);
			if (json.has(JSON_AUTHOR))
				mAuthor = json.getString(JSON_AUTHOR);
			if (json.has(JSON_DIFFICULTY))
				mDifficulty = (float) json.getDouble(JSON_DIFFICULTY);
			return true;
		} catch (JSONException e) {
			return false;
		}
	}

	/**
	 * @return JSON Object containing all the info. This object can then be used
	 *         to restore the cipher.
	 * @throws JSONException
	 *             If the object cannot be stored in a JSON object.
	 */
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put(JSON_ID, mId);
		if (mDescription != null)
			json.put(JSON_DESCRIPTION, mDescription);
		if (mAuthor != null)
			json.put(JSON_AUTHOR, mAuthor);
		if (mDifficulty != null)
			json.put(JSON_DIFFICULTY, mDifficulty);
		return json;
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

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		mDescription = description;
	}

	public String getAuthor() {
		return mAuthor;
	}

	public void setAuthor(String author) {
		mAuthor = author;
	}

	public Markup getMarkup() {
		return mMarkup;
	}

	public void setMarkup(Markup markup) {
		mMarkup = markup;
	}

	public String getFolderName() {
		return mFolderName;
	}

	public void setFolderName(String folderName) {
		mFolderName = folderName;
	}

	public Bitmap getOriginalImage() {
		return mOriginalImage;
	}

	public void setOriginalImage(Bitmap originalImage) {
		mOriginalImage = originalImage;
	}

	/**
	 * @return True if the cipher is default Zodiac-340 cipher, false otherwise.
	 */
	public boolean isZodiac340() {
		return (mId == CipherShortInfo.ZODIAC340_ID);
	}

	public String getMarkupAsString() {
		return mMarkupAsString;
	}

	public void setMarkupAsString(String markupAsString) {
		mMarkupAsString = markupAsString;
	}

	public Float getDifficulty() {
		return mDifficulty;
	}

	public void setDifficulty(Float difficulty) {
		mDifficulty = difficulty;
	}
}
