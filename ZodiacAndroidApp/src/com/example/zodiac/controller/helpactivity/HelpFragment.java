package com.example.zodiac.controller.helpactivity;

import com.example.zodiac.R;
import com.example.zodiac.model.help.HelpInfoLab;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HelpFragment extends Fragment {
	/*
	 * Фрагмент, выводящий элемент-страницу справки.
	 */

	/* Ключ для хранения аргумента. */
	private static final String EXTRA_PAGE = "com.example.zodiac.controller.helpactivity.page";

	/* Выводимая информация. */
	private String mInfo;

	/* Виджет. */
	private TextView mTextView;

	private long mTouchDownTime;
	private static final long WAIT_TIME = 3000;
	private static final String DIALOG_AUTHOR = "author";

	/*
	 * Создание экземпляра фрагмента и упаковка его вместе с аргументом.
	 * Аргумент - номер выводимой страницы поиска.
	 */
	public static HelpFragment newInstance(int page) {
		Bundle args = new Bundle();
		args.putInt(EXTRA_PAGE, page);

		HelpFragment fragment = new HelpFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int page = getArguments().getInt(EXTRA_PAGE);
		mInfo = HelpInfoLab.getInstance(getActivity()).getHelpInfo(page);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_help, parent, false);

		mTextView = (TextView) v.findViewById(R.id.help_textView);
		mTextView.setText(mInfo);
		mTextView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mTouchDownTime = System.currentTimeMillis();
					break;
				case MotionEvent.ACTION_UP:
					if (System.currentTimeMillis() - mTouchDownTime > WAIT_TIME) {
						new AuthorDialog().show(getActivity().getSupportFragmentManager(), DIALOG_AUTHOR);
					}
					break;
				}
				return true;
			}
		});

		return v;
	}

	private class AuthorDialog extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_author, null);
			return new AlertDialog.Builder(getActivity()).setTitle(R.string.author_title).setView(v)
					.setPositiveButton(android.R.string.ok, null).create();
		}
	}

}
