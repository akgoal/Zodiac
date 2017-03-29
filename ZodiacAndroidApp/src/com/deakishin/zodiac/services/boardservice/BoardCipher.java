package com.deakishin.zodiac.services.boardservice;

import java.util.Date;

import com.deakishin.zodiac.services.userservice.User;

/**
 * Cipher on the board. Contains following cipher's fields and getters-setters
 * for them: id, author, title, description, number of milliseconds passed since
 * cipher's creation, cipher's markup, indicator that the cipher has been liked
 * by current user, the date it was created, its rating (number of likes), user
 * that first solved the cipher, number of users solving it, cipher's solution -
 * correct answer (if it is available), cipher's difficulty and reward (number
 * of points) for solving it, maximum number of users that can solve the cipher
 * and get reward for it (if this number is not reached then the solution is not
 * provided), number of users that solved the cipher, indicator that the cipher
 * was solved by current user.
 */
public class BoardCipher {

	private Long mId;
	private User mAuthor;
	private String mTitle;
	private String mDescription;
	private long mTimeAgo;
	private String mCipherMarkup;
	private boolean mLikedByUser;
	private Date mDate;
	private int mRate;
	private User mSolvedBy;
	private int mSolvingCount;
	private String mCorrectAnswer;
	private float mDifficulty;
	private float mReward;

	private int mMaxSolvedCount;
	private int mSolvedCount;

	private boolean mSolvedByUser;

	public BoardCipher() {
	}

	public void changeLikedByUser() {
		mLikedByUser = !mLikedByUser;
	}

	/**
	 * Increases/decreases cipher's rate by one.
	 * 
	 * @param increase
	 *            True to increase rate, false to decrease rate.
	 */
	public void changeRate(boolean increase) {
		if (increase) {
			mRate++;
		} else if (mRate != 0)
			mRate--;
	}

	public Long getId() {
		return mId;
	}

	public void setId(Long id) {
		mId = id;
	}

	public User getAuthor() {
		return mAuthor;
	}

	public void setAuthor(User author) {
		mAuthor = author;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public String getCipherMarkup() {
		return mCipherMarkup;
	}

	public void setCipherMarkup(String cipherMarkup) {
		mCipherMarkup = cipherMarkup;
	}

	public boolean isLikedByUser() {
		return mLikedByUser;
	}

	public void setLikedByUser(boolean likedByUser) {
		mLikedByUser = likedByUser;
	}

	public User getSolvedBy() {
		return mSolvedBy;
	}

	public boolean hasAnswer() {
		return mCorrectAnswer != null;
	}

	/**
	 * @return True if the cipher is already solved and contains its correct
	 *         solution.
	 */
	public boolean isSolved() {
		return mSolvedBy != null && mCorrectAnswer != null;
	}

	public void setSolvedBy(User solvedBy) {
		mSolvedBy = solvedBy;
	}

	public int getRate() {
		return mRate;
	}

	public void setRate(int rate) {
		mRate = rate;
	}

	public String getCorrectAnswer() {
		return mCorrectAnswer;
	}

	public void setCorrectAnswer(String correctAnswer) {
		mCorrectAnswer = correctAnswer;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		mDescription = description;
	}

	public long getTimeAgo() {
		return mTimeAgo;
	}

	public void setTimeAgo(long timeAgo) {
		mTimeAgo = timeAgo;
	}

	public Date getDate() {
		return mDate;
	}

	public void setDate(Date date) {
		mDate = date;
	}

	public int getSolvingCount() {
		return mSolvingCount;
	}

	/**
	 * Increases solving count, i.e. number of users that are currently solving
	 * the cipher.
	 */
	public void increaseSolvingCount() {
		mSolvingCount++;
	}

	public void setSolvingCount(int solvingCount) {
		mSolvingCount = solvingCount;
	}

	public float getDifficulty() {
		return mDifficulty;
	}

	public void setDifficulty(float difficulty) {
		mDifficulty = difficulty;
	}

	public float getReward() {
		return mReward;
	}

	public void setReward(float reward) {
		mReward = reward;
	}

	public int getMaxSolvedCount() {
		return mMaxSolvedCount;
	}

	public void setMaxSolvedCount(int maxSolvedCount) {
		mMaxSolvedCount = maxSolvedCount;
	}

	public int getSolvedCount() {
		return mSolvedCount;
	}

	public void setSolvedCount(int solvedCount) {
		mSolvedCount = solvedCount;
	}

	/**
	 * @return True if maximum number of users have already solved the cipher.
	 */
	public boolean isSolvedMax() {
		return mSolvedCount >= mMaxSolvedCount;
	}

	/** @return True if the cipher has a reward in points for solving it. */
	public boolean hasReward() {
		return mReward > 0;
	}

	public boolean isSolvedByUser() {
		return mSolvedByUser;
	}

	public void setSolvedByUser(boolean solvedByUser) {
		mSolvedByUser = solvedByUser;
	}
}
