package com.casino.web.common;

import com.casino.common.reload.Reload;
import com.casino.common.user.Bridge;
import com.casino.common.validation.Validator;
import com.casino.service.user.UserService;
import com.casino.web.holdem.CasinoMessage;
import jakarta.inject.Inject;
import jakarta.websocket.Session;

import java.math.BigDecimal;
import java.util.UUID;

public class CommonEndpoint {

    @Inject
    private UserService userService;
    protected Bridge bridge;
    protected UUID tableId;

    protected void validateMessageExist(CasinoMessage message) {
        if (!containsMessage(message))
            throw new IllegalArgumentException("CommonEndpoint: message is missing");
    }

    protected boolean containsMessage(CasinoMessage message) {
        return message != null && message.getAction() != null;
    }

    protected void buildAndConnectBridge(Session session) {
        bridge = userService.createGuestPlayerBridge(tableId, session);
    }

    protected boolean isBridgeConnecting(Session session) {
        return this.bridge != null && session.isOpen();
    }

    public void setTableId(UUID tableId) {
        this.tableId = tableId;
    }

    protected void onOpen(String tableId2) {
        UUID id = Validator.validateId(tableId2);
        this.tableId = id;
    }

    protected void tearDown() {
        this.tableId = null;
        this.bridge = null;
    }

    public Bridge getBridge() {
        return bridge;
    }

    public UUID getTableId() {
        return tableId;
    }

    protected BigDecimal tryWithdrawFromWallet(BigDecimal amount) {
        return userService.withdrawFromWallet(bridge.userId(), amount);
    }

    protected void finalizeReload(Reload finishedReload) {
        userService.finalizeReload(finishedReload);
    }
}
