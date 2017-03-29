package com.deakishin.zodiac.services.boardservice.boardservicegae;

import com.deakishin.zodiac.cipherboardendpoint.model.DTOCipher;
import com.deakishin.zodiac.cipherboardendpoint.model.DTOCipherToAdd;
import com.deakishin.zodiac.cipherboardendpoint.model.DTOSolutionCheckingResult;
import com.deakishin.zodiac.cipherboardendpoint.model.DTOUserAdvancedStats;
import com.deakishin.zodiac.cipherboardendpoint.model.DTOUserStats;
import com.deakishin.zodiac.services.boardservice.BoardCipher;
import com.deakishin.zodiac.services.boardservice.BoardServiceI.SolutionCheckResult;
import com.deakishin.zodiac.services.boardservice.BoardServiceI.UserAdvancedStats;
import com.deakishin.zodiac.services.boardservice.BoardServiceI.UserStats;
import com.deakishin.zodiac.services.userservice.User;

/**
 * Converter that can convert service objects to model objects and model objects
 * to service objects.
 */
public class BoardDtoConverter {

	/**
	 * Converts a cipher in the service to a cipher in the model.
	 * 
	 * @param dto
	 *            Cipher in the service.
	 * @return Cipher in the model.
	 */
	public BoardCipher dtoToCipher(DTOCipher dto) {
		BoardCipher cipher = new BoardCipher();

		cipher.setId(dto.getId());
		cipher.setTitle(dto.getTitle());
		cipher.setDescription(dto.getDescription());
		cipher.setCipherMarkup(dto.getCipherMarkup());

		User author = null;
		if (dto.getAuthorId() != null && !dto.getAuthorId().equals(0L) && dto.getAuthorName() != null)
			author = new User(dto.getAuthorId(), dto.getAuthorName(), dto.getAuthorAvatarMarkup());
		cipher.setAuthor(author);

		if (dto.getSolvedById() != null && !dto.getSolvedById().equals(0L)) {
			User solvedBy = new User(dto.getSolvedById(), dto.getSolvedByName(), dto.getSolvedByAvatarMarkup());
			cipher.setSolvedBy(solvedBy);
		} else {
			cipher.setSolvedBy(null);
		}

		if (dto.getSolvingCount() == null)
			cipher.setSolvingCount(0);
		else
			cipher.setSolvingCount(dto.getSolvingCount());

		cipher.setDifficulty(dto.getDifficulty());

		if (dto.getReward() == null)
			cipher.setReward(0);
		else
			cipher.setReward(dto.getReward());

		if (dto.getSolvedCount() == null)
			cipher.setSolvedCount(0);
		else
			cipher.setSolvedCount(dto.getSolvedCount());
		if (dto.getMaxSolvedCount() == null)
			cipher.setMaxSolvedCount(0);
		else
			cipher.setMaxSolvedCount(dto.getMaxSolvedCount());

		cipher.setCorrectAnswer(dto.getSolution());

		cipher.setTimeAgo(dto.getCreatedMillisecAgo());

		if (dto.getSolvedByUser() == null)
			cipher.setSolvedByUser(false);
		else
			cipher.setSolvedByUser(dto.getSolvedByUser());

		return cipher;
	}

	/**
	 * Converts a cipher in the model that is being added to the board to the a
	 * cipher in the service.
	 * 
	 * @param cipher
	 *            Cipher that is being added to the board.
	 * @return Cipher to add in the service.
	 */
	public DTOCipherToAdd cipherToAddToDto(BoardCipher cipher) {
		DTOCipherToAdd dto = new DTOCipherToAdd();

		dto.setTitle(cipher.getTitle());
		dto.setDescription(cipher.getDescription());
		dto.setCipherMarkup(cipher.getCipherMarkup());
		dto.setSolution(cipher.getCorrectAnswer());
		dto.setDifficulty(cipher.getDifficulty());

		return dto;
	}

	/**
	 * Converts user stats in the service to user stats in the model.
	 * 
	 * @param dto
	 *            User stats in the service.
	 * @return User stats in the model.
	 */
	public UserStats dtoUserStatsToUserStats(DTOUserStats dto) {
		UserStats res = new UserStats();

		User user = new User(dto.getId(), dto.getName(), dto.getAvatarMarkup());
		res.setUser(user);

		res.setPoints(dto.getPoints());

		res.setRank(dto.getRank());
		res.setUserSpecific(dto.getUserSpecific());

		return res;
	}

	/**
	 * Converts user advanced stats in the service to user advanced stats in the
	 * model.
	 * 
	 * @param dto
	 *            User advanced stats in the service.
	 * @return User advanced stats in the model.
	 */
	public UserAdvancedStats dtoAdvancedUserStatsToUserAdvancedStats(DTOUserAdvancedStats dto) {
		UserAdvancedStats stats = new UserAdvancedStats();

		stats.setRank(dto.getRank());
		stats.setPoints(dto.getPoints());
		stats.setCreatedCount(dto.getCreatedCount());
		stats.setSolvedCount(dto.getSolvedCount());

		if (dto.getSolvedFirstCount() != null)
			stats.setSolvedFirstCount(dto.getSolvedFirstCount());
		else
			stats.setSolvedFirstCount(0);

		return stats;
	}

	/**
	 * Converts a solution checking result in the service to a solution checking
	 * result in the model.
	 * 
	 * @param dtoRes
	 *            Solution checking result in the service.
	 * @return Solution checking result in the service.
	 */
	public SolutionCheckResult dtoCheckResultToCheckResult(DTOSolutionCheckingResult dtoRes) {
		SolutionCheckResult res = new SolutionCheckResult();

		if (dtoRes.getReward() != null)
			res.setReward(dtoRes.getReward());
		else
			res.setReward((float) 0);

		res.setCorrect(dtoRes.getCorrect());
		res.setOwnCipher(dtoRes.getOwnCipher());
		res.setAlreadySolved(dtoRes.getAlreadySolved());

		res.setSolveNumber(dtoRes.getSolvedOrdinal());

		return res;
	}
}
