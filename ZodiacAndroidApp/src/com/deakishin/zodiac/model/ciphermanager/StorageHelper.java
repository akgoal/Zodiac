package com.deakishin.zodiac.model.ciphermanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.model.framework.FileIO;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

/** Helper class for reading from and writing to the file system. */
class StorageHelper {

	private static final String TAG = "StorageHelper";

	/* Name of the file to write ciphers' short infos to. */
	private static final String FILENAME = "ciphers.json";

	/* Name of the default source image */
	private static final String DEFAULT_ORIGINAL_IMAGE_FILENAME = "ZodiacCipher.png";
	/* Name of the file where cipher's info is stored. */
	private static final String INFO_FILENAME = "cipher_info.zdcm";

	/** Delimiter between a header and a body in the file with cipher's info. */
	public static final String HEADER_DELIMITER = "~";

	/* Application context. */
	private Context mContext;

	/* Object for file input-output. */
	private FileIO mFileIO;

	/* Helper for reading/writing markup info. */
	private MarkupIOHelper mMarkupHelper;

	public StorageHelper(Context context) {
		mContext = context.getApplicationContext();
		mFileIO = FileIO.getInstance(mContext);
		mMarkupHelper = new MarkupIOHelper();
	}

	/** @return Loaded default source image. */
	public Bitmap loadDefaultOriginalImage() {
		try {
			return mFileIO.loadAssetBitmap(DEFAULT_ORIGINAL_IMAGE_FILENAME);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Reads info about present ciphers.
	 * 
	 * @return List of short info of ciphers.
	 */
	public ArrayList<CipherShortInfo> readCipherShortInfos() {
		ArrayList<CipherShortInfo> ciphers = new ArrayList<CipherShortInfo>();

		CipherShortInfo zodiac340Info = new CipherShortInfo(CipherShortInfo.ZODIAC340_ID,
				mContext.getString(R.string.zodiac340_cipher_name), "", false);
		ciphers.add(zodiac340Info);

		BufferedReader reader = null;
		InputStream in = null;
		try {
			in = mFileIO.readExtCiphersFile(null, FILENAME);
			reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder jsonString = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				jsonString.append(line);
			}
			JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();

			for (int i = 0; i < array.length(); i++) {
				try {
					ciphers.add(new CipherShortInfo(array.getJSONObject(i)));
				} catch (JSONException e) {
				}
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} catch (JSONException e) {
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
				}
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
				}
		}

		return ciphers;
	}

	/**
	 * Saves ciphers to the file system. Zodiac-340 cipher is not getting saved.
	 * 
	 * @param ciphers
	 *            List of short info of ciphers to save.
	 * @return True if saved successfully, false otherwise.
	 */
	public boolean saveCipherShortInfos(ArrayList<CipherShortInfo> ciphers) {
		JSONArray array = new JSONArray();

		for (CipherShortInfo cipher : ciphers) {
			if (!cipher.isZodiac340()) {
				try {
					array.put(cipher.toJSON());
				} catch (JSONException e1) {
				}
			}
		}

		Writer writer = null;
		OutputStream out = null;
		try {
			out = mFileIO.writeExtCiphersFile(null, FILENAME);
			writer = new OutputStreamWriter(out);
			writer.write(array.toString());
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			if (writer != null)
				try {
					writer.close();
				} catch (IOException e) {
				}
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
				}
		}
	}

	/**
	 * Reads data for the cipher using info about the folder to use (this info
	 * has to present in the given object). Then this data gets written to the
	 * given object.
	 * 
	 * @param cipher
	 *            Info of the cipher. This info must include name of the folder
	 *            to use.
	 * @return True is the cipher is loaded successfully, false otherwise.
	 */
	public boolean loadCipherInfo(CipherInfo cipher) {
		InputStream stream = null;
		InputStreamReader isr = null;
		BufferedReader reader = null;
		try {
			stream = getInputStream(cipher);
			isr = new InputStreamReader(stream);
			reader = new BufferedReader(isr);
			readInfo(reader, cipher);
			return true;
		} catch (Exception e) {
			Log.e(TAG, "" + e);
			return false;
		} finally {
			try {
				reader.close();
				isr.close();
				stream.close();
			} catch (Exception ex) {
				Log.e(TAG, "" + ex);
			}
		}
	}

	/* Get InputStream for the cipher to read data. */
	private InputStream getInputStream(CipherInfo cipher) throws IOException {
		if (cipher.isZodiac340()) {
			return mFileIO.loadAsset(INFO_FILENAME);
		}
		return mFileIO.readExtCiphersFile(cipher.getFolderName(), INFO_FILENAME);
	}

	/* Read data from the reader and write this data to the cipher object. */
	private void readInfo(BufferedReader reader, CipherInfo cipher) throws Exception {
		if (cipher.isZodiac340()) {
			cipher.setAuthor(mContext.getString(R.string.zodiac340_cipher_author));
			cipher.setDescription(mContext.getString(R.string.zodiac340_cipher_descr));
			Markup markup = mMarkupHelper.readMarkup(reader, cipher.getOriginalImage());
			cipher.setMarkup(markup);
		} else {
			String line = reader.readLine();
			String[] lineParts = line.split(HEADER_DELIMITER);
			String jsonString = lineParts[0];
			JSONObject json = new JSONObject(jsonString);
			if (!cipher.readInfoFromJson(json)) {
				throw new Exception("Corrupt cipher file");
			}
			Markup markup = mMarkupHelper.readMarkupFromString(lineParts[1], cipher.getOriginalImage());
			cipher.setMarkup(markup);
		}
	}

	/**
	 * Saves cipher by writing its data to the file
	 * 
	 * @param cipher
	 *            Cipher to save.
	 * @return True if saved successfully, false otherwise.
	 */
	public boolean saveCipher(CipherInfo cipher) {
		if (cipher.getMarkupAsString() == null)
			return false;

		Writer writer = null;
		OutputStream out = null;
		BufferedWriter bw = null;
		try {
			out = mFileIO.writeExtCiphersFile(cipher.getFolderName(), INFO_FILENAME);
			writer = new OutputStreamWriter(out);
			bw = new BufferedWriter(writer);
			bw.write(cipher.toJSON().toString() + HEADER_DELIMITER + cipher.getMarkupAsString());
			return true;
		} catch (IOException e) {
			return false;
		} catch (JSONException e) {
			return false;
		} finally {
			if (bw != null)
				try {
					bw.close();
				} catch (IOException e) {
				}
			if (writer != null)
				try {
					writer.close();
				} catch (IOException e) {
				}
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
				}
		}
	}

	/**
	 * Deletes all the info about the cipher contained in its folder.
	 * 
	 * @param cipher
	 *            Cipher to delete.
	 */
	public void deleteCipher(CipherShortInfo cipher) {
		mFileIO.deleteCipherFolder(cipher.getFolderName());
	}
}
