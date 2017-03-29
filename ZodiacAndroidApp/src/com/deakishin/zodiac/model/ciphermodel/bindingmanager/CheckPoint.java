package com.deakishin.zodiac.model.ciphermodel.bindingmanager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Checkpoint is a binding (i.e. mapping between symbols' ids and letters) with
 * a title and an id.
 */
public class CheckPoint {

	/* Keys for storing in a JSON object. */
	private static final String JSON_ID = "id";
	private static final String JSON_TITLE = "title";

	/* Binding. */
	private Map<Integer, Character> mBinding;
	/* Title. */
	private String mTitle;
	/* Id. */
	private UUID mId;

	/**
	 * Constructs checkpoint.
	 * 
	 * @param binding
	 *            Binding, i.e. mapping between symbols' ids and letters.
	 * @param title
	 *            Title for the checkpoint.
	 */
	public CheckPoint(Map<Integer, Character> binding, String title) {
		mBinding = new HashMap<Integer, Character>();
		for (Integer i : binding.keySet())
			mBinding.put(i, binding.get(i));
		mTitle = title;
		mId = UUID.randomUUID();
	}

	/**
	 * Constructs checkpoint from a JSON object.
	 * 
	 * @param json
	 *            JSON object with checkpoint data in it.
	 * @throws JSONException
	 *             If the JSON object has incorrect format.
	 */
	public CheckPoint(JSONObject json) throws JSONException {
		mBinding = new HashMap<Integer, Character>();
		Iterator<String> keys = json.keys();
		String key;
		while (keys.hasNext()) {
			key = keys.next();
			if (key.equals(JSON_ID)) {
				mId = UUID.fromString(json.getString(JSON_ID));
			} else if (key.equals(JSON_TITLE)) {
				mTitle = json.getString(JSON_TITLE);
			} else
				mBinding.put(Integer.parseInt(key), json.getString(key).charAt(0));
		}
	}

	/**
	 * @return JSON object that contains checkpoint's data and that can then be
	 *         used to restore the checkpoint.
	 * 
	 * @throws JSONExeption
	 *             If unable to construct the JSON object.
	 */
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put(JSON_ID, mId.toString());
		json.put(JSON_TITLE, mTitle);
		for (Integer i : mBinding.keySet()) {
			json.put(i.toString(), mBinding.get(i));
		}
		return json;
	}

	public Map<Integer, Character> getBinding() {
		return mBinding;
	}

	public String getTitle() {
		return mTitle;
	}

	public UUID getId() {
		return mId;
	}

}