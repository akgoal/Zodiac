package com.deakishin.zodiac.model.avatargenerator;

import android.graphics.Bitmap.Config;

/**
 * Class containing parameters for drawing avatars. For example, in which order
 * and in what position each part of an avatar has to be drawn.
 */
public class AvatarPaintParams {

	/** Avatar image size in pixels. */
	public static final int WIDTH = 120, HEIGHT = 120;

	/** Default maximum offset for an avatar part. */
	public static final int DEF_MAX_OFFSET = 25;
	/** Default maximum scale coef for an avatar part. */
	private static final float DEF_MAX_SCALE = (float) 0.4;

	/** Avatar image configuration info. */
	public static final Config CONFIG = Config.ARGB_8888;

	/** Array of paint parameters for each avatar parts. */
	public static final PartPaintParams[] PART_PAINT_PARAMS = {
			new PartPaintParams(AvatarPartsStorageHelper.ID_HEADS, WIDTH / 2, HEIGHT / 2 - 25, DEF_MAX_OFFSET,
					DEF_MAX_OFFSET, DEF_MAX_SCALE, DEF_MAX_SCALE),
			new PartPaintParams(AvatarPartsStorageHelper.ID_JAWS, WIDTH / 2 + 5, HEIGHT / 2 + 27, DEF_MAX_OFFSET,
					DEF_MAX_OFFSET, DEF_MAX_SCALE, DEF_MAX_SCALE),
			new PartPaintParams(AvatarPartsStorageHelper.ID_NOSES, WIDTH / 2 + 2, HEIGHT / 2 + 15, DEF_MAX_OFFSET,
					DEF_MAX_OFFSET, DEF_MAX_SCALE, DEF_MAX_SCALE),
			new PartPaintParams(AvatarPartsStorageHelper.ID_EYES, WIDTH / 2 + 2, HEIGHT / 2, DEF_MAX_OFFSET,
					DEF_MAX_OFFSET, DEF_MAX_SCALE, DEF_MAX_SCALE),
			new PartPaintParams(AvatarPartsStorageHelper.ID_MOUTHS, WIDTH / 2 + 2, HEIGHT / 2 + 29, DEF_MAX_OFFSET,
					DEF_MAX_OFFSET, DEF_MAX_SCALE, DEF_MAX_SCALE),
			new PartPaintParams(AvatarPartsStorageHelper.ID_EYEBROWS, WIDTH / 2, HEIGHT / 2 - 7, DEF_MAX_OFFSET,
					DEF_MAX_OFFSET, DEF_MAX_SCALE, DEF_MAX_SCALE),
			new PartPaintParams(AvatarPartsStorageHelper.ID_MOUSTACHES, WIDTH / 2 + 2, HEIGHT / 2 + 26, DEF_MAX_OFFSET,
					DEF_MAX_OFFSET, DEF_MAX_SCALE, DEF_MAX_SCALE),
			new PartPaintParams(AvatarPartsStorageHelper.ID_BEARDS, WIDTH / 2 + 2, HEIGHT / 2 + 33, DEF_MAX_OFFSET,
					DEF_MAX_OFFSET, DEF_MAX_SCALE, DEF_MAX_SCALE),
			new PartPaintParams(AvatarPartsStorageHelper.ID_HAIR, WIDTH / 2, HEIGHT / 2, DEF_MAX_OFFSET, DEF_MAX_OFFSET,
					DEF_MAX_SCALE, DEF_MAX_SCALE),
			new PartPaintParams(AvatarPartsStorageHelper.ID_GLASSES, WIDTH / 2 + 1, HEIGHT / 2 + 2, DEF_MAX_OFFSET,
					DEF_MAX_OFFSET, DEF_MAX_SCALE, DEF_MAX_SCALE), };

	/**
	 * Paint parameters for a specific avatar part. These parameters are used to
	 * draw the part on the avatar image.
	 */
	public static class PartPaintParams {
		/* Part id. */
		private int mPartId;

		/* Coordinates of the center. */
		private int mCenterX, mCenterY;

		/* Maximum offset values. */
		private int mMaxOffsetX, mMaxOffsetY;

		/*
		 * Maximum offsets from 1 for scaling coefs. For example, if this offset
		 * is 0.3 then the part can be scaled with coefs from 0.7 to 1.3.
		 */
		private float mMaxScaleX, mMaxScaleY;

		/**
		 * Construct parameters for a specific avatar part.
		 * 
		 * @param partId
		 *            Id of the part.
		 * @param centerX
		 *            Coordinate X of the center.
		 * @param centerY
		 *            Coordinate Y of the center.
		 * @param maxOffsetX
		 *            Maximum offset on the X-axis.
		 * @param maxOffsetY
		 *            Maximum offset on the Y-axis.
		 * @param maxScaleX
		 *            Maximum offset from 1 for the scale coef on the X-axis.
		 * @param maxScaleY
		 *            Maximum offset from 1 for the scale coef on the Y-axis.
		 */
		public PartPaintParams(int partId, int centerX, int centerY, int maxOffsetX, int maxOffsetY, float maxScaleX,
				float maxScaleY) {
			super();
			mPartId = partId;
			mCenterX = centerX;
			mCenterY = centerY;
			mMaxOffsetX = maxOffsetX;
			mMaxOffsetY = maxOffsetY;
			mMaxScaleX = maxScaleX;
			mMaxScaleY = maxScaleY;
		}

		/**
		 * Construct parameters for a specific avatar part.
		 * 
		 * @param partId
		 *            Id of the part.
		 * @param centerX
		 *            Coordinate X of the center.
		 * @param centerY
		 *            Coordinate Y of the center.
		 * @param maxOffsetX
		 *            Maximum offset on the X-axis.
		 * @param maxOffsetY
		 *            Maximum offset on the Y-axis.
		 * @param maxScaleX
		 *            Maximum offset from 1 for the scale coef on the X-axis.
		 * @param maxScaleY
		 *            Maximum offset from 1 for the scale coef on the Y-axis.
		 */
		public PartPaintParams(int partId, int centerX, int centerY, int maxOffsetX, int maxOffsetY, double maxScaleX,
				double maxScaleY) {
			this(partId, centerX, centerY, maxOffsetX, maxOffsetY, (float) maxScaleX, (float) maxScaleY);
		}

		/** @return Coordinate X of the center. */
		public int getCenterX() {
			return mCenterX;
		}

		public void setCenterX(int centerX) {
			mCenterX = centerX;
		}

		/** @return Coordinate Y of the center. */
		public int getCenterY() {
			return mCenterY;
		}

		public void setCenterY(int centerY) {
			mCenterY = centerY;
		}

		/** @return Maximum offset on the X-axis. */
		public int getMaxOffsetX() {
			return mMaxOffsetX;
		}

		public void setMaxOffsetX(int maxOffsetX) {
			mMaxOffsetX = maxOffsetX;
		}

		/** @return Maximum offset on the Y-axis. */
		public int getMaxOffsetY() {
			return mMaxOffsetY;
		}

		public void setMaxOffsetY(int maxOffsetY) {
			mMaxOffsetY = maxOffsetY;
		}

		/**
		 * @return Maximum offset from 1 for the scale coef on the X-axis. For
		 *         example, if this value is 0.2 then the avatar part can be
		 *         scaled with the scale coef from 0.8 to 1.2.
		 */
		public float getMaxScaleX() {
			return mMaxScaleX;
		}

		public void setMaxScaleX(float maxScaleX) {
			mMaxScaleX = maxScaleX;
		}

		/**
		 * @return Maximum offset from 1 for the scale coef on the Y-axis. For
		 *         example, if this value is 0.2 then the avatar part can be
		 *         scaled with the scale coef from 0.8 to 1.2.
		 */
		public float getMaxScaleY() {
			return mMaxScaleY;
		}

		public void setMaxScaleY(float maxScaleY) {
			mMaxScaleY = maxScaleY;
		}

		/** @return Id of the part. */
		public int getPartId() {
			return mPartId;
		}

		public void setPartId(int partId) {
			mPartId = partId;
		}
	}
}
