package com.casino.common.cards;

import java.util.Set;

public interface IDeck {

	public void shuffle();

	public Set<Card> take(int count);
}
