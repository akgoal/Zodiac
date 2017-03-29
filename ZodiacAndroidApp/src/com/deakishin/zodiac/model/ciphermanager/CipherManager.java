package com.deakishin.zodiac.model.ciphermanager;

import java.util.ArrayList;

import com.deakishin.zodiac.model.ciphermodel.Model;
import com.deakishin.zodiac.model.settings.SettingsPersistent;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Ciphers manager implemented as a Singleton. Manages information about all the
 * ciphers. Also holds the current cipher.
 */
public class CipherManager {

	/* Application context. */
	private Context mContext;

	/* Short information about all the ciphers. */
	private ArrayList<CipherShortInfo> mCipherShortInfos;
	/* List of ids of all the ciphers. */
	private ArrayList<Long> mCiphersIds;

	/* Information about current cipher. */
	private CipherInfo mCurrentCipherInfo;

	/* Helper for saving/loading info. */
	private StorageHelper mStorageHelper;

	/* Default source image. */
	private Bitmap mDefaultOriginalImage;

	/* Application settings. */
	private SettingsPersistent mSettings;

	private static CipherManager sCipherManager;

	/**
	 * Returns the single instance of the class.
	 * 
	 * @param context
	 *            Application context.
	 * @return The sole instance of the singleton.
	 */
	public static CipherManager getInstance(Context context) {
		if (sCipherManager == null)
			sCipherManager = new CipherManager(context.getApplicationContext());
		return sCipherManager;
	}

	private CipherManager(Context context) {
		mContext = context;

		mStorageHelper = new StorageHelper(mContext);

		mDefaultOriginalImage = mStorageHelper.loadDefaultOriginalImage();

		mCipherShortInfos = mStorageHelper.readCipherShortInfos();

		mCiphersIds = new ArrayList<Long>();
		if (mCipherShortInfos != null) {
			for (CipherShortInfo cipher : mCipherShortInfos)
				mCiphersIds.add(cipher.getId());
		}

		mSettings = SettingsPersistent.getInstance(mContext);

		if (!setLastCipher())
			setDefaultCipher();
	}

	/** @return Current cipher's model. */
	public Model getCipherModel() {
		if (Model.getInstance() == null)
			if (!setLastCipher())
				setDefaultCipher();

		return Model.getInstance();
	}

	/** @return List of short informations about all the ciphers. */
	public ArrayList<CipherShortInfo> getCipherInfos() {
		return mCipherShortInfos;
	}

	/** @return List of ids of all the ciphers. */
	public ArrayList<Long> getCipherIds() {
		return mCiphersIds;
	}

	/** @return List of short info of all ciphers except the current one. */
	public ArrayList<CipherShortInfo> getOtherCipherInfos() {
		ArrayList<CipherShortInfo> res = new ArrayList<CipherShortInfo>();
		for (CipherShortInfo c : mCipherShortInfos) {
			if (c.getId() != mCurrentCipherInfo.getId())
				res.add(c);
		}
		return res;
	}

	/*
	 * Set last cipher, saved in the app settings. Return true if set
	 * successfully.
	 */
	private boolean setLastCipher() {
		long cipherId = mSettings.getLastCipherId();
		if (mCipherShortInfos == null)
			return false;

		for (CipherShortInfo csi : mCipherShortInfos) {
			if (csi.getId() == cipherId) {
				if (setCurrentCipher(csi) != null)
					return true;
				else {
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * Sets current cipher by its short info.
	 * 
	 * @param cipherShortInfo
	 *            Short info of the cipher.
	 * @return Info of the cipher that was set or null if setting failed.
	 */
	public CipherInfo setCurrentCipher(CipherShortInfo cipherShortInfo) {
		CipherInfo newCipher = new CipherInfo();
		newCipher.setShortInfo(cipherShortInfo);
		newCipher.setOriginalImage(mDefaultOriginalImage);
		if (!mStorageHelper.loadCipherInfo(newCipher)) {
			newCipher.recycle();
			newCipher = null;
			return null;
		}

		if (Model.setModelFromCipherInfo(mContext, newCipher)) {
			if (mCurrentCipherInfo != null)
				mCurrentCipherInfo.recycle();
			mCurrentCipherInfo = newCipher;
			mSettings.setLastCipherId(mCurrentCipherInfo.getId());
			return getCurrentCipherInfo();
		} else {
			newCipher.recycle();
			newCipher = null;
			return null;
		}
	}

	/**
	 * Sets default cipher as a current one. The cipher is Zodiac-340 cipher.
	 */
	public void setDefaultCipher() {
		for (CipherShortInfo cipher : mCipherShortInfos) {
			if (cipher.isZodiac340()) {
				setCurrentCipher(cipher);
				return;
			}
		}
		if (!mCipherShortInfos.isEmpty())
			setCurrentCipher(mCipherShortInfos.get(0));
	}

	/**
	 * Deletes cipher from the list of ciphers by the cipher's short info.
	 * Default Zodiac-340 cipher cannot be deleted.
	 * 
	 * @param cipher
	 *            Short info of a cipher to delete.
	 */
	public void deleteCipher(CipherShortInfo cipher) {
		if (cipher.isZodiac340())
			return;
		mCipherShortInfos.remove(cipher);
		mCiphersIds.remove(cipher.getId());
		mStorageHelper.saveCipherShortInfos(mCipherShortInfos);
		mStorageHelper.deleteCipher(cipher);
	}

	/** @return Info of the current cipher. */
	public CipherInfo getCurrentCipherInfo() {
		return mCurrentCipherInfo;
	}

	/**
	 * Adds a cipher to the list of ciphers.
	 * 
	 * @param cipher
	 *            Info of a cipher to add.
	 * @return True if the cipher is added successfully, false otherwise.
	 */
	public boolean addCipher(CipherInfo cipher) {
		cipher.setFolderName("" + cipher.getId());
		mCipherShortInfos.add(new CipherShortInfo(cipher.getId(), cipher.getTitle(), cipher.getFolderName(), true));
		if (mStorageHelper.saveCipher(cipher))
			if (mStorageHelper.saveCipherShortInfos(mCipherShortInfos)) {
				mCiphersIds.add(cipher.getId());
				return true;
			}
		return false;
	}
}
