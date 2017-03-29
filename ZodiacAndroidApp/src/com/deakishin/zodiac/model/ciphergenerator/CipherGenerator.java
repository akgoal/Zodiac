package com.deakishin.zodiac.model.ciphergenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.deakishin.zodiac.model.ciphermanager.Markup;
import com.deakishin.zodiac.model.ciphermanager.MarkupIOHelper;
import com.deakishin.zodiac.model.ciphermodel.Block;
import com.deakishin.zodiac.model.framework.FileIO;
import com.deakishin.zodiac.model.settings.SettingsPersistent;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;

/**
 * Singleton for generating ciphers. Only Enlgish characters are encrypted.
 * Generated cipher is an instance of {@link GeneratedCipher} that contains
 * cipher's markup (description of symbols), source plaintext, both cipher and
 * its solution images, and difficulty coef. Difficulty is calculated with this
 * formula: diff = symb *
 * min(symb/lett,{@link #MAX_HOMOPHONIC_DIFFICULTY_COEFF}}), where symb - number
 * of unique symbols in the cipher, lett - number of letters in the source
 * plaintext.
 */
public class CipherGenerator {

	private static final String TAG = "CipherGenerator";

	/* Filenames for source image and its markup. */
	private static final String LETTER_IMAGE_FILENAME = "ZodiacCipher.png";
	private static final String MARKUP_FILENAME = "markup.zdcm";

	/* Regular expressions for valid and invalid characters for encryption. */
	private static final String VALID_CHARACTERS_REGULAR_EXPRESSION = "[A-Za-z]+";
	private static final String INVALID_CHARACTER_REGULAR_EXPRESSION = "[^A-Za-z]";

	/**
	 * Maximum difficulty coef for homophonic ciphers, i.e. ciphers that can
	 * have a letter being encrypted with multiple symbols..
	 */
	public static final float MAX_HOMOPHONIC_DIFFICULTY_COEFF = 2;

	/*
	 * Parameters of the grid that is placed on the image to determine symbols'
	 * area.
	 */
	private int mOffsetX, mOffsetY, mCellWidth, mCellHeight, mGridWidth, mGridHeight;

	/* Application context. */
	private Context mContext;

	/* List of symbols for encryption. */
	private ArrayList<CipherSymbol> mSymbols;

	/* Configuration for creating images. */
	private Bitmap.Config mImageConfig;

	private static CipherGenerator sCipherGenerator;

	/**
	 * Provides access to the sole instance of the Singleton.
	 * 
	 * @param context
	 *            Application context.
	 * @return The sole instance of the class.
	 */
	public static CipherGenerator getInstance(Context context) {
		if (sCipherGenerator == null)
			sCipherGenerator = new CipherGenerator(context.getApplicationContext());
		return sCipherGenerator;
	}

	private CipherGenerator(Context context) {
		mContext = context;
		setGenerator();
	}

	/* Set and configure generator. */
	private void setGenerator() {
		FileIO fileIO = FileIO.getInstance(mContext);
		try {
			Bitmap originalImage = fileIO.loadAssetBitmap(LETTER_IMAGE_FILENAME);
			mImageConfig = originalImage.getConfig();

			InputStream stream = fileIO.loadAsset(MARKUP_FILENAME);
			InputStreamReader isr = new InputStreamReader(stream);
			BufferedReader reader = new BufferedReader(isr);
			MarkupIOHelper markupHelper = new MarkupIOHelper();
			try {
				Markup markup = markupHelper.readMarkup(reader, originalImage);
				setGeneratorFromMarkup(markup);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			} finally {
				try {
					reader.close();
					isr.close();
					stream.close();
					if (originalImage != null) {
						originalImage.recycle();
						originalImage = null;
					}
				} catch (Exception ex) {
					Log.e(TAG, ex.getMessage());
				}
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}

	/* Set generator from markup of the original image. */
	private void setGeneratorFromMarkup(Markup markup) {
		mOffsetX = markup.getOffsetX();
		mOffsetY = markup.getOffsetY();
		mCellWidth = markup.getCellWidth();
		mCellHeight = markup.getCellHeight();
		mGridWidth = markup.getGridWidth();
		mGridHeight = markup.getGridHeight();

		mSymbols = new ArrayList<CipherSymbol>();
		Block[][] blocks = markup.getBlocks();
		for (int y = 0; y < mGridHeight; y++) {
			for (int x = 0; x < mGridWidth; x++) {
				addSymbol(blocks[x][y]);
			}
		}
	}

	/* Add a symbol to the list of symbols if there is no such symbols yet. */
	private void addSymbol(Block block) {
		for (CipherSymbol s : mSymbols) {
			if (s.getId() == block.getImgId()) {
				return;
			}
		}
		CipherSymbol s = new CipherSymbol();
		s.setId(block.getImgId());
		s.setImage(block.getImage());
		mSymbols.add(s);
	}

	/**
	 * Indicated if a character is valid and can be encrypted.
	 * 
	 * @param c
	 *            Character to check.
	 * @return True if the character is valid.
	 */
	public static boolean isCharacterValid(Character c) {
		return c.toString().matches(VALID_CHARACTERS_REGULAR_EXPRESSION);
	}

	/**
	 * Indicated if a string is valid and can be encrypted. For a string to be
	 * valid all the characters in it must be valid.
	 * 
	 * @param s
	 *            String to check.
	 * @return True if the string is valid.
	 */
	public static boolean isStringValid(String s) {
		return s.matches(VALID_CHARACTERS_REGULAR_EXPRESSION);
	}

	/**
	 * Constructs valid string from the given one.
	 * 
	 * @param s
	 *            Source string.
	 * @return Valid string that is constructed by erasing all invalid
	 *         characters in the source string.
	 */
	public static String getValidString(String s) {
		return s.replaceAll(INVALID_CHARACTER_REGULAR_EXPRESSION, "");

		/*
		 * char[] chars = s.toCharArray(); StringBuilder sb = new
		 * StringBuilder(); for (char c : chars) { if (isCharacterValid(c))
		 * sb.append(c); } return sb.toString();
		 */
	}

	/**
	 * Generates a cipher.
	 * 
	 * @param plainText
	 *            Source text to encrypt.
	 * @param homophonic
	 *            Indicates if the cipher has to be homophonic. Homophonic
	 *            cipher is a cipher that encrypts a letter with multiple
	 *            symbols.
	 * @return Generated cipher. If plainText is empty or not valid, returns
	 *         null.
	 */
	@SuppressLint("DefaultLocale")
	public GeneratedCipher generateCipher(String plainText, boolean homophonic) {
		if (plainText == null || plainText.equals("") || !plainText.matches(VALID_CHARACTERS_REGULAR_EXPRESSION))
			return null;

		plainText = plainText.toUpperCase();

		GeneratedCipher cipher = new GeneratedCipher();

		int len = plainText.length();

		int gridHeight = (int) Math.ceil(Math.sqrt(len));
		int gridWidth = gridHeight - 1;
		if (gridWidth * gridHeight < len) {
			gridWidth += 1;
		}

		int[] gridParams = { mOffsetX, mOffsetY, mCellWidth, mCellHeight, mGridWidth, mGridHeight, gridWidth,
				gridHeight };

		CipherKey key = generateKey();
		ArrayList<CipherSymbol> cipherSymbs = new ArrayList<CipherSymbol>();
		int[] ids = new int[len];
		char[] letters = plainText.toCharArray();
		for (int i = 0; i < letters.length; i++) {
			CipherSymbol symb = key.getSymbol(letters[i], homophonic);
			cipherSymbs.add(symb);
			ids[i] = symb.getId();
		}

		MarkupIOHelper markupHelper = new MarkupIOHelper();
		cipher.setMarkup(markupHelper.convertToString(gridParams, ids));
		cipher.setPlainText(plainText);

		cipher.setDifficulty(calculateCipherDifficulty(plainText, ids));

		cipher.setImages(generateCipherImage(cipher.getMarkup(), cipher.getPlainText()));

		return cipher;
	}

	/* Generate the cipher key, i.e. letter-symbol mapping. */
	private CipherKey generateKey() {
		return new CipherKey(mSymbols);
	}

	/*
	 * Calculate difficult of a cipher that encrypts plainText. ids is an array
	 * of ids of symbols, each replacing a character in the plainText.
	 */
	private float calculateCipherDifficulty(String plaintext, int[] ids) {
		char[] letters = plaintext.toCharArray();
		ArrayList<Character> uniqueLetters = new ArrayList<Character>();
		for (int i = 0; i < letters.length; i++) {
			char letter = letters[i];
			if (!uniqueLetters.contains(letter)) {
				uniqueLetters.add(letter);
			}
		}
		int uniqueLettersCount = uniqueLetters.size();

		ArrayList<Integer> uniqueSymbIds = new ArrayList<Integer>();
		for (int i = 0; i < ids.length; i++) {
			int id = ids[i];
			if (!uniqueSymbIds.contains(id)) {
				uniqueSymbIds.add(id);
			}
		}
		int uniqueSymbsCount = uniqueSymbIds.size();

		return uniqueSymbsCount
				* Math.min(uniqueSymbsCount / (float) uniqueLettersCount, MAX_HOMOPHONIC_DIFFICULTY_COEFF);
	}

	/**
	 * Generates cipher's image by cipher's markup and solution's image by
	 * cipher's solution line.
	 * 
	 * @param markup
	 *            Cipher's markup, i.e. its String representation.
	 * @param solution
	 *            Solution for the cipher, i.e. the plaintext that was used to
	 *            create the cipher.
	 * @return Two Bitmap images. First one is the cipher's image with
	 *         encrypting symbols, second one is the image of the solution with
	 *         characters from the cipher's source plaintext.
	 */
	public Bitmap[] generateCipherImage(String markup, String solution) {
		Bitmap cipherImage = null;
		Bitmap solutionImage = null;
		try {
			int[][] params = new MarkupIOHelper().convertFromStringToArray(markup);
			int gridWidth = params[0][6];
			int gridHeight = params[0][7];
			int[] ids = params[1];
			int len = ids.length;

			cipherImage = Bitmap.createBitmap(gridWidth * mCellWidth, gridHeight * mCellHeight, mImageConfig);
			Canvas cipherCanvas = new Canvas(cipherImage);
			Canvas solutionCanvas = null;
			if (solution != null) {
				solutionImage = Bitmap.createBitmap(cipherImage.getWidth(), cipherImage.getHeight(),
						cipherImage.getConfig());
				solutionCanvas = new Canvas(solutionImage);
			}
			int i = 0;
			Paint bgPaint = new Paint();
			bgPaint.setColor(Color.WHITE);
			Paint fontPaint = new Paint();
			fontPaint.setColor(SettingsPersistent.getInstance(mContext).getFontColorProfile().getSecondaryColor());
			fontPaint.setTextAlign(Paint.Align.CENTER);
			fontPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
			fontPaint.setTextSize(mCellHeight);
			cipherCanvas.drawRect(0, 0, cipherImage.getWidth(), cipherImage.getHeight(), bgPaint);
			if (solutionCanvas != null)
				solutionCanvas.drawRect(0, 0, solutionImage.getWidth(), solutionImage.getHeight(), bgPaint);
			CipherSymbol cipherSymb;
			for (int y = 0; y < gridHeight; y++) {
				for (int x = 0; x < gridWidth; x++) {
					if (i < len) {
						int currX = x * mCellWidth;
						int currY = y * mCellHeight;
						Rect rect = new Rect(currX, currY, currX + mCellWidth, currY + mCellHeight);
						cipherSymb = null;
						for (CipherSymbol cs : mSymbols)
							if (cs.getId() == ids[i]) {
								cipherSymb = cs;
								break;
							}
						if (cipherSymb == null)
							throw new Exception();

						cipherCanvas.drawBitmap(cipherSymb.getImage(), null, rect, null);

						if (solutionCanvas != null && solution != null) {
							if (i < solution.length()) {
								Character chr = solution.charAt(i);
								int charY = (int) (currY + 0.9 * mCellHeight);
								solutionCanvas.drawText(chr.toString(), currX + mCellWidth / 2, charY, fontPaint);
							}
						}
						i++;
					}
				}
			}
			return new Bitmap[] { cipherImage, solutionImage };
		} catch (Exception e) {
			if (cipherImage != null) {
				cipherImage.recycle();
				cipherImage = null;
			}
			if (solutionImage != null) {
				solutionImage.recycle();
				solutionImage = null;
			}
			return null;
		}

	}

	/** Result of the cipher generating. */
	public static class GeneratedCipher {
		/* Markup, i.e. String representation of the cipher. */
		private String mMarkup;

		/* Source plaintext. */
		private String mPlainText;

		/* Cipher's image with encrypting symbols on it. */
		private Bitmap mImage;

		/* Solution's image with plaintext characters on it. */
		private Bitmap mSolutionImage;

		/* Cipher's difficulty. */
		private float mDifficulty;

		public GeneratedCipher() {
		}

		public String getMarkup() {
			return mMarkup;
		}

		public void setMarkup(String markup) {
			mMarkup = markup;
		}

		public String getPlainText() {
			return mPlainText;
		}

		public void setPlainText(String plainText) {
			mPlainText = plainText;
		}

		public Bitmap getImage() {
			return mImage;
		}

		public void setImage(Bitmap image) {
			mImage = image;
		}

		public Bitmap getSolutionImage() {
			return mSolutionImage;
		}

		public void setSolutionImage(Bitmap solutionImage) {
			mSolutionImage = solutionImage;
		}

		/**
		 * @return Two images - first one is the cipher's image, second one is
		 *         solution image.
		 */
		public Bitmap[] getImages() {
			return new Bitmap[] { mImage, mSolutionImage };
		}

		/**
		 * Sets images.
		 * 
		 * @param images
		 *            Two images - first one is the cipher's image, second one
		 *            is solution image.
		 */
		public void setImages(Bitmap[] images) {
			mImage = images[0];
			mSolutionImage = images[1];
		}

		public float getDifficulty() {
			return mDifficulty;
		}

		public void setDifficulty(float difficulty) {
			mDifficulty = difficulty;
		}

		/** Releases resources. */
		public void recycle() {
			if (mImage != null) {
				mImage.recycle();
				mImage = null;
			}
		}

	}
}
