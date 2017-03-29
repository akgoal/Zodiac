package com.deakishin.zodiac.controller.mainscreen;

import java.util.ArrayList;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.AnimatedExpandableListView;
import com.deakishin.zodiac.controller.BaseDrawerListAdapter;
import com.deakishin.zodiac.model.ciphermanager.CipherShortInfo;
import com.deakishin.zodiac.model.ciphermodel.bindingmanager.CheckPoint;
import com.deakishin.zodiac.services.userservice.UserService;
import com.deakishin.zodiac.services.userservice.UserServiceI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;

/** Adapter for the list in the navigation drawer of the main activity. */
public class DrawerListAdapter extends BaseDrawerListAdapter
		implements ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener {

	/*
	 * Index number for each element in the list. This number determines where
	 * in the list the element will be displayed.
	 */
	private static final int CHECKPOINTS_GROUP_INDEX = 0;
	private static final int CHECK_SOLUTION_GROUP_INDEX = 1;
	private static final int CIPHERS_GROUP_INDEX = 2;
	private static final int BOARD_GROUP_INDEX = 3;
	private static final int SAVE_IMAGE_GROUP_INDEX = 4;
	private static final int SETTINGS_GROUP_INDEX = 5;
	private static final int HELP_GROUP_INDEX = 6;
	private static final int FEEDBACK_GROUP_INDEX = 7;
	/* Number of elements. */
	private static final int GROUP_COUNT = 8;

	/* Application context. */
	private Context mContext;

	/* Views inflater. */
	private LayoutInflater mLayoutInflater;

	/* List of checkpoints for Checkpoints sublist. */
	private ArrayList<CheckPoint> mCheckpoints;

	/* List of cipher infos for Ciphers sublist. */
	private ArrayList<CipherShortInfo> mCipherInfos;

	/* User service. */
	private UserServiceI mUserService;

	/**
	 * Callback interface to receive events about elements being clicked.
	 */
	public static interface ClickCallback {
		/**
		 * Invoked when a checkpoint is clicked.
		 * 
		 * @param checkpoint
		 *            Checkpoint that was clicked.
		 */
		public void onCheckpointClicked(CheckPoint checkpoint);

		/**
		 * Invoked when a checkpoint's delete button is clicked.
		 * 
		 * @param checkpoint
		 *            Checkpoint that was clicked.
		 */
		public void onCheckpointDeleted(CheckPoint checkpoint);

		/** Invoked when the Help element is clicked. */
		public void onHelpGroupClicked();

		/** Invoked when the Settings element is clicked. */
		public void onSettingsGroupClicked();

		/** Invoked when the Save Image element is clicked. */
		public void onSaveImageGroupClicked();

		/** Invoked when the Board element is clicked. */
		public void onBoardGroupSelected();

		/**
		 * Invoked when a cipher is clicked.
		 * 
		 * @param cipherInfo
		 *            Selected cipher.
		 */
		public void onCipherClicked(CipherShortInfo cipherInfo);

		/**
		 * Invoked when a cipher's delete button is clicked.
		 * 
		 * @param cipherInfo
		 *            Selected cipher.
		 */
		public void onCipherDeleted(CipherShortInfo cipherInfo);

		/** Invoked when the Check Solution element is clicked. */
		public void onCheckSolutionClicked();

		/** Invoked when the Feedback element is clicked. */
		public void onFeedbackGroupClicked();
	}

	/* Clicks callback receiver. */
	private ClickCallback mClickCallback;

	/**
	 * Constructs an adapter.
	 * 
	 * @param context
	 *            Application context.
	 * @param checkpoints
	 *            List of checkpoints for Checkpoints sublist.
	 * @param cipherInfos
	 *            List of ciphers short infos for the Ciphers sublist.
	 * @param clickCallback
	 *            Clicks callback receiver.
	 */
	public DrawerListAdapter(Context context, ArrayList<CheckPoint> checkpoints, ArrayList<CipherShortInfo> cipherInfos,
			ClickCallback clickCallback) {
		super(context);
		mContext = context;
		mUserService = UserService.getInstance(mContext);
		mLayoutInflater = getLayoutInflater();
		mClickCallback = clickCallback;
		setData(checkpoints, cipherInfos);
	}

	/**
	 * Updates data displayed in sublists.
	 * 
	 * @param checkpoints
	 *            List of checkpoints for Checkpoints sublist.
	 * @param cipherInfos
	 *            List of ciphers short infos for the Ciphers sublist.
	 */
	public void updateData(ArrayList<CheckPoint> checkpoints, ArrayList<CipherShortInfo> cipherInfos) {
		setData(checkpoints, cipherInfos);
		this.notifyDataSetChanged();
	}

	/**
	 * Updates data displayed in Ciphers sublists.
	 * 
	 * @param cipherInfos
	 *            List of ciphers short infos for the Ciphers sublist.
	 */
	public void updateData(ArrayList<CipherShortInfo> cipherInfos) {
		setData(cipherInfos);
		this.notifyDataSetChanged();
	}

	/* Set data for sublists. */
	private void setData(ArrayList<CheckPoint> checkpoints, ArrayList<CipherShortInfo> cipherInfos) {
		if (checkpoints == null)
			mCheckpoints = new ArrayList<CheckPoint>();
		else
			mCheckpoints = checkpoints;
		setData(cipherInfos);
	}

	/* Set data for Ciphers sublist. */
	private void setData(ArrayList<CipherShortInfo> cipherInfos) {
		if (cipherInfos == null) {
			mCipherInfos = new ArrayList<CipherShortInfo>();
		} else {
			mCipherInfos = cipherInfos;
		}
	}

	@Override
	protected GroupInfo getGroupInfo(int groupPosition) {
		GroupInfo gr = new GroupInfo();
		switch (groupPosition) {
		case CIPHERS_GROUP_INDEX:
			gr.setTitle(mContext.getString(R.string.drawer_ciphers, mCipherInfos.size()));
			gr.setIconRes(R.drawable.ic_menu_ciphers);
			gr.setHasExpander(true);
			break;
		case CHECKPOINTS_GROUP_INDEX:
			gr.setTitle(mContext.getString(R.string.drawer_checkpoints, mCheckpoints.size()));
			gr.setIconRes(R.drawable.ic_menu_checkpoint);
			gr.setHasExpander(true);
			gr.setHasDividerTop(true);
			break;
		case CHECK_SOLUTION_GROUP_INDEX:
			gr.setTitle(mContext.getString(R.string.drawer_check_solution));
			gr.setIconRes(R.drawable.ic_menu_check);
			break;
		case HELP_GROUP_INDEX:
			gr.setTitle(mContext.getString(R.string.drawer_help));
			gr.setIconRes(R.drawable.ic_menu_help);
			break;
		case FEEDBACK_GROUP_INDEX:
			gr.setTitle(mContext.getString(R.string.drawer_feedback));
			gr.setIconRes(R.drawable.ic_menu_feedback);
			break;
		case SETTINGS_GROUP_INDEX:
			gr.setTitle(mContext.getString(R.string.drawer_settings));
			gr.setIconRes(R.drawable.ic_menu_settings);
			gr.setHasDividerTop(true);
			break;
		case SAVE_IMAGE_GROUP_INDEX:
			gr.setTitle(mContext.getString(R.string.drawer_save_image));
			gr.setIconRes(R.drawable.ic_menu_saveimage);
			break;
		case BOARD_GROUP_INDEX:
			gr.setTitle(mContext.getString(R.string.drawer_board));
			gr.setIconRes(R.drawable.ic_menu_board);
		default:
			break;
		}
		return gr;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		switch (groupPosition) {
		case CIPHERS_GROUP_INDEX:
			return mCipherInfos.get(childPosition);
		case CHECKPOINTS_GROUP_INDEX:
			return mCheckpoints.get(childPosition);
		default:
			return null;
		}
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		switch (groupPosition) {
		case CIPHERS_GROUP_INDEX:
			return getCipherView(childPosition, convertView, parent);
		case CHECKPOINTS_GROUP_INDEX:
			return getCheckpointView(childPosition, convertView, parent);
		default:
			return null;
		}
	}

	/* Create Checkpoints sublist element view for the given position. */
	private View getCheckpointView(int childPosition, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.drawer_list_subitem, null);
		}

		CheckPoint checkpoint = mCheckpoints.get(childPosition);

		TextView mTitleView = (TextView) convertView.findViewById(R.id.subitem_title_textView);
		mTitleView.setText(checkpoint.getTitle());

		ImageButton mDeleteButton = (ImageButton) convertView.findViewById(R.id.subitem_delete_button);
		mDeleteButton.setOnClickListener(new DeleteButtonClickListener(checkpoint));
		mDeleteButton.setEnabled(true);
		mDeleteButton.setVisibility(View.VISIBLE);

		return convertView;
	}

	/** Listener to checkpoint's delete button being clicked. */
	private class DeleteButtonClickListener implements View.OnClickListener {

		private CheckPoint mCheckpoint;

		/**
		 * Constructs listener for the specific checkpoint.
		 * 
		 * @param checkpoint
		 *            Checkpoint to listen to.
		 */
		DeleteButtonClickListener(CheckPoint checkpoint) {
			mCheckpoint = checkpoint;
		}

		@Override
		public void onClick(View v) {
			mClickCallback.onCheckpointDeleted(mCheckpoint);
		}
	}

	/* Create Ciphers sublist element view for the given position. */
	private View getCipherView(int childPosition, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.drawer_list_subitem, null);
		}

		CipherShortInfo cipher = mCipherInfos.get(childPosition);

		TextView mTitleView = (TextView) convertView.findViewById(R.id.subitem_title_textView);
		mTitleView.setText(cipher.getTitle());

		ImageButton mDeleteButton = (ImageButton) convertView.findViewById(R.id.subitem_delete_button);
		mDeleteButton.setOnClickListener(new CipherDeleteButtonClickListener(cipher));
		if (cipher.isDeletable()) {
			mDeleteButton.setEnabled(true);
			mDeleteButton.setVisibility(View.VISIBLE);
		} else {
			mDeleteButton.setEnabled(false);
			mDeleteButton.setVisibility(View.GONE);
		}

		return convertView;
	}

	/** Listener to cipher's delete button being clicked. */
	private class CipherDeleteButtonClickListener implements View.OnClickListener {

		private CipherShortInfo mCipher;

		/**
		 * Constructs listener for the specific cipher.
		 * 
		 * @param cipher
		 *            Cipher to listen to.
		 */
		CipherDeleteButtonClickListener(CipherShortInfo cipher) {
			mCipher = cipher;
		}

		@Override
		public void onClick(View v) {
			mClickCallback.onCipherDeleted(mCipher);
		}
	}

	@Override
	public int getRealChildrenCount(int groupPosition) {
		switch (groupPosition) {
		case CIPHERS_GROUP_INDEX:
			return mCipherInfos.size();
		case CHECKPOINTS_GROUP_INDEX:
			return mCheckpoints.size();
		default:
			return 0;
		}
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
		case CIPHERS_GROUP_INDEX:
			mClickCallback.onCipherClicked(mCipherInfos.get(childPosition));
			return true;
		case CHECKPOINTS_GROUP_INDEX:
			mClickCallback.onCheckpointClicked(mCheckpoints.get(childPosition));
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean onGroupClick(ExpandableListView listView, View v, int groupPosition, long id) {
		switch (groupPosition) {
		case CIPHERS_GROUP_INDEX:
			break;
		case CHECKPOINTS_GROUP_INDEX:
			break;
		case HELP_GROUP_INDEX:
			mClickCallback.onHelpGroupClicked();
			return true;
		case FEEDBACK_GROUP_INDEX:
			mClickCallback.onFeedbackGroupClicked();
			return true;
		case CHECK_SOLUTION_GROUP_INDEX:
			mClickCallback.onCheckSolutionClicked();
			return true;
		case SETTINGS_GROUP_INDEX:
			mClickCallback.onSettingsGroupClicked();
			return true;
		case SAVE_IMAGE_GROUP_INDEX:
			mClickCallback.onSaveImageGroupClicked();
			return true;
		case BOARD_GROUP_INDEX:
			mClickCallback.onBoardGroupSelected();
			return true;
		default:
			break;
		}

		if (!(listView instanceof AnimatedExpandableListView))
			return false;

		if (listView.isGroupExpanded(groupPosition)) {
			((AnimatedExpandableListView) listView).collapseGroupWithAnimation(groupPosition);
		} else {
			((AnimatedExpandableListView) listView).expandGroupWithAnimation(groupPosition);
			/*
			 * int position = listView.getPositionForView(v);
			 * listView.smoothScrollToPositionFromTop(position, 0);
			 */
		}
		return true;
	}
}
