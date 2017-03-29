package com.deakishin.zodiac.controller.boardscreen;

import java.io.IOException;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.AnimatedExpandableListView;
import com.deakishin.zodiac.controller.boardscreen.userstatsscreen.UserStatsActivity;
import com.deakishin.zodiac.model.ciphermanager.CipherInfo;
import com.deakishin.zodiac.model.ciphermanager.CipherManager;
import com.deakishin.zodiac.model.settings.SettingsPersistent;
import com.deakishin.zodiac.services.boardservice.BoardCipher;
import com.deakishin.zodiac.services.boardservice.BoardServiceI;
import com.deakishin.zodiac.services.boardservice.BoardServiceImpl;
import com.deakishin.zodiac.services.userservice.User;
import com.deakishin.zodiac.services.userservice.UserService;
import com.deakishin.zodiac.services.userservice.UserServiceI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListView;

/** Fragment for displaying and managing CipherBoard. */
public class BoardFragment extends Fragment implements BoardListAdapter.OnClickListener {

	/* Ids for child fragments. */
	private static final String DIALOG_SORT = "sort";
	private static final String DIALOG_BOARD_HELP = "boardHelp";

	/* Request codes for child fragments. */
	private static final int REQUEST_SORT = 10;
	private static final int REQUEST_PROFILE_CHANGED = 11;

	/* Widgets. */
	private AnimatedExpandableListView mListView;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private boolean mLoadingErrorIsShown = false;
	private View mLoadingErrorView;
	private View mLoadingBottomProgressView;
	private View mEmptyListMessageView;
	private boolean mEmptyListMessageIsShown = false;
	/* Main list adapter. */
	private BoardListAdapter mListAdapter;
	/*
	 * Last position in the list before fragment was destroyed. Needed to
	 * restore list state.
	 */
	private int mLastListPosition;

	/* Board service for managing Cipher Board. */
	private BoardServiceI mBoardService;
	/* User service for managing current user. */
	private UserServiceI mUserService;
	/* Current logged-in user. */
	private User mUser;

	/* Information about next page in the query to CipherBoard. */
	private BoardServiceI.PageInfo mNextPageInfo = null;
	/* Indicates if there are no more items in the board. */
	private boolean mBoardItemsAreOver = false;

	/* How many items are loaded from board at once. */
	private static final int BOARD_ITEMS_LOAD_STEP = 10;

	/*
	 * Flag indicating that fragment was just created. If the fragment is just
	 * created, data needs to be automatically loaded from the board.
	 */
	private boolean mFirstCreated = true;
	/* Flag indicating that loading is in process. */
	private boolean mLoading = false;
	/* Background loader. */
	private LoadingTask mLoadingTask;

	/* Application settings. */
	private SettingsPersistent mSettings;

	/* Cipher manager. */
	private CipherManager mCipherManager;

	/* Current search query. */
	private String mSearchQuery = null;

	/**
	 * The listener interface receiving notifications when user tries to leave a
	 * like without logging in first.
	 */
	public static interface OnNonLoggedLikeListener {
		/**
		 * Invoked when there is an attempt to leave a like without being logged
		 * in.
		 */
		public void onNonLoggedLiked();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);

		mSettings = SettingsPersistent.getInstance(getActivity());

		mCipherManager = CipherManager.getInstance(getActivity());

		mBoardService = BoardServiceImpl.getImpl(getActivity());

		mUserService = UserService.getInstance(getActivity());
		mUser = mUserService.getUser();

		mListAdapter = new BoardListAdapter(getActivity(), this, mUserService.getUser());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.board_fragment, parent, false);

		mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.board_swiperefresh);
		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

			@Override
			public void onRefresh() {
				reloadData();
			}
		});

		mListView = (AnimatedExpandableListView) v.findViewById(R.id.board_expListView);
		View loadingBottomProgressAndErrorView = ((LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.board_list_footer_panel, null,
						false);
		mListView.addFooterView(loadingBottomProgressAndErrorView);
		mEmptyListMessageView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.board_empty_list_message, null, false);
		if (mFirstCreated)
			mListView.addHeaderView(mEmptyListMessageView);
		mListView.setAdapter(mListAdapter);
		mListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				if (mListView.isGroupExpanded(groupPosition)) {
					mListView.collapseGroupWithAnimation(groupPosition);
				} else {
					mListView.expandGroupWithAnimation(groupPosition);
					int position = mListView.getPositionForView(v);
					mListView.smoothScrollToPositionFromTop(position, 0);
				}
				return true;
			}
		});

		mLoadingErrorView = loadingBottomProgressAndErrorView.findViewById(R.id.board_loading_error_textView);
		mLoadingErrorView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadData();
			}
		});
		mLoadingBottomProgressView = loadingBottomProgressAndErrorView
				.findViewById(R.id.board_loading_bottom_progressbar_panel);

		mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				int itemcount = totalItemCount;
				if (firstVisibleItem + visibleItemCount == itemcount && itemcount != 0) {
					if (!mLoading && !mBoardItemsAreOver)
						loadData();
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
		});

		updateLoadingProgressBar();
		updateLoadingErrorView();
		updateEmptyListMessage();

		mListView.setSelection(mLastListPosition);

		return v;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mLastListPosition = mListView.getFirstVisiblePosition();
	}

	/**
	 * Updates fragment by realoding data from the board.
	 */
	public void update() {
		mUser = mUserService.getUser();
		mListAdapter.setUser(mUser);
		reloadData();
	}

	/**
	 * Sets search query.
	 * 
	 * @param query
	 *            Search query or null if there is no need to search.
	 */
	public void setSearchQuery(String query) {
		mSearchQuery = query;
		reloadData();
	}

	/**
	 * Clears current search query to load data without the search.
	 */
	public void clearSearhQuery() {
		if (mSearchQuery != null) {
			setSearchQuery(null);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		mListAdapter.notifyContextChanged(getActivity());
		// Automatically load data if fragment has been just created.
		if (mFirstCreated) {
			loadData();
			mFirstCreated = false;
		}
	}

	/* Load data from the board by executing background loader. */
	private void loadData() {
		if (mLoading && mLoadingTask != null)
			mLoadingTask.cancel(true);
		mLoadingTask = new LoadingTask(BOARD_ITEMS_LOAD_STEP, mNextPageInfo);
		mLoadingTask.execute();
	}

	/* Clear all data regarding previous data loadings. */
	private void clearData() {
		mListAdapter.setData(null);
		mNextPageInfo = null;
		mBoardItemsAreOver = false;
		mLastListPosition = 0;
	}

	/* Reload data from the board. */
	private void reloadData() {
		clearData();
		loadData();
	}

	/** {@link AsyncTask} task that loads data fromo the board in the background. */
	private class LoadingTask extends AsyncTask<Void, Void, BoardServiceI.Result> {

		private Integer mLimit;
		private BoardServiceI.PageInfo mPageInfo;

		/**
		 * Constructs background loader.
		 * 
		 * @param limit
		 *            Maximum number of items to load.
		 * @param pageInfo
		 *            Information about the page to load.
		 */
		public LoadingTask(Integer limit, BoardServiceI.PageInfo pageInfo) {
			mLimit = limit;
			mPageInfo = pageInfo;
		}

		@Override
		protected void onPreExecute() {
			mLoading = true;
			mLoadingErrorIsShown = false;
			mEmptyListMessageIsShown = false;
			updateLoadingProgressBar();
			updateLoadingErrorView();
			updateEmptyListMessage();
		}

		@Override
		protected BoardServiceI.Result doInBackground(Void... params) {
			try {
				if (mSearchQuery != null) {
					return mBoardService.searchCiphers(mSearchQuery, mSettings.getBoardSortSortByOption(),
							mSettings.getBoardSortShowSolvedOption(), mPageInfo, mLimit, mUser);
				} else {
					return mBoardService.getCiphers(mSettings.getBoardSortSortByOption(),
							mSettings.getBoardSortShowSolvedOption(), mPageInfo, mLimit, mUser);
				}
			} catch (IOException e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(BoardServiceI.Result res) {
			if (isCancelled())
				return;

			mLoading = false;
			updateLoadingProgressBar();
			if (res != null) {
				mBoardItemsAreOver = res.isEnd();
				mListAdapter.addData(res.getBoardCiphers());
				if (!mListAdapter.hasData()) {
					mEmptyListMessageIsShown = true;
					updateEmptyListMessage();
				}
				mNextPageInfo = res.getNextPageInfo();
			} else {
				mBoardItemsAreOver = true;
				mLoadingErrorIsShown = true;
				updateLoadingErrorView();
			}
		}
	}

	@Override
	public void onLikeClicked(BoardCipher cipher) {
		if (mUser == null) {
			notifyActivityNonLoggedLike();
			return;
		}
		cipher.changeLikedByUser();
		cipher.changeRate(cipher.isLikedByUser());
		new AsyncTask<BoardCipher, Void, Void>() {
			@Override
			protected Void doInBackground(BoardCipher... arg0) {
				try {
					BoardCipher cipher = arg0[0];
					mBoardService.addLike(cipher, mUserService.getUser(), cipher.isLikedByUser());
					return null;
				} catch (IOException e) {
					return null;
				}
			}

		}.execute(cipher);
		mListAdapter.notifyDataSetChanged();
	}

	/* Notify the activity that there is an attempt to leave a like without being logged in. */
	private void notifyActivityNonLoggedLike() {
		Activity activity = getActivity();
		if (activity != null && activity instanceof OnNonLoggedLikeListener)
			((OnNonLoggedLikeListener) activity).onNonLoggedLiked();
	}

	// Callback to a cipher's Import button being clicked.
	@Override
	public void onImportClicked(BoardCipher boardCipher) {
		CipherInfo cipherInfo = new CipherInfo();

		cipherInfo.setId(boardCipher.getId());
		cipherInfo.setTitle(boardCipher.getTitle());
		cipherInfo.setDescription(boardCipher.getDescription());
		User author = boardCipher.getAuthor();
		if (author != null)
			cipherInfo.setAuthor(author.getName());
		cipherInfo.setMarkupAsString(boardCipher.getCipherMarkup());
		cipherInfo.setDifficulty(boardCipher.getDifficulty());

		mCipherManager.addCipher(cipherInfo);

		boardCipher.increaseSolvingCount();
		new AsyncTask<BoardCipher, Void, Void>() {
			@Override
			protected Void doInBackground(BoardCipher... arg0) {
				try {
					BoardCipher cipher = arg0[0];
					mBoardService.increaseSolvingCount(cipher);
					return null;
				} catch (IOException e) {
					return null;
				}
			}

		}.execute(boardCipher);

		mListAdapter.notifyDataSetChanged();
	}

	// Callback to a cipher's author being clicked.
	@Override
	public void onAuthorClicked(BoardCipher boardCipher) {
		User author = boardCipher.getAuthor();
		if (author == null)
			return;

		Intent i = new Intent(getActivity(), UserStatsActivity.class);
		i.putExtra(UserStatsActivity.EXTRA_USER, author);
		startActivityForResult(i, REQUEST_PROFILE_CHANGED);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_board, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.board_menu_item_sort:
			FragmentManager fm = getActivity().getSupportFragmentManager();
			SortDialogFragment dialog = new SortDialogFragment();
			dialog.setTargetFragment(BoardFragment.this, REQUEST_SORT);
			dialog.show(fm, DIALOG_SORT);
			return true;
		case R.id.board_menu_item_refresh:
			reloadData();
			return true;
		case R.id.board_menu_item_help:
			showHelpDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_SORT) {
			if (resultCode != Activity.RESULT_OK)
				return;
			reloadData();
			return;
		}
		if (requestCode == REQUEST_PROFILE_CHANGED) {
			if (resultCode != Activity.RESULT_OK)
				return;
			reloadData();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/* Show Help dialog. */
	private void showHelpDialog() {
		FragmentManager fm = getActivity().getSupportFragmentManager();
		new DialogBoardHelp().show(fm, DIALOG_BOARD_HELP);
	}

	/*Update loading indicator. */
	private void updateLoadingProgressBar() {
		if (mLoading) {
			if (mListAdapter.hasData()) {
				mLoadingBottomProgressView.setVisibility(View.VISIBLE);
				mSwipeRefreshLayout.setRefreshing(false);
			} else {
				mLoadingBottomProgressView.setVisibility(View.INVISIBLE);
				mSwipeRefreshLayout.setRefreshing(true);
			}
		} else {
			mLoadingBottomProgressView.setVisibility(View.INVISIBLE);
			mSwipeRefreshLayout.setRefreshing(false);
		}
	}

	/* Update connection error message visibility. */
	private void updateLoadingErrorView() {
		if (mLoadingErrorIsShown) {
			mLoadingErrorView.setVisibility(View.VISIBLE);
		} else {
			mLoadingErrorView.setVisibility(View.GONE);
		}
	}

	/* Update empty list message visibility. */
	private void updateEmptyListMessage() {
		if (mEmptyListMessageIsShown) {
			mListView.addHeaderView(mEmptyListMessageView);
		} else {
			if (mListView.getHeaderViewsCount() > 0)
				mListView.removeHeaderView(mEmptyListMessageView);
		}
	}

	// Callback to a cipher's first solver being clicked.
	@Override
	public void onFirstSolverClicked(BoardCipher boardCipher) {
		User firstSolver = boardCipher.getSolvedBy();
		if (firstSolver == null)
			return;

		Intent i = new Intent(getActivity(), UserStatsActivity.class);
		i.putExtra(UserStatsActivity.EXTRA_USER, firstSolver);
		startActivityForResult(i, REQUEST_PROFILE_CHANGED);
	}
}
