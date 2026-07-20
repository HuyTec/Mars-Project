# Bug Review Report

## Summary
A concrete bug is present in the mod’s item asset pipeline: the custom item `mars_beacon` is registered in code, but its model asset is missing. This causes the client to log a model-loading warning and the item will not render correctly in-game.

## Evidence
- The item is registered in [src/main/java/com/marsproject/terraformingmars/TerraformingMarsMod.java](src/main/java/com/marsproject/terraformingmars/TerraformingMarsMod.java).
- The item registration points to the ID `mars_beacon`.
- The runtime log contains this warning:

> Unable to load model: 'terraforming_mars:mars_beacon#inventory' referenced from: terraforming_mars:mars_beacon#inventory: java.io.FileNotFoundException: terraforming_mars:models/item/mars_beacon.json

- The expected asset file is missing from [src/main/resources/assets/terraforming_mars/models/item](src/main/resources/assets/terraforming_mars/models/item).

## Root Cause
The mod code registers the item successfully, but the corresponding resource definition was never added. In Minecraft/NeoForge, a registered item still needs a valid model JSON and texture path to display properly.

## Impact
- The item may appear as a missing model or missing texture in the inventory and creative tab.
- Players can still receive or use the item in some cases, but the visual experience is broken.
- This reduces polish and makes the mod feel incomplete.

## Recommended Fix
1. Create a model file at [src/main/resources/assets/terraforming_mars/models/item/mars_beacon.json](src/main/resources/assets/terraforming_mars/models/item/mars_beacon.json).
2. Add a matching texture file under [src/main/resources/assets/terraforming_mars/textures/item](src/main/resources/assets/terraforming_mars/textures/item).
3. Add a localization entry for the item name in [src/main/resources/assets/terraforming_mars/lang/en_us.json](src/main/resources/assets/terraforming_mars/lang/en_us.json).
4. Re-run the game and confirm the item appears correctly in the inventory.

## Secondary Issue Observed
A second issue is also visible in the mod initialization code: the key-mapping registration callback is never attached to the event bus. In [src/main/java/com/marsproject/terraformingmars/TerraformingMarsMod.java](src/main/java/com/marsproject/terraformingmars/TerraformingMarsMod.java), the method `registerKeyMappings` exists, but the constructor does not call `modEventBus.addListener(this::registerKeyMappings)`. As a result, the custom cutscene key binding may fail to register.

## Verification Notes
- The project compiles successfully with Gradle.
- The asset bug is confirmed by the runtime log warning above.
