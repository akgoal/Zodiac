package com.deakishin.zodiac.services.boardservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import com.deakishin.zodiac.model.avatargenerator.AvatarGenerator;
import com.deakishin.zodiac.model.ciphergenerator.CipherGenerator;
import com.deakishin.zodiac.services.userservice.User;

import android.content.Context;
import android.os.SystemClock;

/** Board service implementation for debugging. */
public class BoardServiceDummy implements BoardServiceI {

	/* Ciphers are created dynamically.. */
	private ArrayList<BoardCipher> mData;

	/* Cipher generator. */
	private CipherGenerator mCipherGenerator;

	/* Avatar generator. */
	private AvatarGenerator mAvatarGenerator;

	private static BoardServiceDummy sService;

	/**
	 * @param context
	 *            Application context.
	 * @return The sole intance of the debugging implementation of the service.
	 */
	public static BoardServiceDummy getInstance(Context context) {
		if (sService == null)
			sService = new BoardServiceDummy(context.getApplicationContext());
		return sService;
	}

	private BoardServiceDummy(Context context) {
		mCipherGenerator = CipherGenerator.getInstance(context);
		mAvatarGenerator = AvatarGenerator.getInstance(context);

		mData = new ArrayList<BoardCipher>();
		BoardCipher cipher;
		Random rand = new Random();
		Calendar calendar = Calendar.getInstance();
		for (int i = 1; i < 33; i++) {
			cipher = new BoardCipher();
			cipher.setId((long) i);
			cipher.setTitle("Cipher #" + i);
			cipher.setAuthor(new User((long) i, "Author #" + i, mAvatarGenerator.generateRandomProfile().toMarkup()));

			if (rand.nextFloat() > 0.5)
				cipher.getAuthor().setAvatarMarkup(null);

			if (rand.nextFloat() > 0.3)
				calendar.set(Calendar.HOUR_OF_DAY, 1 + rand.nextInt(24));
			if (rand.nextFloat() > 0.3)
				calendar.set(Calendar.DAY_OF_MONTH, 1 + rand.nextInt(18));
			if (rand.nextFloat() > 0.3)
				calendar.set(Calendar.MINUTE, 1 + rand.nextInt(50));
			calendar.set(Calendar.SECOND, rand.nextInt(60));
			cipher.setDate(calendar.getTime());

			cipher.setTimeAgo(new Date().getTime() - cipher.getDate().getTime());

			String plainText = "testcipher";
			CipherGenerator.GeneratedCipher cipherInfo = mCipherGenerator.generateCipher(plainText, false);
			cipher.setCipherMarkup(cipherInfo.getMarkup());
			cipherInfo.recycle();
			cipherInfo = null;

			if (rand.nextFloat() > 0.7) {
				cipher.setSolvedBy(new User((long) i, "SmartAss #" + i));
				cipher.setCorrectAnswer(plainText.toUpperCase());
			} else {
				cipher.setSolvedBy(null);
				cipher.setCorrectAnswer(null);
			}

			cipher.setLikedByUser((rand.nextFloat() > 0.6));

			cipher.setRate(rand.nextInt(10) + (cipher.isLikedByUser() ? 1 : 0));

			int dif = rand.nextInt(200) + 1;
			cipher.setDifficulty(dif + rand.nextFloat());
			cipher.setReward(rand.nextInt(dif) + rand.nextFloat());
			if (rand.nextFloat() > 0.7)
				cipher.setReward(0);
			if (cipher.getAuthor().getId().equals(5L)) {
				cipher.setDifficulty(300);
			}
			cipher.setSolvingCount(rand.nextInt(40));

			cipher.setMaxSolvedCount(rand.nextInt(502));
			cipher.setSolvedCount(rand.nextInt(912));

			if (i % 5 == 0)
				cipher.setDescription(cipher.getCipherMarkup());
			else if (i % 5 == 1)
				cipher.setDescription(null);
			else if (i % 5 == 2)
				cipher.setDescription("");
			else if (i % 5 == 3)
				cipher.setDescription(cipher.getTitle() + "\n" + cipher.getAuthor());
			else if (i % 5 == 4) {
				StringBuilder sb = new StringBuilder();
				for (int j = 0; j < 500; j++)
					sb.append(Integer.toString(j).charAt(0));
				cipher.setDescription(sb.toString());
			}

			if (rand.nextFloat() > 0.6)
				cipher.setSolvedByUser(true);
			else
				cipher.setSolvedByUser(false);

			mData.add(cipher);
		}
	}

	/* Compare two items. */
	private boolean compare(BoardCipher item1, BoardCipher item2, SortByOption sortedBy) {
		switch (sortedBy) {
		case Date:
			return item1.getDate().getTime() > item2.getDate().getTime();
		case Rate:
			return item1.getRate() > item2.getRate();
		case Reward:
			return item1.getDifficulty() > item2.getDifficulty();
		default:
			return false;
		}
	}

	/* Sort list of ciphers in the descending order. */
	private ArrayList<BoardCipher> sort(ArrayList<BoardCipher> list, SortByOption sortedBy) {
		ArrayList<BoardCipher> sorted = new ArrayList<BoardCipher>();
		for (BoardCipher bc : list) {
			int i = 0;
			int len = sorted.size();
			for (BoardCipher sortedBc : sorted) {
				if (compare(bc, sortedBc, sortedBy)) {
					sorted.add(i, bc);
					break;
				}
				i++;
			}
			if (i == len)
				sorted.add(bc);
		}
		return sorted;
	}

	@Override
	public Result getCiphers(SortByOption sortedBy, ShowSolvedOption showSolvedOption, PageInfo pageInfo, int len,
			User user) throws IOException {
		int first = 0;
		if (pageInfo != null)
			first = pageInfo.getStart();
		if (pageInfo != null && pageInfo.getInfo() != null)
			first = Integer.parseInt(pageInfo.getInfo());

		ArrayList<BoardCipher> resCiphers = new ArrayList<BoardCipher>();
		for (BoardCipher cipher : mData) {
			boolean toAdd;
			switch (showSolvedOption) {
			case SolvedOnly:
				toAdd = cipher.isSolved();
				break;
			case UnsolvedOnly:
				toAdd = !cipher.isSolved();
				break;
			default:
				toAdd = true;
				break;
			}
			if (toAdd) {
				resCiphers.add(cipher);
			}
		}

		resCiphers = sort(resCiphers, sortedBy);

		boolean isEnd;
		if (len < 1)
			return null;
		if (first >= resCiphers.size())
			return new Result(null, true);

		isEnd = first + len >= resCiphers.size() - 1;
		int end = (first + len) >= resCiphers.size() ? resCiphers.size() : (first + len);

		ArrayList<BoardCipher> resList = new ArrayList<BoardCipher>();
		for (int i = first; i < end; i++)
			resList.add(resCiphers.get(i));

		SystemClock.sleep(2500);
		float f = new Random().nextFloat();
		if (f > 0.005)
			return new Result(resList, true);
		if (f > 1.3)
			return new Result(new ArrayList<BoardCipher>(), true);

		throw new IOException();
	}

	int logInCount = 0;

	@Override
	public User getUser(String username, String password) throws IOException {
		SystemClock.sleep(2500);

		if (username.equals("error"))
			throw new IOException();
		if (username.equals("fail"))
			return null;
		if (username.equals("good") || username.length() > 2)
			return new User(5L, username, mAvatarGenerator.generateRandomProfile().toMarkup());

		logInCount++;
		if (logInCount % 3 == 0)
			return null;
		if (logInCount % 3 == 1)
			return new User(new Date().getTime(), username);
		throw new IOException();
	}

	@Override
	public boolean addCipher(BoardCipher cipher, User user) throws IOException {
		SystemClock.sleep(2500);
		if (cipher.getTitle().equals("Ok")) {
			cipher.setDate(new Date());
			cipher.setRate(20);
			cipher.setAuthor(user);
			mData.add(cipher);
			return true;
		}
		return false;
	}

	@Override
	public void addLike(BoardCipher cipher, User user, boolean toLike) throws IOException {

	}

	@Override
	public Result searchCiphers(String query, SortByOption sortedBy, ShowSolvedOption showSolvedOption,
			PageInfo pageInfo, int len, User user) throws IOException {
		SystemClock.sleep(1500);

		ArrayList<BoardCipher> res = new ArrayList<BoardCipher>();
		for (BoardCipher bc : mData)
			if (bc.getTitle().contains(query))
				res.add(bc);

		return new Result(res, true);
	}

	private int count = 0;

	@Override
	public SolutionCheckResult checkSolution(long cipherId, String solution, User user) throws IOException {
		SystemClock.sleep(2500);

		count++;
		if (count % 10 == 0)
			return new SolutionCheckResult(true, 2, (float) 42);
		if (count % 10 == 1)
			return new SolutionCheckResult(true, 11, (float) 42);
		if (count % 10 == 2)
			return new SolutionCheckResult(true, 63, (float) 42);
		if (count % 10 == 3)
			return new SolutionCheckResult(true, 20, (float) 42);
		if (count % 10 == 4)
			return new SolutionCheckResult(true, 1, (float) 0);
		if (count % 10 == 5)
			return new SolutionCheckResult(false, 20, (float) 0);
		if (count % 10 == 6)
			return new SolutionCheckResult(false, 0, (float) 21);
		if (count % 10 == 7)
			return new SolutionCheckResult(true, -1, (float) 78, true, false);
		if (count % 10 == 8)
			return new SolutionCheckResult(true, -2, (float) 33, false, true);
		throw new IOException();
	}

	@Override
	public User addUser(String username, String password) throws IOException {
		if (username.equals("taken"))
			return null;
		return getUser(username, password);
	}

	@Override
	public void increaseSolvingCount(BoardCipher cipher) throws IOException {
	}

	@Override
	public ArrayList<UserStats> getTopUsers(int limit, User user) throws IOException {
		ArrayList<UserStats> res = new ArrayList<UserStats>();

		Random rand = new Random();
		for (int i = 0; i < limit; i++) {
			UserStats stats = new UserStats();
			User statsUser = new User((long) i, "Smart ass #" + (i + 1));
			stats.setUser(statsUser);
			stats.setRank(i + 1);

			stats.setPoints(rand.nextInt(1000000) + rand.nextFloat());

			res.add(stats);
		}

		if (user != null) {
			UserStats stats = new UserStats();
			stats.setUser(user);
			int rank = rand.nextInt(1000000) + limit + 1;
			stats.setRank(rank);
			stats.setPoints(1000 * rand.nextInt(1000000) + rand.nextFloat());
			stats.setUserSpecific(true);
			res.add(stats);
		}

		SystemClock.sleep(2500);
		return res;
	}

	@Override
	public UserAdvancedStats getUserStats(User user) throws IOException {
		UserAdvancedStats stats = new UserAdvancedStats();
		Random rand = new Random();
		stats.setRank(rand.nextInt(100) + 1);
		stats.setPoints(rand.nextInt(1000) + rand.nextFloat());
		stats.setCreatedCount(rand.nextInt(20));
		stats.setSolvedCount(rand.nextInt(30));
		stats.setSolvedFirstCount(rand.nextInt(20));

		SystemClock.sleep(2500);
		if (user.getName().length() < 4)
			throw new IOException();
		return stats;
	}

	@Override
	public boolean changeUserAvatar(User user, String newAvatarMarkup) throws IOException {
		SystemClock.sleep(2500);
		if (user.getName().length() < 4)
			throw new IOException();
		return true;
	}

}
