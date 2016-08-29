package com.deakishin.zodiac.controller.mainscreen;

import java.util.ArrayList;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.model.bindingmanager.CheckPoint;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class DrawerListAdapter extends BaseExpandableListAdapter
		implements ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener {
	/*
	 * Адаптер для списка в боковом меню главной активности. Адаптер также
	 * обрабатывает события касания на элементах списка.
	 */

	/* Порядковые номера главных элементов списка и их общее число. */
	private static final int CHECKPOINTS_GROUP_INDEX = 0;
	private static final int SAVE_IMAGE_GROUP_INDEX = 1;
	private static final int SETTINGS_GROUP_INDEX = 2;
	private static final int HELP_GROUP_INDEX = 3;
	private static final int GROUP_COUNT = 4;

	/* Контекст приложения. */
	private Context mContext;

	/* Заполнитель представлений. */
	private LayoutInflater mLayoutInflater;

	/* Данные для подменю чекпоинтов. */
	private ArrayList<CheckPoint> mCheckpoints;

	/* Обратные вызовы после обработки касания. */
	public static interface ClickCallback {
		public void onCheckpointClicked(CheckPoint checkpoint);

		public void onCheckpointDeleted(CheckPoint checkpoint);

		public void onHelpGroupClicked();

		public void onSettingsGroupClicked();

		public void onSaveImageGroupClicked();
	}

	private ClickCallback mClickCallback;

	public DrawerListAdapter(Context context, ArrayList<CheckPoint> checkpoints, ClickCallback clickCallback) {
		mContext = context;
		mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mClickCallback = clickCallback;
		if (checkpoints == null)
			mCheckpoints = new ArrayList<CheckPoint>();
		else
			mCheckpoints = checkpoints;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		switch (groupPosition) {
		case CHECKPOINTS_GROUP_INDEX:
			return mCheckpoints.get(childPosition);
		case HELP_GROUP_INDEX:
			return null;
		case SETTINGS_GROUP_INDEX:
			return null;
		case SAVE_IMAGE_GROUP_INDEX:
			return null;
		default:
			return null;
		}
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		switch (groupPosition) {
		case CHECKPOINTS_GROUP_INDEX:
			return getCheckpointView(childPosition, convertView, parent);
		case HELP_GROUP_INDEX:
			return null;
		case SETTINGS_GROUP_INDEX:
			return null;
		default:
			return null;
		}
	}

	/* Создание элемента подменю для чекпоинта. */
	private View getCheckpointView(int childPosition, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.drawer_list_item_checkpoint, null);
		}

		CheckPoint checkpoint = mCheckpoints.get(childPosition);

		TextView mTitleView = (TextView) convertView.findViewById(R.id.checkpoint_title_textView);
		mTitleView.setText(checkpoint.getTitle());

		ImageButton mDeleteButton = (ImageButton) convertView.findViewById(R.id.checkpoint_delete_button);
		mDeleteButton.setOnClickListener(new DeleteButtonClickListener(checkpoint));

		return convertView;
	}

	private class DeleteButtonClickListener implements View.OnClickListener {

		private CheckPoint mCheckpoint;

		DeleteButtonClickListener(CheckPoint checkpoint) {
			mCheckpoint = checkpoint;
		}

		@Override
		public void onClick(View v) {
			mClickCallback.onCheckpointDeleted(mCheckpoint);
		}
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		switch (groupPosition) {
		case CHECKPOINTS_GROUP_INDEX:
			return mCheckpoints.size();
		case HELP_GROUP_INDEX:
			return 0;
		case SETTINGS_GROUP_INDEX:
			return 0;
		case SAVE_IMAGE_GROUP_INDEX:
			return 0;
		default:
			return 0;
		}
	}

	/* Информация о главных элементах меню. */
	private static class GroupInfo {
		private String mTitle;
		private int mIconRes;
		private boolean mHasExpander = false;

		public String getTitle() {
			return mTitle;
		}

		public void setTitle(String title) {
			mTitle = title;
		}

		public int getIconRes() {
			return mIconRes;
		}

		public void setIconRes(int iconRes) {
			mIconRes = iconRes;
		}

		public boolean hasExpander() {
			return mHasExpander;
		}

		public void setHasExpander(boolean hasExpander) {
			mHasExpander = hasExpander;
		}
	}

	@Override
	public Object getGroup(int groupPosition) {
		GroupInfo gr = new GroupInfo();
		switch (groupPosition) {
		case CHECKPOINTS_GROUP_INDEX:
			gr.setTitle(mContext.getString(R.string.drawer_checkpoints, mCheckpoints.size()));
			gr.setIconRes(R.drawable.ic_menu_checkpoint);
			gr.setHasExpander(true);
			break;
		case HELP_GROUP_INDEX:
			gr.setTitle(mContext.getString(R.string.drawer_help));
			gr.setIconRes(R.drawable.ic_menu_help);
			break;
		case SETTINGS_GROUP_INDEX:
			gr.setTitle(mContext.getString(R.string.drawer_settings));
			gr.setIconRes(R.drawable.ic_menu_settings);
			break;
		case SAVE_IMAGE_GROUP_INDEX:
			gr.setTitle(mContext.getString(R.string.drawer_save_image));
			gr.setIconRes(R.drawable.ic_menu_saveimage);
			break;
		default:
			break;
		}
		return gr;
	}

	@Override
	public int getGroupCount() {
		return GROUP_COUNT;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.drawer_list_header, null);
		}

		GroupInfo group = (GroupInfo) getGroup(groupPosition);

		TextView titleView = (TextView) convertView.findViewById(R.id.drawer_header_textView);
		titleView.setText(group.getTitle());
		ImageView imageView = (ImageView) convertView.findViewById(R.id.drawer_header_icon);
		imageView.setImageResource(group.getIconRes());
		int expanderRes = 0;
		if (group.hasExpander()) {
			if (isExpanded) {
				expanderRes = R.drawable.expander_close;
			} else {
				expanderRes = R.drawable.expander_open;
			}
		}
		titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, expanderRes, 0);

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		switch (groupPosition) {
		case CHECKPOINTS_GROUP_INDEX:
			mClickCallback.onCheckpointClicked(mCheckpoints.get(childPosition));
			return true;
		case HELP_GROUP_INDEX:
			return false;
		case SETTINGS_GROUP_INDEX:
			return false;
		case SAVE_IMAGE_GROUP_INDEX:
			return false;
		default:
			return false;
		}
	}

	@Override
	public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
		switch (groupPosition) {
		case CHECKPOINTS_GROUP_INDEX:
			return false;
		case HELP_GROUP_INDEX:
			mClickCallback.onHelpGroupClicked();
			return true;
		case SETTINGS_GROUP_INDEX:
			mClickCallback.onSettingsGroupClicked();
			return true;
		case SAVE_IMAGE_GROUP_INDEX:
			mClickCallback.onSaveImageGroupClicked();
			return true;
		default:
			return false;
		}
	}
}
