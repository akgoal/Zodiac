package com.deakishin.zodiac.model.avatargenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.deakishin.zodiac.model.avatargenerator.AvatarProfile.PartProfile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

/** Singleton for generating avatars. */
public class AvatarGenerator {

	/* List of avatar parts. */
	private ArrayList<AvatarPart> mParts;

	/* Mapping between parts and their ids. */
	private Map<Integer, AvatarPart> mPartsMap;

	/* Default avatar image. */
	private Bitmap mDefaultAvatar;

	/* Application context. */
	private Context mContext;

	private static AvatarGenerator sAvatarGenerator;

	/**
	 * Returns Singleton instance.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Singleton instance.
	 */
	public static AvatarGenerator getInstance(Context context) {
		if (sAvatarGenerator == null)
			sAvatarGenerator = new AvatarGenerator(context.getApplicationContext());
		return sAvatarGenerator;
	}

	private AvatarGenerator(Context context) {
		mContext = context;

		// Load avatar parts.
		AvatarPartsStorageHelper storageHelper = new AvatarPartsStorageHelper(mContext);
		mParts = storageHelper.loadParts();

		if (mParts != null) {
			// Map parts with their ids.
			mPartsMap = new HashMap<Integer, AvatarPart>();
			for (AvatarPart part : mParts) {
				mPartsMap.put(part.getId(), part);
			}
		}

		// Load default avatar image.
		mDefaultAvatar = storageHelper.loadDefaultAvatar();
	}

	/** @return Default avatar image. */
	public Bitmap getDefaultAvatar() {
		return mDefaultAvatar;
	}

	/**
	 * Generates and returns avatar image by its profile.
	 * 
	 * @param profile
	 *            Avatar profile that is used to generate the image.
	 * @param bgColor
	 *            Background color for the image, or null if no background is
	 *            needed.
	 * @return Bitmap avatar image.
	 */
	public Bitmap generateAvatarBitmap(AvatarProfile profile, Integer bgColor) {
		if (profile == null || profile.isEmpty()) {
			return mDefaultAvatar.copy(mDefaultAvatar.getConfig(), true);
		} else {
			Bitmap res = Bitmap.createBitmap(AvatarPaintParams.WIDTH, AvatarPaintParams.HEIGHT,
					AvatarPaintParams.CONFIG);
			Canvas canvas = new Canvas(res);
			if (bgColor != null) {
				canvas.drawColor(bgColor);
			}
			if (mPartsMap != null) {
				for (AvatarPaintParams.PartPaintParams partParams : AvatarPaintParams.PART_PAINT_PARAMS) {
					int partId = partParams.getPartId();
					AvatarPart part = mPartsMap.get(partId);
					AvatarProfile.PartProfile partProfile = profile.getPartProfiles().get(partId);
					if (part != null && partProfile != null) {
						Bitmap partBitmap = part.getOptionBitmapByOptionId(partProfile.getOptionId());
						if (partBitmap != null) {
							int centerX = (int) (partParams.getCenterX()
									+ partProfile.getOffsetX() * partParams.getMaxOffsetX());
							int centerY = (int) (partParams.getCenterY()
									+ partProfile.getOffsetY() * partParams.getMaxOffsetY());
							int w = (int) (partBitmap.getWidth()
									* (1 + partProfile.getScaleX() * partParams.getMaxScaleX()));
							int h = (int) (partBitmap.getHeight()
									* (1 + partProfile.getScaleY() * partParams.getMaxScaleY()));
							drawBitmapCenter(canvas, partBitmap, centerX, centerY, w, h);
						}
					}
				}
			}
			return res;
		}

	}

	/**
	 * Generates and returns avatar image by its profile with no background.
	 * 
	 * @param profile
	 *            Avatar profile that is used to generate the image.
	 * @return Bitmap avatar image.
	 */
	public Bitmap generateAvatarBitmap(AvatarProfile profile) {
		return generateAvatarBitmap(profile, null);
	}

	/*
	 * Draw bitmap image by its center's coordinates x, y and its width w and
	 * height h.
	 */
	private void drawBitmapCenter(Canvas canvas, Bitmap bitmap, int x, int y, int w, int h) {
		int left = x - w / 2;
		int top = y - h / 2;
		canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth() - 1, bitmap.getHeight() - 1),
				new Rect(left, top, left + w - 1, top + h - 1), null);
		// canvas.drawBitmap(bitmap, left, top, null);
	}

	/**
	 * @return Default avatar profile. Not the same as
	 *         {@link #getDefaultAvatar()} as it returns a profile with specific
	 *         part options in it.
	 */
	public AvatarProfile getDefaultProfile() {
		AvatarProfile profile = new AvatarProfile();

		Map<Integer, PartProfile> partProfiles = new HashMap<Integer, PartProfile>();
		for (AvatarPart part : mParts) {
			PartProfile pp = new PartProfile();
			pp.setOptionId(part.getOptions().get(0).getId());
			partProfiles.put(part.getId(), pp);
		}
		profile.setPartProfiles(partProfiles);

		return profile;
	}

	/** @return Generates and returns random avatar profile. */
	public AvatarProfile generateRandomProfile() {
		AvatarProfile profile = new AvatarProfile();

		Random rand = new Random();
		Map<Integer, PartProfile> partProfiles = new HashMap<Integer, PartProfile>();
		for (AvatarPart part : mParts) {
			PartProfile pp = new PartProfile();
			pp.setOptionId(part.getOptions().get(rand.nextInt(part.getOptions().size())).getId());
			partProfiles.put(part.getId(), pp);
		}
		profile.setPartProfiles(partProfiles);

		return profile;
	}

	/** @return List of the avatar parts used to generate an avatar. */
	public ArrayList<AvatarPart> getParts() {
		return mParts;
	}
}
