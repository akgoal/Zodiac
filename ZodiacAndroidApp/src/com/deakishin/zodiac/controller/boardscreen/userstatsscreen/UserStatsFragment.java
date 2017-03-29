package com.deakishin.zodiac.controller.boardscreen.userstatsscreen;

import java.io.IOException;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.model.avatargenerator.AvatarGenerator;
import com.deakishin.zodiac.model.avatargenerator.AvatarProfile;
import com.deakishin.zodiac.services.boardservice.BoardServiceI;
import com.deakishin.zodiac.services.boardservice.BoardServiceImpl;
import com.deakishin.zodiac.services.userservice.User;
import com.deakishin.zodiac.services.userservice.UserService;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/** Fragment for displaying user stats. */
public class UserStatsFragment extends Fragment {

	/* Keys for arguments. */
	private static final String EXTRA_USER = "user";

	/* Widgets. */
	private ImageView mAvatarImageView;
	private TextView mUsernameTextView;
	private ListView mStatsListView;
	private View mLoadingErrorView;
	private View mLoadingProgressView;

	/* Adapter for the list of stats elements. */
	private StatsListAdapter mListAdapter;

	/* User whose stats are being displayed. */
	private User mUser;

	/* Board service to get stats from. */
	private BoardServiceI mBoardService;

	/* Avatar generator to generate user's avatar image. */
	private AvatarGenerator mAvatarGenerator;

	/*
	 * Flag indicating that the fragment is just created so data needs to be
	 * auto loaded.
	 */
	private boolean mFirstCreated = true;
	/* Flag indicating that loading data is in process. */
	private boolean mLoading = false;
	/* Data loader that perform loading in the background. */
	private LoadingTask mLoadingTask;
	/* Flag indicating that connection error has to be shown. */
	private boolean mLoadingErrorIsShown = false;

	/**
	 * Constructs and returns fragment.
	 * 
	 * @param user
	 *            User whose stats has to be displayed.
	 * @return Configured fragment.
	 */
	public static UserStatsFragment getInstance(User user) {
		UserStatsFragment fragment = new UserStatsFragment();
		Bundle args = new Bundle();
		if (user != null) {
			args.putParcelable(EXTRA_USER, user);
		}
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);

		Bundle args = getArguments();
		if (args != null && args.containsKey(EXTRA_USER)) {
			mUser = args.getParcelable(EXTRA_USER);
		}
		if (mUser == null) {
			mUser = UserService.getInstance(getActivity()).getUser();
		}

		mBoardService = BoardServiceImpl.getImpl(getActivity());
		mAvatarGenerator = AvatarGenerator.getInstance(getActivity());

		mListAdapter = new StatsListAdapter(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater layoutInflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = layoutInflater.inflate(R.layout.userstats_fragment, null);

		mAvatarImageView = (ImageView) v.findViewById(R.id.userstats_avatar_imageView);
		mAvatarImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
			}
		});
		mUsernameTextView = (TextView) v.findViewById(R.id.userstats_username_textView);

		mLoadingErrorView = v.findViewById(R.id.userstats_error_loading_textView);
		mLoadingErrorView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadData();
			}
		});

		mLoadingProgressView = v.findViewById(R.id.userstats_loading_panel);

		mStatsListView = (ListView) v.findViewById(R.id.userstats_stats_listView);
		mStatsListView.setAdapter(mListAdapter);

		updateBasicUserViews();

		updateLoadingProgressBar();
		updateLoadingErrorView();

		return v;
	}

	/**
	 * Updates user info. Has to invoked every time user's profile is changed.
	 */
	public void update() {
		mUser = UserService.getInstance(getActivity()).getUser();
		updateBasicUserViews();
	}

	/*
	 * Update widgets regarding basic user information like user's avatar and name.
	 */
	private void updateBasicUserViews() {
		if (mUser == null)
			return;

		mAvatarImageView
				.setImageBitmap(mAvatarGenerator.generateAvatarBitmap(new AvatarProfile(mUser.getAvatarMarkup())));
		mUsernameTextView.setText(mUser.getName());
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mFirstCreated) {
			loadData();
			mFirstCreated = false;
		}
	}

	/* Load data by executing loader. */
	private void loadData() {
		mListAdapter.setData(null);
		if (mLoading && mLoadingTask != null)
			mLoadingTask.cancel(true);
		mLoadingTask = new LoadingTask();
		mLoadingTask.execute(mUser);
	}

	/** {@link AsyncTask} for loading data in the background. */
	private class LoadingTask extends AsyncTask<User, Void, BoardServiceI.UserAdvancedStats> {

		public LoadingTask() {
		}

		@Override
		protected void onPreExecute() {
			mLoading = true;
			mLoadingErrorIsShown = false;
			updateLoadingErrorView();
			updateLoadingProgressBar();
		}

		@Override
		protected BoardServiceI.UserAdvancedStats doInBackground(User... params) {
			if (params.length != 1)
				return null;

			User user = params[0];
			if (user == null)
				return null;

			try {
				return mBoardService.getUserStats(user);
			} catch (IOException e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(BoardServiceI.UserAdvancedStats res) {
			if (isCancelled())
				return;

			mLoading = false;
			updateLoadingProgressBar();
			if (res != null) {
				mListAdapter.setData(res);
			} else {
				mListAdapter.setData(null);
				mLoadingErrorIsShown = true;
				updateLoadingErrorView();
			}
		}
	}

	/* Update connection error visibility. */
	private void updateLoadingErrorView() {
		if (mLoadingErrorIsShown) {
			mLoadingErrorView.setVisibility(View.VISIBLE);
			mStatsListView.setVisibility(View.GONE);
		} else {
			mLoadingErrorView.setVisibility(View.GONE);
			mStatsListView.setVisibility(View.VISIBLE);
		}
	}

	/* Update loading indicator. */
	private void updateLoadingProgressBar() {
		if (mLoading) {
			mLoadingProgressView.setVisibility(View.VISIBLE);
		} else {
			mLoadingProgressView.setVisibility(View.INVISIBLE);
		}
	}
}
