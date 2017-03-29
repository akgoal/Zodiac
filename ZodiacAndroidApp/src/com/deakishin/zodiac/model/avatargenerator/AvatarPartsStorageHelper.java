package com.deakishin.zodiac.model.avatargenerator;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.deakishin.zodiac.model.framework.FileIO;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

/** Helper class for loading avatar parts. */
public class AvatarPartsStorageHelper {

	/** Ids of the parts. */
	public static final int ID_HEADS = 0, ID_JAWS = 1, ID_BEARDS = 2, ID_EYES = 3, ID_MOUSTACHES = 4, ID_EYEBROWS = 5,
			ID_GLASSES = 6, ID_NOSES = 7, ID_MOUTHS = 8, ID_HAIR = 9;

	/* Filename where parts are read and loaded from. */
	private static final String FILENAME_ALL_NO_HAIR = "all_compact_top15_100x100_no_hair.png";
	private static final String FILENAME_HAIR = "all_hair_125x125.png";
	/* Standart width and height of an element in the filed. */
	private static final int PART_WIDTH = 100, PART_HEIGHT = 100;

	/*
	 * Array of infos about each part in the file in the same order there will
	 * be displayed on the Avatar generating screen.
	 */
	private static final PartFileInfo[] PARTS_INFO_NO_HAIR = { new PartFileInfo(ID_HEADS, 9, false, 4),
			new PartFileInfo(ID_JAWS, 13, false, 5), new PartFileInfo(ID_EYES, 15, false, 2),
			new PartFileInfo(ID_EYEBROWS, 15, true, 1), new PartFileInfo(ID_NOSES, 15, false, 8),
			new PartFileInfo(ID_MOUTHS, 15, false, 7), new PartFileInfo(ID_MOUSTACHES, 15, true, 6),
			new PartFileInfo(ID_BEARDS, 15, true, 0), new PartFileInfo(ID_GLASSES, 15, true, 3) };
	private static final PartFileInfo PART_INFO_HAIR = new PartFileInfo(ID_HAIR, 67, true, 0, 125, 125, 12);

	/* Maximum number of options loaded for each part. */
	private static final int MAX_PART_OPTIONS = 100;

	/* Filename where default avatar image is stored. */
	private static final String FILENAME_DEFAULT_AVATAR = "avatar_default.png";

	/* Application context. */
	private Context mContext;

	/* Object for file input-output. */
	private FileIO mFileIO;

	public AvatarPartsStorageHelper(Context context) {
		mContext = context;

		mFileIO = FileIO.getInstance(mContext);
	}

	/** @return Loaded default avatar image. */
	public Bitmap loadDefaultAvatar() {
		try {
			return mFileIO.loadAssetBitmap(FILENAME_DEFAULT_AVATAR);
		} catch (IOException e) {
			return null;
		}
	}

	/** @return List of avatar parts. */
	public ArrayList<AvatarPart> loadParts() {
		ArrayList<AvatarPart> parts = new ArrayList<AvatarPart>();

		Bitmap srcBitmap = null;
		try {
			srcBitmap = mFileIO.loadAssetBitmap(FILENAME_ALL_NO_HAIR);

			for (PartFileInfo partFileInfo : PARTS_INFO_NO_HAIR) {
				int headerPosition = partFileInfo.isNeedEmpty() ? 1 : 0;
				parts.add(new AvatarPart(partFileInfo.getPartId(), loadPartOptions(partFileInfo, srcBitmap),
						headerPosition));
			}

			if (srcBitmap != null) {
				srcBitmap.recycle();
			}

			srcBitmap = mFileIO.loadAssetBitmap(FILENAME_HAIR);
			int headerPosition = PART_INFO_HAIR.isNeedEmpty() ? 1 : 0;
			parts.add(new AvatarPart(PART_INFO_HAIR.getPartId(), loadPartOptions(PART_INFO_HAIR, srcBitmap),
					headerPosition));

		} catch (IOException e) {
		} finally {
			if (srcBitmap != null && !srcBitmap.isRecycled()) {
				srcBitmap.recycle();
				srcBitmap = null;
			}
		}

		return parts;
	}

	/*
	 * Загрузка вариантов конкретной части аватарки по информации о файле.
	 * srcBitmap - исходное изображение.
	 */
	/**
	 * Loads options for a specific part from the file.
	 * 
	 * @param partFileInfo
	 *            Info about the part in the file.
	 * @param srcBitmap
	 *            Source image.
	 * @return List of options.
	 */
	private ArrayList<AvatarPartOption> loadPartOptions(PartFileInfo partFileInfo, Bitmap srcBitmap) {
		ArrayList<AvatarPartOption> res = new ArrayList<AvatarPartOption>();

		int w = partFileInfo.getWidth();
		int h = partFileInfo.getHeight();
		if (partFileInfo.isNeedEmpty()) {
			res.add(new AvatarPartOption(0, Bitmap.createBitmap(w, h, Config.ARGB_8888)));
		}
		int len = partFileInfo.getOptionsCount();
		int y = partFileInfo.getRowY();
		int rowLen = partFileInfo.getOptionsInRowCount();
		int id = 1;
		for (int i = 0; i < Math.min(len, MAX_PART_OPTIONS); i++) {
			int rowJ = i / rowLen;
			int rowI = i - rowJ * rowLen;

			res.add(new AvatarPartOption(id, getSubimage(srcBitmap, rowI * w, y + rowJ * h, w, h)));
			id++;
		}
		return res;
	}

	/* Get area in the image. */
	private Bitmap getSubimage(Bitmap image, int x, int y, int w, int h) {
		return Bitmap.createBitmap(image, x, y, w, h);
	}

	/** Information about how a specific avatar part is stored in the file. */
	private static class PartFileInfo {
		/* Part's id. */
		private int mPartId;
		/* Number of options contained in the file. */
		private int mOptionsCount;
		/* If an empty option is needed to be added to the list of options. */
		private boolean mNeedEmpty;
		/* Orinal number of the row in the file where options are placed. */
		private int mRow;

		/* Size of an options in the file. */
		private int mWidth, mHeight;
		/* Число вариантов в строке. */
		private int mOptionsInRowCount;
		/* Coordinate Y of the first row. */
		private int mRowY;

		/**
		 * Constructs an object containing info about how a specific part and
		 * its options are stored in the file. Width and height of each option
		 * are considered standart.
		 * 
		 * @param partId
		 *            Id of the part.
		 * @param optionsCount
		 *            Number of options in the file.
		 * @param needEmpty
		 *            An empty option has to be added in the beginning of the
		 *            options list.
		 * @param row
		 *            Ordinal number (from 0) of the row where options are
		 *            placed.
		 */
		public PartFileInfo(int partId, int optionsCount, boolean needEmpty, int row) {
			this(partId, optionsCount, needEmpty, row, PART_WIDTH, PART_HEIGHT, optionsCount);
		}

		/**
		 * Constructs an object containing info about how a specific part and
		 * its options are stored in the file.
		 * 
		 * @param partId
		 *            Id of the part.
		 * @param optionsCount
		 *            Number of options in the file.
		 * @param needEmpty
		 *            An empty option has to be added in the beginning of the
		 *            options list.
		 * @param row
		 *            Ordinal number (from 0) of the first row where options are
		 *            placed.
		 * @param width
		 *            Width of each option.
		 * @param height
		 *            Height of each option.
		 * @param optionsInRowCount
		 *            Number of options in the row.
		 */
		public PartFileInfo(int partId, int optionsCount, boolean needEmpty, int row, int width, int height,
				int optionsInRowCount) {
			mPartId = partId;
			mOptionsCount = optionsCount;
			mNeedEmpty = needEmpty;
			mRow = row;
			mWidth = width;
			mHeight = height;
			mOptionsInRowCount = optionsInRowCount;

			mRowY = PART_HEIGHT * row;
		}

		public int getPartId() {
			return mPartId;
		}

		public int getOptionsCount() {
			return mOptionsCount;
		}

		public boolean isNeedEmpty() {
			return mNeedEmpty;
		}

		public int getRow() {
			return mRow;
		}

		public int getWidth() {
			return mWidth;
		}

		public int getHeight() {
			return mHeight;
		}

		public int getOptionsInRowCount() {
			return mOptionsInRowCount;
		}

		public int getRowY() {
			return mRowY;
		}
	}

	static class FlushedInputStream extends FilterInputStream {
		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					int b = read();
					if (b < 0) {
						break; // we reached EOF
					} else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}
}
