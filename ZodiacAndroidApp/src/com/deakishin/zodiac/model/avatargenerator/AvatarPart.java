package com.deakishin.zodiac.model.avatargenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;

/**
 * Specific avatar part. Each part has id, options for the part and a header image
 * that can be used as a desciption of the part.
 */
public class AvatarPart {

	/* Part id. */
	private int mId;

	/* Header image that describes the part. */
	private Bitmap mHeader;

	/* List of different options for this part. */
	private ArrayList<AvatarPartOption> mOptions;

	/* Mapping options with their ids. */
	private Map<Integer, AvatarPartOption> mOptionsMap;

	/**
	 * Constructs a specific avatar part.
	 * 
	 * @param id
	 *            Part's id.
	 * @param options
	 *            List of options for this part.
	 * @param headerPos
	 *            Position of the header in the list of options. This header is
	 *            used as a description of the part.
	 */
	public AvatarPart(int id, ArrayList<AvatarPartOption> options, int headerPos) {
		this(id, options.get(headerPos).getBitmap(), options);
	}

	/**
	 * Constructs a specific avatar part.
	 * 
	 * @param id
	 *            Part's id.
	 * @param options
	 *            List of options for this part.
	 * @param header
	 *            Image of the part's header that is used as a description of
	 *            the part.
	 */
	public AvatarPart(int id, Bitmap header, ArrayList<AvatarPartOption> options) {
		super();
		mId = id;
		mHeader = header;
		mOptions = options;

		if (mOptions != null) {
			mOptionsMap = new HashMap<Integer, AvatarPartOption>();
			for (AvatarPartOption opt : mOptions) {
				mOptionsMap.put(opt.getId(), opt);
			}
		}
	}

	/**
	 * Returns options image by the option's id.
	 * 
	 * @param optionId
	 *            Id of the option.
	 * @return Bitmap image of the option.
	 */
	public Bitmap getOptionBitmapByOptionId(int optionId) {
		AvatarPartOption opt = mOptionsMap.get(optionId);
		if (opt != null) {
			return opt.getBitmap();
		} else {
			return null;
		}
	}

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		mId = id;
	}

	public Bitmap getHeader() {
		return mHeader;
	}

	public void setHeader(Bitmap header) {
		mHeader = header;
	}

	public ArrayList<AvatarPartOption> getOptions() {
		return mOptions;
	}

	public void setOptions(ArrayList<AvatarPartOption> options) {
		mOptions = options;
	}
}
