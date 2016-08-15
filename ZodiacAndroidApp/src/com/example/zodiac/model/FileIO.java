package com.example.zodiac.model;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class FileIO {
	/*
	 * Класс для загрузки файлов.
	 */

	/*
	 * Конкекст приложения и соответствующий контексту менеджер ресурсов,
	 * находящихся в папке assets.
	 */
	private Context mContext;
	private AssetManager mAssets;

	public FileIO(Context context) {
		mContext = context;
		mAssets = mContext.getAssets();
	}

	/* Чтение ресурса. */
	public InputStream loadAsset(String filename) throws IOException {
		return mAssets.open(filename);
	}

	/* Чтение изображения. */
	public Bitmap loadAssetBitmap(String filename) throws IOException {
		return BitmapFactory.decodeStream(loadAsset(filename));
	}
}
