package com.deakishin.zodiac.services.boardservice;

import java.io.IOException;
import java.util.ArrayList;

import com.deakishin.zodiac.services.userservice.User;

/**
 * Interface of a Facade for the board service. The service provides access to
 * ciphers ond users on the board.
 */
public interface BoardServiceI {

	/** Options for sorting ciphers on the board. */
	public enum SortByOption {
		Date, Reward, Popular, Rate
	}

	/** Options to show/hide solved ciphers in the board. */
	public enum ShowSolvedOption {
		SolvedOnly, UnsolvedOnly, All
	}

	/** Default sorting option. */
	public static final SortByOption DEFAULT_SORT_OPTION = SortByOption.Date;

	/** Default option for showing/hiding solved ciphers. */
	public static final ShowSolvedOption DEFAULT_SHOW_SOLVED_OPTION = ShowSolvedOption.All;

	/**
	 * Returns ciphers from the board.
	 * 
	 * @param sortedBy
	 *            Option to sort ciphers.
	 * @param showSolvedOption
	 *            Option to show/hide solved ciphers.
	 * @param pageInfo
	 *            Info about a page to return.
	 * @param len
	 *            Number of ciphers to return.
	 * @param user
	 *            Logged-in user.
	 * @return Result for the query.
	 * @throws IOException
	 *             If failed to access the board.
	 */
	public Result getCiphers(SortByOption sortedBy, ShowSolvedOption showSolvedOption, PageInfo pageInfo, int len,
			User user) throws IOException;

	/**
	 * Searches and returns ciphers on the board.
	 * 
	 * @param query
	 *            Search query.
	 * @param sortedBy
	 *            Option to sort ciphers.
	 * @param showSolvedOption
	 *            Option to show/hide solved ciphers.
	 * @param pageInfo
	 *            Info about a page to return.
	 * @param len
	 *            Number of ciphers to return.
	 * @param user
	 *            Logged-in user.
	 * @return Result for the query.
	 * @throws IOException
	 *             If failed to access the board.
	 */
	public Result searchCiphers(String query, SortByOption sortedBy, ShowSolvedOption showSolvedOption,
			PageInfo pageInfo, int len, User user) throws IOException;

	/**
	 * Adds a cipher to the board.
	 * 
	 * @param cipher
	 *            Cipher to add.
	 * @param user
	 *            User that adds the cipher.
	 * @return True if the cipher is added successfully, false otherwise.
	 * @throws IOException
	 *             If failed to access the board.
	 */
	public boolean addCipher(BoardCipher cipher, User user) throws IOException;

	/**
	 * Adds a like/dislike to a cipher on the board.
	 * 
	 * @param cipher
	 *            Cipher to like.
	 * @param user
	 *            User that leaves a like.
	 * @param toLike
	 *            True to add a like, false to add a dislike.
	 * @throws IOException
	 *             If failed to access the board.
	 */
	public void addLike(BoardCipher cipher, User user, boolean toLike) throws IOException;

	/**
	 * Gets user for given username and password.
	 * 
	 * @param username
	 *            User's username.
	 * @param password
	 *            User's password.
	 * @return User with given name and password on the board.
	 * @throws IOException
	 *             If failed to access the board.
	 */
	public User getUser(String username, String password) throws IOException;

	/**
	 * Increases solving count (i.e. number of solvers) for a given cipher.
	 * 
	 * @param cipher
	 *            Cipher to increase solving count for.
	 * @throws IOException
	 *             If failed to access the board.
	 */
	public void increaseSolvingCount(BoardCipher cipher) throws IOException;

	/**
	 * Adds new user with given username and password to the board.
	 * 
	 * @param username
	 *            New user's username.
	 * @param password
	 *            New user's password.
	 * @return Added user, or null if a user with the same name is already
	 *         exists.
	 * @throws IOException
	 *             If failed to access the board.
	 */
	public User addUser(String username, String password) throws IOException;

	/**
	 * Checks solution for a cipher on the board.
	 * 
	 * @param cipherId
	 *            Id of the cipher to check.
	 * @param solution
	 *            Solution to check.
	 * @param user
	 *            User whose solution is checked.
	 * @return Result of the solution checking.
	 * @throws IOException
	 *             If failed to access the board.
	 */
	public SolutionCheckResult checkSolution(long cipherId, String solution, User user) throws IOException;

	/**
	 * Gets stats of top users from the board ordered by number of points in the
	 * descending order.
	 * 
	 * @param limit
	 *            Number of top users to return.
	 * @param user
	 *            If the user is not in the top, its stats are added to the end
	 *            of the result list.
	 * @return List of stats of top users.
	 * @throws IOException
	 *             If failed to access the board.
	 */
	public ArrayList<UserStats> getTopUsers(int limit, User user) throws IOException;

	/**
	 * Gets advanced stats for the given user.
	 * 
	 * @param user
	 *            User to get stats for.
	 * @return User's advanced stats.
	 * @throws IOException
	 *             If failed to access the board.
	 */
	public UserAdvancedStats getUserStats(User user) throws IOException;

	/**
	 * Changes user's avatar.
	 * 
	 * @param user
	 *            User to change avatar of.
	 * @param newAvatarMarkup
	 *            Markup of the new avatar for the user.
	 * @return True if the avatar changed successfully, false otherwise.
	 * @throws IOException
	 *             If failed to access the board.
	 */
	public boolean changeUserAvatar(User user, String newAvatarMarkup) throws IOException;

	/** Result of the query to the board. */
	public static class Result {
		/* Ciphers in the result. */
		private ArrayList<BoardCipher> mBoardCiphers;
		/*
		 * Indicator that there are no more ciphers left on the board for the
		 * query.
		 */
		private boolean isEnd;

		/* Info about the next page in results. */
		private PageInfo mNextPageInfo;

		public Result() {
		}

		/**
		 * Construct query result.
		 * 
		 * @param boardItems
		 *            cipher in the result.
		 * @param isEnd
		 *            True if no more ciphers left on the board for the query.
		 */
		public Result(ArrayList<BoardCipher> boardItems, boolean isEnd) {
			super();
			mBoardCiphers = boardItems;
			this.isEnd = isEnd;
		}

		/** @return True if there are no cipher, false otherwise. */
		public boolean hasBoardCiphers() {
			return (mBoardCiphers != null && mBoardCiphers.size() > 0);
		}

		/** @return Cipher in the result. */
		public ArrayList<BoardCipher> getBoardCiphers() {
			return mBoardCiphers;
		}

		/**
		 * Sets ciphers to the result.
		 * 
		 * @param boardCipher
		 *            List of ciphers to set.
		 */
		public void setBoardCiphers(ArrayList<BoardCipher> boardCiphers) {
			mBoardCiphers = boardCiphers;
		}

		/**
		 * @return True if there are no more ciphers left for the query, false
		 *         otherwise.
		 */
		public boolean isEnd() {
			return isEnd;
		}

		/**
		 * Sets indicator that there are no more ciphers left for the query.
		 * 
		 * @param isEnd
		 *            Indocator to set.
		 */
		public void setEnd(boolean isEnd) {
			this.isEnd = isEnd;
		}

		/** @return Info about the next page in the query. */
		public PageInfo getNextPageInfo() {
			return mNextPageInfo;
		}

		/**
		 * Sets info about the next page.
		 * 
		 * @param nextPageInfo
		 *            Page info to set.
		 */
		public void setNextPageInfo(PageInfo nextPageInfo) {
			mNextPageInfo = nextPageInfo;
		}
	}

	/** Info about a page in results. */
	public static class PageInfo {
		/* Info in a String. */
		private String mInfo = null;
		/* Index of the first item. */
		private Integer mStart = 0;

		/**
		 * Constructs page info.
		 * 
		 * @param info
		 *            String info about the page.
		 * @param start
		 *            Index of the first item on the page.
		 */
		public PageInfo(String info, Integer start) {
			super();
			mInfo = info;
			mStart = start;
		}

		/** @return String info about the page. */
		public String getInfo() {
			return mInfo;
		}

		/**
		 * Sets String info about the page.
		 * 
		 * @param info
		 *            Info to set.
		 */
		public void setInfo(String info) {
			mInfo = info;
		}

		/** @return Index of the first item on the page. */
		public Integer getStart() {
			return mStart;
		}

		/**
		 * Sets index of the first item on the page.
		 * 
		 * @param start
		 *            Index to set.
		 */
		public void setStart(Integer start) {
			mStart = start;
		}
	}

	/**
	 * Result of the solution checking. Includes following fields and
	 * getters-setters for them: indicator that the solution is correct, number
	 * of users that solved the cipher, indicator that user tried to check
	 * solution for their own cipher, indicator that user tried to check
	 * solution for the cipher they had previously solved, number of points
	 * given to the user if the solution was correct (if the solution was not
	 * correct, then the number of points that can be rewarded to the next
	 * solver).
	 */
	public static class SolutionCheckResult {

		/* Solution is correct. */
		private boolean mCorrect;

		/* Number of users that solved the cipher. */
		private int mSolveNumber;

		/*
		 * User that tried to check the solution, was the one who created the
		 * cipher.
		 */
		private boolean mOwnCipher;
		/*
		 * User that tried to check the solution, had already solved the cipher.
		 */
		private boolean mAlreadySolved;

		/*
		 * Number of points given to the user if the solution was correct. If
		 * the solution was not correct, then it is the number of points that
		 * can be rewarded to the next solver.
		 */
		private Float mReward;

		public SolutionCheckResult() {
		}

		public SolutionCheckResult(boolean correct, int solveNumber, Float reward) {
			this(correct, solveNumber, reward, false, false);
		}

		public SolutionCheckResult(boolean correct, int solveNumber, Float reward, boolean ownCipher,
				boolean alreadySolved) {
			mCorrect = correct;
			mSolveNumber = solveNumber;
			mReward = reward;
			mOwnCipher = ownCipher;
			mAlreadySolved = alreadySolved;
		}

		public boolean isCorrect() {
			return mCorrect;
		}

		public void setCorrect(boolean correct) {
			mCorrect = correct;
		}

		public int getSolveNumber() {
			return mSolveNumber;
		}

		public boolean isOwnCipher() {
			return mOwnCipher;
		}

		public void setOwnCipher(boolean ownCipher) {
			mOwnCipher = ownCipher;
		}

		public boolean isAlreadySolved() {
			return mAlreadySolved;
		}

		public void setAlreadySolved(boolean alreadySolved) {
			mAlreadySolved = alreadySolved;
		}

		public void setSolveNumber(int solveNumber) {
			mSolveNumber = solveNumber;
		}

		public Float getReward() {
			return mReward;
		}

		public void setReward(Float reward) {
			mReward = reward;
		}
	}

	/**
	 * User's stats. Includes following field and getter-setters fot them: user,
	 * rank, number of points, indicator that the user is specific.
	 */
	public static class UserStats {
		private User mUser;
		private int mRank;
		private float mPoints;
		private boolean mUserSpecific = false;

		public UserStats() {
		}

		public User getUser() {
			return mUser;
		}

		public void setUser(User user) {
			mUser = user;
		}

		public int getRank() {
			return mRank;
		}

		public void setRank(int rank) {
			mRank = rank;
		}

		public float getPoints() {
			return mPoints;
		}

		public void setPoints(float points) {
			mPoints = points;
		}

		public boolean isUserSpecific() {
			return mUserSpecific;
		}

		public void setUserSpecific(boolean userSpecific) {
			mUserSpecific = userSpecific;
		}
	}

	/**
	 * User's advanced stats. Includes following fields and getter-setters for
	 * them: rank, number of points, number of created ciphers, number of solved
	 * ciphers, number of ciphers that the user was first to solve.
	 */
	public static class UserAdvancedStats {
		private int mRank;
		private float mPoints;
		private int mCreatedCount;
		private int mSolvedCount;
		private int mSolvedFirstCount;

		public UserAdvancedStats() {
		}

		public int getRank() {
			return mRank;
		}

		public void setRank(int rank) {
			mRank = rank;
		}

		public float getPoints() {
			return mPoints;
		}

		public void setPoints(float points) {
			mPoints = points;
		}

		public int getCreatedCount() {
			return mCreatedCount;
		}

		public void setCreatedCount(int createdCount) {
			this.mCreatedCount = createdCount;
		}

		public int getSolvedCount() {
			return mSolvedCount;
		}

		public void setSolvedCount(int solvedCount) {
			this.mSolvedCount = solvedCount;
		}

		public int getSolvedFirstCount() {
			return mSolvedFirstCount;
		}

		public void setSolvedFirstCount(int solvedFirstCount) {
			mSolvedFirstCount = solvedFirstCount;
		}
	}
}
