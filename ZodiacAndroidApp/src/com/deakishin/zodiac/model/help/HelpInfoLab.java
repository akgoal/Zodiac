package com.deakishin.zodiac.model.help;

import java.util.ArrayList;

import com.deakishin.zodiac.R;

import android.content.Context;

/** Singleton that provides info for the Help screen. */
public class HelpInfoLab {

	/* Pages of Help. */
	private ArrayList<HelpText> mInfos;

	/* Sections of Help. */
	private ArrayList<HelpSection> mSections;

	private static HelpInfoLab sHelpInfoLab;

	/**
	 * @param context
	 *            Application context.
	 * @return The sole instance of the singleton.
	 */
	public static HelpInfoLab getInstance(Context context) {
		if (sHelpInfoLab == null)
			sHelpInfoLab = new HelpInfoLab(context.getApplicationContext());
		return sHelpInfoLab;
	}

	private HelpInfoLab(Context c) {
		HelpText page1 = new HelpText(c, R.string.help_page_1, R.string.app_name);
		HelpText page2 = new HelpText(c, R.string.help_page_2, R.string.drawer_settings);
		HelpText page3 = new HelpText(c, R.string.help_page_3, R.string.checkpoint, R.string.drawer_settings,
				R.string.revert, R.string.clear);
		HelpText page4 = new HelpText(c, R.string.help_page_4, R.string.search, R.string.search_keep_binding,
				R.string.search_homophonic);
		HelpText page5 = new HelpText(c, R.string.help_page_5, R.string.drawer_check_solution);
		HelpText page6 = new HelpText(c, R.string.help_page_6, R.string.drawer_save_image,
				R.string.ext_storage_folder_name, R.string.ext_storage_image_folder_name);
		HelpText page7 = new HelpText(c, R.string.help_page_7, R.string.board, R.string.drawer_board,
				R.string.board_item_import, R.string.drawer_ciphers_title, R.string.board_item_show_solution);
		HelpText page8 = new HelpText(c, R.string.help_page_8);
		HelpText page9 = new HelpText(c, R.string.help_page_9, R.string.log_in_sign_up, R.string.board);
		HelpText page10 = new HelpText(c, R.string.help_page_10);

		HelpText title1 = new HelpText(c, R.string.help_page_1_title);
		HelpText title2 = new HelpText(c, R.string.help_page_2_title);
		HelpText title3 = new HelpText(c, R.string.help_page_3_title);
		HelpText title4 = new HelpText(c, R.string.help_page_4_title, R.string.board);
		HelpText title5 = new HelpText(c, R.string.help_page_5_title);
		HelpText title6 = new HelpText(c, R.string.help_page_6_title);
		HelpText title7 = new HelpText(c, R.string.help_page_7_title);

		HelpSection HelpSection1 = new HelpSection(title1.getText(), page1.getText());
		HelpSection HelpSection2 = new HelpSection(title2.getText(),
				page2.join(page3).join(page4).join(page5).getText());
		HelpSection HelpSection3 = new HelpSection(title3.getText(), page6.getText());
		HelpSection HelpSection4 = new HelpSection(title4.getText(), page7.getText());
		HelpSection HelpSection5 = new HelpSection(title5.getText(), page8.getText());
		HelpSection HelpSection6 = new HelpSection(title6.getText(), page9.getText());
		HelpSection HelpSection7 = new HelpSection(title7.getText(), page10.getText());

		mSections = new ArrayList<HelpSection>();
		mSections.add(HelpSection1);
		mSections.add(HelpSection2);
		mSections.add(HelpSection3);
		mSections.add(HelpSection4);
		mSections.add(HelpSection5);
		mSections.add(HelpSection6);
		mSections.add(HelpSection7);

		mInfos = new ArrayList<HelpText>();
		mInfos.add(page1);
		mInfos.add(page2);
		mInfos.add(page3);
		mInfos.add(page4);
		mInfos.add(page5);
		mInfos.add(page6);
		mInfos.add(page7);
		mInfos.add(page8);
		mInfos.add(page9);
		mInfos.add(page10);
	}

	/** @return Number of pages. */
	public int getSize() {
		return mInfos.size();
	}

	/**
	 * Provides a page of the Help info by its index.
	 * 
	 * @param Index
	 *            of the page.
	 * @return Text of the page of the Help info.
	 */
	public CharSequence getHelpInfo(int index) {
		return mInfos.get(index).getText();
	}

	/** @return List of all the pages of Help info. */
	public ArrayList<HelpText> getHelpInfos() {
		return mInfos;
	}

	/** @return List of sections of the Help info. */
	public ArrayList<HelpSection> getHelpSections() {
		return mSections;
	}

	/** Page of the Help info. Contains just text. */
	public static class HelpText {
		/* Page text. */
		private String mText;

		public HelpText() {
		}

		/**
		 * Constructs a page.
		 * 
		 * @param c
		 *            Application context.
		 * @param mainResId
		 *            Id of the main String resource.
		 * @param resIds
		 *            Ids of the String resources that must be embedded to the
		 *            main text.
		 */
		public HelpText(Context c, int mainResId, int... resIds) {
			if (resIds == null) {
				mText = String.valueOf(c.getText(mainResId));
				return;
			}

			String[] params = new String[resIds.length];
			for (int i = 0; i < resIds.length; i++) {
				params[i] = c.getString(resIds[i]);
			}

			mText = c.getString(mainResId, params);
		}

		/**
		 * Combines the page with another page.
		 * 
		 * @param helpText
		 *            Page to combine with.
		 * @return Combined page.
		 */
		public HelpText join(HelpText helpText) {
			HelpText res = new HelpText();
			res.setText(mText + "\n" + helpText.getText());
			return res;
		}

		/**
		 * Construct a page with a text.
		 * 
		 * @param text
		 *            Text that the page will contain.
		 */
		public HelpText(String text) {
			mText = text;
		}

		/** Text of the page. */
		public String getText() {
			return mText;
		}

		/**
		 * Sets the text of the page.
		 * 
		 * @param text
		 *            Text to set.
		 */
		public void setText(String text) {
			mText = text;
		}
	}

	/** Section of the Help info. Contains a title and text. */
	public static class HelpSection {
		/* Title. */
		private String mTitle;
		/* Text. */
		private String mText;

		public HelpSection(String title, String text) {
			super();
			mTitle = title;
			mText = text;
		}

		public String getTitle() {
			return mTitle;
		}

		public void setTitle(String title) {
			mTitle = title;
		}

		public String getText() {
			return mText;
		}

		public void setText(String text) {
			mText = text;
		}
	}

}
