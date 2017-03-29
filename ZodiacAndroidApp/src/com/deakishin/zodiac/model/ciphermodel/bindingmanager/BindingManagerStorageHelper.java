package com.deakishin.zodiac.model.ciphermodel.bindingmanager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.deakishin.zodiac.model.framework.FileIO;

import android.content.Context;

/**
 * Helper class for saving and loading Binding manager data (checkpoints,
 * current and remembered bindings).
 */
public class BindingManagerStorageHelper {

	/* Name of the file to store data in, */
	private static final String CHECKPOINTS_FILENAME = "checkpoints.json";

	/* Key for storing number of checkpoints. */
	private static final String JSON_CHECKPOINT_COUNT = "count";

	/* Keys for storing info about current and remembers bindings. */
	private static final String JSON_CURRENT_BINDING = "currentBinding";
	private static final String JSON_REMEMBERED_BINDING = "rememberedBinding";
	private static final String JSON_IS_CHECKPOINT = "isCheckpoint";

	/* Name of the folder where the file is located. */
	private String mFolderName;

	/* Application context. */
	private Context mContext;

	/* Object for reading from and writing to the file. */
	private FileIO mFileIO;

	/**
	 * Contructs helper.
	 * 
	 * @param context
	 *            Application context.
	 * @param checkpointsFolderName
	 *            Name of the folder where the file with data is located.
	 */
	public BindingManagerStorageHelper(Context context, String checkpointsFolderName) {
		mContext = context;
		mFileIO = FileIO.getInstance(mContext);
		mFolderName = checkpointsFolderName;
	}

	/**
	 * Saves data to the file.
	 * 
	 * @param storageUnit
	 *            Data to save.
	 * @throws JSONException
	 *             If unable to construct JSON object to save data.
	 * @throws IOException
	 *             If unable to write to file.
	 */
	public void saveCheckpoints(StorageUnit storageUnit) throws JSONException, IOException {
		ArrayList<CheckPoint> checkpoints = storageUnit.getCheckpoints();
		int checkpointCount = storageUnit.getCheckpointCount();
		JSONArray array = new JSONArray();

		JSONObject headerJSON = new JSONObject();
		headerJSON.put(JSON_CHECKPOINT_COUNT, checkpointCount);

		headerJSON.put(JSON_IS_CHECKPOINT, storageUnit.isCheckpoint());
		if (storageUnit.getСurrBinding() != null) {
			CheckPoint currBind = new CheckPoint(storageUnit.getСurrBinding(), JSON_CURRENT_BINDING);
			headerJSON.put(JSON_CURRENT_BINDING, currBind.toJSON());
		}
		if (storageUnit.getRememberedBinding() != null) {
			CheckPoint rememberedBind = new CheckPoint(storageUnit.getRememberedBinding(), JSON_REMEMBERED_BINDING);
			headerJSON.put(JSON_REMEMBERED_BINDING, rememberedBind.toJSON());
		}

		array.put(headerJSON);

		for (CheckPoint checkpoint : checkpoints)
			array.put(checkpoint.toJSON());

		Writer writer = null;
		try {
			OutputStream out;
			if (mFolderName == null || mFolderName.equals(""))
				out = mFileIO.writeExtFile(CHECKPOINTS_FILENAME);
			else
				out = mFileIO.writeExtCiphersFile(mFolderName, CHECKPOINTS_FILENAME);
			writer = new OutputStreamWriter(out);
			writer.write(array.toString());
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	/**
	 * Loads data from the file.
	 * 
	 * @return Loaded data.
	 * @throws IOException
	 *             If unable to read from the file.
	 * @throws JSONException
	 *             If data in the file has incorrect format.
	 */
	public StorageUnit loadCheckpoints() throws IOException, JSONException {
		StorageUnit res = new StorageUnit();
		BufferedReader reader = null;
		try {
			InputStream in;
			if (mFolderName == null || mFolderName.equals(""))
				in = mFileIO.readExtFile(CHECKPOINTS_FILENAME);
			else
				in = mFileIO.readExtCiphersFile(mFolderName, CHECKPOINTS_FILENAME);
			reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder jsonString = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				jsonString.append(line);
			}
			JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();
			ArrayList<CheckPoint> checkpoints = new ArrayList<CheckPoint>();
			int checkpointCount = 1;

			boolean isCheckpoint = false;
			Map<Integer, Character> currBind = null;
			Map<Integer, Character> rememberedBind = null;

			if (array.length() > 0) {
				// checkpointCount =
				// array.getJSONObject(0).getInt(JSON_CHECKPOINT_COUNT);
				JSONObject headerData = array.getJSONObject(0);
				checkpointCount = headerData.getInt(JSON_CHECKPOINT_COUNT);
				if (headerData.has(JSON_IS_CHECKPOINT)) {
					isCheckpoint = headerData.getBoolean(JSON_IS_CHECKPOINT);
				}
				if (headerData.has(JSON_CURRENT_BINDING)) {
					currBind = new CheckPoint(headerData.getJSONObject(JSON_CURRENT_BINDING)).getBinding();
				}
				if (headerData.has(JSON_REMEMBERED_BINDING)) {
					rememberedBind = new CheckPoint(headerData.getJSONObject(JSON_REMEMBERED_BINDING)).getBinding();
				}
			}
			res.setCheckpointCount(checkpointCount);
			res.setCheckpoint(isCheckpoint);
			res.setСurrBinding(currBind);
			res.setRememberedBinding(rememberedBind);

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

	/**
	 * Encapsulation of the data that's being saved/loaded. This data includes a
	 * counter number for checkpoints naming, checkpoints themselves, current
	 * and remembered bindings and a flag indicating whether current binding is
	 * a checkpoint or not.
	 */
	public static class StorageUnit {
		/* Counter naming checkpoints naming. */
		private int mCheckpointCount = 1;
		/* Checkpoints. */
		private ArrayList<CheckPoint> mCheckpoints = new ArrayList<CheckPoint>();

		/* Current and remembered bindings. */
		private Map<Integer, Character> mСurrBinding;
		private Map<Integer, Character> mRememberedBinding;
		/* Flag indicating that current binding is a checkpoint. */
		private boolean mCheckpoint;

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

		public Map<Integer, Character> getСurrBinding() {
			return mСurrBinding;
		}

		public void setСurrBinding(Map<Integer, Character> сurrBinding) {
			mСurrBinding = сurrBinding;
		}

		public Map<Integer, Character> getRememberedBinding() {
			return mRememberedBinding;
		}

		public void setRememberedBinding(Map<Integer, Character> rememberedBinding) {
			mRememberedBinding = rememberedBinding;
		}

		public boolean isCheckpoint() {
			return mCheckpoint;
		}

		public void setCheckpoint(boolean isCheckpoint) {
			mCheckpoint = isCheckpoint;
		}
	}
}
