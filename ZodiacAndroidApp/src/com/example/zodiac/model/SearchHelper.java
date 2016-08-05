package com.example.zodiac.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.util.Log;

public class SearchHelper {

	/*
	 * Помощник для поиска слов. Производится поиск возможных привязок букв к
	 * символам, которые бы обеспечили наличие данных слов. Поиск производится
	 * для всех указанных направлений.
	 */

	/* Блоки из модели. */
	private Block[][] blocks;
	/* Параметры массива блоков. */
	private int blocksWidth, blocksHeight, blocksSize;

	/* Направление поиска. */
	public static enum Direction {
		LEFT, UP, RIGHT, DOWN
	}

	/* Максимальное и минимальное значения прогресса поиска. */
	public static final int MAX_PROGRESS = 100;
	public static final int MIN_PROGRESS = 0;

	/* Интерфейс слушателя прогресса поиска. */
	public interface ProgressListener {

		public void onProgressChanged(int value);
	}

	/* Интерфейс индикатора отмены поиска. */
	public interface CancelIndicator {
		public boolean isCancelled();
	}

	/* Слушатель прогресса поиска */
	private ProgressListener progressListener;

	/*
	 * Индикатор отмены поиска. Во время поиска регулярно проверяется его
	 * значение для своевременной остановки.
	 */
	private CancelIndicator cancelIndicator;

	/* Параметры рассчета прогресса поиска. */
	private float progressPerDirection = 0;
	private float progressPerWord = 0;
	private float progressPerSearchResult = 0;
	private float currProgress;
	private int currIntegerProgress;

	public SearchHelper(Block[][] blocks) {
		this.blocks = blocks;
		blocksWidth = blocks.length;
		blocksHeight = blocks[0].length;
		blocksSize = blocksWidth * blocksHeight;
	}

	/*
	 * Поиск слов words по направлениям directions, initBinding - начальная
	 * разметка, isHomophonic - разрешается ли использовать одну букву для
	 * разных символов, progressListener - слушатель прогресса поиска,
	 * cancelIndicator - индикатор отмены поиска.
	 */
	public ArrayList<Map<Integer, Character>> search(String[] words, Map<Integer, Character> initBinding,
			boolean isHomophonic, ArrayList<Direction> directions, ProgressListener progressListener,
			CancelIndicator cancelIndicator) {
		if (words.length == 0) {
			return null;
		}
		this.progressListener = progressListener;
		this.cancelIndicator = cancelIndicator;
		ArrayList<Map<Integer, Character>> res = new ArrayList<Map<Integer, Character>>();
		updateProgress(MIN_PROGRESS);
		if (directions.isEmpty()) {
			progressPerDirection = 0;
		} else {
			progressPerDirection = (MAX_PROGRESS - MIN_PROGRESS) / (float) directions.size();
		}
		int dirNum = 0;
		for (Direction direction : directions) {
			res.addAll(search(words, initBinding, isHomophonic, direction));
			dirNum++;
			updateProgress(progressPerDirection * dirNum);
			if (isCancelled())
				return res;
		}
		updateProgress(MAX_PROGRESS);
		return res;
	}

	/* Обновление прогресса поиска. */
	private void updateProgress(float value) {
		currProgress = value;
		notifyProgressListener();
	}

	/* Увеличение прогресса поиска. */
	private void increaseProgress(float value) {
		currProgress += value;
		notifyProgressListener();
	}

	/*
	 * Оповещение слушателя об изменении прогресса. Оповещение происходит только
	 * в том случае, если изменилось целочисленное представление прогресса.
	 */
	private void notifyProgressListener() {
		if (currIntegerProgress != (int) currProgress) {
			currIntegerProgress = (int) currProgress;
			progressListener.onProgressChanged(currIntegerProgress);
		}
	}

	/*
	 * Поиск слов в массиве блоков, с учетом первоначальной привязки
	 * initBinding, isHomophonic - разрешение на использование одной букв для
	 * разных символов direciton - направление поиска. Результат - список
	 * привязок id символа к букве.
	 */
	private ArrayList<Map<Integer, Character>> search(String[] words, Map<Integer, Character> initBinding,
			boolean isHomophonic, Direction direction) {
		ArrayList<Map<Integer, Character>> res = new ArrayList<Map<Integer, Character>>();

		ArrayList<SearchResult> searchResults = new ArrayList<SearchResult>();
		if (initBinding != null) {
			searchResults.add(new SearchResult(initBinding));
		} else {
			searchResults.add(new SearchResult());
		}

		ArrayList<SearchResult> nextSearchResults = new ArrayList<SearchResult>();
		Set<String> uniqueWords = retrieveUniques(words);
		if (!uniqueWords.isEmpty()) {
			progressPerWord = progressPerDirection / uniqueWords.size();
		}
		float currDirProgress = currProgress;
		int wordNum = 0;

		for (String w : uniqueWords) {
			if (searchResults.isEmpty()) {
				progressPerSearchResult = 0;
			} else {
				progressPerSearchResult = progressPerWord / searchResults.size();
			}
			for (SearchResult sr : searchResults) {
				searchWord(w, sr, isHomophonic, direction, nextSearchResults);
				increaseProgress(progressPerSearchResult);
				if (isCancelled())
					return res;
			}
			searchResults = nextSearchResults;
			nextSearchResults = new ArrayList<SearchResult>();
			wordNum++;
			updateProgress(currDirProgress + progressPerWord * wordNum);
			if (isCancelled())
				return res;
		}

		for (SearchResult sr : searchResults) {
			res.add(sr.getBinding());
		}

		return res;
	}

	/* Отменена ли операция поиска. Проверяется объект индикатора отмены. */
	private boolean isCancelled() {
		if (cancelIndicator == null)
			return false;
		return cancelIndicator.isCancelled();
	}

	/* Множество неповторяющихся слов длиной больше 0 */
	private Set<String> retrieveUniques(String[] words) {
		Set<String> res = new HashSet<String>();
		for (String w : words) {
			if (w.length() > 0) {
				res.add(w);
			}
		}
		return res;
	}

	/*
	 * Поиск слова с необходимостью соответствовать результату searchResult. Для
	 * оптимизации, результаты не возвращаются, а сразу добавляются в список
	 * результатов results.
	 */
	private void searchWord(String word, SearchResult searchResult, boolean isHomophonic, Direction direction,
			ArrayList<SearchResult> results) {
		ArrayList<SearchResult> uniqueResults = new ArrayList<SearchResult>();
		int len = word.length();
		Map<Integer, Character> currBind;
		for (int i = 0; i < blocksSize; i++) {
			if (isCancelled())
				return;

			Log.i("LOL", "RUNNING");

			if (len > blocksSize - i) {
				break;
			}

			currBind = new HashMap<Integer, Character>();
			boolean wordIsChecked = true;
			int j = 0;
			int bId;
			Character chr;
			while (wordIsChecked && j < len) {
				if (searchResult.isPosOccupied(i + j)) {
					wordIsChecked = false;
				}

				chr = word.charAt(j);
				bId = getBlockId(i + j, direction);
				if (wordIsChecked && searchResult.containsBindInfo(bId)) {
					if (!searchResult.getCharacterInBinding(bId).equals(chr)) {
						wordIsChecked = false;
					}
				}

				if (!isHomophonic) {
					if (wordIsChecked && searchResult.containsCharacterBinding(chr)) {
						if (searchResult.containsBindInfo(bId)) {
							if (!searchResult.getCharacterInBinding(bId).equals(chr)) {
								wordIsChecked = false;
							}
						} else {
							wordIsChecked = false;
						}
					}

					if (wordIsChecked && currBind.containsValue(chr)) {
						if (currBind.containsKey(bId)) {
							if (!currBind.get(bId).equals(chr)) {
								wordIsChecked = false;
							}
						} else {
							wordIsChecked = false;
						}
					}
				}

				if (wordIsChecked && currBind.containsKey(bId)) {
					if (!currBind.get(bId).equals(chr)) {
						wordIsChecked = false;
					}
				}

				if (wordIsChecked) {
					currBind.put(bId, chr);
				}
				j++;
			}

			if (wordIsChecked) {
				addToUniqueResults(uniqueResults, new SearchResult(currBind, i, len));
			}
		}

		for (SearchResult sr : uniqueResults) {
			results.add(searchResult.join(sr));
		}
	}

	/*
	 * Добавление результата в список, если в списке еще нет этого результата.
	 */
	private void addToUniqueResults(ArrayList<SearchResult> resList, SearchResult resToAdd) {

		for (SearchResult sr : resList) {
			if (sr.hasSameBinding(resToAdd)) {
				return;
			}
		}

		resList.add(resToAdd);
	}

	private int posX, posY;

	/*
	 * Получение id блока по порядковому номера блока. Порядок зависит от
	 * направления поиска.
	 */
	private int getBlockId(int pos, Direction direction) {
		switch (direction) {
		case LEFT:
			posY = pos / blocksWidth;
			posX = (blocksWidth - 1) - (pos - posY * blocksWidth);
			break;
		case UP:
			posX = pos / blocksHeight;
			posY = (blocksHeight - 1) - (pos - posX * blocksHeight);
			break;
		case RIGHT:
			posY = pos / blocksWidth;
			posX = pos - posY * blocksWidth;
			break;
		case DOWN:
			posX = pos / blocksHeight;
			posY = pos - posX * blocksHeight;
			break;
		}
		return blocks[posX][posY].getImgId();
	}

	/* Класс, определяющий результат поиска. */
	private class SearchResult {

		/* Привязка букв к id символа */
		private Map<Integer, Character> binding = new HashMap<Integer, Character>();
		/*
		 * Список занятых позиций, т.е. позиций, символам в которых уже
		 * поставлена в соответствие буква.
		 */
		private ArrayList<Integer> occupiedPositions = new ArrayList<Integer>();

		public SearchResult() {
		}

		public SearchResult(Map<Integer, Character> binding, ArrayList<Integer> occupiedPositions) {
			this.binding = binding;
			this.occupiedPositions = occupiedPositions;
		}

		public SearchResult(Map<Integer, Character> binding, int firstPos, int length) {
			this.binding = binding;
			for (int i = 0; i < length; i++) {
				this.occupiedPositions.add(firstPos + i);
			}
		}

		public SearchResult(Map<Integer, Character> binding) {
			this.binding = binding;
		}

		public Map<Integer, Character> getBinding() {
			return binding;
		}

		public ArrayList<Integer> getOccupiedPositions() {
			return occupiedPositions;
		}

		public boolean isPosOccupied(int position) {
			return occupiedPositions.contains(position);
		}

		public boolean containsBindInfo(int bId) {
			return binding.containsKey(bId);
		}

		public boolean containsCharacterBinding(Character chr) {
			return binding.containsValue(chr);
		}

		public Character getCharacterInBinding(int bId) {
			return binding.get(bId);
		}

		/*
		 * Объединение с другим результатом. Предполагается, что несоответствий
		 * нет.
		 */
		public SearchResult join(SearchResult sr) {
			Map<Integer, Character> bind = new HashMap<Integer, Character>();
			ArrayList<Integer> occPos = new ArrayList<Integer>();
			bind.putAll(binding);
			occPos.addAll(occupiedPositions);

			Map<Integer, Character> srBind = sr.getBinding();
			for (Integer i : srBind.keySet()) {
				if (!bind.containsKey(i)) {
					bind.put(i, srBind.get(i));
				}
			}
			occPos.addAll(sr.getOccupiedPositions());

			return new SearchResult(bind, occPos);
		}

		public SearchResult join(Map<Integer, Character> binding, int firstPos, int length) {
			Map<Integer, Character> tmpBind = new HashMap<Integer, Character>();
			tmpBind.putAll(binding);
			for (Integer i : binding.keySet()) {
				if (!tmpBind.containsKey(i)) {
					tmpBind.put(i, binding.get(i));
				}
			}
			ArrayList<Integer> tmpOccPos = new ArrayList<Integer>();
			tmpOccPos.addAll(occupiedPositions);
			for (int i = 0; i < length; i++) {
				tmpOccPos.add(firstPos + i);
			}
			return new SearchResult(tmpBind, tmpOccPos);
		}

		public boolean hasSameBinding(SearchResult sr) {
			return binding.equals(sr.getBinding());
		}

	}
}
