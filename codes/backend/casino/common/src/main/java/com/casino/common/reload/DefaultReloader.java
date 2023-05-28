package com.casino.common.reload;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DefaultReloader implements Reloader {
    private final Map<CompletableFuture<Reload>, Reload> reloads;

    public DefaultReloader() {
        this.reloads = new HashMap<>();
    }

    @Override
    public CompletableFuture<Reload> addPendingReload(Reload pendingReload) {
        CompletableFuture<Reload> cf = new CompletableFuture<>();
        this.reloads.put(cf, pendingReload);
        return cf;
    }

    @Override
    public synchronized void completePendingReloads() {
        for (Map.Entry<CompletableFuture<Reload>, Reload> entry : reloads.entrySet())
            reload(entry.getKey(), entry.getValue());
        reloads.clear();
    }

    private void reload(CompletableFuture<Reload> reloadFuture, Reload reloadData) {
    	//TODO if not sitting in table anymore.
        Reloadable reloadPlayer = reloadData.getInput().reloadable();
        BigDecimal usedAmount = BigDecimal.ZERO;
        if (reloadPlayer != null)
            usedAmount = reloadPlayer.tryFillUpToLimit(reloadData.getInput().reloadAmountAttempt(), reloadData.getInput().upToLimit());
        reloadData.setUsedAmount(usedAmount);
        reloadFuture.complete(reloadData);
    }

    @Override
    public boolean hasPendingReloads() {
        return !reloads.isEmpty();
    }

}
