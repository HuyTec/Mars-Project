package com.marsproject.terraformingmars.block.entity;

import com.marsproject.terraformingmars.block.CableBlock;
import com.marsproject.terraformingmars.block.AirVentBlock;
import com.marsproject.terraformingmars.block.MachineBlock;
import com.marsproject.terraformingmars.block.PipeBlock;
import com.marsproject.terraformingmars.gas.GasType;
import com.marsproject.terraformingmars.atmosphere.RoomAtmosphereManager;
import com.marsproject.terraformingmars.survival.SpaceSuitService;
import com.marsproject.terraformingmars.machine.MachineMenu;
import com.marsproject.terraformingmars.machine.MachineRecipe;
import com.marsproject.terraformingmars.machine.MachineRecipeInput;
import com.marsproject.terraformingmars.machine.MachineType;
import com.marsproject.terraformingmars.machine.MachineOperation;
import com.marsproject.terraformingmars.machine.ResourceStorage;
import com.marsproject.terraformingmars.power.PowerNetworkScanner;
import com.marsproject.terraformingmars.pipe.PipeNetworkScanner;
import com.marsproject.terraformingmars.pipe.PipeNetworkSnapshot;
import com.marsproject.terraformingmars.registry.ModBlockEntities;
import com.marsproject.terraformingmars.registry.ModRecipeTypes;
import com.marsproject.terraformingmars.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.EnumMap;
import java.util.Map;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MachineBlockEntity extends BlockEntity
        implements MenuProvider, GeoBlockEntity, ResourceStorage {
    public static final int STATUS_IDLE = 0;
    public static final int STATUS_WORKING = 1;
    public static final int STATUS_NO_POWER = 2;
    public static final int STATUS_NO_INPUT = 3;
    public static final int STATUS_OUTPUT_FULL = 4;
    public static final int GAS_CAPACITY = 10_000;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ItemStackHandler items;
    private int progress;
    private int processingTime;
    private int operationTicks;
    private final EnumMap<GasType, Integer> storedResources = new EnumMap<>(GasType.class);
    private int status = STATUS_IDLE;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> processingTime;
                case 2 -> status;
                case 3 -> operationTicks;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> processingTime = value;
                case 2 -> status = value;
                case 3 -> operationTicks = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public MachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE.get(), pos, state);
        MachineType type = machineType(state);
        int inputCount = type == null ? 1 : type.inputSlotCount();
        int outputCount = type == null ? 1 : type.outputSlotCount();
        items = new ItemStackHandler(inputCount + outputCount) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return slot < inputCount;
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  MachineBlockEntity machine) {
        MachineType type = machine.getMachineType();
        if (machine.shouldAdvanceOperationClock(type)) machine.operationTicks++;
        if (machine.operationTicks >= type.operationIntervalTicks()) {
            machine.operationTicks = 0;
            machine.setStatus(machine.tryOperate());
            machine.setChanged();
        }

        Optional<RecipeHolder<MachineRecipe>> match = machine.findRecipe(level);
        if (match.isEmpty() || !machine.canAccept(match.get().value().output())) {
            machine.resetRecipeProgress();
            return;
        }

        MachineRecipe recipe = match.get().value();
        machine.processingTime = recipe.processingTimeTicks();
        if (!machine.isActive()) {
            return;
        }

        machine.progress++;
        if (machine.progress >= recipe.processingTimeTicks()) {
            machine.finishRecipe(recipe);
            machine.progress = 0;
            machine.setChanged();
            machine.syncToClient();
        }
    }

    private boolean shouldAdvanceOperationClock(MachineType type) {
        if (type.operation() != MachineOperation.WATER_EXTRACTION
                || !(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return true;
        double temperature = com.marsproject.terraformingmars.atmosphere.RoomClimateManager
                .temperatureNear(serverLevel, worldPosition)
                .orElseGet(() -> com.marsproject.terraformingmars.event.PlayerSurvivalHandler
                        .ambientTemperature(serverLevel, worldPosition));
        if (temperature < -20.0) return level.getGameTime() % 2 == 0;
        if (temperature < 5.0) return level.getGameTime() % 4 != 0;
        return true;
    }

    private Optional<RecipeHolder<MachineRecipe>> findRecipe(Level level) {
        MachineType type = getMachineType();
        ItemStack[] inputs = new ItemStack[type.inputSlotCount()];
        for (int slot = 0; slot < inputs.length; slot++) {
            inputs[slot] = items.getStackInSlot(slot);
        }
        return level.getRecipeManager().getRecipeFor(
                ModRecipeTypes.MACHINE_TYPE.get(),
                new MachineRecipeInput(type.machineTypeId(), inputs),
                level
        );
    }

    public boolean isActive() {
        return status == STATUS_WORKING;
    }

    public String getStatusTranslationKey() {
        return switch (status) {
            case STATUS_WORKING -> "message.terraforming_mars.machine_active";
            case STATUS_NO_POWER -> "message.terraforming_mars.machine_no_power";
            case STATUS_NO_INPUT -> "message.terraforming_mars.machine_no_input";
            case STATUS_OUTPUT_FULL -> "message.terraforming_mars.machine_output_full";
            default -> "message.terraforming_mars.machine_idle";
        };
    }

    public int getEnergyPerOperation() {
        return getMachineType().energyPerOperation();
    }

    public int getStoredOutputGas() {
        return getStoredResource(getOutputGasType());
    }

    public int getGasCapacity() {
        return GAS_CAPACITY;
    }

    public String getResourceSummary() {
        if (storedResources.isEmpty()) return "EMPTY";
        return storedResources.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey().name() + " " + entry.getValue() + "/" + GAS_CAPACITY)
                .collect(java.util.stream.Collectors.joining(" | "));
    }

    public GasType getOutputGasType() {
        return getMachineType().operation().outputGas();
    }

    public boolean hasGas(GasType gasType, int amount) {
        return amount >= 0 && getStoredResource(gasType) >= amount;
    }

    public boolean tryConsumeGas(GasType gasType, int amount) {
        if (amount <= 0) {
            return true;
        }
        if (!hasGas(gasType, amount)) {
            return false;
        }
        return extractResource(gasType, amount) == amount;
    }

    public int refillSuitFromOxygenBuffer(Player player, int transferLimit) {
        int storedOxygen = getStoredResource(GasType.OXYGEN);
        if (transferLimit <= 0 || storedOxygen <= 0) {
            return 0;
        }
        int offered = Math.min(transferLimit, storedOxygen);
        int accepted = SpaceSuitService.refill(SpaceSuitService.getChestplate(player), offered);
        if (accepted > 0) {
            extractResource(GasType.OXYGEN, accepted);
        }
        return accepted;
    }

    private int tryOperate() {
        MachineType type = getMachineType();
        MachineOperation operation = type.operation();
        return switch (operation) {
            case WATER_EXTRACTION -> tryExtractWater(type);
            case ELECTROLYSIS -> tryElectrolyze(type);
            case CO2_COLLECTION -> tryCollectCarbonDioxide(type);
            case SABATIER_REACTION -> trySabatier(type);
            case METHANE_HEATING -> tryHeat(type);
            case METHANE_POWER -> tryGenerateMethanePower(type);
            default -> tryLegacyGasOperation(type);
        };
    }

    private int tryLegacyGasOperation(MachineType type) {
        int produced = type.operation().outputAmount();
        int storedOutputGas = getStoredResource(type.operation().outputGas());
        if (type.operation().isAirCreator() && storedOutputGas > 0
                && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            extractResource(type.operation().outputGas(), RoomAtmosphereManager.fillFromMachine(
                    serverLevel, worldPosition, storedOutputGas));
        }
        if (!hasCapacity(type.operation().outputGas(), produced)) {
            return STATUS_OUTPUT_FULL;
        }

        if (type.operation().isAirCreator()) {
            return tryCreateAir(type);
        }
        if (type.operation().requiresAirVent() && !hasAirVentInput()) {
            return STATUS_NO_INPUT;
        }
        if (!tryConsumePower()) {
            return STATUS_NO_POWER;
        }

        insertResource(type.operation().outputGas(), produced);
        playOperationSound();
        syncToClient();
        return STATUS_WORKING;
    }

    private int tryCreateAir(MachineType type) {
        BlockState state = getBlockState();
        ResourceStorage oxygenSource = findResourceSource(
                MachineBlock.oxygenInputPipePos(worldPosition, state),
                GasType.OXYGEN,
                type.operation().oxygenInput()
        );
        ResourceStorage nitrogenSource = findResourceSource(
                MachineBlock.nitrogenInputPipePos(worldPosition, state),
                GasType.NITROGEN,
                type.operation().nitrogenInput()
        );
        if (oxygenSource == null || nitrogenSource == null) {
            return STATUS_NO_INPUT;
        }
        if (!tryConsumePower()) {
            return STATUS_NO_POWER;
        }

        // Sources were checked on the server thread immediately before this transaction.
        if (oxygenSource.extractResource(GasType.OXYGEN, type.operation().oxygenInput())
                != type.operation().oxygenInput()
                || nitrogenSource.extractResource(GasType.NITROGEN, type.operation().nitrogenInput())
                != type.operation().nitrogenInput()) {
            return STATUS_NO_INPUT;
        }
        insertResource(type.operation().outputGas(), type.operation().outputAmount());
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            int storedAir = getStoredResource(GasType.BREATHABLE_AIR);
            extractResource(GasType.BREATHABLE_AIR, RoomAtmosphereManager.fillFromMachine(
                    serverLevel, worldPosition, storedAir));
        }
        playOperationSound();
        syncToClient();
        return STATUS_WORKING;
    }

    private int tryExtractWater(MachineType type) {
        ItemStack ice = items.getStackInSlot(0);
        boolean block = ice.is(ModItems.MARS_WATER_ICE_ITEM.get());
        if (!block && !ice.is(ModItems.RAW_WATER_ICE_CHUNK.get())) return STATUS_NO_INPUT;
        int produced = block ? 900 : 225;
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            double temperature = com.marsproject.terraformingmars.atmosphere.RoomClimateManager
                    .temperatureNear(serverLevel, worldPosition)
                    .orElseGet(() -> com.marsproject.terraformingmars.event.PlayerSurvivalHandler
                            .ambientTemperature(serverLevel, worldPosition));
            if (temperature > 40.0) produced = (int) Math.floor(produced * 0.90);
        }
        if (!hasCapacity(GasType.WATER, produced)
                || !canAccept(new ItemStack(ModItems.MINERAL_RESIDUE.get()))) {
            return STATUS_OUTPUT_FULL;
        }
        if (!tryConsumePower()) return STATUS_NO_POWER;
        items.extractItem(0, 1, false);
        insertResource(GasType.WATER, produced);
        insertOutputItem(new ItemStack(ModItems.MINERAL_RESIDUE.get()));
        playOperationSound();
        return STATUS_WORKING;
    }

    private int tryElectrolyze(MachineType type) {
        if (!hasCapacity(GasType.HYDROGEN, 2_000)
                || !hasCapacity(GasType.OXYGEN, 1_000)) return STATUS_OUTPUT_FULL;
        ResourceStorage water = findResourceSource(
                MachineBlock.fluidPortPos(worldPosition, getBlockState()), GasType.WATER, 1_000);
        if (water == null) return STATUS_NO_INPUT;
        if (!tryConsumePower()) return STATUS_NO_POWER;
        if (water.extractResource(GasType.WATER, 1_000) != 1_000) return STATUS_NO_INPUT;
        insertResource(GasType.HYDROGEN, 2_000);
        insertResource(GasType.OXYGEN, 1_000);
        playOperationSound();
        return STATUS_WORKING;
    }

    private int tryCollectCarbonDioxide(MachineType type) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)
                || !serverLevel.dimension().equals(
                com.marsproject.terraformingmars.screen.TeleportHelper.MARS_LEVEL_KEY)) {
            return STATUS_NO_INPUT;
        }
        ItemStack filter = items.getStackInSlot(0);
        if (!filter.is(ModItems.AIR_FILTER.get())) return STATUS_NO_INPUT;
        if (!hasAirVentInput()) return STATUS_NO_INPUT;
        int produced = atmosphericCollectionAmount(serverLevel);
        if (!hasCapacity(GasType.CARBON_DIOXIDE, produced)) return STATUS_OUTPUT_FULL;
        if (!tryConsumePower()) return STATUS_NO_POWER;
        insertResource(GasType.CARBON_DIOXIDE, produced);
        int wear = com.marsproject.terraformingmars.weather.MarsWeatherData.get(serverLevel)
                .isDustStorm() ? 3 : 1;
        damageItem(filter, wear);
        playOperationSound();
        return STATUS_WORKING;
    }

    private int atmosphericCollectionAmount(net.minecraft.server.level.ServerLevel serverLevel) {
        double environmentMultiplier = worldPosition.getY() < 64 ? 1.15
                : worldPosition.getY() > 128 ? 0.80 : 1.0;
        if (com.marsproject.terraformingmars.weather.MarsWeatherData.get(serverLevel).isDustStorm()) {
            environmentMultiplier *= 0.40;
        }
        int nearbyCollectors = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
                worldPosition.offset(-16, -4, -16), worldPosition.offset(16, 4, 16))) {
            if (serverLevel.getBlockState(pos).getBlock() instanceof MachineBlock block
                    && block.getMachineType().operation() == MachineOperation.CO2_COLLECTION) {
                nearbyCollectors++;
            }
        }
        int sharedIntakeDivisor = Math.max(1, (nearbyCollectors + 3) / 4);
        return Math.max(1, (int) Math.round(1_000 * environmentMultiplier / sharedIntakeDivisor));
    }

    private int trySabatier(MachineType type) {
        if (!hasCapacity(GasType.METHANE, 1_000)
                || !hasCapacity(GasType.WATER, 850)) return STATUS_OUTPUT_FULL;
        ItemStack catalyst = items.getStackInSlot(0);
        if (!catalyst.is(ModItems.NICKEL_CATALYST.get())) return STATUS_NO_INPUT;
        ResourceStorage hydrogen = findResourceSource(
                MachineBlock.oxygenInputPipePos(worldPosition, getBlockState()),
                GasType.HYDROGEN, 4_000);
        ResourceStorage carbonDioxide = findResourceSource(
                MachineBlock.nitrogenInputPipePos(worldPosition, getBlockState()),
                GasType.CARBON_DIOXIDE, 1_000);
        if (hydrogen == null || carbonDioxide == null) return STATUS_NO_INPUT;
        if (!tryConsumePower()) return STATUS_NO_POWER;
        if (hydrogen.extractResource(GasType.HYDROGEN, 4_000) != 4_000
                || carbonDioxide.extractResource(GasType.CARBON_DIOXIDE, 1_000) != 1_000) {
            return STATUS_NO_INPUT;
        }
        damageCatalyst(catalyst);
        insertResource(GasType.METHANE, 1_000);
        insertResource(GasType.WATER, 850);
        playOperationSound();
        return STATUS_WORKING;
    }

    private int tryHeat(MachineType type) {
        ResourceStorage methane = findResourceSource(
                MachineBlock.oxygenInputPipePos(worldPosition, getBlockState()), GasType.METHANE, 10);
        ResourceStorage oxygen = findResourceSource(
                MachineBlock.nitrogenInputPipePos(worldPosition, getBlockState()), GasType.OXYGEN, 20);
        if (methane == null || oxygen == null) return STATUS_NO_INPUT;
        if (!tryConsumePower()) return STATUS_NO_POWER;
        if (methane.extractResource(GasType.METHANE, 10) != 10
                || oxygen.extractResource(GasType.OXYGEN, 20) != 20) return STATUS_NO_INPUT;
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            com.marsproject.terraformingmars.atmosphere.RoomClimateManager.supplyHeat(
                    serverLevel, worldPosition, 100);
        }
        playOperationSound();
        return STATUS_WORKING;
    }

    private int tryGenerateMethanePower(MachineType type) {
        if (!hasPowerStorageDemand()) return STATUS_OUTPUT_FULL;
        if (!hasCapacity(GasType.CARBON_DIOXIDE, 20)
                || !hasCapacity(GasType.WATER, 17)) return STATUS_OUTPUT_FULL;
        ResourceStorage methane = findResourceSource(
                MachineBlock.oxygenInputPipePos(worldPosition, getBlockState()), GasType.METHANE, 20);
        ResourceStorage oxygen = findResourceSource(
                MachineBlock.nitrogenInputPipePos(worldPosition, getBlockState()), GasType.OXYGEN, 40);
        if (methane == null || oxygen == null) return STATUS_NO_INPUT;
        if (methane.extractResource(GasType.METHANE, 20) != 20
                || oxygen.extractResource(GasType.OXYGEN, 40) != 40) return STATUS_NO_INPUT;
        insertResource(GasType.CARBON_DIOXIDE, 20);
        insertResource(GasType.WATER, 17);
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            com.marsproject.terraformingmars.atmosphere.RoomClimateManager.supplyHeat(
                    serverLevel, worldPosition, 80);
        }
        playOperationSound();
        return STATUS_WORKING;
    }

    private boolean hasPowerStorageDemand() {
        if (level == null || !(getBlockState().getBlock() instanceof MachineBlock block)) return false;
        for (BlockPos cablePos : block.cablePortPositions(worldPosition, getBlockState())) {
            if (!(level.getBlockState(cablePos).getBlock() instanceof CableBlock)) continue;
            if (PowerNetworkScanner.findInputUps(level, cablePos).stream()
                    .anyMatch(ups -> ups.getStoredEnergy() < ups.getEnergyCapacity())) return true;
        }
        return false;
    }

    private void damageCatalyst(ItemStack catalyst) {
        damageItem(catalyst, 1);
    }

    private void damageItem(ItemStack stack, int amount) {
        int damage = stack.getDamageValue() + amount;
        if (damage >= stack.getMaxDamage()) items.extractItem(0, 1, false);
        else stack.setDamageValue(damage);
    }

    private void insertOutputItem(ItemStack output) {
        MachineType type = getMachineType();
        for (int slot = type.inputSlotCount(); slot < items.getSlots(); slot++) {
            ItemStack existing = items.getStackInSlot(slot);
            if (existing.isEmpty()) {
                items.setStackInSlot(slot, output);
                return;
            }
            if (ItemStack.isSameItemSameComponents(existing, output)
                    && existing.getCount() < existing.getMaxStackSize()) {
                existing.grow(output.getCount());
                return;
            }
        }
    }

    private void playOperationSound() {
        if (level != null) {
            level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.BEACON_AMBIENT,
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.32F, 0.95F);
        }
    }

    private boolean hasAirVentInput() {
        if (level == null) {
            return false;
        }
        BlockPos pipePos = MachineBlock.inputPipePos(worldPosition, getBlockState());
        if (!(level.getBlockState(pipePos).getBlock() instanceof PipeBlock)) {
            return false;
        }
        return PipeNetworkScanner.scan(level, pipePos).connectedMachines().stream()
                .anyMatch(pos -> {
                    BlockState state = level.getBlockState(pos);
                    if (!(state.getBlock() instanceof AirVentBlock)) return false;
                    BlockPos sample = AirVentBlock.samplePos(pos, state);
                    return level instanceof net.minecraft.server.level.ServerLevel serverLevel
                            && !RoomAtmosphereManager.hasBreathableAir(serverLevel, sample)
                            && (serverLevel.canSeeSky(sample) || serverLevel.getBlockState(sample).isAir());
                });
    }

    private @Nullable ResourceStorage findResourceSource(BlockPos pipePos, GasType resource,
                                                         int requiredAmount) {
        if (level == null || !(level.getBlockState(pipePos).getBlock() instanceof PipeBlock)) {
            return null;
        }
        PipeNetworkSnapshot network = PipeNetworkScanner.scan(level, pipePos);
        List<ResourceStorage> outputSources = network.connectedMachines().stream()
                .sorted(java.util.Comparator.comparingLong(BlockPos::asLong))
                .filter(pos -> !pos.equals(worldPosition))
                .map(level::getBlockEntity)
                .filter(ResourceStorage.class::isInstance)
                .map(ResourceStorage.class::cast)
                .filter(source -> !(source instanceof MachineBlockEntity machine)
                        || machine.isOutputNetwork(resource, network.pipes()))
                .toList();
        return outputSources.stream()
                .filter(source -> source.getStoredResource(resource) >= requiredAmount)
                .findFirst()
                .orElse(null);
    }

    public boolean isOutputNetwork(GasType resource, Set<BlockPos> networkPipes) {
        BlockState state = getBlockState();
        MachineOperation operation = getMachineType().operation();
        BlockPos output = switch (operation) {
            case ELECTROLYSIS -> resource == GasType.OXYGEN
                    ? MachineBlock.secondaryOutputPipePos(worldPosition, state)
                    : MachineBlock.outputPipePos(worldPosition, state);
            case SABATIER_REACTION -> resource == GasType.WATER
                    ? MachineBlock.fluidPortPos(worldPosition, state)
                    : MachineBlock.outputPipePos(worldPosition, state);
            case METHANE_HEATING -> MachineBlock.outputPipePos(worldPosition, state);
            case METHANE_POWER -> resource == GasType.WATER
                    ? MachineBlock.fluidPortPos(worldPosition, state)
                    : resource == GasType.HEAT
                    ? MachineBlock.secondaryOutputPipePos(worldPosition, state)
                    : MachineBlock.outputPipePos(worldPosition, state);
            default -> MachineBlock.outputPipePos(worldPosition, state);
        };
        return networkPipes.contains(output);
    }

    @Override
    public int getStoredResource(GasType resource) {
        return storedResources.getOrDefault(resource, 0);
    }

    @Override
    public int getResourceCapacity(GasType resource) {
        return GAS_CAPACITY;
    }

    @Override
    public int insertResource(GasType resource, int amount) {
        if (amount <= 0) return 0;
        int accepted = Math.min(amount, GAS_CAPACITY - getStoredResource(resource));
        if (accepted > 0) {
            storedResources.merge(resource, accepted, Integer::sum);
            setChanged();
            syncToClient();
        }
        return accepted;
    }

    @Override
    public int extractResource(GasType resource, int amount) {
        if (amount <= 0) return 0;
        int extracted = Math.min(amount, getStoredResource(resource));
        if (extracted > 0) {
            int remaining = getStoredResource(resource) - extracted;
            if (remaining == 0) storedResources.remove(resource);
            else storedResources.put(resource, remaining);
            setChanged();
            syncToClient();
        }
        return extracted;
    }

    private boolean hasCapacity(GasType resource, int amount) {
        return amount >= 0 && getStoredResource(resource) <= GAS_CAPACITY - amount;
    }

    public boolean isPowerProducing() {
        return getMachineType().operation().isPowerGenerator() && status == STATUS_WORKING;
    }

    private boolean tryConsumePower() {
        int requestedEnergy = getEnergyPerOperation();
        if (requestedEnergy == 0) {
            return true;
        }
        if (level == null || level.isClientSide()) {
            return false;
        }

        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof MachineBlock block)) {
            return false;
        }
        Set<BlockPos> checkedUps = new HashSet<>();
        for (BlockPos cablePos : block.cablePortPositions(worldPosition, state)) {
            if (!(level.getBlockState(cablePos).getBlock() instanceof CableBlock)) {
                continue;
            }
            for (UpsBlockEntity ups : PowerNetworkScanner.findOutputUps(level, cablePos)) {
                if (checkedUps.add(ups.getBlockPos()) && ups.tryConsumeEnergy(requestedEnergy)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canAccept(ItemStack output) {
        MachineType type = getMachineType();
        for (int slot = type.inputSlotCount(); slot < items.getSlots(); slot++) {
            ItemStack existing = items.getStackInSlot(slot);
            if (existing.isEmpty()
                    || ItemStack.isSameItemSameComponents(existing, output)
                    && existing.getCount() + output.getCount() <= existing.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    private void finishRecipe(MachineRecipe recipe) {
        for (int slot = 0; slot < recipe.inputs().size(); slot++) {
            items.extractItem(slot, 1, false);
        }
        MachineType type = getMachineType();
        for (int slot = type.inputSlotCount(); slot < items.getSlots(); slot++) {
            ItemStack existing = items.getStackInSlot(slot);
            if (existing.isEmpty()) {
                items.setStackInSlot(slot, recipe.output().copy());
                break;
            }
            if (ItemStack.isSameItemSameComponents(existing, recipe.output())) {
                existing.grow(recipe.output().getCount());
                items.setStackInSlot(slot, existing);
                break;
            }
        }
    }

    private void setActive(boolean active) {
        setStatus(active ? STATUS_WORKING : STATUS_NO_POWER);
    }

    private void setStatus(int newStatus) {
        if (status != newStatus) {
            status = newStatus;
            setChanged();
            syncToClient();
        }
    }

    private void resetRecipeProgress() {
        if (progress != 0 || processingTime != 0) {
            progress = 0;
            processingTime = 0;
            setChanged();
            syncToClient();
        }
    }

    public MachineType getMachineType() {
        MachineType type = machineType(getBlockState());
        if (type == null) {
            throw new IllegalStateException("MachineBlockEntity is not attached to MachineBlock");
        }
        return type;
    }

    private static @Nullable MachineType machineType(BlockState state) {
        return state.getBlock() instanceof MachineBlock block ? block.getMachineType() : null;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ContainerData getData() {
        return data;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId,
                                                      Inventory inventory, Player player) {
        return new MachineMenu(containerId, inventory, this, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", items.serializeNBT(registries));
        tag.putInt("Progress", progress);
        tag.putInt("ProcessingTime", processingTime);
        tag.putInt("OperationTicks", operationTicks);
        CompoundTag resources = new CompoundTag();
        storedResources.forEach((resource, amount) -> resources.putInt(resource.name(), amount));
        tag.put("StoredResources", resources);
        tag.putInt("StoredOutputGas", getStoredOutputGas());
        tag.putInt("Status", status);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.deserializeNBT(registries, tag.getCompound("Items"));
        progress = Math.max(0, tag.getInt("Progress"));
        processingTime = Math.max(0, tag.getInt("ProcessingTime"));
        operationTicks = Math.max(0, tag.getInt("OperationTicks"));
        storedResources.clear();
        if (tag.contains("StoredResources")) {
            CompoundTag resources = tag.getCompound("StoredResources");
            for (GasType resource : GasType.values()) {
                int amount = Math.max(0, Math.min(GAS_CAPACITY, resources.getInt(resource.name())));
                if (amount > 0) storedResources.put(resource, amount);
            }
        } else {
            int legacyGas = Math.max(0, Math.min(GAS_CAPACITY, tag.getInt("StoredOutputGas")));
            if (legacyGas > 0) storedResources.put(getOutputGasType(), legacyGas);
        }
        status = switch (tag.getInt("Status")) {
            case STATUS_WORKING -> STATUS_WORKING;
            case STATUS_NO_POWER -> STATUS_NO_POWER;
            case STATUS_NO_INPUT -> STATUS_NO_INPUT;
            case STATUS_OUTPUT_FULL -> STATUS_OUTPUT_FULL;
            default -> STATUS_IDLE;
        };
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "machine_controller", 5, animation -> {
            MachineType type = getMachineType();
            String name = switch (status) {
                case STATUS_WORKING -> type.workingAnimation();
                case STATUS_NO_POWER -> type.noPowerAnimation();
                default -> type.idleAnimation();
            };
            animation.setAnimation(RawAnimation.begin().thenLoop(name));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
