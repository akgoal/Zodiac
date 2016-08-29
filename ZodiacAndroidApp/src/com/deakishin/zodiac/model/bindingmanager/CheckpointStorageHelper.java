package com.deakishin.zodiac.model.bindingmanager;

import java.io.BufferedReader;
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

import com.deakishin.zodiac.model.framework.FileIO;

import android.content.Context;

public class CheckpointStorageHelper {
	/*
	 * Класс для сохранения чекпоинтов в файл и их чтения из файлов.
	 */

	private static final String CHECKPOINTS_FILENAME = "checkpoints.json";

	/* Ключ для хранения счетчика чекпоинтов. */
	private static final String JSON_CHECKPOINT_COUNT = "count";

	/* Контекст приложения. */
	private Context mContext;

	private FileIO mFileIO;

	public CheckpointStorageHelper(Context context) {
		mContext = context;
		mFileIO = new FileIO(mContext);
	}

	/* Запись чекпоинтов в файл. */
	public void saveCheckpoints(StorageUnit storageUnit) throws JSONException, IOException {
		ArrayList<CheckPoint> checkpoints = storageUnit.getCheckpoints();
		int checkpointCount = storageUnit.getCheckpointCount();
		JSONArray array = new JSONArray();

		JSONObject countJSON = new JSONObject();
		countJSON.put(JSON_CHECKPOINT_COUNT, checkpointCount);

		array.put(countJSON);
		for (CheckPoint checkpoint : checkpoints)
			array.put(checkpoint.toJSON());

		Writer writer = null;
		try {
			// OutputStream out = mContext.openFileOutput(CHECKPOINTS_FILENAME,
			// Context.MODE_PRIVATE);
			OutputStream out = mFileIO.writeExtFile(CHECKPOINTS_FILENAME);
			writer = new OutputStreamWriter(out);
			writer.write(array.toString());
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	/* Чтение чекпоинтов из файла. */
	public StorageUnit loadCheckpoints() throws IOException, JSONException {
		StorageUnit res = new StorageUnit();
		BufferedReader reader = null;
		try {
			// InputStream in = mContext.openFileInput(CHECKPOINTS_FILENAME);
			InputStream in = mFileIO.readExtFile(CHECKPOINTS_FILENAME);
			reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder jsonString = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				jsonString.append(line);
			}
			JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();
			ArrayList<CheckPoint> checkpoints = new ArrayList<CheckPoint>();
			int checkpointCount = 1;

			if (array.length() > 0)
				checkpointCount = array.getJSONObject(0).getInt(JSON_CHECKPOINT_COUNT);
			res.setCheckpointCount(checkpointCount);

			for (int i = 1; i < array.length(); i++) {
				checkpoints.add(new CheckPoint(array.getJSONObject(i)));
			}
			res.setCheckpoints(checkpoints);
		} catch (FileNotFoundException e) {

		} finally {
			if (reader != null)
				reader.close();
		}
		return res;
	}

	/* "Единица" хранения. */
	public static class StorageUnit {
		/* Счетчик для именования чекпоинтов. */
		private int mCheckpointCount = 1;
		/* Чекпоинты. */
		private ArrayList<CheckPoint> mCheckpoints = new ArrayList<CheckPoint>();

		public int getCheckpointCount() {
			return mCheckpointCount;
		}

		public void setCheckpointCount(int checkpointCount) {
			mCheckpointCount = checkpointCount;
		}

		public ArrayList<CheckPoint> getCheckpoints() {
			return mCheckpoints;
		}

		public void setCheckpoints(ArrayList<CheckPoint> checkpoints) {
			mCheckpoints = checkpoints;
		}
	}
}
