package com.deakishin.zodiac.controller.boardscreen.topusersscreen;

import java.io.IOException;
import java.util.ArrayList;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.boardscreen.userstatsscreen.UserStatsActivity;
import com.deakishin.zodiac.services.boardservice.BoardServiceI;
import com.deakishin.zodiac.services.boardservice.BoardServiceImpl;
import com.deakishin.zodiac.services.userservice.User;
import com.deakishin.zodiac.services.userservice.UserService;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/** Fragment that displays the list of top users. */
public class TopUsersFragment extends Fragment implements TopUsersListAdapter.OnClickListener {

	/* Number of displayed top users. */
	private static final int LIMIT = 10;

	/* Widgets. */
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private ListView mListView;
	/* Flag indicating that error is shown. */
	private boolean mLoadingErrorIsShown = false;
	private View mLoadingErrorView;
	private View mLoadingBottomProgressAndErrorView;
	/* Adapter for the list of top users. */
	private TopUsersListAdapter mListAdapter;
	/*
	 * Last position in list before fragment was destroyed. Needed to restore
	 * list state after configuration changes.
	 */
	private int mLastListPosition;

	/* Board service to get displayed data from. */
	private BoardServiceI mBoardService;

	/* User service to get current user log-in status. */
	private UserService mUserService;

	/*
	 * Flag indicating that the fragment was just created and it's needed to
	 * load data.
	 */
	private boolean mFirstCreated = true;
	/* Flag indicating the loading is in process. */
	private boolean mLoading = false;
	/* Data loader. */
	private LoadingTask mLoadingTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);

		mBoardService = BoardServiceImpl.getImpl(getActivity());

		mUserService = UserService.getInstance(getActivity());

		mListAdapter = new TopUsersListAdapter(getActivity(), this, mUserService.getUser());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.board_fragment_top_users, parent, false);

		mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.board_top_users_swiperefresh);
		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

			@Override
			public void onRefresh() {
				reloadData();
			}
		});

		mListView = (ListView) v.findViewById(R.id.board_top_users_listView);
		mLoadingBottomProgressAndErrorView = ((LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.board_list_footer_panel, null,
						false);
		mListView.addFooterView(mLoadingBottomProgressAndErrorView);
		mLoadingErrorView = mLoadingBottomProgressAndErrorView.findViewById(R.id.board_loading_error_textView);
		mLoadingErrorView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadData();
			}
		});
		View loadingBottomProgressView = mLoadingBottomProgressAndErrorView
				.findViewById(R.id.board_loading_bottom_progressbar_panel);
		loadingBottomProgressView.setVisibility(View.GONE);

		mListView.setAdapter(mListAdapter);

		updateLoadingProgressBar();
		updateLoadingErrorView();

		mListView.setSelection(mLastListPosition);

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		mListAdapter.notifyContextChanged(getActivity());
		if (mFirstCreated) {
			loadData();
			mFirstCreated = false;
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mLastListPosition = mListView.getFirstVisiblePosition();
	}

	/* Load data by executing loader. */
	private void loadData() {
		if (mLoading && mLoadingTask != null)
			mLoadingTask.cancel(true);
		mLoadingTask = new LoadingTask(LIMIT);
		mLoadingTask.execute();
	}

	/* Clear all data. */
	private void clearData() {
		mListAdapter.setData(null);
		mLastListPosition = 0;
	}

	/* Reload all data. */
	private void reloadData() {
		clearData();
		loadData();
	}

	/** {@link AsyncTask} for loading data in the background. */
	private class LoadingTask extends AsyncTask<Void, Void, ArrayList<BoardServiceI.UserStats>> {

		private Integer mLimit;

		/**
		 * Constructs loader.
		 * 
		 * @param limit
		 *            Number of top users to load.
		 */
		public LoadingTask(Integer limit) {
			mLimit = limit;
		}

		@Override
		protected void onPreExecute() {
			mLoading = true;
			mLoadingErrorIsShown = false;
			updateLoadingProgressBar();
			updateLoadingErrorView();
		}

		@Override
		protected ArrayList<BoardServiceI.UserStats> doInBackground(Void... params) {
			try {
				return mBoardService.getTopUsers(mLimit, mUserService.getUser());
			} catch (IOException e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(ArrayList<BoardServiceI.UserStats> res) {
			if (isCancelled())
				return;

			mLoading = false;
			updateLoadingProgressBar();
			if (res != null) {
				mListAdapter.setData(res);
			} else {
				mLoadingErrorIsShown = true;
				updateLoadingErrorView();
			}
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_board_top_users, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_top_users_refresh:
			reloadData();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* Update loading indicator. */
	private void updateLoadingProgressBar() {
		mSwipeRefreshLayout.setRefreshing(mLoading);
	}

	/* Update connection error visibility. */
	private void updateLoadingErrorView() {
		if (mLoadingErrorIsShown) {
			mLoadingBottomProgressAndErrorView.setVisibility(View.VISIBLE);
			mLoadingErrorView.setVisibility(View.VISIBLE);
		} else {
			mLoadingBottomProgressAndErrorView.setVisibility(View.GONE);
			mLoadingErrorView.setVisibility(View.GONE);
		}
	}

	// Callback for a user being clicked.
	@Override
	public void onUserClicked(User user) {
		if (user == null)
			return;

		Intent i = new Intent(getActivity(), UserStatsActivity.class);
		i.putExtra(UserStatsActivity.EXTRA_USER, user);
		startActivity(i);
	}
}
