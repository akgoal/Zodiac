package com.deakishin.zodiac.controller.helpscreen;

import java.util.ArrayList;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.model.help.HelpInfoLab;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/** Fragment for displaying Help information. */
public class HelpFragment extends Fragment {

	/* Object containing Help information. */
	private HelpInfoLab mHelpLab;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mHelpLab = HelpInfoLab.getInstance(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.help_fragment_list, parent, false);

		ListView list = (ListView) v.findViewById(R.id.help_list);
		list.setAdapter(new HelpListAdapter(mHelpLab.getHelpSections()));
		return v;
	}

	/** Adapter for the list of the Help elements. */
	private class HelpListAdapter extends BaseAdapter {

		/* Help sections. Each has title and text. */
		private ArrayList<HelpInfoLab.HelpSection> mSections;

		public HelpListAdapter(ArrayList<HelpInfoLab.HelpSection> helpSections) {
			super();
			mSections = helpSections;
		}

		@Override
		public int getCount() {
			return mSections.size();
		}

		@Override
		public Object getItem(int position) {
			return mSections.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.help_fragment_list_item, null);
			}

			HelpInfoLab.HelpSection section = (HelpInfoLab.HelpSection) getItem(position);

			TextView title = (TextView) convertView.findViewById(R.id.help_list_item_header);
			title.setText(section.getTitle());
			TextView content = (TextView) convertView.findViewById(R.id.help_list_item_content);
			content.setText(section.getText());
			return convertView;
		}

	}
}
