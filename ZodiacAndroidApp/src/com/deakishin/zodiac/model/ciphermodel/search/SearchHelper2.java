package com.deakishin.zodiac.model.ciphermodel.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.deakishin.zodiac.model.ciphermodel.Block;

import android.annotation.SuppressLint;
import android.util.SparseArray;

/**
 * Search helper that uses {@link SparseArray} objects in its implementation of
 * the search. Therefore this implementation is optimized for Android devices.
 */
public class SearchHelper2 extends SearchHelper {

	/**
	 * Constructs search helper for a specific cipher.
	 * 
	 * @param blocks
	 *            Two-dimensional array of blocks that represent the cipher.
	 *            Each symbol in the cipher is represented by a block that
	 *            contains info about this symbols.
	 */
	public SearchHelper2(Block[][] blocks) {
		super(blocks);
	}

	@Override
	protected ArrayList<Map<Integer, Character>> search(String[] words, Map<Integer, Character> initBinding,
			boolean isHomophonic, ArrayList<Direction> directions) {
		ArrayList<SparseArray<Character>> res = new ArrayList<SparseArray<Character>>();

		SparseArray<Character> initBindingAsSparseArray = new SparseArray<Character>();
		if (initBinding != null) {
			for (Integer key : initBinding.keySet()) {
				initBindingAsSparseArray.put(key, initBinding.get(key));
			}
		}

		updateProgress(MIN_PROGRESS);
		if (directions.isEmpty()) {
			progressPerDirection = 0;
		} else {
			progressPerDirection = (MAX_PROGRESS - MIN_PROGRESS) / (float) directions.size();
		}
		int dirNum = 0;
		for (Direction direction : directions) {
			res.addAll(search(words, initBindingAsSparseArray, isHomophonic, direction));
			dirNum++;
			if (isCancelled())
				return new ArrayList<Map<Integer, Character>>();
			updateProgress(progressPerDirection * dirNum);
		}
		updateProgress(MAX_PROGRESS);

		return convertToMapArrayList(res);
	}

	/* Convert a list of SparseArray-s to a list of Maps. */
	@SuppressLint("UseSparseArrays")
	private ArrayList<Map<Integer, Character>> convertToMapArrayList(ArrayList<SparseArray<Character>> arrays) {
		ArrayList<Map<Integer, Character>> res = new ArrayList<Map<Integer, Character>>();
		Map<Integer, Character> map;
		for (SparseArray<Character> arr : arrays) {
			map = new HashMap<Integer, Character>();
			for (int i = 0; i < arr.size(); i++)
				map.put(arr.keyAt(i), arr.valueAt(i));
			res.add(map);
		}
		return res;
	}

	/* Search words in a specific direction. */
	private ArrayList<SparseArray<Character>> search(String[] words, SparseArray<Character> initBinding,
			boolean isHomophonic, Direction direction) {
		ArrayList<SparseArray<Character>> res = new ArrayList<SparseArray<Character>>();

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

	/* Get a set of unique non-empty words from an array of words. */
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
	 * Search words with the obligation to satisfy and not contradict existing
	 * search result. Search results are getting not returned but added to
	 * "results" param.
	 */
	private void searchWord(String word, SearchResult searchResult, boolean isHomophonic, Direction direction,
			ArrayList<SearchResult> results) {
		ArrayList<SearchResult> uniqueResults = new ArrayList<SearchResult>();
		int len = word.length();
		SparseArray<Character> currBind;
		for (int i = 0; i < blocksSize; i++) {
			if (isCancelled())
				return;

			if (len > blocksSize - i) {
				break;
			}

			currBind = new SparseArray<Character>();
			boolean wordIsChecked = true;
			int j = 0;
			int bId;
			Character chr;
			while (wordIsChecked && j < len) {
				if (isCancelled())
					return;

				if (searchResult.isPosOccupied(i + j)) {
					wordIsChecked = false;
				}

				chr = word.charAt(j);
				bId = getBlockId(i + j, direction);

				if (bId == Block.EMPTY_BLOCK.getImgId())
					wordIsChecked = false;

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

					if (wordIsChecked && currBind.indexOfValue(chr) >= 0) {
						if (currBind.get(bId) == null || !currBind.get(bId).equals(chr)) {
							wordIsChecked = false;
						}
					}
				}

				if (wordIsChecked && currBind.get(bId) != null) {
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

	/* Add a result to the list of result if there is no such result already. */
	private void addToUniqueResults(ArrayList<SearchResult> resList, SearchResult resToAdd) {

		for (SearchResult sr : resList) {
			if (sr.hasSameBinding(resToAdd)) {
				return;
			}
		}

		resList.add(resToAdd);
	}

	/**
	 * Search result. Contains binding (i.e. mapping between symbols' ids and
	 * letters) as well as a list of occupied positions - positions that contain
	 * found word(s) and that therefore cannot partake in any following searches
	 * to find new words.
	 */
	private class SearchResult {

		/* Binding - mapping between symbols' ids and letters. */
		private SparseArray<Character> binding = new SparseArray<Character>();

		/*
		 * List of positions that contain found word(s) and therefore cannot
		 * partake in any following searches.
		 */
		private ArrayList<Integer> occupiedPositions = new ArrayList<Integer>();

		public SearchResult() {
		}

		/**
		 * Constructs a search result.
		 * 
		 * @param binding
		 *            Mapping between symbols' ids and letters.
		 * @param occupiedPositions
		 *            List of positions that contain found word(s) and therefore
		 *            cannot partake in any following searches.
		 */
		public SearchResult(SparseArray<Character> binding, ArrayList<Integer> occupiedPositions) {
			this(binding);
			this.occupiedPositions = occupiedPositions;
		}

		/**
		 * Constructs a search result.
		 * 
		 * @param binding
		 *            Mapping between symbols' ids and letters.
		 * @param firstPos
		 *            First position in the the occupied positions.
		 * @param length
		 *            Number of positions in the the occupied positions.
		 */
		public SearchResult(SparseArray<Character> binding, int firstPos, int length) {
			this(binding);
			for (int i = 0; i < length; i++) {
				this.occupiedPositions.add(firstPos + i);
			}
		}

		/**
		 * Constructs a search result.
		 * 
		 * @param binding
		 *            Mapping between symbols' ids and letters.
		 */
		public SearchResult(SparseArray<Character> binding) {
			this.binding = binding.clone();
		}

		/** @return Binding of the result. */
		public SparseArray<Character> getBinding() {
			return binding;
		}

		/**
		 * @return List of occupied positions - positions that cannot partake in
		 *         any following searches.
		 */
		public ArrayList<Integer> getOccupiedPositions() {
			return occupiedPositions;
		}

		/**
		 * Indicates that the position is occupied and therefore cannot partake
		 * in any following searches.
		 * 
		 * @param position
		 *            Position to check.
		 * @return True if the position is occupied, false otherwise.
		 */
		public boolean isPosOccupied(int position) {
			return occupiedPositions.contains(position);
		}

		/**
		 * Indicates that a symbols with given id is mapped to a letter.
		 * 
		 * @param bId
		 *            Symbol's id.
		 * @return True if there is a mapping for given id, false otherwise.
		 */
		public boolean containsBindInfo(int bId) {
			return binding.get(bId) != null;
		}

		/**
		 * Indicates that there is a symbol mapped to the given letter.
		 * 
		 * @param chr
		 *            Letter to check.
		 * @return True if there is a mapping for given letter, false otherwise.
		 */
		public boolean containsCharacterBinding(Character chr) {
			return binding.indexOfValue(chr) >= 0;
		}

		/**
		 * Returns a letter that is mapped to a symbol with the given id.
		 * 
		 * @param bId
		 *            Symbol's id.
		 * @return Letter mapped to the symbol.
		 */
		public Character getCharacterInBinding(int bId) {
			return binding.get(bId);
		}

		/**
		 * Combining with another search result. It is expected that there are
		 * no contradictions between combined search results, i.e. there is no
		 * such symbol that is mapped to one letter in one result and a
		 * different letter in the other result.
		 * 
		 * @param sr
		 *            Search result to compine with.
		 * @return New search result that is a compination of this result and
		 *         the result that was passed as an argument.
		 */
		public SearchResult join(SearchResult sr) {
			SparseArray<Character> bind = binding.clone();
			ArrayList<Integer> occPos = new ArrayList<Integer>();
			occPos.addAll(occupiedPositions);

			SparseArray<Character> srBind = sr.getBinding();
			for (int i = 0; i < srBind.size(); i++) {
				int key = srBind.keyAt(i);
				if (bind.get(key) == null) {
					bind.put(key, srBind.get(key));
				}
			}
			occPos.addAll(sr.getOccupiedPositions());

			return new SearchResult(bind, occPos);
		}

		/**
		 * Checks whether the search result has the same binding as the given
		 * result.
		 * 
		 * @param sr
		 *            Search result to compare with.
		 * @return True if binding is the same in both results, false otherwise.
		 */
		public boolean hasSameBinding(SearchResult sr) {
			SparseArray<Character> srBind = sr.getBinding();
			if (srBind == null || binding == null || srBind.size() != binding.size())
				return false;
			for (int i = 0; i < srBind.size(); i++) {
				int key = srBind.keyAt(i);
				if (binding.get(key) == null || !binding.get(key).equals(srBind.get(key)))
					return false;
			}

			return true;
		}

	}
}
