package com.deakishin.zodiac.model.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.deakishin.zodiac.R;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;

/**
 * Singleton for workin with the file system: reading from and writing to files.
 */
public class FileIO {

	/* Application context. */
	private Context mContext;
	/* Manager for accessing raw asset files. */
	private AssetManager mAssets;

	/* External storage. */
	private File mExternalStorage;
	/* Storage to save images to. */
	private File mExternalImageStorage;
	/* Storage for storing ciphers. */
	private File mExternalCiphersStorage;

	private static FileIO sFileIO;

	/**
	 * Provides access to the single instance of the class to work with the file
	 * system.
	 * 
	 * @param context
	 *            Application context.
	 * @return The sole instance of the singleton.
	 */
	public static FileIO getInstance(Context context) {
		if (sFileIO == null)
			sFileIO = new FileIO(context.getApplicationContext());
		return sFileIO;
	}

	/**
	 * Rebuilds the sole object of the class. Needs to be invoked when a
	 * permission to work the external storage is granted.
	 * 
	 * @param context
	 *            Application context.
	 */
	public static void rebuild(Context context) {
		sFileIO.build(context.getApplicationContext());
	}

	private FileIO(Context context) {
		build(context);
	}

	/* Build and configure the object. */
	private void build(Context context) {
		mContext = context;
		mAssets = mContext.getAssets();

		String folderName = mContext.getString(R.string.ext_storage_folder_name);
		String imageFolderName = mContext.getString(R.string.ext_storage_image_folder_name);
		String ciphersFolderName = mContext.getString(R.string.ext_storage_ciphers_folder_name);

		mExternalStorage = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + folderName);

		String oldFolderName = mContext.getString(R.string.ext_storage_folder_name_old);
		File mOldExtStorage = new File(
				Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + oldFolderName);
		if (mOldExtStorage.exists())
			mOldExtStorage.renameTo(mExternalStorage);

		mExternalStorage.mkdir();
		mExternalImageStorage = new File(mExternalStorage.getAbsolutePath() + "/" + imageFolderName);
		mExternalImageStorage.mkdir();
		mExternalCiphersStorage = new File(mExternalStorage.getAbsolutePath() + "/" + ciphersFolderName);
		mExternalCiphersStorage.mkdir();
	}

	/**
	 * Loads asset and provides stream to access the data in it.
	 * 
	 * @param filename
	 *            Name of the asset.
	 * @return Stream to read data from.
	 * @throws IOException
	 *             If unable to load the asset.
	 */
	public InputStream loadAsset(String filename) throws IOException {
		return mAssets.open(filename);
	}

	/**
	 * Loads and returns asset bitmap image.
	 * 
	 * @param filename
	 *            Name of the asset where the image is stored.
	 * @return Loaded image.
	 * @throws IOException
	 *             If unable to load the asset or if the asset is not an image.
	 */
	public Bitmap loadAssetBitmap(String filename) throws IOException {
		InputStream is = null;
		try {
			is = loadAsset(filename);
			Bitmap bitmap = BitmapFactory.decodeStream(is);
			return bitmap;
		} catch (IOException e) {
			throw e;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Reads from a file in the external storage.
	 * 
	 * @param filename
	 *            Name of the file.
	 * @return Stream to read data from.
	 * @throws IOException
	 *             If unable to read from the file.
	 */
	public InputStream readExtFile(String filename) throws IOException {
		return new FileInputStream(mExternalStorage.getAbsolutePath() + "/" + filename);
	}

	/**
	 * Writes data to a file in the external storage.
	 * 
	 * @param filename
	 *            Name of the file.
	 * @return Stream to write data to.
	 * @throws IOException
	 *             If unable to write to the file.
	 */
	public OutputStream writeExtFile(String filename) throws IOException {
		if (!mExternalStorage.exists())
			mExternalStorage.mkdir();
		return new FileOutputStream(mExternalStorage.getAbsolutePath() + "/" + filename);
	}

	/**
	 * Reads cipher data from a file in the external storage.
	 * 
	 * @param foldername
	 *            Name of the folder in which the file is stored.
	 * @param filename
	 *            Name of the file.
	 * @return Stream to read data from.
	 * @throws IOException
	 *             If unable to read from the file.
	 */
	public InputStream readExtCiphersFile(String foldername, String filename) throws IOException {
		String folderString;
		if (foldername != null && !foldername.equals("")) {
			folderString = "/" + foldername;
		} else
			folderString = "";
		return new FileInputStream(mExternalCiphersStorage.getAbsolutePath() + folderString + "/" + filename);
	}

	/**
	 * Writes cipher data to a file in the external storage.
	 * 
	 * @param foldername
	 *            Name of the folder in which the file is stored.
	 * @param filename
	 *            Name of the file.
	 * @return Stream to write data to.
	 * @throws IOException
	 *             If unable to write to the file.
	 */
	public OutputStream writeExtCiphersFile(String foldername, String filename) throws IOException {
		String folderString;
		if (foldername != null && !foldername.equals("")) {
			folderString = "/" + foldername;
		} else
			folderString = "";
		File folder = new File(mExternalCiphersStorage.getAbsolutePath() + folderString);
		if (!folder.exists())
			folder.mkdirs();
		File file = new File(mExternalCiphersStorage.getAbsolutePath() + folderString + "/" + filename);
		return new FileOutputStream(file);
	}

	/**
	 * Saves an image to a file in the external storage.
	 * 
	 * @param bitmap
	 *            Image to save.
	 * @param filename
	 *            Name of the file.
	 * @return True if saved successfully, false otherwise.
	 */
	public boolean writeExtBitmap(Bitmap bitmap, String filename) {
		if (!mExternalImageStorage.exists())
			mExternalImageStorage.mkdirs();
		OutputStream stream = null;
		try {
			File file = new File(mExternalImageStorage.getAbsolutePath() + "/" + filename + ".png");
			int i = 1;
			while (file.exists()) {
				file = new File(mExternalImageStorage.getAbsolutePath() + "/" + filename + " (" + i + ")" + ".png");
				i++;
			}
			stream = new FileOutputStream(file);
			bitmap.compress(CompressFormat.PNG, 100, stream);
			stream.flush();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (stream != null)
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * Deleles a folder of a cipher.
	 * 
	 * @param folderName
	 *            Name of the folder to delete.
	 */
	public void deleteCipherFolder(String folderName) {
		File folder = new File(mExternalCiphersStorage.getAbsolutePath() + "/" + folderName);
		deleteFile(folder);
	}

	/**
	 * Deletes a file. If it is a folder, then deletes everything in it, too.
	 * 
	 * @param file
	 *            File (or folder) to delete.
	 */
	private void deleteFile(File file) {
		File[] files = file.listFiles();
		if (files != null) {
			for (File f : files)
				deleteFile(f);
		}
		file.delete();
	}
}
