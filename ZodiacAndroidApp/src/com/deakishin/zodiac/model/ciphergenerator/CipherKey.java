package com.deakishin.zodiac.model.ciphergenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.util.Log;

/**
 * Cipher's key, i.e. mapping between characters and symbols and encrypt them.
 */
class CipherKey {

	private Map<Character, ArrayList<CipherSymbol>> mKey;

	private Random mRandom;

	/**
	 * Constructs key.
	 * 
	 * @param symbols
	 *            List of symbols that can encrypt letters.
	 */
	public CipherKey(ArrayList<CipherSymbol> symbols) {
		mKey = new HashMap<Character, ArrayList<CipherSymbol>>();
		LetterInfo[] letterInfos = Frequences.getInfo(symbols.size());

		mRandom = new Random();
		ArrayList<CipherSymbol> symbolsCopy = new ArrayList<CipherSymbol>(symbols);
		for (LetterInfo li : letterInfos) {
			ArrayList<CipherSymbol> symbsForLetter = new ArrayList<CipherSymbol>();
			for (int j = 0; j < li.getNumber(); j++) {
				int i = mRandom.nextInt(symbolsCopy.size());
				CipherSymbol symb = symbolsCopy.get(i);
				symbolsCopy.remove(i);
				symbsForLetter.add(symb);
			}
			mKey.put(li.getLetter(), symbsForLetter);
		}
	}

	/**
	 * Returns a symbols for the given letter from the key. If a cipher is
	 * homophonic then each letter is mapped to a list of symbols. In this case
	 * a random symbol from this list is returned.
	 * 
	 * @param letter
	 *            Character to return symbol for.
	 * @param homophonic
	 *            Indicates if encryption is homophonic.
	 * @return Symbol that can encrypt the letter.
	 */
	public CipherSymbol getSymbol(char letter, boolean homophonic) {
		ArrayList<CipherSymbol> symbols = mKey.get(letter);
		if (homophonic) {
			return symbols.get(mRandom.nextInt(symbols.size()));
		} else {
			return symbols.get(0);
		}
	}

	/**
	 * Frequencies of the letters (English characters). This determines how many
	 * symbols can encrypt a character if enryption is homophonic. Letters are
	 * stored are returned in the alphabetical order.
	 */
	private static class Frequences {

		/* Default number of encrypting symbols. */
		private static final int DEF_SYMBOLS_NUM = 62;

		/* Frequencies in percentage. */
		private static final LetterInfo[] LETTER_INFOS_PERCENTAGE = { new LetterInfo('A', 8.167),
				new LetterInfo('B', 1.492), new LetterInfo('C', 2.782), new LetterInfo('D', 4.253),
				new LetterInfo('E', 12.702), new LetterInfo('F', 2.228), new LetterInfo('G', 2.015),
				new LetterInfo('H', 6.094), new LetterInfo('I', 6.966), new LetterInfo('J', 0.153),
				new LetterInfo('K', 0.772), new LetterInfo('L', 4.025), new LetterInfo('M', 2.406),
				new LetterInfo('N', 6.749), new LetterInfo('O', 7.507), new LetterInfo('P', 1.929),
				new LetterInfo('Q', 0.095), new LetterInfo('R', 5.987), new LetterInfo('S', 6.327),
				new LetterInfo('T', 9.056), new LetterInfo('U', 2.758), new LetterInfo('V', 0.978),
				new LetterInfo('W', 2.360), new LetterInfo('X', 0.150), new LetterInfo('Y', 1.974),
				new LetterInfo('Z', 0.074) };

		/* Frequencies when number of symbols is default. */
		private static final LetterInfo[] DEF_LETTER_INFOS = { new LetterInfo('A', 5, 0), new LetterInfo('B', 1, 0),
				new LetterInfo('C', 2, 0), new LetterInfo('D', 2, 0), new LetterInfo('E', 8, 0),
				new LetterInfo('F', 1, 0), new LetterInfo('G', 1, 0), new LetterInfo('H', 4, 0),
				new LetterInfo('I', 4, 0), new LetterInfo('J', 1, 0), new LetterInfo('K', 1, 0),
				new LetterInfo('L', 2, 0), new LetterInfo('M', 1, 0), new LetterInfo('N', 4, 0),
				new LetterInfo('O', 4, 0), new LetterInfo('P', 1, 0), new LetterInfo('Q', 1, 0),
				new LetterInfo('R', 4, 0), new LetterInfo('S', 4, 0), new LetterInfo('T', 5, 0),
				new LetterInfo('U', 1, 0), new LetterInfo('V', 1, 0), new LetterInfo('W', 1, 0),
				new LetterInfo('X', 1, 0), new LetterInfo('Y', 1, 0), new LetterInfo('Z', 1, 0) };

		/**
		 * Returns information about letters and how many symbols can encrypt
		 * each letter.
		 * 
		 * @param symbNum
		 *            Number of encrypting symbols.
		 * @return Letters and their info in the alphabetical order.
		 */
		public static LetterInfo[] getInfo(int symbNum) {

			if (symbNum == DEF_SYMBOLS_NUM)
				return DEF_LETTER_INFOS;

			LetterInfo[] infos = new LetterInfo[LETTER_INFOS_PERCENTAGE.length];

			for (int i = 0; i < LETTER_INFOS_PERCENTAGE.length; i++) {
				LetterInfo li = LETTER_INFOS_PERCENTAGE[i];
				int num = (int) (symbNum * li.getPercentage() / 100 + 0.29);
				if (num < 1) {
					infos[i] = new LetterInfo(li.getLetter(), 1, 0);
				} else
					infos[i] = new LetterInfo(li.getLetter(), num, 0);
			}

			return infos;
		}

	}

	/**
	 * Info about a letter. Contains the letter itself, number of symbols that
	 * encrypts it and percentage of this number relative to the entire
	 * alphabet.
	 */
	private static class LetterInfo {
		private char mLetter;
		private int mSymbNumber;
		private double mPercentage;

		public LetterInfo(char letter, int number, double percentage) {
			mLetter = letter;
			mSymbNumber = number;
			mPercentage = percentage;
		}

		public LetterInfo(char letter, double percentage) {
			mLetter = letter;
			mPercentage = percentage;
		}

		public char getLetter() {
			return mLetter;
		}

		public int getNumber() {
			return mSymbNumber;
		}

		public double getPercentage() {
			return mPercentage;
		}
	}
}
