package com.deakishin.zodiac.controller;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.AnimatedExpandableListView.AnimatedExpandableListAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Abstract class for an adapter of a list in the navigation drawer. To maintain
 * consistency, the class must be extended by every drawer list class in the
 * app. Note that subclasses must invoke {@link #BaseDrawerListAdapter(Context)}
 * with correct app context for correct behaviour.
 * 
 * Implementation must specify list elements by overriding
 * {@link #getGroupInfo(int)} method.
 */
public abstract class BaseDrawerListAdapter extends AnimatedExpandableListAdapter {

	/* View inflater. */
	private LayoutInflater mLayoutInflater;

	public BaseDrawerListAdapter() {
		super();
	}

	/**
	 * Constructs an object by a given app context. This constructor MUST be
	 * invoked in subclasses.
	 * 
	 * @param context
	 *            Application context is needed to get access to LayoutInflater
	 *            object for inflating views.
	 */
	public BaseDrawerListAdapter(Context context) {
		super();
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * Class of POJOs that contain information about a group in the list (i.e.
	 * main elements of the list that can be expanded).
	 */
	protected static class GroupInfo {
		private String mTitle;
		private int mIconRes;
		private boolean mHasExpander = false;
		private boolean mHasDividerTop;
		private boolean mEnabled = true;

		public GroupInfo() {
		}

		/** @return Group's title. */
		public String getTitle() {
			return mTitle;
		}

		/**
		 * Sets group's title.
		 * 
		 * @param title
		 *            Title.
		 */
		public void setTitle(String title) {
			mTitle = title;
		}

		/** @return Group's icon resource id. */
		public int getIconRes() {
			return mIconRes;
		}

		/**
		 * Sets group's icon resource id.
		 * 
		 * @param iconRes
		 *            Icon resource id.
		 */
		public void setIconRes(int iconRes) {
			mIconRes = iconRes;
		}

		/** @return True if group has expander. */
		public boolean hasExpander() {
			return mHasExpander;
		}

		/**
		 * Sets group's expander presence.
		 * 
		 * @param hasExpander
		 *            True if group has to have expander.
		 */
		public void setHasExpander(boolean hasExpander) {
			mHasExpander = hasExpander;
		}

		/** @return True if group has divider on the top. */
		public boolean hasDividerTop() {
			return mHasDividerTop;
		}

		/**
		 * Sets divider'presence on the top the group.
		 * 
		 * @param hasDividerTop
		 *            True if group has to have divider on the top.
		 */
		public void setHasDividerTop(boolean hasDividerTop) {
			mHasDividerTop = hasDividerTop;
		}

		/** @return True if group is enabled. */
		public boolean isEnabled() {
			return mEnabled;
		}

		/**
		 * Sets group's enability.
		 * 
		 * @param enabled
		 *            True to enable the group, false otherwise.
		 */
		public void setEnabled(boolean enabled) {
			mEnabled = enabled;
		}
	}

	/**
	 * Main method for customizing list elements. Every class implementation
	 * must return specific GroupInfo object for given position.
	 * 
	 * @param groupPosition
	 *            Position in the list for which info must be returned.
	 * @return GroupInfo object incapsulating information about list element.
	 */
	protected abstract GroupInfo getGroupInfo(int groupPosition);

	/**
	 * Method for accessing view inflater.
	 * 
	 * @return LayoutInflater object for inflating views. Can be null if Context
	 *         was not passed to the constructor
	 *         {@link #BaseDrawerListAdapter(Context)}.
	 */
	protected LayoutInflater getLayoutInflater() {
		return mLayoutInflater;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return getGroupInfo(groupPosition);
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.drawer_list_item, null);
		}

		GroupInfo group = (GroupInfo) getGroup(groupPosition);

		TextView titleView = (TextView) convertView.findViewById(R.id.drawer_header_textView);
		titleView.setText(group.getTitle());
		titleView.setEnabled(group.isEnabled());
		ImageView imageView = (ImageView) convertView.findViewById(R.id.drawer_header_icon);
		imageView.setImageResource(group.getIconRes());
		imageView.setEnabled(group.isEnabled());
		int expanderRes = 0;
		if (group.hasExpander()) {
			if (isExpanded) {
				expanderRes = R.drawable.expander_close;
			} else {
				expanderRes = R.drawable.expander_open;
			}
		}
		titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, expanderRes, 0);

		View divider = convertView.findViewById(R.id.drawer_item_top_divider);
		if (group.hasDividerTop())
			divider.setVisibility(View.VISIBLE);
		else
			divider.setVisibility(View.GONE);

		return convertView;
	}
}
