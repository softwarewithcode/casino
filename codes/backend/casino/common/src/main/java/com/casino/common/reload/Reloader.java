package com.casino.common.reload;

import java.util.concurrent.CompletableFuture;

/**
 * Handles reloads, reloads happen in the future.
 */
public interface Reloader {

    CompletableFuture<Reload> addPendingReload(Reload pendingReload);

    boolean hasPendingReloads();

    void completePendingReloads();
}
