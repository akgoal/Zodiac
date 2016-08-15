package com.example.zodiac.model.help;

import java.util.ArrayList;

import com.example.zodiac.R;

import android.content.Context;

public class HelpInfoLab {
	/*
	 * Синглетон-класс, содержащий информацию справки.
	 */

	/* Страницы справки. */
	private ArrayList<HelpInfo> mInfos;

	private static HelpInfoLab sHelpInfoLab;

	public static HelpInfoLab getInstance(Context context) {
		if (sHelpInfoLab == null)
			sHelpInfoLab = new HelpInfoLab(context.getApplicationContext());
		return sHelpInfoLab;
	}

	private HelpInfoLab(Context c) {
		mInfos = new ArrayList<HelpInfo>();

		mInfos.add(new HelpInfo(c.getString(R.string.help_page_1, c.getString(R.string.app_name))));
		mInfos.add(new HelpInfo(c.getString(R.string.help_page_2)));
		mInfos.add(new HelpInfo(c.getString(R.string.help_page_3, c.getString(R.string.remember),
				c.getString(R.string.retrieve), c.getString(R.string.clear))));
		mInfos.add(new HelpInfo(c.getString(R.string.help_page_4, c.getString(R.string.search),
				c.getString(R.string.keep_binding), c.getString(R.string.homophonic))));
		mInfos.add(new HelpInfo(c.getString(R.string.help_page_5)));
	}

	/* Количество страниц справки. */
	public int getSize() {
		return mInfos.size();
	}

	/* Получение справки по индексу. */
	public String getHelpInfo(int index) {
		return mInfos.get(index).getText();
	}

	public ArrayList<HelpInfo> getHelpInfos() {
		return mInfos;
	}

	/* Страница справки. */
	public static class HelpInfo {
		/* Текст справки. */
		private String mText;

		public HelpInfo(String text) {
			mText = text;
		}

		public String getText() {
			return mText;
		}

		public void setText(String text) {
			mText = text;
		}
	}

}
