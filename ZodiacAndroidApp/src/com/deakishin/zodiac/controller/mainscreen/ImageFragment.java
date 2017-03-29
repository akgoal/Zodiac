package com.deakishin.zodiac.controller.mainscreen;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.mainscreen.dialogs.CipherStatsDialogFragment;
import com.deakishin.zodiac.controller.mainscreen.dialogs.SymbolDialogFragment;
import com.deakishin.zodiac.controller.mainscreen.dialogs.searchdialog.SearchDialogFragment;
import com.deakishin.zodiac.model.ciphermanager.CipherManager;
import com.deakishin.zodiac.model.ciphermodel.bindingmanager.BindingManager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Fragment for displaying main cipher image to interact with and the search
 * results panel.
 */
public class ImageFragment extends Fragment {

	/* Ids for dialogs. */
	private static final String DIALOG_SYMBOL = "symbol";
	private static final String DIALOG_SEARCH = "search";
	private static final String DIALOG_STATS = "stats";

	/* Request codes for child fragments. */
	private static final int REQUEST_SYMBOL = 0;
	private static final int REQUEST_SEARCH = 1;

	/* Widgets and menu items. */
	private MainImageView mMainImageView;

	private LinearLayout mSearchPanel;
	private LinearLayout mSearchNavigPanel;
	private TextView mSearchTitleTextView;
	private ImageButton mCloseSearchButton;
	private ImageButton mPrevResButton;
	private TextView mSearchResCounterTextView;
	private ImageButton mNextResButton;

	private MenuItem mRevertMenuItem;

	/* BindingManager contains info about symbol-letter mapping. */
	private BindingManager mBindingManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mBindingManager = CipherManager.getInstance(getActivity()).getCipherModel().getBindingManager();
	}

	@Override
	public void onResume() {
		super.onResume();
		mMainImageView.update();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_image, parent, false);
		mMainImageView = (MainImageView) v.findViewById(R.id.image_view);
		mMainImageView.setOnSymbolClickListener(new MainImageView.OnSymbolClickListener() {
			@Override
			public void onSymbolClick(int symbId) {
				FragmentManager fm = getActivity().getSupportFragmentManager();
				Character chr = mBindingManager.getCharacter(symbId);
				SymbolDialogFragment dialog = SymbolDialogFragment.newInstance(symbId, chr);
				dialog.setTargetFragment(ImageFragment.this, REQUEST_SYMBOL);
				dialog.show(fm, DIALOG_SYMBOL);
			}
		});

		mSearchPanel = (LinearLayout) v.findViewById(R.id.searchPanel);
		mSearchNavigPanel = (LinearLayout) v.findViewById(R.id.search_navigationPanel);
		mSearchTitleTextView = (TextView) v.findViewById(R.id.search_title_textView);
		mPrevResButton = (ImageButton) v.findViewById(R.id.search_prev_res_button);
		mSearchResCounterTextView = (TextView) v.findViewById(R.id.search_res_counter_textView);
		mNextResButton = (ImageButton) v.findViewById(R.id.search_next_res_button);
		mCloseSearchButton = (ImageButton) v.findViewById(R.id.search_close_button);

		updateSearchPanel();

		mPrevResButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mBindingManager.goToPrevSearchRes();
				updateSearchButtonEnabilityAndNavigText();
				updateImageView();
			}
		});

		mNextResButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mBindingManager.goToNextSearchRes();
				updateSearchButtonEnabilityAndNavigText();
				updateImageView();
			}
		});

		mCloseSearchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				clearSearchInfo();
			}
		});

		return v;
	}

	/* Clear search info and update search panel. */
	private void clearSearchInfo() {
		mBindingManager.deleteSearchResults();
		updateSearchPanel();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;
		if (requestCode == REQUEST_SYMBOL) {
			int symbId = data.getIntExtra(SymbolDialogFragment.EXTRA_SYMBOL_ID, 0);
			Character symbChr = null;
			if (data.hasExtra((SymbolDialogFragment.EXTRA_SYMBOL_CHAR)))
				symbChr = data.getCharExtra(SymbolDialogFragment.EXTRA_SYMBOL_CHAR, '_');
			mBindingManager.updateBinding(symbId, symbChr);
			updateImageView();
			return;
		}
		if (requestCode == REQUEST_SEARCH) {
			mBindingManager.goToFirstSearchRes();
			updateSearchPanel();
			updateImageView();
			return;
		}
	}

	/**
	 * Updates fragment. Search info gets cleared and main image gets updated.
	 */
	public void update() {
		clearSearchInfo();
		updateImageView();
	}

	/** Updates image's font colors according to current settings. */
	public void updateColors() {
		mMainImageView.updateColors();
	}

	/**
	 * Updates cipher model that's being used to the current one.
	 */
	public void updateModelInUse() {
		mBindingManager = CipherManager.getInstance(getActivity()).getCipherModel().getBindingManager();
		setWorkWithBindingManager();
		clearSearchInfo();
		mMainImageView.updateModelInUse();
	}

	/** Updates main image view. */
	public void updateImage() {
		updateImageView();
	}

	/* Update image view. */
	private void updateImageView() {
		mMainImageView.update();
	}

	/**
	 * @return Displayed image in the original size and with replacing letters.
	 */
	public Bitmap getBitmapImage() {
		// return mMainImageView.createBitmapImage();
		String signature = getActivity().getString(R.string.saved_image_signature,
				getActivity().getString(R.string.app_name));
		if (CipherManager.getInstance(getActivity()).getCurrentCipherInfo().isZodiac340())
			return mMainImageView.createOriginalBitmapImage(signature);
		else
			return mMainImageView.createBitmapImage(signature);
	}

	/* Update search panel with fresh data. */
	private void updateSearchPanel() {
		if (mBindingManager.hasSearchInfo()) {
			mSearchPanel.setVisibility(View.VISIBLE);
			if (mBindingManager.hasSearchResults()) {
				mSearchNavigPanel.setVisibility(View.VISIBLE);
				mSearchTitleTextView.setText(getString(R.string.search_results, mBindingManager.getSearchLine()));
				updateSearchButtonEnabilityAndNavigText();
			} else {
				mSearchNavigPanel.setVisibility(View.GONE);
				mSearchTitleTextView.setText(getString(R.string.search_no_results, mBindingManager.getSearchLine()));
			}
		} else {
			mSearchPanel.setVisibility(View.GONE);
		}
	}

	/*
	 * Update buttons' enability and search results navigation info on the
	 * search panel.
	 */
	private void updateSearchButtonEnabilityAndNavigText() {
		mPrevResButton.setEnabled(!mBindingManager.isFirstRes());
		mNextResButton.setEnabled(!mBindingManager.isLastRes());
		mSearchResCounterTextView.setText(getString(R.string.search_navig_info,
				mBindingManager.getCurrSearchIndex() + 1, mBindingManager.getSearchResSize()));
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_image, menu);
		mRevertMenuItem = menu.findItem(R.id.menu_item_revert);
		setWorkWithBindingManager();
	}

	/* Configure work with the Binding manager. */
	private void setWorkWithBindingManager() {
		mRevertMenuItem.setEnabled(mBindingManager.isRevertAvailable());
		mBindingManager.setRevertAvailabilityListener(new BindingManager.RevertAvailabilityListener() {
			@Override
			public void onRevertAvailable() {
				mRevertMenuItem.setEnabled(true);
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_search:
			FragmentManager fm = getActivity().getSupportFragmentManager();
			SearchDialogFragment dialog = new SearchDialogFragment();
			dialog.setTargetFragment(ImageFragment.this, REQUEST_SEARCH);
			dialog.show(fm, DIALOG_SEARCH);
			return true;
		case R.id.menu_item_clear:
			mBindingManager.clearBinding();
			updateImageView();
			return true;
		case R.id.menu_item_revert:
			mBindingManager.revertToCheckPoint();
			updateImageView();
			return true;
		case R.id.menu_item_stats:
			new CipherStatsDialogFragment().show(getActivity().getSupportFragmentManager(), DIALOG_STATS);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
