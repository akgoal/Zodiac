package com.deakishin.zodiac.model.avatargenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * Profile of a specific avatar. Contains information about its parts and what
 * options are chosen for each part. Info about each part is contained in a
 * {@link PartProfile} object.
 */
public class AvatarProfile {

	/* Mapping between parts profiles and their ids. */
	private Map<Integer, PartProfile> mPartProfiles = new HashMap<Integer, PartProfile>();

	/* Parameters for markup - String representation of the profile. */
	private static final String PARTS_DELIMITER = ";";
	private static final String PART_PARAMS_DELIMITER = "-";

	/*
	 * Signature that is added in the beginning of the markup. Allows to
	 * differentiate versions of markups.
	 */
	private static final String SIGNATURE = "V1!";

	/* Markup used to create the profile was empty. */
	private boolean mEmptyMarkup = false;

	public AvatarProfile() {
	}

	/**
	 * Constructs profile by its markup.
	 * 
	 * @param markup
	 *            Markup - String representation of the profile.
	 */
	public AvatarProfile(String markup) {
		this();

		mEmptyMarkup = markup == null || markup.equals("") || !markup.startsWith(SIGNATURE);

		if (!mEmptyMarkup)
			setFromMarkup(markup.substring(SIGNATURE.length()));
	}

	/** @return True if the profile is empty. */
	public boolean isEmpty() {
		return mEmptyMarkup || mPartProfiles == null || mPartProfiles.size() == 0;
	}

	public Map<Integer, PartProfile> getPartProfiles() {
		return mPartProfiles;
	}

	public void setPartProfiles(Map<Integer, PartProfile> partProfiles) {
		mPartProfiles = partProfiles;
	}

	/**
	 * Sets specific part profile.
	 * 
	 * @param partId
	 *            Id of the part.
	 * @param partProfile
	 *            Profile of the part.
	 */
	public void setPartProfile(int partId, PartProfile partProfile) {
		mPartProfiles.put(partId, partProfile);
	}

	/**
	 * Returns part profile by the part's id.
	 * 
	 * @param partId
	 *            Id of the part.
	 * @return Profile of the part or null if the part is not found.
	 */
	public PartProfile getOptionForPart(int partId) {
		if (mPartProfiles == null)
			return null;

		return mPartProfiles.get(partId);
	}

	/** @return Profile's markup - the String representation of the profile. */
	public String toMarkup() {
		StringBuilder sb = new StringBuilder();

		sb.append(SIGNATURE);

		for (Integer partId : mPartProfiles.keySet()) {
			PartProfile partProfile = mPartProfiles.get(partId);
			sb.append(partId + PART_PARAMS_DELIMITER + partProfile.toStringMarkup() + PARTS_DELIMITER);
		}
		return sb.toString();
	}

	/**
	 * Configures the profile by its markup.
	 * 
	 * @param markup
	 *            Profile's markup.
	 */
	public void setFromMarkup(String markup) {
		mPartProfiles = new HashMap<Integer, PartProfile>();

		if (markup == null) {
			return;
		}

		String[] parts = markup.split(PARTS_DELIMITER);
		for (String part : parts) {
			String[] params = part.split(PART_PARAMS_DELIMITER);
			if (params.length < 2)
				continue;

			int partId = Integer.parseInt(params[0]);
			PartProfile partProfile = new PartProfile(params[1]);
			mPartProfiles.put(partId, partProfile);
		}
	}

	/**
	 * Information about a specific avatar's part: what option is chosen for it
	 * and offset and scale parameters.
	 */
	public static class PartProfile {
		/* Option id. */
		private int mOptionId;

		/* Scale coefs of the part. From -1 to 1. */
		private float mScaleX, mScaleY;

		/* Offsets of the part. From -1 to 1. */
		private float mOffsetX, mOffsetY;

		/*
		 * Delimiter between parameters in the String representation of the part
		 * profile.
		 */
		private static final String DELIMITER = ",";
		/*
		 * Предел числа для конвертации float полей в int. float от -1 до 1
		 * переводится в int от 0 до 100.
		 */
		/*
		 * Limit number for converting float number in [-1,1] to int number in
		 * [0,MAX_INT_FOR_CONVERT].
		 */
		private static final int MAX_INT_FOR_CONVERT = 100;

		public PartProfile() {
		}

		/**
		 * Constructs part profile.
		 * 
		 * @param optionId
		 *            Id of the option for the part.
		 * @param offsetX
		 *            Offset coef on the X-axis in [-1,1].
		 * @param offsetY
		 *            Offset coef on the Y-axis in [-1,1].
		 * @param scaleX
		 *            Scale coef on the X-axis in [-1,1].
		 * @param scaleY
		 *            Scale coef on the Y-axis in [-1,1].
		 */
		public PartProfile(int optionId, float offsetX, float offsetY, float scaleX, float scaleY) {
			this();
			mOptionId = optionId;
			mScaleX = scaleX;
			mScaleY = scaleY;
			mOffsetX = offsetX;
			mOffsetY = offsetY;
		}

		/**
		 * Constructs part profile by its markup
		 * 
		 * @param partMarkup
		 *            Markup (String representation) of a part profile.
		 */
		public PartProfile(String partMarkup) {
			this();
			try {
				setFromString(partMarkup);
			} catch (Exception e) {
				mOptionId = 0;
				mScaleX = 0;
				mScaleY = 0;
				mOffsetX = 0;
				mOffsetY = 0;
			}
		}

		/** @return String representation of the part profile. */
		public String toStringMarkup() {
			StringBuilder sb = new StringBuilder();
			sb.append(mOptionId + DELIMITER);
			sb.append(floatToInt(mOffsetX) + DELIMITER);
			sb.append(floatToInt(mOffsetY) + DELIMITER);
			sb.append(floatToInt(mScaleX) + DELIMITER);
			sb.append(floatToInt(mScaleY) + DELIMITER);
			String res = sb.toString();
			return res;
		}

		/**
		 * Sets the part profile from its markup
		 * 
		 * @param markup
		 *            Markup of the part profile.
		 * @throws Exception
		 *             If passed markup is not valid.
		 */
		private void setFromString(String markup) throws Exception {
			String[] params = markup.split(DELIMITER);
			if (params.length < 5)
				return;
			mOptionId = Integer.parseInt(params[0]);
			mOffsetX = intToFloat(Integer.parseInt(params[1]));
			mOffsetY = intToFloat(Integer.parseInt(params[2]));
			mScaleX = intToFloat(Integer.parseInt(params[3]));
			mScaleY = intToFloat(Integer.parseInt(params[4]));
		}

		/* Convert float to int. */
		private int floatToInt(float floatNumber) {
			return (int) ((floatNumber + 1) * MAX_INT_FOR_CONVERT / 2);
		}

		/* Convert int to float. */
		private float intToFloat(int intNumber) {
			return 2 * intNumber / (float) MAX_INT_FOR_CONVERT - 1;
		}

		public int getOptionId() {
			return mOptionId;
		}

		public void setOptionId(int optionId) {
			mOptionId = optionId;
		}

		public float getScaleX() {
			return mScaleX;
		}

		public void setScaleX(float scaleX) {
			mScaleX = scaleX;
		}

		public float getScaleY() {
			return mScaleY;
		}

		public void setScaleY(float scaleY) {
			mScaleY = scaleY;
		}

		public float getOffsetX() {
			return mOffsetX;
		}

		public void setOffsetX(float offsetX) {
			mOffsetX = offsetX;
		}

		public float getOffsetY() {
			return mOffsetY;
		}

		public void setOffsetY(float offsetY) {
			mOffsetY = offsetY;
		}
	}
}
