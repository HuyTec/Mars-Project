package com.marsproject.terraformingmars.client;

import com.marsproject.terraformingmars.network.MarsEnvironmentSyncPayload;

public class ClientMarsEnvironmentData {
    private static MarsEnvironmentSyncPayload latest = null;

    public static void update(MarsEnvironmentSyncPayload payload) {
        System.out.println("CLIENT RECEIVED progress=" + payload.terraformProgress());
        latest = payload;
    }

    public static MarsEnvironmentSyncPayload get() {
        return latest;
    }
}