package com.marsproject.terraformingmars.machine;

import com.marsproject.terraformingmars.gas.GasType;

public interface ResourceStorage {
    int getStoredResource(GasType resource);

    int getResourceCapacity(GasType resource);

    int insertResource(GasType resource, int amount);

    int extractResource(GasType resource, int amount);
}
