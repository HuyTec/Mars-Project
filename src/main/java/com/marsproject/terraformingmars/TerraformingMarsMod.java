package com.marsproject.terraformingmars;

import com.marsproject.terraformingmars.client.ClientMarsEnvironmentData;
import com.marsproject.terraformingmars.client.MarsEnvironmentHud;
import com.marsproject.terraformingmars.command.MarsCommands;
import com.marsproject.terraformingmars.environment.MarsEnvironmentStageLoader;
import com.marsproject.terraformingmars.item.MarsBeaconItem;
import com.marsproject.terraformingmars.network.IntroFinishedPayload;
import com.marsproject.terraformingmars.network.MarsEnvironmentSyncPayload;
import com.marsproject.terraformingmars.network.OpenIntroPayload;
import com.marsproject.terraformingmars.registry.ModBlocks;
import com.marsproject.terraformingmars.registry.ModCreativeTabs;
import com.marsproject.terraformingmars.registry.ModEffects;
import com.marsproject.terraformingmars.registry.ModItems;
import com.marsproject.terraformingmars.screen.IntroScreen;
import com.marsproject.terraformingmars.screen.TeleportHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(TerraformingMarsMod.MODID)
public class TerraformingMarsMod {

    public static final String MODID = "terraforming_mars";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredItem<Item> MARS_BEACON = ITEMS.register("mars_beacon",
            () -> new MarsBeaconItem(new Item.Properties().stacksTo(1)));

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public TerraformingMarsMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);

        // Register the Deferred Register to the mod event bus so blocks get registered

        ModBlocks.register();
        ModItems.register();

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        ModEffects.EFFECTS.register(modEventBus);
        
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.AddReloadListenerEvent event) ->
                event.addListener(new MarsEnvironmentStageLoader()));

        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.RegisterCommandsEvent event) ->
                MarsCommands.register(event.getDispatcher()));

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                OpenIntroPayload.TYPE,
                OpenIntroPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    net.minecraft.client.Minecraft.getInstance().setScreen(new IntroScreen());
                })
        );

        registrar.playToServer(
                IntroFinishedPayload.TYPE,
                IntroFinishedPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                        TeleportHelper.teleportToMars(serverPlayer);
                    }
                })
        );

        registrar.playToClient(
                MarsEnvironmentSyncPayload.TYPE,
                MarsEnvironmentSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientMarsEnvironmentData.update(payload))
        );
    }

    private static final KeyMapping TEST_CUTSCENE_KEY = new KeyMapping(
            "key.terraforming_mars.test_cutscene",
            GLFW.GLFW_KEY_G,
            "key.categories.terraforming_mars"
    );

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TEST_CUTSCENE_KEY);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = TerraformingMarsMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    static class ClientModEvents {
        @SubscribeEvent
        static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }

        @SubscribeEvent
        static void onRegisterDimensionEffects(net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent event) {
            LOGGER.info(">>> ĐANG ĐĂNG KÝ MARS DIMENSION EFFECTS <<<");
            event.register(
                    new net.minecraft.resources.ResourceLocation("terraforming_mars", "mars_effects"),
                    new com.marsproject.terraformingmars.client.MarsDimensionEffects()
            );
        }

        @SubscribeEvent
        static void registerGuiLayers(RegisterGuiLayersEvent event) {
            event.registerAboveAll(
                    new ResourceLocation(
                            TerraformingMarsMod.MODID,
                            "mars_environment"
                    ),
                    new MarsEnvironmentHud()
            );
        }
    }

    @EventBusSubscriber(modid = TerraformingMarsMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class ClientGameEvents {
        @SubscribeEvent
        static void onClientTick(ClientTickEvent.Post event) {
            while (TEST_CUTSCENE_KEY.consumeClick()) {
                Minecraft.getInstance().setScreen(new IntroScreen());
            }
        }
    }
}
