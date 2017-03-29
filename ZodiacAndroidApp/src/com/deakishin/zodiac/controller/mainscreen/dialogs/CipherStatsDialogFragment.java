package com.deakishin.zodiac.controller.mainscreen.dialogs;

import java.util.ArrayList;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.CustomDialogFragment;
import com.deakishin.zodiac.model.ciphermanager.CipherManager;
import com.deakishin.zodiac.model.ciphermodel.Model;
import com.deakishin.zodiac.model.ciphermodel.SymbModel;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Dialog that displays current cipher's symbol stats. Current cipher is
 * accesses through {@link CipherManager}.
 */
public class CipherStatsDialogFragment extends CustomDialogFragment {

	/* Cipher's model. */
	private Model mModel;

	public CipherStatsDialogFragment() {
		super();
		mModel = CipherManager.getInstance(getActivity()).getCipherModel();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_stats, null);

		TextView totalCountTextView = (TextView) v.findViewById(R.id.stats_dialog_total_textView);
		totalCountTextView
				.setText(getActivity().getString(R.string.stats_dialog_total_symbols, mModel.getSymbolCount()));

		GridView gridView = (GridView) v.findViewById(R.id.stats_dialog_symbols_gridView);
		gridView.setAdapter(new SymbolStatsAdapter(mModel.getSymbolsByCount()));

		AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(v).setPositiveButton(R.string.ok, null)
				.create();
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	/** Adapter for the list (or grid) of symbols' stats. */
	private class SymbolStatsAdapter extends BaseAdapter {

		/* Symbols infos that are being displayed. */
		private ArrayList<SymbModel.SymbInfo> mSymbInfos;

		/**
		 * Constructs adapter.
		 * 
		 * @param symbInfos
		 *            List of symbols infos that has to be displayed.
		 */
		public SymbolStatsAdapter(ArrayList<SymbModel.SymbInfo> symbInfos) {
			mSymbInfos = symbInfos;
		}

		@Override
		public int getCount() {
			if (mSymbInfos == null)
				return 0;
			return mSymbInfos.size();
		}

		@Override
		public Object getItem(int position) {
			return mSymbInfos.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.dialog_stats_item, null);
			}

			SymbModel.SymbInfo symb = (SymbModel.SymbInfo) getItem(position);

			ImageView symbImageView = (ImageView) convertView.findViewById(R.id.stats_dialog_symbol_imageView);
			symbImageView.setImageBitmap(symb.getImage());

			TextView symbCountTextView = (TextView) convertView.findViewById(R.id.stats_dialog_symbol_count_textView);
			symbCountTextView.setText(getActivity().getString(R.string.stats_dialog_symbol_count, symb.getCount()));

			return convertView;
		}
	}
}
