package com.deakishin.zodiac.controller.boardscreen;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.AnimatedExpandableListView.AnimatedExpandableListAdapter;
import com.deakishin.zodiac.model.avatargenerator.AvatarGenerator;
import com.deakishin.zodiac.model.avatargenerator.AvatarProfile;
import com.deakishin.zodiac.model.ciphergenerator.CipherGenerator;
import com.deakishin.zodiac.model.ciphermanager.CipherManager;
import com.deakishin.zodiac.model.settings.SettingsPersistent;
import com.deakishin.zodiac.services.boardservice.BoardCipher;
import com.deakishin.zodiac.services.userservice.User;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/** Adapter for the list of CipherBoard's items. */
public class BoardListAdapter extends AnimatedExpandableListAdapter {

	/* Application context. */
	private Context mContext;

	/* Views inflater. */
	private LayoutInflater mLayoutInflater;

	/* List of ciphers to display. */
	private ArrayList<BoardCipher> mData = new ArrayList<BoardCipher>();

	/* Ids of imported ciphers. */
	private ArrayList<Long> mImportedIds;

	/* Ids of cipher which solutions are shown. */
	private ArrayList<Long> mSolutionShownIds;

	/* Cipher generator. Needed to generate cipher image. */
	private CipherGenerator mCipherGenerator;

	/* Avatar generator. */
	private AvatarGenerator mAvatarGenerator;

	/* Application Settings. */
	private SettingsPersistent mSettings;

	/* Current logged-in user. */
	private User mUser = null;

	/* Интерфейс слушателя внутренних событий списка. */
	/**
	 * The listener interface for receiving events regarding actions on
	 * particular cipher.
	 */
	public static interface OnClickListener {
		/**
		 * Invoked when a cipher's like button is clicked.
		 * 
		 * @param item
		 *            Cipher that was clicked.
		 */
		public void onLikeClicked(BoardCipher item);

		/**
		 * Invoked when a cipher's import button is clicked.
		 * 
		 * @param item
		 *            Cipher that was clicked.
		 */
		public void onImportClicked(BoardCipher item);

		/**
		 * Invoked when a cipher's author is clicked.
		 * 
		 * @param item
		 *            Cipher that was clicked.
		 */
		public void onAuthorClicked(BoardCipher item);

		/**
		 * Invoked when a cipher's first solver is clicked.
		 * 
		 * @param item
		 *            Cipher that was clicked.
		 */
		public void onFirstSolverClicked(BoardCipher item);
	}

	private OnClickListener mOnClickListener;

	/**
	 * Constructs adapter.
	 * 
	 * @param context
	 *            Application context.
	 * @param listener
	 *            Listener to actions on a cipher.
	 * @param user
	 *            Logged-in user. Null if user is not logged in.
	 */
	public BoardListAdapter(Context context, OnClickListener listener, User user) {
		mContext = context;
		mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mCipherGenerator = CipherGenerator.getInstance(mContext);
		mSettings = SettingsPersistent.getInstance(mContext);

		mUser = user;

		mImportedIds = CipherManager.getInstance(mContext).getCipherIds();

		mSolutionShownIds = new ArrayList<Long>();

		mOnClickListener = listener;

		mAvatarGenerator = AvatarGenerator.getInstance(mContext);
	}

	/**
	 * Updates logged-in user. Needs to be invoked every time user's status
	 * changes.
	 * 
	 * @param user
	 *            Logged-in user or null if user is not logged in.
	 */
	public void setUser(User user) {
		mUser = user;
	}

	/**
	 * Sets displayed ciphers.
	 * 
	 * @param data
	 *            List of ciphers to display.
	 */
	public void setData(ArrayList<BoardCipher> data) {
		if (data == null)
			mData = new ArrayList<BoardCipher>();
		else {
			mData = filter(data);
		}
		this.notifyDataSetChanged();
	}

	/* Filter ciphers to exclude imported ones if necessary. */
	private ArrayList<BoardCipher> filter(ArrayList<BoardCipher> data) {
		if (!mSettings.isBoardSortHideImported()) {
			return data;
		}
		ArrayList<BoardCipher> filtered = new ArrayList<BoardCipher>();
		for (BoardCipher cipher : data) {
			if (!mImportedIds.contains(cipher.getId())) {
				filtered.add(cipher);
			}
		}
		return filtered;
	}

	/* Indicates if the cipher was created by current user. */
	private boolean createdByCurrentUser(BoardCipher cipher) {
		return mUser != null && cipher.getAuthor() != null && cipher.getAuthor().getId().equals(mUser.getId());
	}

	/**
	 * Adds ciphers to the list of ciphers that are already being displayed. New
	 * ciphers are added to the end of the list.
	 * 
	 * @param data
	 *            List of additional ciphers.
	 */
	public void addData(ArrayList<BoardCipher> data) {
		if (data != null && data.size() > 0) {
			mData.addAll(filter(data));
			this.notifyDataSetChanged();
		}
	}

	/**
	 * Indicates if there are any ciphers that are being displayed.
	 */
	public boolean hasData() {
		return !mData.isEmpty();
	}

	/**
	 * Changes application context. Needed to get fresh {@link LayoutInflater}
	 * object that corresponds to the current configuration.
	 * 
	 * @param context
	 *            Application context.
	 */
	public void notifyContextChanged(Context context) {
		mContext = context;
		mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.board_list_item_child, null);
		}

		BoardCipher cipher = (BoardCipher) getGroup(groupPosition);

		TextView descrTextView = (TextView) convertView.findViewById(R.id.board_list_item_description);
		if (cipher.getDescription() != null && !cipher.getDescription().equals(""))
			descrTextView.setText(cipher.getDescription());
		else
			descrTextView.setText(R.string.cipher_info_no_description);

		CipherPreviewView previewView = (CipherPreviewView) convertView.findViewById(R.id.board_list_item_previewView);
		previewView
				.setImages(mCipherGenerator.generateCipherImage(cipher.getCipherMarkup(), cipher.getCorrectAnswer()));
		previewView.setShowSolution(false);

		Button importButton = (Button) convertView.findViewById(R.id.board_list_item_import);
		if (mImportedIds.contains(cipher.getId())) {
			importButton.setEnabled(false);
			importButton.setText(R.string.board_item_imported);
		} else {
			importButton.setEnabled(true);
			importButton.setText(R.string.board_item_import);
		}
		importButton.setOnClickListener(new OnItemClickListener(cipher) {
			@Override
			public void onClick(View v) {
				mOnClickListener.onImportClicked(getItem());
			}
		});

		View showSolutionPanel = convertView.findViewById(R.id.board_list_item_show_solution_panel);
		Button showSolutionButton = (Button) convertView.findViewById(R.id.board_list_item_show_solution);
		if (cipher.hasAnswer()) {
			showSolutionPanel.setVisibility(View.VISIBLE);
			showSolutionButton.setOnClickListener(
					new OnShowSolutionClickListener(cipher.getId(), previewView, showSolutionButton));
			updateSolutionVisibility(cipher.getId(), previewView, showSolutionButton);
		} else
			showSolutionPanel.setVisibility(View.INVISIBLE);

		View bottomDivider = (View) convertView.findViewById(R.id.board_item_child_bottomDivider);
		if (isLastChild) {
			bottomDivider.setVisibility(View.VISIBLE);
		} else
			bottomDivider.setVisibility(View.INVISIBLE);
		return convertView;
	}

	/* Update solution visibility for the cipher with given id. */
	private void updateSolutionVisibility(Long cipherId, CipherPreviewView previewView, Button showSolutionButton) {
		if (mSolutionShownIds.contains(cipherId)) {
			previewView.setShowSolution(true);
			showSolutionButton.setText(R.string.board_item_hide_solution);
		} else {
			previewView.setShowSolution(false);
			showSolutionButton.setText(R.string.board_item_show_solution);
		}
	}

	/** Listener to a cipher's "show solution" button being clicked. */
	private class OnShowSolutionClickListener implements View.OnClickListener {
		private CipherPreviewView mPreviewView;
		private Button mShowSolutionButton;
		private Long mCipherId;

		public OnShowSolutionClickListener(Long cipherId, CipherPreviewView previewView, Button showSolutionButton) {
			mPreviewView = previewView;
			mShowSolutionButton = showSolutionButton;
			mCipherId = cipherId;
		}

		@Override
		public void onClick(View v) {
			if (mCipherId == null || mPreviewView == null || mShowSolutionButton == null)
				return;

			if (mSolutionShownIds.contains(mCipherId))
				mSolutionShownIds.remove(mCipherId);
			else
				mSolutionShownIds.add(mCipherId);
			updateSolutionVisibility(mCipherId, mPreviewView, mShowSolutionButton);
		}
	}

	@Override
	public int getRealChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mData.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mData.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.board_list_item_header, null);
		}

		BoardCipher cipher = (BoardCipher) getGroup(groupPosition);

		TextView titleTextView = (TextView) convertView.findViewById(R.id.board_item_header_title_textView);
		String title = cipher.getTitle() == null ? mContext.getString(R.string.board_item_untitled) : cipher.getTitle();
		titleTextView.setText(mContext.getString(R.string.board_item_title, title));

		TextView timestampTextView = (TextView) convertView.findViewById(R.id.board_item_header_timestamp_textView);
		timestampTextView.setText(createTimeStamp(cipher.getTimeAgo()));

		User author = cipher.getAuthor();

		ImageView authorAvatarImageView = (ImageView) convertView.findViewById(R.id.board_item_header_author_imageView);
		Bitmap authorAvatarImage;
		if (author == null || author.getAvatarMarkup() == null) {
			authorAvatarImage = mAvatarGenerator.generateAvatarBitmap(null);
		} else {
			authorAvatarImage = mAvatarGenerator.generateAvatarBitmap(new AvatarProfile(author.getAvatarMarkup()));
		}
		authorAvatarImageView.setImageBitmap(authorAvatarImage);
		authorAvatarImageView.setOnClickListener(new OnItemClickListener(cipher) {
			@Override
			public void onClick(View v) {
				mOnClickListener.onAuthorClicked(getItem());
			}
		});

		TextView rewardTextView = (TextView) convertView.findViewById(R.id.board_item_header_reward_textView);
		rewardTextView
				.setText(mContext.getString(R.string.board_item_reward, cipher.getReward(), cipher.getDifficulty()));
		int rewardTextColorRes;
		if (cipher.hasReward()) {
			rewardTextColorRes = R.color.board_cipher_rewardable_text;
		} else {
			rewardTextColorRes = R.color.board_cipher_notrewardable_text;
		}
		rewardTextView.setTextColor(ContextCompat.getColor(mContext, rewardTextColorRes));

		TextView solvedCountTextView = (TextView) convertView
				.findViewById(R.id.board_item_header_solved_count_textView);
		solvedCountTextView.setText(mContext.getString(R.string.board_item_solved_count, cipher.getSolvedCount(),
				cipher.getMaxSolvedCount()));
		int solvedCountTextColorRes;
		if (cipher.isSolvedMax()) {
			solvedCountTextColorRes = R.color.board_cipher_notrewardable_text;
		} else {
			solvedCountTextColorRes = R.color.board_cipher_rewardable_text;
		}
		solvedCountTextView.setTextColor(ContextCompat.getColor(mContext, solvedCountTextColorRes));
		int solvedCountIconResId = cipher.isSolvedByUser() ? R.drawable.ic_solved_count_correct
				: R.drawable.ic_solved_count;
		Drawable solvedCountIcon = ContextCompat.getDrawable(mContext, solvedCountIconResId);
		solvedCountIcon.setBounds(0, 0, solvedCountIcon.getIntrinsicWidth(), solvedCountIcon.getIntrinsicHeight());
		solvedCountTextView.setCompoundDrawables(solvedCountIcon, null, null, null);

		TextView solvingCounterTextView = (TextView) convertView
				.findViewById(R.id.board_item_header_solving_counter_textView);
		solvingCounterTextView
				.setText(mContext.getString(R.string.board_item_solving_counter, cipher.getSolvingCount()));

		TextView firstSolverTextView = (TextView) convertView
				.findViewById(R.id.board_item_header_first_solver_textView);
		if (cipher.getSolvedBy() != null) {
			firstSolverTextView.setVisibility(View.VISIBLE);
			firstSolverTextView
					.setText(mContext.getString(R.string.board_item_first_solver, cipher.getSolvedBy().getName()));
			firstSolverTextView.setOnClickListener(new OnItemClickListener(cipher) {
				@Override
				public void onClick(View v) {
					mOnClickListener.onFirstSolverClicked(getItem());
				}
			});
		} else {
			firstSolverTextView.setOnClickListener(null);
			firstSolverTextView.setVisibility(View.GONE);
		}

		View bottomDivider = (View) convertView.findViewById(R.id.board_item_header_bottomDivider);
		if (!isExpanded) {
			bottomDivider.setVisibility(View.VISIBLE);
		} else
			bottomDivider.setVisibility(View.INVISIBLE);

		return convertView;
	}

	/** Abstract listener to an item being clicked. */
	private abstract class OnItemClickListener implements View.OnClickListener {
		private BoardCipher mItem;

		/**
		 * Construcs listener.
		 * 
		 * @param item
		 *            Item to listen to selection of.
		 */
		public OnItemClickListener(BoardCipher item) {
			mItem = item;
		}

		protected BoardCipher getItem() {
			return mItem;
		}
	}

	/*
	 * Create time stamp from number of milliseconds passed from the cipher's
	 * creation time.
	 */
	private String createTimeStamp(long millisecAgo) {
		long days = TimeUnit.MILLISECONDS.toDays(millisecAgo);
		long hours = TimeUnit.MILLISECONDS.toHours(millisecAgo);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millisecAgo);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millisecAgo);

		if (seconds < 60)
			return mContext.getString(R.string.board_item_timestamp_now);
		if (minutes < 60)
			return mContext.getString(R.string.board_item_timestamp_minutes, minutes);
		if (hours < 24)
			return mContext.getString(R.string.board_item_timestamp_hours, hours);
		return mContext.getString(R.string.board_item_timestamp_days, days);
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

}
