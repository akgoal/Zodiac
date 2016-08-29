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

public class FileIO {
	/*
	 * Класс для сохранения и загрузки файлов.
	 */

	/*
	 * Конкекст приложения и соответствующий контексту менеджер ресурсов,
	 * находящихся в папке assets.
	 */
	private Context mContext;
	private AssetManager mAssets;

	/* Внешнее хранилище. */
	private File mExternalStorage;
	/* Внешнее хранилище сохраненных изображений. */
	private File mExternalImageStorage;

	public FileIO(Context context) {
		mContext = context;
		mAssets = mContext.getAssets();

		String folderName = mContext.getString(R.string.ext_storage_folder_name);
		String imageFolderName = mContext.getString(R.string.ext_storage_image_folder_name);

		mExternalStorage = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + folderName);
		mExternalStorage.mkdir();
		mExternalImageStorage = new File(mExternalStorage.getAbsolutePath() + "/" + imageFolderName);
		mExternalImageStorage.mkdir();
	}

	/* Чтение ресурса. */
	public InputStream loadAsset(String filename) throws IOException {
		return mAssets.open(filename);
	}

	/* Чтение изображения. */
	public Bitmap loadAssetBitmap(String filename) throws IOException {
		return BitmapFactory.decodeStream(loadAsset(filename));
	}

	/* Чтение и запись файла во внешнем хранилище. */
	public InputStream readExtFile(String filename) throws IOException {
		return new FileInputStream(mExternalStorage.getAbsolutePath() + "/" + filename);
	}

	public OutputStream writeExtFile(String filename) throws IOException {
		if (!mExternalStorage.exists())
			mExternalStorage.mkdir();
		return new FileOutputStream(mExternalStorage.getAbsolutePath() + "/" + filename);
	}

	/*
	 * Сохранение изображения вов нешнем хранилище. Возвращает true в случае
	 * удачного сохранения.
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
}
