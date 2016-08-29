package com.deakishin.zodiac.model.bindingmanager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

public class CheckPoint {
	/*
	 * Чекпоинт - привязка с названием и идентификатором.
	 */

	/* Ключи для хранения в объекте JSON. */
	private static final String JSON_ID = "id";
	private static final String JSON_TITLE = "title";

	private Map<Integer, Character> mBinding;
	private String mTitle;
	private UUID mId;

	public CheckPoint(Map<Integer, Character> binding, String title) {
		mBinding = new HashMap<Integer, Character>();
		for (Integer i : binding.keySet())
			mBinding.put(i, binding.get(i));
		mTitle = title;
		mId = UUID.randomUUID();
	}

	/* Конвертация из объекта JSON. */
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

	/* Конвертация в объект JSON. */
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