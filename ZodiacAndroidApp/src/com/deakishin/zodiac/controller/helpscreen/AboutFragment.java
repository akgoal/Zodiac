package com.deakishin.zodiac.controller.helpscreen;

import com.deakishin.zodiac.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/** Fragment for displaying About info. */
public class AboutFragment extends Fragment{
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.help_fragment_about, parent, false);
		return v;
	}
}
