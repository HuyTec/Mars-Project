# Hướng dẫn tự chỉnh sửa Terraforming Mars

Tài liệu này áp dụng cho trạng thái hiện tại của project:

- Minecraft 1.20.6
- NeoForge 20.6.139
- Java 21
- GeckoLib 4.5.4
- Mod ID: `terraforming_mars`

Không chỉnh file trong `build/`, `run/`, `.gradle/` hoặc `Install/`. Đây là file sinh tự
động, dữ liệu chạy thử hoặc runtime đóng gói. Code và tài nguyên cần sửa nằm trong
`src/main/java` và `src/main/resources`.

## 1. Chạy và kiểm tra

Trên Windows, chạy tại thư mục gốc project:

```powershell
.\gradlew.bat compileJava
.\gradlew.bat runClient
.\gradlew.bat build
```

- `compileJava`: kiểm tra code Java.
- `runClient`: mở client phát triển để kiểm tra trong game.
- `build`: tạo mod JAR trong `build/libs/`.
- Sau khi sửa JSON có thể dùng `F3 + T` để reload resource phía client. Datapack/worldgen
  thường cần `/reload`; thay đổi dimension/worldgen lớn nên tạo world mới.

## 2. Bản đồ project

| Khu vực | Nơi sửa |
|---|---|
| Đăng ký block | `src/main/java/com/marsproject/terraformingmars/registry/ModBlocks.java` |
| Đăng ký item | `src/main/java/com/marsproject/terraformingmars/registry/ModItems.java` |
| Creative tab | `registry/ModCreativeTabs.java` |
| Block entity | `registry/ModBlockEntities.java` và `block/entity/` |
| Logic block | `block/` |
| Điện và cable | `power/`, `block/CableBlock.java`, `block/UpsBlock.java` |
| Ống khí | `pipe/`, `block/PipeBlock.java` |
| Môi trường terraform | `environment/` và `data/terraforming_mars/mars_environment/` |
| Thời tiết | `weather/` và `event/MarsWeatherHandler.java` |
| HUD/sky/fog | `client/` và `client/renderer/` |
| Model GeckoLib | `client/model/`, `client/renderer/`, `assets/.../geo`, `animations`, `textures` |
| Dimension/biome/ore | `data/terraforming_mars/dimension*` và `worldgen/` |
| Loot khi đào | `data/terraforming_mars/loot_tables/blocks/` |
| Tên hiển thị | `assets/terraforming_mars/lang/en_us.json` |
| Model/blockstate | `assets/terraforming_mars/blockstates/` và `models/` |
| Phiên bản mod/dependency | `gradle.properties`, `build.gradle` |

## 3. Chỉnh thông số block và vật phẩm

### 3.1 Độ cứng, âm thanh và màu block

Các block địa chất được đăng ký trong `ModBlocks.java`.

```java
solid("ironstone", MapColor.COLOR_BROWN, 3.0F, SoundType.DEEPSLATE);
```

Bạn có thể đổi:

- `3.0F`: độ cứng/phá block.
- `MapColor`: màu trên map.
- `SoundType`: âm thanh bước chân và phá block.
- `.requiresCorrectToolForDrops()`: bắt buộc đúng loại/cấp tool mới có drop.

Helper `falling(...)` đang áp dụng độ cứng `0.5F` chung cho các block rơi. Muốn đổi
tất cả block bụi/sỏi thì sửa trong helper; muốn đổi riêng một block, tạo đăng ký riêng
với `new MarsDustBlock(...)`.

Các máy nằm cuối `ModBlocks.java`:

- Solar Array: strength `2.0F`
- Power Cable: strength `0.8F`
- Life Support Unit: strength `2.5F`
- Atmospheric Sampler: strength `2.0F`
- UPS: strength `3.0F`

`strength(x)` đặt cả destroy time và explosion resistance theo quy ước của Minecraft.
Nếu cần hai giá trị khác nhau, dùng `.strength(destroyTime, explosionResistance)`.

### 3.2 Stack size và durability của item

Item nguyên liệu hiện dùng:

```java
ITEMS.registerSimpleItem(name);
```

Do đó mặc định stack được 64. Muốn đặt riêng, thay bằng:

```java
public static final DeferredItem<Item> APATITE_RAW =
        ITEMS.register("apatite_raw", () -> new Item(new Item.Properties().stacksTo(32)));
```

Các tùy chọn phổ biến:

```java
new Item.Properties()
        .stacksTo(16)       // số lượng tối đa trong một stack
        .durability(250)    // độ bền; item có durability thường không stack
        .fireResistant();   // không cháy trong lava/fire
```

`HABITAT_KIT` và `MARS_BEACON` đang `.stacksTo(1)`. BlockItem được tạo bằng
`registerSimpleBlockItem`, mặc định stack 64. Muốn đổi stack của một BlockItem:

```java
ITEMS.register("ups",
        () -> new BlockItem(ModBlocks.UPS.get(), new Item.Properties().stacksTo(1)));
```

Không đăng ký cùng một ID hai lần.

### 3.3 Item ăn được

Project chưa có food item. Có thể thêm bằng `FoodProperties`:

```java
FoodProperties food = new FoodProperties.Builder()
        .nutrition(4)
        .saturationModifier(0.6F)
        .build();

ITEMS.register("example_food",
        () -> new Item(new Item.Properties().food(food)));
```

Sau đó vẫn cần model item, texture, tên dịch và thêm vào creative tab.

### 3.4 Loot và số lượng drop

Sửa trong:

```text
src/main/resources/data/terraforming_mars/loot_tables/blocks/<block_id>.json
```

Ví dụ ore apatite:

- Silk Touch trả `apatite_vein`.
- Trường hợp thường trả `apatite_raw`.
- `minecraft:apply_bonus` áp dụng Fortune.
- `minecraft:explosion_decay` giảm drop khi nổ.

Muốn drop cố định 2–4 item, thêm function:

```json
{
  "function": "minecraft:set_count",
  "count": {
    "type": "minecraft:uniform",
    "min": 2.0,
    "max": 4.0
  }
}
```

Loot table quyết định vật phẩm rơi; `ModBlocks` quyết định block có yêu cầu đúng tool
hay không. Cần kiểm tra cả hai khi block không drop.

### 3.5 Tool dùng để đào

Tag hiện nằm ở:

```text
data/minecraft/tags/blocks/mineable/pickaxe.json
data/minecraft/tags/blocks/mineable/shovel.json
```

Thêm ID block vào tag phù hợp. Nếu muốn yêu cầu cấp tool, tạo hoặc cập nhật tag như:

```text
data/minecraft/tags/blocks/needs_iron_tool.json
data/minecraft/tags/blocks/needs_diamond_tool.json
```

## 4. Thêm block địa chất mới

Ví dụ thêm `example_rock`:

1. Đăng ký block trong `ModBlocks.java`:

   ```java
   public static final DeferredBlock<Block> EXAMPLE_ROCK =
           solid("example_rock", MapColor.COLOR_GRAY, 2.0F, SoundType.STONE);
   ```

2. Đăng ký BlockItem trong `ModItems.java`:

   ```java
   public static final DeferredItem<BlockItem> EXAMPLE_ROCK_ITEM =
           blockItem("example_rock", ModBlocks.EXAMPLE_ROCK);
   ```

3. Thêm `blockstates/example_rock.json`.
4. Thêm `models/block/example_rock.json`.
5. Thêm `models/item/example_rock.json`.
6. Thêm `textures/block/example_rock.png`.
7. Thêm `block.terraforming_mars.example_rock` vào `lang/en_us.json`.
8. Thêm loot table `loot_tables/blocks/example_rock.json`.
9. Thêm vào tag tool thích hợp.
10. Nếu sinh trong world, thêm configured feature, placed feature và tham chiếu nó từ biome.

Creative tab hiện tự lấy toàn bộ `ModBlocks.BLOCKS`, trừ `MULTIBLOCK_PART`, nên block mới sẽ
tự xuất hiện. Item không phải BlockItem cần được thêm qua `RAW_MATERIAL_ITEMS` hoặc sửa
`ModCreativeTabs`.

ID registry, tên file JSON và đường dẫn texture phải viết thường, dùng dấu gạch dưới,
và phải giống nhau.

## 5. Texture, model và blockstate

### Block thường

Luồng tham chiếu:

```text
blockstates/<id>.json
  -> models/block/<id>.json
  -> textures/block/<id>.png
```

Item dạng block thường có:

```json
{ "parent": "terraforming_mars:block/example_rock" }
```

Texture block chuẩn thường là PNG 16×16; có thể dùng độ phân giải bội số của 16.

### Block layer

`SevenLayerBlock.MAX_LAYERS = 7`. `dust_layer` và `salt_layer` chỉ cho 1–7 layer.
Nếu đổi giới hạn này, phải đồng bộ:

- `SevenLayerBlock.java`
- blockstate variants theo `layers`
- các model `*_layer_1.json` đến `*_layer_7.json`
- logic tích tụ trong `MarsWeatherHandler`

Không đặt lại layer 8 nếu vẫn dùng `SevenLayerBlock`, vì code hiện chủ động sửa các
world cũ từ layer 8 về 7.

### GeckoLib

Các bộ file:

```text
assets/terraforming_mars/geo/*.geo.json
assets/terraforming_mars/animations/*.animation.json
assets/terraforming_mars/textures/block/*.png
client/model/*Model.java
client/renderer/*Renderer.java
```

Tên bone dùng trong Java phải giống hoàn toàn tên trong geo JSON. Ví dụ CableModel
ẩn/hiện bone theo sáu property `north/south/east/west/up/down`; UPS dùng bone `on`
và `off`.

UPS lấy hướng từ `HORIZONTAL_FACING`. GeckoLib `GeoBlockRenderer` đã xoay toàn bộ
PoseStack theo hướng block. Không thêm `root.setRotY()` vào `UpsModel`, nếu không
box-UV có thể lệch và model có thể bị xoay hai lần.

## 6. Hệ thống điện

### Solar Array

Mọi tier dùng chung:

- `block/SolarArrayType.java`: thuộc tính bất biến của tier.
- `block/SolarArrayBlock.java`: placement, cable port, cleaning và phát điện.
- `block/entity/SolarArrayBlockEntity.java`: bụi, day/night và watt hiện tại.

Các tier được khai báo bằng `solarType(...)` trong `ModBlocks.java`. Thứ tự tham số:
watt cơ bản, bụi/tick, phạt bụi tối đa, model scale, rộng, cao, sâu. Basic hiện 250 W,
Advanced 600 W và Farm 1200 W. Ban đêm hoặc không thấy trời thì output bằng 0.

Chuột phải lau bụi; Shift + chuột phải bật/tắt solar tracking. Cable chỉ nối vào mặt
sau Solar Array. Muốn asset riêng cho một tier, đổi model/texture/animation
`ResourceLocation` của type đó.

### Cable

`CableBlock` kiểm tra sáu hướng và lưu bằng sáu BooleanProperty. Shape của lõi và
các nhánh được điều chỉnh qua `box(minX, minY, minZ, maxX, maxY, maxZ)`, đơn vị
0–16 trong một block.

`PowerNetworkScanner.MAX_CABLES_PER_SCAN = 4096` giới hạn kích thước mạng. Tăng số
này cho phép mạng lớn hơn nhưng mỗi lần quét tốn CPU hơn.

### Air Pipe

`PipeBlock` có cách hiển thị sáu hướng tương tự cable nhưng thuộc mạng khí độc lập:

- Pipe chỉ tự nối với `PipeBlock` khác.
- Một thiết bị chỉ nối được pipe khi implement `PipeConnectable`.
- `CableConnectable` và `PipeConnectable` là hai interface khác nhau. Không implement
  `PipeConnectable` cho Solar Array, UPS hoặc thiết bị chỉ dùng điện.
- `PowerNetworkScanner` không duyệt pipe và `PipeNetworkScanner` không duyệt cable.

Để một máy mới có cổng khí, implement:

```java
@Override
public boolean canConnectPipe(LevelReader level, BlockPos machinePos,
                              BlockState machineState, BlockPos pipePos) {
    Direction port = machineState.getValue(FACING).getOpposite();
    return machinePos.relative(port).equals(pipePos);
}
```

Quét topology từ một vị trí pipe:

```java
PipeNetworkSnapshot network = PipeNetworkScanner.scan(level, startPipe);

Set<BlockPos> pipes = network.pipes();
Set<BlockPos> machines = network.connectedMachines();
boolean incomplete = network.truncated();
```

`PipeNetworkScanner.MAX_PIPES_PER_SCAN = 4096` giới hạn số pipe trong một lần BFS.
Scanner hiện chỉ trả topology và các máy nối vào mạng; chưa tự mô phỏng loại khí,
áp suất, thể tích, lưu lượng hoặc chuyển khí. Các cơ chế đó nên dùng snapshot này
làm đầu vào thay vì thêm logic khí trực tiếp vào `PipeBlock`.

### UPS

`UpsBlockEntity.NETWORK_SCAN_INTERVAL = 10` nghĩa là quét input mỗi 10 tick
(0,5 giây). Giảm số này phản hồi nhanh hơn nhưng tốn CPU hơn.

`UpsBlockEntity.ENERGY_CAPACITY = 10_000` là dung lượng lưu trữ. Mỗi lần scan, năng
lượng được nạp theo watt đầu vào và thời gian scan. `StoredEnergy` được lưu vào NBT,
vì vậy UPS vẫn bật sau khi nguồn vào mất. Máy tiêu thụ phía output phải gọi
`consumeEnergy(requestedEnergy)`; project hiện chưa có consumer tự động gọi API này.

UPS có:

- Input thứ 5: local cell `(0,0,1)`, mặt trái.
- Bốn output: khai báo trong `UpsBlock.isCablePort()`.
- Kích thước occupancy: 2×2×2 qua `MultiblockPartBlock` dùng chung.

Tọa độ local được đổi sang world bằng `UpsBlock.partPos()`. Luôn mô tả port bằng
`partPos(...).relative(direction dựa trên FACING)`; không hard-code NORTH/SOUTH/EAST/WEST.

`isCablePort()` cho phép cable hiển thị kết nối tại cả input và output. Luồng nạp
năng lượng bắt đầu từ `inputCablePos()`; output chỉ làm cạn pin khi consumer gọi
`consumeEnergy()`.

### Hệ thống multiblock dùng chung

Các class nằm trong `block/` và `block/entity/`:

- `MultiblockPart`: mô tả offset local, hitbox của một ô và việc ô đó có chặn
  placement hay không.
- `MultiblockController`: logic dùng chung để kiểm tra chỗ trống, xoay offset, đặt
  và xóa các ô phụ.
- `MultiblockPartBlock`: block vô hình dùng chung cho mọi loại multiblock.
- `MultiblockPartBlockEntity`: lưu `anchorRelative`, `partIndex` và registry ID của
  controller vào NBT.

Không thêm `IntegerProperty` để lưu `PART_X`, `PART_Y`, `PART_Z` hoặc `PART_INDEX`
vào blockstate. Số lượng phần tử của `List<MultiblockPart>` không có giới hạn cứng
từ blockstate; một máy có thể khai báo 2, 50 hoặc nhiều part hơn mà không phải sửa
`MultiblockPartBlock`.

#### Hệ tọa độ local

Mỗi `BlockPos offset` được khai báo theo không gian local khi `FACING = NORTH`:

- X dương: bên trái của máy.
- Y dương: phía trên.
- Z dương: phía sau máy.
- `(0, 0, 0)` là controller/anchor và không được đưa vào danh sách part.

`MultiblockController.rotateOffset()` tự xoay offset theo `FACING` lúc runtime.
Không tự xoay offset trước khi đưa vào danh sách và không hard-code tọa độ world.

#### Khai báo danh sách part

Ví dụ một máy chiếm hai ô phía trên controller:

```java
private static final List<MultiblockPart> PARTS = List.of(
        new MultiblockPart(
                new BlockPos(0, 1, 0),
                Block.box(2, 0, 2, 14, 16, 14)
        ),
        new MultiblockPart(
                new BlockPos(0, 2, 0),
                Block.box(0, 0, 0, 16, 8, 16)
        )
);
```

Tọa độ `Block.box(...)` dùng đơn vị 0–16 và chỉ mô tả hitbox bên trong đúng ô đó.
Constructor hai tham số mặc định `blocksPlacement=true`.

Nếu một ô chỉ cần hitbox nhưng được phép bị block khác thay thế:

```java
new MultiblockPart(
        new BlockPos(0, 3, 0),
        Block.box(1, 0, 1, 15, 4, 15),
        false
)
```

Khi `blocksPlacement=false`, ô đang bị block khác chiếm sẽ không làm placement của
controller thất bại. Nếu part vô hình đã tồn tại, block khác có thể thay thế nó mà
không phá controller.

#### Implement controller mới

Block controller cần implement `MultiblockController`, có property
`HORIZONTAL_FACING` và trả về danh sách part:

```java
public final class ExampleMachineBlock extends BaseEntityBlock
        implements MultiblockController {
    public static final DirectionProperty FACING =
            BlockStateProperties.HORIZONTAL_FACING;

    @Override
    public List<MultiblockPart> getMultiblockParts() {
        return PARTS;
    }
}
```

Nối ba lifecycle method của block vào helper dùng chung:

```java
@Override
public BlockState getStateForPlacement(BlockPlaceContext context) {
    BlockState placed = defaultBlockState().setValue(
            FACING, context.getHorizontalDirection().getOpposite());
    return multiblockStateForPlacement(context, placed);
}

@Override
public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                        @Nullable LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(level, pos, state, placer, stack);
    placeMultiblockParts(level, pos, state, placer, stack);
}

@Override
protected void onRemove(BlockState state, Level level, BlockPos pos,
                        BlockState newState, boolean movedByPiston) {
    if (!state.is(newState.getBlock())) {
        removeMultiblockParts(level, pos, state);
    }
    super.onRemove(state, level, pos, newState, movedByPiston);
}
```

Phải gọi `super` như ví dụ để không làm mất lifecycle của `BaseEntityBlock`.
Không gọi `removeMultiblockParts()` khi chỉ đổi property của cùng một block, ví dụ
đổi `POWERED`; điều kiện `!state.is(newState.getBlock())` ngăn trường hợp này.

#### Tương tác qua ô phụ

Mặc định chuột phải vào part trả về `InteractionResult.PASS`. Controller có thể
override hook sau để chuyển tương tác về anchor:

```java
@Override
public InteractionResult useMultiblockPart(
        BlockState controllerState,
        Level level,
        BlockPos controllerPos,
        Player player,
        BlockHitResult hitResult
) {
    return interact(level, controllerPos, player);
}
```

UPS đang dùng cách này nên chuột phải vào bất kỳ ô nào vẫn hiện trạng thái UPS.
Phá một part có `blocksPlacement=true` sẽ phá controller; controller bị phá sẽ dọn
toàn bộ part vẫn thuộc đúng anchor và registry ID của nó.

#### Khai báo part cho Solar Array

`SolarArrayType` có accessor `parts()`. Danh sách cuối lời gọi `solarType(...)`
trong `ModBlocks.java` là nơi khai báo hitbox riêng cho từng tier:

```java
solarType(
        250, 0.0000025F, 0.70F, 1.0F,
        1.0, 0.75, 1.0,
        model, texture, animation,
        List.of(
                new MultiblockPart(
                        new BlockPos(0, 1, 0),
                        Block.box(3, 0, 3, 13, 16, 13)
                )
        )
)
```

`SolarArrayBlock` đã implement sẵn `MultiblockController`, vì vậy thêm/bớt part
trong `SolarArrayType` không cần sửa `SolarArrayBlock`,
`MultiblockPartBlock` hoặc `MultiblockPartBlockEntity`.

#### Registry và dữ liệu lưu

`MULTIBLOCK_PART` và block entity tương ứng đã được đăng ký dùng chung trong
`ModBlocks` và `ModBlockEntities`. Khi thêm controller mới, không đăng ký thêm một
part block riêng.

Sau khi đặt part, `placeMultiblockParts()` cấu hình block entity với:

- Offset từ part quay về anchor dưới dạng `BlockPos`.
- Index thật trong `List<MultiblockPart>` dưới dạng NBT integer.
- Registry ID của block controller để tránh xóa nhầm multiblock khác.

Không thay đổi thứ tự danh sách part của một loại máy đã tồn tại trong world nếu
không có migration dữ liệu. `partIndex` đã lưu sẽ trỏ sang phần tử khác sau khi đổi
thứ tự. Có thể thêm part mới ở cuối danh sách an toàn hơn; world cũ chỉ nhận các part
mới sau khi controller được đặt lại hoặc có code migration/rebuild riêng.

Life Support hiện chưa dùng `PowerNetworkScanner`: `MarsAirSupplyHandler` chỉ tìm một
Life Support trong bán kính `(8,5,8)` quanh entity, rồi tìm Solar Array thấy trời
trong bán kính `(12,6,12)` quanh máy. Đây là logic riêng cần sửa nếu muốn Life Support
thật sự phụ thuộc cable/UPS.

## 6.1 Framework machine dùng chung

Framework này dành cho các máy có cùng quy luật tổng quát: nhận item, chờ đủ thời
gian và điện, sau đó sinh item đầu ra. Oxygen Generator là implementation mẫu.
UPS và Solar Array vẫn dùng class cũ, không phụ thuộc framework này.

### Các class dùng chung

| Thành phần | Vai trò |
|---|---|
| `machine/MachineType.java` | Dữ liệu cố định của một loại máy |
| `machine/MachineRecipe.java` | Recipe nhiều input dùng chung |
| `machine/MachineRecipeSerializer.java` | Đọc recipe JSON và đồng bộ qua network |
| `block/MachineBlock.java` | Block controller dùng chung, placement, GUI, cable và multiblock |
| `block/entity/MachineBlockEntity.java` | Inventory, tick, recipe, progress và animation state |
| `machine/MachineMenu.java` | Tự tạo số slot theo `MachineType` |
| `client/screen/MachineScreen.java` | GUI nền chung và progress bar |
| `client/model/MachineModel.java` | Chọn model/texture/animation từ `MachineType` |
| `registry/ModRecipeTypes.java` | Một RecipeType và serializer cho mọi máy |
| `registry/ModMenuTypes.java` | Một MenuType cho mọi máy |

Không tạo `OxygenGeneratorBlock`, `OxygenGeneratorBlockEntity`,
`OxygenGeneratorMenu` hoặc `OxygenGeneratorScreen`. Nếu máy mới vẫn có logic
item vào → xử lý → item ra thì dùng nguyên các class chung.

### MachineType

Một type đầy đủ có dạng:

```java
new MachineType(
        new ResourceLocation(TerraformingMarsMod.MODID, "oxygen_generator"),
        1, // inputSlotCount
        1, // outputSlotCount
        new ResourceLocation(TerraformingMarsMod.MODID, "geo/ups.geo.json"),
        new ResourceLocation(TerraformingMarsMod.MODID, "textures/block/ups.png"),
        new ResourceLocation(TerraformingMarsMod.MODID, "animations/ups.animation.json"),
        List.of(), // MultiblockPart
        "idle",
        "working",
        "no_power"
)
```

- `machineTypeId` phải khớp chính xác `machine_type` trong recipe JSON.
- Slot input luôn nằm trước slot output trong `ItemStackHandler`.
- `parts` dùng trực tiếp framework multiblock ở mục trên; `List.of()` nghĩa là máy
  chỉ chiếm ô controller.
- Ba tên animation phải tồn tại trong animation JSON của type. Có thể dùng animation
  tĩnh/rỗng trong lúc chưa hoàn thiện asset.
- Một `MachineBlockEntity` dùng số slot lấy từ type của block đang chứa nó.

### Đăng ký một machine block

Helper `machine(name, type)` trong `ModBlocks` tự thêm block vào `MACHINES`. Danh
sách này được dùng để tạo một `BlockEntityType<MachineBlockEntity>` chung:

```java
public static final DeferredBlock<MachineBlock> OXYGEN_GENERATOR = machine(
        "oxygen_generator",
        new MachineType(/* dữ liệu type */)
);
```

Sau đó đăng ký BlockItem trong `ModItems`:

```java
public static final DeferredItem<BlockItem> OXYGEN_GENERATOR_ITEM =
        blockItem("oxygen_generator", ModBlocks.OXYGEN_GENERATOR);
```

Không sửa `ModBlockEntities`, `ModMenuTypes`, `ModRecipeTypes`, `MachineMenu`,
`MachineScreen` hoặc renderer khi thêm machine mới.

### Định dạng machine recipe

Recipe nằm trong:

```text
src/main/resources/data/terraforming_mars/recipes/
```

Ví dụ `oxygen_from_co2.json`:

```json
{
  "type": "terraforming_mars:machine_recipe",
  "machine_type": "terraforming_mars:oxygen_generator",
  "inputs": [
    {
      "item": "terraforming_mars:co2_canister"
    }
  ],
  "output": {
    "item": "terraforming_mars:o2_canister",
    "count": 1
  },
  "processing_time": 200,
  "power_cost": 40
}
```

- `type` luôn là `terraforming_mars:machine_recipe`, không tạo RecipeType riêng.
- `machine_type` phải là ResourceLocation đầy đủ gồm namespace.
- Mỗi phần tử `inputs` ứng với đúng một input slot theo thứ tự.
- Số ingredient không được lớn hơn `inputSlotCount`.
- Mỗi lần hoàn tất hiện tiêu thụ một item trong từng input slot.
- `processing_time` dùng tick; 20 tick xấp xỉ một giây.
- `power_cost` là công suất tối thiểu tính theo watt mà network phải cung cấp trong
  lúc progress chạy.
- `output.count` phải vừa stack limit và còn chỗ trong ít nhất một output slot.

Recipe có thể dùng item tag theo cú pháp Ingredient của NeoForge:

```json
{
  "tag": "minecraft:coals"
}
```

Tất cả machine recipe dùng chung serializer; `MachineRecipe.matches()` lọc thêm
`machine_type`, vì vậy recipe của Smelter không thể chạy trong Oxygen Generator.

### Tick, progress và thiếu điện

Mỗi server tick, `MachineBlockEntity`:

1. Tạo `MachineRecipeInput` từ các input slot.
2. Tìm recipe thuộc RecipeType chung và đúng `machine_type`.
3. Kiểm tra output slot còn nhận được kết quả.
4. Tìm cable ở mặt sau machine bằng `FACING.getOpposite()`.
5. Gọi `PowerNetworkScanner.scan()` từ cable đó.
6. Nếu `totalWatts >= power_cost`, tăng progress.
7. Khi đủ `processing_time`, trừ input và chèn output.

Nếu thiếu điện giữa chừng, progress được giữ nguyên và status đổi sang
`STATUS_NO_POWER`. Khi điện trở lại, máy tiếp tục từ progress cũ. `Progress`,
`ProcessingTime`, `Status` và inventory đều được lưu NBT nên không mất khi unload
chunk hoặc thoát world.

Lưu ý giới hạn hiện tại: `PowerNetworkScanner` cung cấp công suất tức thời nhưng
chưa có bộ phân bổ tải chung giữa nhiều consumer. Vì vậy `power_cost` hiện kiểm tra
network có đủ watt, chưa chống trường hợp nhiều machine cùng nhìn thấy và dùng toàn
bộ một nguồn. Khi demo cần mô phỏng chia tải chính xác, thêm power network manager
ở tầng `power/`; không viết cơ chế phân bổ riêng trong từng machine.

### Cable port

Machine dùng chung hiện có một cable port ở mặt sau:

```java
BlockPos cablePos = machinePos.relative(
        machineState.getValue(MachineBlock.FACING).getOpposite()
);
```

`MachineBlock` implement `CableConnectable`; không cần thêm logic cable vào type.
Nếu một loại máy cần nhiều port hoặc vị trí port khác, đó là thay đổi hành vi và nên
mở rộng `MachineType` bằng danh sách port thay vì tạo class machine riêng.

### Inventory và GUI

`MachineMenu` tự canh giữa input slot ở hàng trên và output slot ở hàng dưới.
Inventory người chơi nằm bên dưới. Output slot từ chối item do người chơi đặt vào,
nhưng cho phép lấy output và shift-click về inventory.

Ba giá trị `ContainerData` được đồng bộ:

| Index | Giá trị |
|---|---|
| 0 | progress hiện tại |
| 1 | processing time của recipe |
| 2 | idle/working/no-power |

`MachineScreen` dùng một texture GUI vanilla chung và vẽ progress bar:

- Xanh: working.
- Đỏ: no power.
- Xám: idle.

Nếu số slot quá lớn để vừa một hàng, cần nâng cấp thuật toán layout thành nhiều hàng.
Framework hiện ưu tiên cấu hình nhỏ cho demo.

### GeckoLib animation

`MachineModel` đọc ba ResourceLocation trực tiếp từ type. Một
`AnimationController` chung chọn:

- `idleAnimation` khi không có recipe đang chạy.
- `workingAnimation` khi có recipe và đủ điện.
- `noPowerAnimation` khi có recipe nhưng thiếu điện.

Tên animation trong `MachineType` phải giống tuyệt đối tên trong file animation.
Renderer chỉ đăng ký một lần cho `ModBlockEntities.MACHINE`.

### Thêm Smelter hoặc machine thứ hai

Nếu logic vào-ra giống Oxygen Generator:

1. Chuẩn bị geo, texture và animation.
2. Thêm một lời gọi `machine("smelter", new MachineType(...))` trong `ModBlocks`.
3. Thêm một BlockItem trong `ModItems`.
4. Thêm blockstate, block model, item model, loot table và translation.
5. Thêm một hoặc nhiều JSON `machine_recipe` với
   `"machine_type": "terraforming_mars:smelter"`.
6. Chạy `compileJava`, `runClient` và thử placement, cable, GUI, mất điện, reload.

Không có class Java mới. Phần Java riêng cho type thường khoảng 12–20 dòng cấu hình
trong `ModBlocks` và 2 dòng BlockItem; recipe/asset là dữ liệu. Chỉ tạo subclass hoặc
logic Java mới khi quy luật xử lý thực sự khác, ví dụ nhiều fluid tank, áp suất khí,
upgrade slot hoặc output ngẫu nhiên.

## 7. Môi trường và terraform

Dữ liệu dễ chỉnh nhất nằm ở:

```text
data/terraforming_mars/mars_environment/stage_00.json
data/terraforming_mars/mars_environment/stage_01.json
```

Các field:

| Field | Ý nghĩa |
|---|---|
| `progress` | Mốc terraform 0.0–1.0 |
| `radiation` | Bức xạ, HUD đang hiển thị mSv/h |
| `magnetic_field_percent` | Từ trường so với Trái Đất |
| `temperature_celsius` | Nhiệt độ °C |
| `water_percent` | Nước so với Trái Đất |
| `atmosphere_pressure_percent` | Áp suất so với Trái Đất |
| `atmosphere_composition` | Phần trăm oxygen, nitrogen, argon, CO₂... |
| `biology_percent` | Sinh học so với Trái Đất |

Có thể thêm `stage_02.json`, `stage_03.json` với các `progress` trung gian. Loader tự
sắp xếp theo progress và `MarsEnvironmentManager` nội suy tuyến tính giữa hai stage.
Nên giữ cùng tập key khí ở mọi stage vì `lerpMap()` hiện duyệt key từ stage dưới.

Điều kiện sống được trong `MarsEnvironmentManager`:

- Radiation `< 0.1`
- Pressure gần `100%` trong sai số `5`
- Oxygen gần `21%` trong sai số `5`
- Nitrogen gần `78%` trong sai số `5`
- Temperature `>-20°C` và `<45°C`

Đổi các hằng số này nếu muốn cân bằng survival. HUD có ngưỡng màu riêng trong
`EnvironmentHudResolver`; đổi gameplay threshold không tự đổi màu HUD.

Lệnh kiểm thử được đăng ký dưới `/mars`, gồm xem trạng thái/progress và điều khiển
weather. Xem cú pháp chính xác trong `command/MarsCommands.java`.

## 8. Damage, không khí và trọng lực

- `MarsEnvironmentEffectHandler.CHECK_INTERVAL_TICKS = 40`: kiểm tra môi trường mỗi
  2 giây.
- Effect radiation kéo dài `60` tick mỗi lần áp dụng.
- `RadiationEffect.TICKS_PER_APPLICATION = 40`: gây damage mỗi 2 giây.
- `RadiationEffect.DAMAGE_PER_TICK = 1.0F`: 1 health = nửa tim.
- `MarsGravityHandler.MARS_GRAVITY_FACTOR = 0.38D`: trọng lực Mars bằng 38% vanilla.
- `DryIceLayerBlock.SUBLIMATION_TEMPERATURE_CELSIUS = -45.0`: trên nhiệt độ này dry
  ice có thể thăng hoa khi random tick.

Landing suit hiện được giả lập bằng đủ bộ Netherite armor trong
`MarsAirSupplyHandler.hasLandingSuit()`. Thay các `Items.NETHERITE_*` bằng item suit
của mod khi đã đăng ký armor riêng.

## 9. Weather

Trong `MarsWeatherData`:

- Storm kéo dài `2400 + random(0..3600)` tick: khoảng 2–5 phút.
- Clear kéo dài `6000 + random(0..6000)` tick: khoảng 5–10 phút.
- Khi bắt đầu storm: 30% Dry Ice Storm, 70% Dust Storm.
- Intensity bị clamp từ 1 đến 3.

Trong `MarsWeatherHandler`:

- Sync định kỳ: `100` tick.
- Thử tích layer mỗi `20` tick.
- Bán kính quanh người chơi: `24` block.
- Số lần thử theo intensity: `2`, `5`, `9`.
- Dry Ice chỉ tích trong biome `cryotic_wastes`.

Fog color nằm trong `MarsFogHandler`; particle gravity, friction, lifetime và kích
thước nằm trong `client/particle/MarsDustParticle.java` và `DryIceParticle.java`.

## 10. Worldgen

### Tần suất và độ cao ore

Mỗi ore thường có hai file:

```text
worldgen/configured_feature/<ore>.json
worldgen/placed_feature/<ore>_placed.json
```

Configured feature:

- `size`: kích thước vein.
- `discard_chance_on_air_exposure`: xác suất bỏ block ore tiếp xúc không khí.
- `targets`: block/tag có thể bị thay thế.
- `state.Name`: block ore được đặt.

Placed feature:

- `count`: số lần thử mỗi chunk.
- `rarity_filter.chance`: trung bình một lần mỗi N chunk.
- `height_range`: độ cao min/max và kiểu uniform/trapezoid.
- `minecraft:biome`: chỉ sinh nếu feature được biome đó tham chiếu.

Sau đó thêm placed feature vào đúng bước trong mảng `features` của file biome.
Ore thường nằm ở bước underground ores (index 6). Sai index có thể làm datapack
không load.

### Biome

Trong `worldgen/biome/*.json` có thể đổi:

- temperature/downfall/precipitation
- màu fog, sky, water
- carver
- feature
- mob spawner
- mood sound

Phân bố biome theo multi-noise nằm trong `data/terraforming_mars/dimension/mars.json`.
Các khoảng `temperature`, `humidity`, `erosion`, `continentalness`, `weirdness`
quyết định biome xuất hiện ở đâu.

`dimension_type/mars.json` chỉnh chiều cao world, skylight, bed, raid, ambient light
và dimension effects. Thay `min_y`, `height` hoặc noise settings có thể làm hỏng
khả năng mở world cũ; nên tạo world test mới.

## 11. Habitat Kit

`HabitatKitItem` tạo phòng 7×5×7:

- vòng lặp `x/z = -3..3`, `y = 0..4` quyết định kích thước;
- shell dùng Iron Block;
- cửa sổ dùng Tinted Glass;
- airlock ở `origin.offset(0, 1, -3)` và `(0, 2, -3)`;
- Life Support đặt tại `origin.offset(0, 1, 0)`.

Nếu đổi kích thước, phải sửa cả `hasRoom()` và `buildHabitat()` cùng lúc, đồng thời
cập nhật message `habitat_obstructed`.

## 12. HUD, sky, fog và phím

- Kích thước/màu panel: `client/MarsEnvironmentHud.java`.
- Ngưỡng SAFE/WARNING/CRITICAL: `client/EnvironmentHudResolver.java`.
- Phím bật HUD: `client/ModKeyMappings.java`.
- Fog theo môi trường/weather: `client/MarsFogHandler.java`.
- Màu dimension: `client/MarsDimensionEffects.java`.
- Kích thước mặt trời/mặt trăng, sao: `client/renderer/MarsVanillaSkyRenderer.java`
  và `MarsSkyRenderer.java`.
- Texture sky: `assets/terraforming_mars/textures/environment/`.

Màu Java dạng `0xAARRGGBB`; `AA` là alpha.

## 13. Ngôn ngữ và tên hiển thị

Thêm/sửa tiếng Anh tại:

```text
assets/terraforming_mars/lang/en_us.json
```

Muốn thêm tiếng Việt, tạo:

```text
assets/terraforming_mars/lang/vi_vn.json
```

Các key phổ biến:

```json
{
  "block.terraforming_mars.example_rock": "Đá ví dụ",
  "item.terraforming_mars.example_item": "Vật phẩm ví dụ",
  "message.terraforming_mars.example": "Thông báo ví dụ"
}
```

JSON không cho phép comment và không được có dấu phẩy thừa ở phần tử cuối.

## 14. Recipe

Project hiện chưa có thư mục recipe. Tạo:

```text
src/main/resources/data/terraforming_mars/recipes/
```

MC 1.20.6 dùng thư mục `recipes` số nhiều. Ví dụ shaped recipe:

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": ["III", "ICI", "III"],
  "key": {
    "I": { "item": "minecraft:iron_ingot" },
    "C": { "item": "terraforming_mars:power_cable" }
  },
  "result": {
    "id": "terraforming_mars:ups",
    "count": 1
  }
}
```

Kiểm tra schema 1.20.6 khi thêm loại recipe khác.

## 15. Phiên bản và metadata

Trong `gradle.properties`:

- `minecraft_version`
- `neo_version`
- `geckolib_version`
- `mod_id`
- `mod_name`
- `mod_version`
- `mod_group_id`

Metadata template nằm trong `src/main/templates/META-INF/neoforge.mods.toml`.
Không đổi `mod_id` giữa chừng nếu muốn giữ tương thích world: mọi registry ID,
ResourceLocation, thư mục asset/data và translation key đều phụ thuộc nó.

## 16. Mức độ an toàn khi tự sửa

### Dễ, chủ yếu chỉnh dữ liệu

- Tên hiển thị và message.
- Texture.
- Loot count.
- Ore size/count/height.
- Biome color và feature list.
- Các stage môi trường.
- Recipe.

### Trung bình, chỉnh một vài hằng số Java

- Block strength/sound.
- Item stack size.
- Solar watts.
- Weather duration/intensity.
- Radiation damage.
- Gravity.
- HUD threshold/color.
- UPS scan interval và cable limit.

### Khó, phải đồng bộ nhiều nơi

- Thêm block/item/machine hoàn toàn mới.
- Thêm BlockEntity hoặc GeckoLib renderer.
- Đổi kích thước UPS multi-block.
- Đổi giới hạn layer.
- Đổi mod ID.
- Đổi dimension height/noise router.
- Biến Life Support sang mạng điện thật.
- Thêm energy storage, input/output có hướng hoặc lưu năng lượng trong UPS.

## 17. Checklist trước khi kết thúc một thay đổi

- ID registry và tên file đều lowercase/snake_case.
- Block đã có BlockItem nếu người chơi cần cầm/đặt.
- Có blockstate, block model, item model và texture.
- Có translation key.
- Có loot table.
- Có tag tool phù hợp.
- Nếu là worldgen: configured + placed + biome reference.
- Nếu là GeckoLib: geo + animation + texture + model + renderer + đăng ký renderer.
- JSON parse được, không có dấu phẩy thừa.
- Chạy `.\gradlew.bat compileJava`.
- Chạy client và kiểm tra ít nhất placement, breaking/drop, creative tab và reload.
- Với worldgen/dimension, kiểm tra bằng world mới.
