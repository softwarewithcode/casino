package com.casino.blackjack.table;

import java.math.BigDecimal;
import java.util.List;

import com.casino.common.language.Language;
import com.casino.common.table.Player;
import com.casino.common.table.Table;

public class BlackjackTable implements Table {

	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void open() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onOpen() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getMinPlayers() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxPlayers() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BigDecimal getMinBet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigDecimal getMaxBet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getAvailableSeats() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Language getLanguage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Player getPlayerInTurn() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addPlayer(Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addWatcher(Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removePlayer(Player p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeWatcher(Player p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Player> getPlayers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Player> getWatchers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Player> getPlayersAndWatchers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getAITurnTimeInMillis() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getPlayerTurnTimeInMillis() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void startTime(int initialDelay) {
		// TODO Auto-generated method stub
		
	}

}
