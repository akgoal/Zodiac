package com.deakishin.zodiac.services.boardservice.boardservicegae;

import java.io.IOException;
import java.util.ArrayList;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.cipherboardendpoint.Cipherboardendpoint;
import com.deakishin.zodiac.cipherboardendpoint.model.CollectionResponseDTOCipher;
import com.deakishin.zodiac.cipherboardendpoint.model.CollectionResponseDTOUserStats;
import com.deakishin.zodiac.cipherboardendpoint.model.DTOCipher;
import com.deakishin.zodiac.cipherboardendpoint.model.DTOCipherToAdd;
import com.deakishin.zodiac.cipherboardendpoint.model.DTOSolutionCheckingResult;
import com.deakishin.zodiac.cipherboardendpoint.model.DTOUser;
import com.deakishin.zodiac.cipherboardendpoint.model.DTOUserAdvancedStats;
import com.deakishin.zodiac.cipherboardendpoint.model.DTOUserStats;
import com.deakishin.zodiac.services.boardservice.BoardCipher;
import com.deakishin.zodiac.services.boardservice.BoardServiceI;
import com.deakishin.zodiac.services.userservice.User;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson2.JacksonFactory;

import android.content.Context;
import android.os.SystemClock;

/**
 * Board service implementation that uses Google App Engine. It is assumed that
 * its methods are called in an separate thread.
 */
public class BoardServiceGAE implements BoardServiceI {

	/* Endpoint to work with backend. */
	private Cipherboardendpoint mCipherBoardEndpoint;

	/* Object that converts model objects and service objects to each other. */
	private BoardDtoConverter mConverter;

	/* Application context. */
	private Context mContext;

	public BoardServiceGAE(Context context) {
		mContext = context.getApplicationContext();
		mConverter = new BoardDtoConverter();

		Cipherboardendpoint.Builder cipherBoardEndpointBuilder = new Cipherboardendpoint.Builder(
				AndroidHttp.newCompatibleTransport(), new JacksonFactory(), new HttpRequestInitializer() {
					public void initialize(HttpRequest httpRequest) {
					}
				});
		mCipherBoardEndpoint = CloudEndpointUtils.updateBuilder(cipherBoardEndpointBuilder)
				.setApplicationName(mContext.getString(R.string.app_name)).build();
	}

	@Override
	public Result getCiphers(SortByOption sortedBy, ShowSolvedOption showSolvedOption, PageInfo pageInfo, int len,
			User user) throws IOException {
		return getOrSearchElements(sortedBy, showSolvedOption, pageInfo, len, user, null);
	}

	/* Get all cipher or search cipher if query!=null. */
	private Result getOrSearchElements(SortByOption sortedBy, ShowSolvedOption showSolvedOption, PageInfo pageInfo,
			int len, User user, String query) throws IOException {
		Integer sortByCode = null;
		switch (sortedBy) {
		case Date:
			sortByCode = 0;
			break;
		case Rate:
			sortByCode = 1;
			break;
		case Popular:
			sortByCode = 2;
			break;
		case Reward:
			sortByCode = 4;
			break;
		}

		Integer filterCode = null;
		switch (showSolvedOption) {
		case SolvedOnly:
			filterCode = 0;
			break;
		case UnsolvedOnly:
			filterCode = 1;
			break;
		case All:
			filterCode = 2;
			break;
		}

		Long userId = null;
		if (user != null)
			userId = user.getId();

		String pageToken = pageInfo == null ? null : pageInfo.getInfo();
		Integer first = pageInfo == null ? null : pageInfo.getStart();

		CollectionResponseDTOCipher dtoCiphers = mCipherBoardEndpoint.getCiphers().setFirst(first).setLimit(len)
				.setSortbyCode(sortByCode).setSolvedFilterCode(filterCode).setUserId(userId).setSearchString(query)
				.setCursor(pageToken).execute();
		Result res = new Result();
		ArrayList<BoardCipher> resList = new ArrayList<BoardCipher>();
		if (dtoCiphers != null && dtoCiphers.getItems() != null && !dtoCiphers.getItems().isEmpty()) {
			for (DTOCipher dto : dtoCiphers.getItems()) {
				if (dto == null) {
					continue;
				}
				resList.add(mConverter.dtoToCipher(dto));
			}
			if (dtoCiphers.getItems().size() < len)
				res.setEnd(true);
			else
				res.setEnd(false);
		} else {
			res.setEnd(true);
		}
		res.setBoardCiphers(resList);
		res.setNextPageInfo(new PageInfo(dtoCiphers == null ? null : dtoCiphers.getNextPageToken(),
				first == null ? len : first + len));
		return res;
	}

	@Override
	public User getUser(String username, String password) throws IOException {
		DTOUser dtoUser = mCipherBoardEndpoint.getUser(username, password).execute();
		if (dtoUser.getSuccess() == false)
			return null;
		return new User(dtoUser.getId(), dtoUser.getName(), dtoUser.getAvatarMarkup());
	}

	@Override
	public boolean addCipher(BoardCipher cipher, User user) throws IOException {
		if (user == null)
			throw new IOException("Null user");
		DTOCipherToAdd cipherToAdd = mCipherBoardEndpoint.addCipher(user.getId(), mConverter.cipherToAddToDto(cipher))
				.execute();
		return cipherToAdd != null;
	}

	@Override
	public void addLike(BoardCipher cipher, User user, boolean toLike) throws IOException {
		if (user == null)
			throw new IOException("Null user");
		if (cipher == null)
			throw new IOException("Null cipher");
		mCipherBoardEndpoint.addLike(cipher.getId(), user.getId(), toLike).execute();
	}

	@Override
	public Result searchCiphers(String query, SortByOption sortedBy, ShowSolvedOption showSolvedOption,
			PageInfo pageInfo, int len, User user) throws IOException {
		return getOrSearchElements(sortedBy, showSolvedOption, pageInfo, len, user, query);
	}

	@Override
	public SolutionCheckResult checkSolution(long cipherId, String solution, User user) throws IOException {
		if (user == null)
			throw new IOException("Null user");
		SystemClock.sleep(500);
		DTOSolutionCheckingResult dtoRes = mCipherBoardEndpoint.checkSolution(cipherId, user.getId(), solution)
				.execute();
		return mConverter.dtoCheckResultToCheckResult(dtoRes);
	}

	@Override
	public User addUser(String username, String password) throws IOException {
		DTOUser dtoUser = mCipherBoardEndpoint.addUser(username, password).execute();
		if (dtoUser.getSuccess() == false)
			return null;
		return new User(dtoUser.getId(), dtoUser.getName(), dtoUser.getAvatarMarkup());
	}

	@Override
	public void increaseSolvingCount(BoardCipher cipher) throws IOException {
		if (cipher == null)
			throw new IOException("Null cipher");
		mCipherBoardEndpoint.increaseSolvingCount(cipher.getId()).execute();
	}

	@Override
	public ArrayList<UserStats> getTopUsers(int limit, User user) throws IOException {
		Long userId = null;
		if (user != null) {
			userId = user.getId();
		}

		CollectionResponseDTOUserStats dtoUserStats = mCipherBoardEndpoint.getTopUsers(limit).setUserId(userId)
				.execute();
		ArrayList<UserStats> resList = new ArrayList<UserStats>();
		if (dtoUserStats != null && dtoUserStats.getItems() != null && !dtoUserStats.getItems().isEmpty()) {
			for (DTOUserStats dto : dtoUserStats.getItems()) {
				if (dto == null) {
					continue;
				}
				resList.add(mConverter.dtoUserStatsToUserStats(dto));
			}
		}
		return resList;
	}

	@Override
	public UserAdvancedStats getUserStats(User user) throws IOException {
		if (user == null || user.getId() == null)
			throw new IOException("Unable to load user advanced stats. Null user");

		DTOUserAdvancedStats dto = mCipherBoardEndpoint.getUserStats(user.getId()).execute();
		return mConverter.dtoAdvancedUserStatsToUserAdvancedStats(dto);
	}

	@Override
	public boolean changeUserAvatar(User user, String newAvatarMarkup) throws IOException {
		DTOUser dto = mCipherBoardEndpoint.changeUserAvatarMarkup(user.getId()).setAvatarMarkup(newAvatarMarkup)
				.execute();
		return dto != null;
	}
}
