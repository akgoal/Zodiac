package com.example.zodiac.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BindingManager {
	/*
	 * Класс для управления привязкой символов к буквам. Хранит текущую
	 * привязку. Также для хранения результатов поиска.
	 */

	/* Текущая привязка. */
	private Map<Integer, Character> mBinding;

	/* Запомненная привязка. */
	private Map<Integer, Character> mRememberedBinding;

	/* Привязки, соответствующие результатам поиска. */
	private ArrayList<Map<Integer, Character>> mSearchResults;
	/* Текущий индекс в результатах поиска. */
	private int mCurrSearchIndex;

	/* Строка, по которой производился поиск. */
	private String mSearchLine;
	/* Предварительная строка, нужна, если результаты не пришли. */
	private String mTmpSearchLine;

	public BindingManager() {
		mBinding = new HashMap<Integer, Character>();
	}

	/* Обновление привязки для конкретного символа. */
	public void updateBinging(Integer id, Character chr) {
		if (chr == null) {
			if (mBinding.containsKey(id))
				mBinding.remove(id);
			return;
		}
		mBinding.put(id, chr);
	}

	/* Получение привязанной к символу буквы. */
	public Character getCharacter(int id) {
		if (mBinding.containsKey(id))
			return mBinding.get(id);
		return null;
	}

	/* Запоминание текущей привязки. */
	public void rememberBinding() {
		mRememberedBinding = new HashMap<Integer, Character>();
		for (Integer i : mBinding.keySet())
			mRememberedBinding.put(i, mBinding.get(i));
	}

	/* Возврат к запомненной привязке. */
	public void retrieveBinding() {
		mBinding = new HashMap<Integer, Character>();
		for (Integer i : mRememberedBinding.keySet())
			mBinding.put(i, mRememberedBinding.get(i));
	}

	/* Очистить текущую привязку. */
	public void clearBinding() {
		mBinding.clear();
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

	public void setBinding(Map<Integer, Character> binding) {
		mBinding = binding;
	}

	/* Установка привязки через копирование карты, а не ссылки. */
	public void setBindingAsCopy(Map<Integer, Character> binding) {
		mBinding = new HashMap<Integer, Character>(binding);
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
}
