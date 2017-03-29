package com.deakishin.zodiac.controller.boardscreen.topusersscreen;

import java.util.ArrayList;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.services.boardservice.BoardServiceI;
import com.deakishin.zodiac.services.userservice.User;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/** Adapter for the list of top users. */
class TopUsersListAdapter extends BaseAdapter {

	/* Views inflater. */
	private LayoutInflater mLayoutInflater;

	/* Application context. */
	private Context mContext;

	/* List of user stats to display. */
	private ArrayList<BoardServiceI.UserStats> mData = new ArrayList<BoardServiceI.UserStats>();

	/* Current logged-in user. */
	private User mUser = null;

	/* Интерфейс слушателя внутренних событий списка. */
	/** The listener interface for receiving events of a user being selected. */
	public static interface OnClickListener {
		/**
		 * Invoked when a user is selected.
		 * 
		 * @param user
		 *            Selected user.
		 */
		public void onUserClicked(User user);
	}

	private OnClickListener mOnClickListener;

	/**
	 * Constructs adapter.
	 * 
	 * @param context
	 *            Application context.
	 * @param listener
	 *            Listener to users being selected.
	 * @param user
	 *            Current logged-in user.
	 */
	public TopUsersListAdapter(Context context, OnClickListener listener, User user) {
		notifyContextChanged(context);
		mUser = user;
		mOnClickListener = listener;
	}

	/**
	 * Sets data to display.
	 * 
	 * @param data
	 *            List of user stats in the same order that have to be
	 *            displayed.
	 */
	public void setData(ArrayList<BoardServiceI.UserStats> data) {
		if (data == null) {
			mData = new ArrayList<BoardServiceI.UserStats>();
		} else {
			mData = data;
		}
		this.notifyDataSetChanged();
	}

	/**
	 * Changes application context. Needed to get fresh {@link LayoutInflater}
	 * object that corresponds to the current configuration.
	 * 
	 * @param context
	 *            Application context.
	 */
	public void notifyContextChanged(Context context) {
		mContext = context.getApplicationContext();
		mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.board_top_users_list_item, null);
		}

		BoardServiceI.UserStats userStats = (BoardServiceI.UserStats) getItem(position);

		User statsUser = userStats.getUser();

		View topDivider = convertView.findViewById(R.id.board_top_users_top_divider);
		if (userStats.isUserSpecific()) {
			topDivider.setVisibility(View.VISIBLE);
		} else {
			topDivider.setVisibility(View.GONE);
		}

		View panel = convertView.findViewById(R.id.board_top_users_list_row_panel);

		int textColorResId;
		if (mUser != null && statsUser != null && mUser.getId().equals(statsUser.getId())) {
			textColorResId = R.color.text_primary;
			panel.setBackgroundResource(R.drawable.top_users_loggedin_user_border);
		} else {
			textColorResId = R.color.text_primary;
			panel.setBackgroundResource(0);
		}
		int textColor = ContextCompat.getColor(mContext, textColorResId);

		TextView rankTextView = (TextView) convertView.findViewById(R.id.board_top_users_field_rank_textView);
		rankTextView.setVisibility(View.VISIBLE);
		rankTextView.setTextColor(textColor);
		rankTextView.setText("" + userStats.getRank());
		TextView rankHeader = (TextView) convertView.findViewById(R.id.board_top_users_column_header_rank);
		rankHeader.setVisibility(View.INVISIBLE);

		TextView usernameTextView = (TextView) convertView.findViewById(R.id.board_top_users_field_username_textView);
		usernameTextView.setVisibility(View.VISIBLE);
		usernameTextView.setTextColor(textColor);
		usernameTextView.setText(statsUser == null ? "" : statsUser.getName());
		panel.setOnClickListener(new OnUserClickListener(statsUser));
		TextView usernameHeader = (TextView) convertView.findViewById(R.id.board_top_users_column_header_username);
		usernameHeader.setVisibility(View.INVISIBLE);

		TextView pointsTextView = (TextView) convertView.findViewById(R.id.board_top_users_field_points_textView);
		pointsTextView.setVisibility(View.VISIBLE);
		pointsTextView.setTextColor(textColor);
		pointsTextView.setText(mContext.getString(R.string.top_users_column_field_points, userStats.getPoints()));
		TextView pointsHeader = (TextView) convertView.findViewById(R.id.board_top_users_column_header_points);
		pointsHeader.setVisibility(View.INVISIBLE);

		return convertView;
	}

	/** Listener to a user being selected. */
	private class OnUserClickListener implements View.OnClickListener {
		private User mUser;

		/**
		 * Construcs listener.
		 * 
		 * @param user
		 *            User to listen to selection of.
		 */
		public OnUserClickListener(User user) {
			mUser = user;
		}

		@Override
		public void onClick(View v) {
			if (mOnClickListener != null)
				mOnClickListener.onUserClicked(mUser);
		}
	}
}