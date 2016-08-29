package com.deakishin.zodiac.model.settings;

import com.deakishin.zodiac.R;

public class CheckpointNameOptions {
	/* Варианты именования чекпоинтов. */

	public final static int SERIAL_NUMBER = 0;
	public final static int CREATION_TIME = 1;
	public final static int CUSTOM = 2;

	private static final CheckpointNameOption DEF_OPTION;

	private static final CheckpointNameOption[] OPTIONS = {
			DEF_OPTION = new CheckpointNameOption(SERIAL_NUMBER, R.string.checkpointname_option_serialnumber),
			new CheckpointNameOption(CREATION_TIME, R.string.checkpointname_option_time),
			new CheckpointNameOption(CUSTOM, R.string.checkpointname_option_custom) };

	public static CheckpointNameOption[] getOptions() {
		return OPTIONS;
	}

	/* Вариант по коду. */
	public static CheckpointNameOption getOption(int code) {
		for (CheckpointNameOption opt : OPTIONS) {
			if (opt.getCode() == code)
				return opt;
		}
		return null;
	}

	/* Вариант по умолчанию. */
	public static CheckpointNameOption getDefaultOption() {
		return DEF_OPTION;
	}

	/* Индекс варианта в массиве. -1, если вариант не найден. */
	public static int getOptionIndex(CheckpointNameOption сheckpointNameOption) {
		int code = сheckpointNameOption.getCode();
		for (int i = 0; i < OPTIONS.length; i++) {
			if (OPTIONS[i].getCode() == code)
				return i;
		}
		return -1;
	}
	
	public static CheckpointNameOption getOptionByIndex(int index){
		if (index < 0 || index >= OPTIONS.length)
			return DEF_OPTION;
		return OPTIONS[index];
	}
}
