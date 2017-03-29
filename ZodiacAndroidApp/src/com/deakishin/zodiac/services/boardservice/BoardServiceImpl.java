package com.deakishin.zodiac.services.boardservice;

import com.deakishin.zodiac.services.boardservice.boardservicegae.BoardServiceGAE;

import android.content.Context;

/** Class that provides implementation of the board service. */
public class BoardServiceImpl {

	/**
	 * Provides access to the implementation of the board service.
	 * 
	 * @param context
	 *            Application context.
	 * @return Implementation of the board service.
	 */
	public static BoardServiceI getImpl(Context context) {
		// return BoardServiceDummy.getInstance(context);
		return new BoardServiceGAE(context);
	}
}
