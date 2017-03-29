package com.deakishin.zodiac.controller.avatarscreen;

import java.util.ArrayList;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.model.avatargenerator.AvatarPartOption;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Adapter for the list of options for an avatar part.
 */
public class FacePartOptionsViewAdapter extends RecyclerView.Adapter<FacePartOptionsViewAdapter.ViewHolder> {

	/* Options to display. */
	private ArrayList<AvatarPartOption> mDataset;

	/* Id of the selected option. */
	private int mSelectedId = -1;

	/* Application context. */
	private Context mContext;

	/**
	 * View holder that holds a reference to a view for a
	 * specific data item.
	 */
	public static class ViewHolder extends RecyclerView.ViewHolder {
		private View mView;

		public ViewHolder(View view) {
			super(view);
			mView = view;
		}

		public View getView() {
			return mView;
		}
	}

	/* Option selection listener. */
	private OnItemClickListener mOnItemClickListener;

	/**
	 * The listener interface for receiving callbacks when an option is
	 * selected.
	 */
	public static interface OnItemClickListener {
		/**
		 * Invoked when an option is selected.
		 * 
		 * @param avatarPartOption
		 *            selected option.
		 */
		void onItemClicked(AvatarPartOption avatarPartOption);
	}

	public FacePartOptionsViewAdapter(Context context) {
		mContext = context.getApplicationContext();
	}

	/**
	 * Constucts adapter for the given dataset.
	 * 
	 * @param context
	 *            Application context.
	 * @param dataset
	 *            List of available options.
	 */
	public FacePartOptionsViewAdapter(Context context, ArrayList<AvatarPartOption> dataset) {
		this(context);
		setDataset(dataset);
	}

	/**
	 * Changes displayed options.
	 * 
	 * @param dataset
	 *            List of new options.
	 */
	public void setDataset(ArrayList<AvatarPartOption> dataset) {
		mDataset = dataset;
		this.notifyDataSetChanged();
	}

	/**
	 * Changes selected option.
	 * 
	 * @param selectedId
	 *            Id of the selected option.
	 */
	public void setSelectedId(int selectedId) {
		mSelectedId = selectedId;
		this.notifyDataSetChanged();
	}

	/*
	 * Возвращает позицию для варианта с заданным идентификатором. -1, если
	 * вариант не найден.
	 */
	/**
	 * Returns position in the option list for the oprion with given id.
	 * 
	 * @param optionId
	 *            Id of the option.
	 * @return Position in the list, starting from 0. Returns -1 if the option
	 *         is not found.
	 */
	public int getPositionForId(int optionId) {
		if (mDataset == null)
			return -1;

		for (int i = 0; i < mDataset.size(); i++) {
			if (mDataset.get(i).getId() == optionId)
				return i;
		}
		return -1;
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		mOnItemClickListener = onItemClickListener;
	}

	// Create new views (invoked by the layout manager)
	@Override
	public FacePartOptionsViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.avatar_creating_face_parts_list_item, parent,
				false);
		ViewHolder vh = new ViewHolder(v);
		return vh;
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		View v = holder.getView();

		AvatarPartOption option = mDataset.get(position);

		View panel = v.findViewById(R.id.avatar_creating_face_parts_list_item_panel);
		panel.setOnClickListener(new OnPanelClickListener(position));

		ImageView imageView = (ImageView) v.findViewById(R.id.avatar_creating_face_parts_list_item_imageView);
		imageView.setImageBitmap(option.getBitmap());

		View borderView = v.findViewById(R.id.avatar_creating_face_parts_list_item_border);

		int bgColorResId;
		if (option.getId() == mSelectedId) {
			borderView.setVisibility(View.VISIBLE);
			bgColorResId = R.color.avatar_creating_part_bg_selected;
		} else {
			borderView.setVisibility(View.GONE);
			bgColorResId = R.color.avatar_creating_part_bg_normal;
		}
		panel.setBackgroundColor(ContextCompat.getColor(mContext, bgColorResId));
	}

	/** Listener to panel (within an option view) being clicked. */
	private class OnPanelClickListener implements View.OnClickListener {

		private int mPosition;

		public OnPanelClickListener(int position) {
			mPosition = position;
		}

		@Override
		public void onClick(View v) {
			if (mOnItemClickListener != null && mDataset != null) {
				mOnItemClickListener.onItemClicked(mDataset.get(mPosition));
			}
		}
	}

	// Return the size of the dataset (invoked by the layout manager)
	@Override
	public int getItemCount() {
		if (mDataset == null)
			return 0;
		return mDataset.size();
	}

}
