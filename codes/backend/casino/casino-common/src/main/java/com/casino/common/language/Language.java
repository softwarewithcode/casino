package com.casino.common.language;

public enum Language {

	ENGLISH(1);

	private int language;

	private Language(int numberFromDb) {
		this.language = numberFromDb;
	}

	public int getLanguage() {
		return language;
	}
}
