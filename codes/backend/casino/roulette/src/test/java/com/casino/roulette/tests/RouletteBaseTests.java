package com.casino.roulette.tests;

import java.math.BigDecimal;
import java.util.UUID;

import com.casino.common.table.structure.TableType;
import org.junit.jupiter.api.BeforeEach;

import com.casino.common.game.phase.bet.ParallelBetPhaser;
import com.casino.common.runner.CasinoMode;
import com.casino.common.user.User;
import com.casino.roulette.game.RouletteData;
import com.casino.roulette.table.RouletteTable_;

public class RouletteBaseTests {

	protected RouletteTable_ multiPlayerTable;
	protected RouletteTable_ multiPlayerTableWithLongSpinningTime;
	protected RouletteTable_ singlePlayerTableWithLongSpinningTime;
	protected RouletteTable_ singlePlayerTable;
	protected User usr;
	protected User usr2;
	protected User singlePlayerUsr;
	protected static final BigDecimal TWENTY = new BigDecimal("20.00");
	protected static final BigDecimal FIVE = new BigDecimal("5.00");
	protected static final BigDecimal THOUSAND = new BigDecimal("1000.00");
	protected int rejected = 0;


	@BeforeEach
	public void initTest() {
		try {
			System.getProperties().setProperty(CasinoMode.TEST_RUNNER_WITH_FIXED_VALUE, "4");
			multiPlayerTable = RouletteTableFactoryForTests.createDefaultMultiplayerTable();
			singlePlayerTable = RouletteTableFactoryForTests.createDefaultSinglePlayerTable();
			multiPlayerTableWithLongSpinningTime=RouletteTableFactoryForTests.createTableWithSpinningTime(2000L, TableType.MULTIPLAYER);
			singlePlayerTableWithLongSpinningTime=RouletteTableFactoryForTests.createTableWithSpinningTime(2000L, TableType.SINGLEPLAYER);
			usr = new User("JohnDoe", multiPlayerTable.getId(), UUID.randomUUID(), null, new BigDecimal("1000"));
			usr2 = new User("JulieDoe", multiPlayerTable.getId(), UUID.randomUUID(), null, new BigDecimal("50000"));
			singlePlayerUsr = new User("JamesDoe", singlePlayerTable.getId(), UUID.randomUUID(), null, new BigDecimal("49.53"));
			rejected = 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void waitBetTime(RouletteTable_ table) {
        //single-player table has null betTime
		ParallelBetPhaser phaser = (ParallelBetPhaser) multiPlayerTable.getDealer();
		sleep(phaser.getBetPhaseTime() * 1000);
	}


	protected void waitSpinTime(RouletteTable_ table) {
		RouletteData data = (RouletteData) table.getDealer().getGameData();
		sleep(data.spinTimeMillis().intValue());
	}

	protected void waitForRoundToBeCompleted(RouletteTable_ table) {
		waitBetTime(table);
		waitSpinTime(table);
		sleep(200); //Time to complete payout
	}

	protected void sleep(int millis) {
		try {
			Thread.currentThread().join(millis);
		} catch (InterruptedException e) {
			System.err.println("TimerError");
		}
	}
}
