package com.deakishin.zodiac.controller.boardscreen.userstatsscreen;

import java.util.ArrayList;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.services.boardservice.BoardServiceI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/** Adapter for the list of the displayed stats elements. */
public class StatsListAdapter extends BaseAdapter {

	/* Application context. */
	private Context mContext;

	/* Views inflater. */
	private LayoutInflater mLayoutInflater;

	/*
	 * List of the stats elements to display.
	 */
	private ArrayList<StatsItem> mStatsItems;

	public StatsListAdapter(Context context) {
		super();
		mContext = context.getApplicationContext();
		mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * Sets user's advanced stats to display.
	 * 
	 * @param stats
	 *            Stats to display.
	 */
	public void setData(BoardServiceI.UserAdvancedStats stats) {
		if (stats == null) {
			mStatsItems = null;
			this.notifyDataSetChanged();
			return;
		}

		mStatsItems = new ArrayList<StatsItem>();
		mStatsItems.add(new StatsItem(StatsItem.ID_RANK, stats, mContext));
		mStatsItems.add(new StatsItem(StatsItem.ID_POINTS, stats, mContext));
		mStatsItems.add(new StatsItem(StatsItem.ID_CREATED_COUNT, stats, mContext));
		mStatsItems.add(new StatsItem(StatsItem.ID_SOLVED_COUNT, stats, mContext));
		mStatsItems.add(new StatsItem(StatsItem.ID_SOLVED_FIRST_COUNT, stats, mContext));
		this.notifyDataSetChanged();
	}

	/** Displayed stats element. */
	private static class StatsItem {
		/** User's rank. */
		public static final int ID_RANK = 1;
		/** User's points. */
		public static final int ID_POINTS = 2;
		/** Number of ciphers created by the user. */
		public static final int ID_CREATED_COUNT = 3;
		/** Number of ciphers solved by the user. */
		public static final int ID_SOLVED_COUNT = 4;
		/** Number of ciphers that the user was the frist to sove. */
		public static final int ID_SOLVED_FIRST_COUNT = 5;

		/* Stats element title resource id. */
		private int mTitleResId;
		/* Stats element value. */
		private String mValueString;

		/**
		 * Constructs stats element.
		 * 
		 * @param id
		 *            Id of the element. Must be one of the following:
		 *            {@link StatsItem#ID_RANK}, {@link StatsItem#ID_POINTS},
		 *            {@link StatsItem#ID_CREATED_COUNT},
		 *            {@link StatsItem#ID_SOLVED_COUNT},
		 *            {@link StatsItem#ID_SOLVED_FIRST_COUNT}.
		 * @param stats
		 *            Stats values.
		 * @param context
		 *            Application context.
		 */
		public StatsItem(int id, BoardServiceI.UserAdvancedStats stats, Context context) {
			if (stats == null)
				return;

			switch (id) {
			case ID_RANK:
				mTitleResId = R.string.userstats_rank;
				mValueString = context.getString(R.string.userstats_value_int, stats.getRank());
				break;
			case ID_POINTS:
				mTitleResId = R.string.userstats_points;
				mValueString = context.getString(R.string.userstats_value_float, stats.getPoints());
				break;
			case ID_SOLVED_COUNT:
				mTitleResId = R.string.userstats_solved_count;
				mValueString = context.getString(R.string.userstats_value_int, stats.getSolvedCount());
				break;
			case ID_CREATED_COUNT:
				mTitleResId = R.string.userstats_created_count;
				mValueString = context.getString(R.string.userstats_value_int, stats.getCreatedCount());
				break;
			case ID_SOLVED_FIRST_COUNT:
				mTitleResId = R.string.userstats_solved_first_count;
				mValueString = context.getString(R.string.userstats_value_int, stats.getSolvedFirstCount());
				break;
			default:
				mTitleResId = 0;
				mValueString = null;
				break;
			}
		}

		/** @return Resource id of the stats element title. */
		public int getTitleResId() {
			return mTitleResId;
		}

		/** @return Stats element value. */
		public String getValueString() {
			return mValueString;
		}
	}

	@Override
	public int getCount() {
		if (mStatsItems == null)
			return 0;

		return mStatsItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mStatsItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.userstats_list_item, null);
		}

		StatsItem stats = (StatsItem) getItem(position);

		TextView titleTextView = (TextView) convertView.findViewById(R.id.userstats_list_item_stat_textView);
		titleTextView.setText(stats.getTitleResId());

		TextView statTextView = (TextView) convertView.findViewById(R.id.userstats_list_item_value_textView);
		statTextView.setText(stats.getValueString());

		return convertView;
	}
}
