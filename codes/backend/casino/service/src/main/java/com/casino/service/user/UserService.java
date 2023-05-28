package com.casino.service.user;

import com.casino.common.functions.Functions;
import com.casino.common.reload.Reload;
import com.casino.common.user.Bridge;
import jakarta.enterprise.context.Dependent;
import jakarta.websocket.Session;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@Dependent
public class UserService {
    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());
    private static final AtomicInteger guestCount = new AtomicInteger();

    public Bridge createGuestPlayerBridge(UUID tableId, Session session) {
        return createDefaultGuestPlayerBridge(tableId, session);
    }

    private Bridge createDefaultGuestPlayerBridge(UUID tableId, Session session) {
        UUID randomGuestId = UUID.randomUUID();
        int guestNumber = guestCount.incrementAndGet();
        return new Bridge("guest" + guestNumber, tableId, randomGuestId, session, new BigDecimal("1000.0"));
    }

    public BigDecimal withdrawFromWallet(UUID playerId, BigDecimal reloadAmount) {
        if (verifyBalanceFromWalletOrLocalBank(playerId, reloadAmount)) 
            return reloadAmount;
         else
            throw new IllegalArgumentException("No balance in bank or wallet etc..");
    }

    private boolean verifyBalanceFromWalletOrLocalBank(UUID playerId, BigDecimal reloadAmount) {
        if (Functions.isFirstMoreThanSecond.apply(BigDecimal.ZERO, reloadAmount))
            throw new IllegalArgumentException("Negative reload not allowed");
        return true; // In reality check from database
    }

    public void finalizeReload(Reload finishedReload) {
        LOGGER.info("UserService completes reload. Used amount " + finishedReload.getUsedAmount() + " tookOriginally from Wallet:" + finishedReload.getInput().reloadAmountAttempt()+ " Id="+finishedReload.getId());
        //Update walletBalance if requireds
    }
}
