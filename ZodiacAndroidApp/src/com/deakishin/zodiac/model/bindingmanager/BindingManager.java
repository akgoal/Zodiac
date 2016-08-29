package com.deakishin.zodiac.model.bindingmanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import android.content.Context;
import android.util.Log;

public class BindingManager {
	/*
	 * Класс для управления привязкой символов к буквам. Хранит текущую
	 * привязку. Также для хранения результатов поиска.
	 */

	/* Текущая привязка. */
	private Map<Integer, Character> mBinding = new HashMap<Integer, Character>();

	/* Запомненная привязка. Последний чекпоинт. */
	private Map<Integer, Character> mRememberedBinding;

	/* Стек чекпоинтов - запомненных привязок с названиями. */
	private ArrayList<CheckPoint> mCheckPoints = new ArrayList<CheckPoint>();

	/* Привязки, соответствующие результатам поиска. */
	private ArrayList<Map<Integer, Character>> mSearchResults;
	/* Текущий индекс в результатах поиска. */
	private int mCurrSearchIndex;

	/* Строка, по которой производился поиск. */
	private String mSearchLine;
	/* Предварительная строка, нужна, если результаты не пришли. */
	private String mTmpSearchLine;

	/* Контекст приложения. */
	private Context mContext;

	/* Помощник для сохранения/загрузки чекпоинтов. */
	private CheckpointStorageHelper mCheckpointStorageHelper;

	/* Счетчик чекпоинтов для их именования. */
	private int mCheckpointCount = 1;

	/*
	 * Является ли текущая привязка чекпоинтом. Точнее, не было ли изменений
	 * после последнего чекпоинта.
	 */
	private boolean mBindingIsCheckpoint = false;

	/*
	 * Слушатель изменения статуса привязки с точки зрения ее "чекпоинтности".
	 */
	private OnBindingStatusChangedListener mOnBindingStatusChangedListener;

	/* Слушатель доступности возможности возврата к последнему чекпоинту. */
	private RevertAvailabilityListener mRevertAvailabilityListener;

	public BindingManager(Context context) {
		mContext = context;
		mCheckpointStorageHelper = new CheckpointStorageHelper(mContext);

		loadCheckpoints();
	}

	/* Обновление привязки для конкретного символа. */
	public void updateBinding(Integer id, Character chr) {
		if (chr == null) {
			if (mBinding.containsKey(id)) {
				mBinding.remove(id);
				setBindingIsCheckpoint(false);
			}
			return;
		}
		if (mBinding.containsKey(id)) {
			if (!mBinding.get(id).equals(chr)) {
				setBindingIsCheckpoint(false);
			}
		} else {
			setBindingIsCheckpoint(false);
		}
		mBinding.put(id, chr);
	}

	/* Получение привязанной к символу буквы. */
	public Character getCharacter(int id) {
		if (mBinding.containsKey(id))
			return mBinding.get(id);
		return null;
	}

	/* Запомнить заданную привязку. */
	private void rememberBinding(Map<Integer, Character> binding) {
		mRememberedBinding = new HashMap<Integer, Character>(binding);
		setBindingIsCheckpoint(true);

		if (mRevertAvailabilityListener != null)
			mRevertAvailabilityListener.onRevertAvailable();
	}

	/* Возврат к запомненной привязке. */
	private void retrieveRememberedBinding() {
		if (mRememberedBinding == null)
			return;

		mBinding = new HashMap<Integer, Character>(mRememberedBinding);
		setBindingIsCheckpoint(true);
	}

	/* Очистить текущую привязку. */
	public void clearBinding() {
		if (!mBinding.isEmpty()) {
			mBinding.clear();
			setBindingIsCheckpoint(false);
		}
	}

	public Map<Integer, Character> getBinding() {
		return mBinding;
	}

	public Map<Integer, Character> getRememberedBinding() {
		return mRememberedBinding;
	}

	public boolean hasRememberedBinding() {
		return mRememberedBinding != null;
	}

	/* Установка привязки через копирование карты, а не ссылки. */
	public void setBindingAsCopy(Map<Integer, Character> binding) {
		mBinding = new HashMap<Integer, Character>(binding);
		setBindingIsCheckpoint(false);
	}

	/* Имеется ли информация о результатах поиска. */
	public boolean hasSearchInfo() {
		return mSearchResults != null;
	}

	/* Есть ли результаты поиска. (Дал ли поиск результаты.) */
	public boolean hasSearchResults() {
		if (mSearchResults == null)
			return false;
		return !mSearchResults.isEmpty();
	}

	/*
	 * Установка результатов поиска. Сопровождается сбрасыванием текущего
	 * индекса в результатах поиска и запоминанием строки, по которой проводился
	 * поиск.
	 */
	public void setSearchResults(ArrayList<Map<Integer, Character>> searchResults) {
		mSearchResults = searchResults;
		mCurrSearchIndex = 0;
		mSearchLine = mTmpSearchLine;
	}

	/* Удаление результатов поиска. */
	public void deleteSearchResults() {
		mSearchResults = null;
		mSearchLine = null;
	}

	/* Получение количества результатов поиска. */
	public int getSearchResSize() {
		if (mSearchResults != null)
			return mSearchResults.size();
		return 0;
	}

	/* Переход текущей привязки к первому результату поиска. */
	public void goToFirstSearchRes() {
		mCurrSearchIndex = 0;
		if (mSearchResults != null && !mSearchResults.isEmpty())
			setBindingAsCopy(mSearchResults.get(0));
	}

	/* Переход текущей привязки к предыдущему результату поиска. */
	public void goToPrevSearchRes() {
		if (!isFirstRes()) {
			mCurrSearchIndex--;
			if (mSearchResults != null && !mSearchResults.isEmpty())
				setBindingAsCopy(mSearchResults.get(mCurrSearchIndex));
		}
	}

	/* Переход текущей привязки к следующему результату поиска. */
	public void goToNextSearchRes() {
		if (!isLastRes()) {
			mCurrSearchIndex++;
			setBindingAsCopy(mSearchResults.get(mCurrSearchIndex));
		}
	}

	/* Является ли текущий результат поиска первым результатом. */
	public boolean isFirstRes() {
		return mCurrSearchIndex == 0;
	}

	/* Является ли текущий результат поиска последним результатом. */
	public boolean isLastRes() {
		if (mSearchResults == null || mSearchResults.isEmpty())
			return true;
		if (mCurrSearchIndex >= mSearchResults.size() - 1)
			return true;
		return false;
	}

	public int getCurrSearchIndex() {
		return mCurrSearchIndex;
	}

	/*
	 * Установка слов, по которым проводится поиск. Слова заносятся в
	 * предварительную строку поиска. Когда поиск закончится, строка перестанет
	 * быть предварительной и станет основной.
	 */
	public void setTmpSearchWords(String[] words) {
		if (words.length < 1)
			return;
		StringBuilder sb = new StringBuilder();
		int i;
		for (i = 0; i < words.length - 1; i++)
			sb.append(words[i] + " ");
		sb.append(words[i]);
		mTmpSearchLine = sb.toString();
	}

	public String getSearchLine() {
		if (mSearchLine == null)
			return "";
		return mSearchLine;
	}

	/* Дальше описаны методы, связанные с чекпоинтами. */
	/* Получение чекпоинтов, от старых к новым. */
	public ArrayList<CheckPoint> getCheckPoints() {
		return mCheckPoints;
	}

	/* Количество чекпоинтов. */
	public int getCheckpointsSize() {
		return mCheckPoints.size();
	}

	/* Добавление текущей привязки к чекпоинтам. */
	public void addCurrToCheckPoints(String title) {
		if (mBindingIsCheckpoint)
			return;

		mCheckpointCount++;
		mCheckPoints.add(0, new CheckPoint(mBinding, title));
		rememberBinding(mBinding);
	}
	
	/* Получение счетчика чекпоинтов. Нужен для именования. */
	public int getCheckpointCount(){
		return mCheckpointCount;
	}

	/* Установка текущей привязки из чекпоинта. */
	public void setCurrBindingFromCheckPoint(CheckPoint checkPoint) {
		setBindingAsCopy(checkPoint.getBinding());
		rememberBinding(checkPoint.getBinding());
	}

	/* Удаление чекпоинта. */
	public void removeCheckPoint(CheckPoint checkPoint) {
		mCheckPoints.remove(checkPoint);
	}

	/* Откат к последнему чекпоинту. */
	public void revertToCheckPoint() {
		retrieveRememberedBinding();
	}

	/* Сохранение чекпоинтов в файл. */
	public void saveCheckpoints() {
		try {
			CheckpointStorageHelper.StorageUnit storageUnit = new CheckpointStorageHelper.StorageUnit();
			storageUnit.setCheckpoints(mCheckPoints);
			storageUnit.setCheckpointCount(mCheckpointCount);
			mCheckpointStorageHelper.saveCheckpoints(storageUnit);
		} catch (JSONException e) {
			Log.e("BindingManager", "Failed to save checkpoints " + e);
		} catch (IOException e) {
			Log.e("BindingManager", "Failed to save checkpoints " + e);
		}
	}

	/* Загрузка чекпоинтов из файла. */
	public void loadCheckpoints() {
		try {
			CheckpointStorageHelper.StorageUnit storageUnit = mCheckpointStorageHelper.loadCheckpoints();
			mCheckPoints = storageUnit.getCheckpoints();
			mCheckpointCount = storageUnit.getCheckpointCount();
		} catch (JSONException e) {
			Log.e("BindingManager", "Failed to load checkpoints: " + e);
			mCheckPoints = new ArrayList<CheckPoint>();
		} catch (IOException e) {
			Log.e("BindingManager", "Failed to load checkpoints: " + e);
			mCheckPoints = new ArrayList<CheckPoint>();
		}
	}

	/* Интерфейс реагирования на окончание загрузки чекпоинтов. */
	public static interface CheckpointsLoadCallback {
		public void action();
	}

	public boolean isCurrBindingCheckpoint() {
		return mBindingIsCheckpoint;
	}

	private void setBindingIsCheckpoint(boolean bindingIsCheckpoint) {
		mBindingIsCheckpoint = bindingIsCheckpoint;
		if (mOnBindingStatusChangedListener != null)
			mOnBindingStatusChangedListener.onBindingStatusChanged(mBindingIsCheckpoint);
	}

	/* Возможноть возврата к последнему чепоинту. */
	public boolean isRevertAvailable() {
		return mRememberedBinding != null;
	}

	/*
	 * Интерфейс слушателя изменения статуса текущей привязки. Статус - является
	 * ли привязка еще не измененным чекпоинтом.
	 */
	public static interface OnBindingStatusChangedListener {
		public void onBindingStatusChanged(boolean isCheckpoint);
	}
	
	public void setOnBindingStatusChangedListener(OnBindingStatusChangedListener onBindingStatusChangedListener) {
		mOnBindingStatusChangedListener = onBindingStatusChangedListener;
	}

	/*
	 * Интерфейс слушателя активации возможности возврата к последнему
	 * чекпоинту.
	 */
	public static interface RevertAvailabilityListener {
		public void onRevertAvailable();
	}

	public void setRevertAvailabilityListener(RevertAvailabilityListener revertAvailabilityListener) {
		mRevertAvailabilityListener = revertAvailabilityListener;
	}
}
