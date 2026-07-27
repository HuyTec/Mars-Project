package com.marsproject.terraformingmars.client;

import com.marsproject.terraformingmars.network.MarsEnvironmentSyncPayload;

public class ClientMarsEnvironmentData {
    private static MarsEnvironmentSyncPayload latest = null;

    public static void update(MarsEnvironmentSyncPayload payload) {
        latest = payload;
    }

    public static MarsEnvironmentSyncPayload get() {
        return latest;
    }
}
