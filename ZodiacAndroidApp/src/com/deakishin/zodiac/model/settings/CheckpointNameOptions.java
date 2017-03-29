package com.deakishin.zodiac.model.settings;

import com.deakishin.zodiac.R;

/** Holds and provides access to different checkpoint naming options. */
public class CheckpointNameOptions {

	/**
	 * Code for the option of a checkpoint being named with its serial number.
	 */
	public final static int SERIAL_NUMBER = 0;
	/**
	 * Code for the option of a checkpoint being named with the time it was
	 * created.
	 */
	public final static int CREATION_TIME = 1;
	/**
	 * Code for the option of a checkpoint being named manually by user.
	 */
	public final static int CUSTOM = 2;

	/* Default option. */
	private static final CheckpointNameOption DEF_OPTION;

	/* Options. */
	private static final CheckpointNameOption[] OPTIONS = {
			DEF_OPTION = new CheckpointNameOption(SERIAL_NUMBER, R.string.checkpointname_option_serialnumber),
			new CheckpointNameOption(CREATION_TIME, R.string.checkpointname_option_time),
			new CheckpointNameOption(CUSTOM, R.string.checkpointname_option_custom) };

	/** @return Array of available options. */
	public static CheckpointNameOption[] getOptions() {
		return OPTIONS;
	}

	/**
	 * @param code
	 *            Option's code.
	 * @return Option with the given code, or null if no such option was found.
	 */
	public static CheckpointNameOption getOption(int code) {
		for (CheckpointNameOption opt : OPTIONS) {
			if (opt.getCode() == code)
				return opt;
		}
		return null;
	}

	/** @return Default option. */
	public static CheckpointNameOption getDefaultOption() {
		return DEF_OPTION;
	}

	/**
	 * @param сheckpointNameOption
	 *            Option to get index of.
	 * @return Index of the given option, or -1 if no such option was found.
	 */
	public static int getOptionIndex(CheckpointNameOption сheckpointNameOption) {
		int code = сheckpointNameOption.getCode();
		for (int i = 0; i < OPTIONS.length; i++) {
			if (OPTIONS[i].getCode() == code)
				return i;
		}
		return -1;
	}

	/**
	 * @param index
	 *            Index in the list of options.
	 * @return Option in the given place in the list of options, or the default
	 *         option if index is out of bounds.
	 */
	public static CheckpointNameOption getOptionByIndex(int index) {
		if (index < 0 || index >= OPTIONS.length)
			return DEF_OPTION;
		return OPTIONS[index];
	}
}
