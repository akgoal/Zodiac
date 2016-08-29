package com.deakishin.zodiac.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

import com.deakishin.zodiac.model.bindingmanager.BindingManager;
import com.deakishin.zodiac.model.framework.FileIO;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

public class Model {
	/*
	 * Синглетон-класс модели зашифрованного сообщения.
	 */

	private static final String TAG = "Model";

	private static final String LETTER_IMAGE_FILENAME = "ZodiacCipher.png";
	private static final String MARKUP_FILENAME = "markup.zdcm";

	/* Контекст приложения. */
	private Context mContext;

	/* Объект, управляющий чтением ресурсов. */
	private FileIO mFileIO;

	/* Оригинальное изображение. */
	private Bitmap mOriginalImage;

	/* Размеченное изображение. */
	private Bitmap mModelImage;

	/* Параметры решетки, накладываемой на исходное изображение. */
	private int mOffsetX, mOffsetY, mCellWidth, mCellHeight, mGridWidth, mGridHeight;

	/* Массив блоков, каждый содержит изображение символа и его id. */
	private Block[][] mBlocks;

	/* Модель символов */
	private SymbModel mSymbModel = new SymbModel();

	/* Объект, управляющий текущей привязкой букв к символам. */
	private BindingManager mBindingManager;

	/* Помощник для поиска. */
	private SearchHelper mSearchHelper;

	private static Model sModel;

	public static Model getInstance(Context context) {
		if (sModel == null) {
			sModel = new Model(context.getApplicationContext());
		}
		return sModel;
	}

	private Model(Context context) {
		mContext = context;
		mBindingManager = new BindingManager(mContext);
		this.mFileIO = new FileIO(context);
		try {
			this.mOriginalImage = mFileIO.loadAssetBitmap(LETTER_IMAGE_FILENAME);
			setModel(mFileIO.loadAsset(MARKUP_FILENAME));
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}

	/* Получение информации и символах. */
	public ArrayList<SymbModel.SymbInfo> getSymbols() {
		return mSymbModel.getSymbInfos();
	}

	/* Получение id символа по координатам в массиве блоков. */
	public int getSymbIdByCoord(int x, int y) {
		if (x < 0 || y < 0) {
			return -1;
		}
		int cellX = x / mCellWidth;
		int cellY = y / mCellHeight;
		if (cellX >= mGridWidth || cellY >= mGridHeight) {
			return -1;
		}
		Block b = mBlocks[cellX][cellY];
		return b.getImgId();
	}

	/* Получение изображения символа по его id. */
	public Bitmap getImageBySymbId(int symbId) {
		return mSymbModel.getSymbImageById(symbId);
	}

	/* Установка модели из потока InputStream. */
	private boolean setModel(InputStream stream) {
		InputStreamReader isr = new InputStreamReader(stream);
		BufferedReader reader = new BufferedReader(isr);
		boolean res = setModel(reader);
		try {
			reader.close();
			isr.close();
			stream.close();
		} catch (IOException ex) {
			Log.e(TAG, ex.getMessage());
		}
		return res;
	}

	/* Установка и настройка модели. */
	private boolean setModel(BufferedReader reader) {
		try {
			readInfoFromFile(reader);
			mSearchHelper = new SearchHelper(mBlocks);
			createImage();
			return true;
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
			return false;
		} finally {
			try {
				if (reader != null) {
					reader.close();

				}
			} catch (IOException ex) {
				Log.e(TAG, ex.getMessage());
			}
		}
	}

	/* Чтение информации из файла. */
	private void readInfoFromFile(BufferedReader br) throws Exception {
		String line = readNonComm(br);
		String[] gridParamsString = line.split(MarkupFileInfo.GRID_PARAMS_DELIMITER);

		mOffsetX = Integer.parseInt(gridParamsString[0]);
		mOffsetY = Integer.parseInt(gridParamsString[1]);
		mCellWidth = Integer.parseInt(gridParamsString[2]);
		mCellHeight = Integer.parseInt(gridParamsString[3]);
		mGridWidth = Integer.parseInt(gridParamsString[4]);
		mGridHeight = Integer.parseInt(gridParamsString[5]);

		line = readNonComm(br);
		String[] markedPositions = line.split(MarkupFileInfo.POSITIONS_DELIMITER);
		if (markedPositions.length != mGridWidth * mGridHeight) {
			throw new IOException("The data is not correct.");
		}

		mBlocks = new Block[mGridWidth][mGridHeight];
		Block block;
		int pos, posX, posY;
		int i = 0;
		for (int y = 0; y < mGridHeight; y++) {
			for (int x = 0; x < mGridWidth; x++, i++) {
				pos = Integer.parseInt(markedPositions[i]);
				block = new Block();
				block.setImgId(pos);
				posY = pos / mGridWidth;
				posX = pos - posY * mGridWidth;
				block.setImage(getSubimage(mOriginalImage, mOffsetX + posX * mCellWidth, mOffsetY + posY * mCellHeight,
						mCellWidth, mCellHeight));
				updateSymbModel(block, x, y);
				mBlocks[x][y] = block;
			}
		}
	}

	/* Получение области изображения. */
	private Bitmap getSubimage(Bitmap image, int x, int y, int w, int h) {
		return Bitmap.createBitmap(image, x, y, w, h);
	}

	/* Обновление модели символов. */
	private void updateSymbModel(Block block, int x, int y) {
		mSymbModel.update(block, x, y);
	}

	/* Чтение следующей строки, не являющейся комментарием. */
	private String readNonComm(BufferedReader br) throws IOException {
		String line = br.readLine();
		if (line == null) {
			return null;
		}
		if (!line.equals(MarkupFileInfo.COMMENT_BEGIN)) {
			return line;
		} else {
			line = br.readLine();
			if (line == null) {
				return null;
			}
			while (!line.equals(MarkupFileInfo.COMMENT_END)) {
				line = br.readLine();
				if (line == null) {
					return null;
				}
			}
			return br.readLine();
		}
	}

	/* Создание изображения модели. */
	private void createImage() {
		mModelImage = Bitmap.createBitmap(mGridWidth * mCellWidth, mGridHeight * mCellHeight,
				mOriginalImage.getConfig());
		Canvas canvas = new Canvas(mModelImage);
		Rect rect;
		for (int y = 0; y < mGridHeight; y++) {
			for (int x = 0; x < mGridWidth; x++) {
				rect = new Rect(x * mCellWidth, y * mCellHeight, x * mCellWidth + mCellWidth,
						y * mCellHeight + mCellHeight);
				canvas.drawBitmap(mBlocks[x][y].getImage(), null, rect, null);
			}
		}
	}

	/* Слушатель прогресса поиска и индикатор отмены поиска. */
	private SearchProgressListener mSearchProgressListener;
	private SearchCancelIndicator mSearchCancelIndicator;

	/*
	 * Поиск слов words в модели. Производится поиск возможных привязок букв к
	 * символам, которые бы обеспечили наличие данных слов. initBinding -
	 * начальная разметка, isHomophonic - разрешается ли использовать одну букву
	 * для разных символов, searchLeft, searchUp, searchRight, searchDown -
	 * определяют направление поиска, listener - слушатель прогресса поиска, 
	 * indicator - индикатор отмены поиска.
	 */
	public ArrayList<Map<Integer, Character>> searchWords(String[] words, Map<Integer, Character> initBinding,
			boolean isHomophonic, boolean searchLeft, boolean searchUp, boolean searchRight, boolean searchDown,
			SearchProgressListener listener, SearchCancelIndicator indicator) {
		if (mSearchHelper != null) {
			mSearchProgressListener = listener;
			mSearchCancelIndicator = indicator;
			ArrayList<SearchHelper.Direction> directions = new ArrayList<SearchHelper.Direction>();
			if (searchLeft) {
				directions.add(SearchHelper.Direction.LEFT);
			}
			if (searchUp) {
				directions.add(SearchHelper.Direction.UP);
			}
			if (searchRight) {
				directions.add(SearchHelper.Direction.RIGHT);
			}
			if (searchDown) {
				directions.add(SearchHelper.Direction.DOWN);
			}
			return mSearchHelper.search(words, initBinding, isHomophonic, directions,
					new SearchHelper.ProgressListener() {
						@Override
						public void onProgressChanged(int value) {
							mSearchProgressListener.onProgressChanged(value);
						}
					}, new SearchHelper.CancelIndicator() {
						@Override
						public boolean isCancelled() {
							return mSearchCancelIndicator.isCancelled();
						}
					});
		} else {
			return null;

		}
	}

	/* Интерфейс, определяющий слушателя изменения прогресса поиска. */
	public static interface SearchProgressListener {
		public void onProgressChanged(int progressValue);
	}

	/* Интерфейс, определяющий индикатор отмены поиска. */
	public static interface SearchCancelIndicator {
		public boolean isCancelled();
	}

	/* Получение минимального значения прогресса выполнения поиска. */
	public static int getMinSearchProgress() {
		return SearchHelper.MIN_PROGRESS;
	}

	/* Получение максимального значения прогресса выполнения поиска. */
	public static int getMaxSearchProgress() {
		return SearchHelper.MAX_PROGRESS;
	}

	public Bitmap getModelImage() {
		return mModelImage;
	}

	public void setModelImage(Bitmap modelImage) {
		mModelImage = modelImage;
	}

	public BindingManager getBindingManager() {
		return mBindingManager;
	}

	public int getCellWidth() {
		return mCellWidth;
	}

	public int getCellHeight() {
		return mCellHeight;
	}

	public Bitmap getOriginalImage() {
		return mOriginalImage;
	}

	public int getOffsetX() {
		return mOffsetX;
	}

	public int getOffsetY() {
		return mOffsetY;
	}
}
