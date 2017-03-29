package com.deakishin.zodiac.controller.avatarscreen;

import java.util.ArrayList;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.model.avatargenerator.AvatarPart;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/** Adapter for the list of different avatar parts. */
public class FacePartsViewAdapter extends RecyclerView.Adapter<FacePartsViewAdapter.ViewHolder> {

	/* Parts to display. */
	private ArrayList<AvatarPart> mDataset;

	/*
	 * First element in the displayed list. Elements from mDataset are placed
	 * after it.
	 */
	// private AvatarPart mFirstItem;

	/* Id of the selected part. */
	private int mSelectedId = -1;

	/* Application context. */
	private Context mContext;

	/**
	 * View holder that holds a reference to a view for a specific data item.
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

	/* Listener to the part selection. */
	private OnItemClickListener mOnItemClickListener;

	/**
	 * The listener interface for receiving callbacks when a part is selected.
	 */
	public static interface OnItemClickListener {
		/**
		 * Invoked when a part is selected.
		 * 
		 * @param avatarPart
		 *            selected part.
		 */
		void onItemClicked(AvatarPart avatarPart);
	}

	public FacePartsViewAdapter(Context context) {
		mContext = context.getApplicationContext();
	}

	/**
	 * Constucts adapter for the given dataset.
	 * 
	 * @param context
	 *            Application context.
	 * @param dataset
	 *            List of available parts.
	 */
	public FacePartsViewAdapter(Context context, ArrayList<AvatarPart> dataset) {
		this(context);
		setDataset(dataset);
	}

	/**
	 * Changes displayed parts.
	 * 
	 * @param dataset
	 *            List of new parts.
	 */
	public void setDataset(ArrayList<AvatarPart> dataset) {
		mDataset = dataset;
		this.notifyDataSetChanged();
	}

	/**
	 * Add a part in the head of the displayed list.
	 * 
	 * @param firstItem
	 *            part to add in the head.
	 */
	public void setFirstItem(AvatarPart firstItem) {
		ArrayList<AvatarPart> newDataset = new ArrayList<AvatarPart>();
		newDataset.addAll(mDataset);
		newDataset.add(0, firstItem);
		mDataset = newDataset;
		this.notifyDataSetChanged();
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		mOnItemClickListener = onItemClickListener;
	}

	/**
	 * Changes selected part.
	 * 
	 * @param selectedId
	 *            Id of the selected part.
	 */
	public void setSelectedId(int selectedId) {
		mSelectedId = selectedId;
		this.notifyDataSetChanged();
	}

	// Create new views (invoked by the layout manager)
	@Override
	public FacePartsViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.avatar_creating_face_parts_list_item, parent,
				false);
		ViewHolder vh = new ViewHolder(v);
		return vh;
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		View v = holder.getView();

		AvatarPart part = mDataset.get(position);

		View panel = v.findViewById(R.id.avatar_creating_face_parts_list_item_panel);
		panel.setOnClickListener(new OnPanelClickListener(position));

		ImageView imageView = (ImageView) v.findViewById(R.id.avatar_creating_face_parts_list_item_imageView);
		imageView.setImageBitmap(part.getHeader());

		View borderView = v.findViewById(R.id.avatar_creating_face_parts_list_item_border);

		int bgColorResId;
		if (part.getId() == mSelectedId) {
			borderView.setVisibility(View.VISIBLE);
			bgColorResId = R.color.avatar_creating_part_bg_selected;
		} else {
			borderView.setVisibility(View.GONE);
			bgColorResId = R.color.avatar_creating_part_bg_normal;
		}
		panel.setBackgroundColor(ContextCompat.getColor(mContext, bgColorResId));
	}

	/** Listener to panel (within a part view) being clicked. */
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
