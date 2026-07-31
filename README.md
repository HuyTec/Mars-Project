# Mars Project

Mars Project là mod Minecraft tập trung vào sinh tồn và cải tạo môi trường Sao Hỏa. Người chơi phải xây dựng hạ tầng năng lượng, xử lý khí quyển, chống lại điều kiện môi trường khắc nghiệt và từng bước đưa Sao Hỏa về trạng thái có thể sinh sống.

Project đang trong giai đoạn phát triển. Một số hệ thống đã hoạt động, một số mới chỉ có nền tảng dữ liệu hoặc topology để mở rộng về sau. Hãy đọc phần "Giới hạn hiện tại" trước khi thay đổi code.

## Thông tin kỹ thuật

| Thành phần | Phiên bản |
| --- | --- |
| Minecraft | 1.20.6 |
| NeoForge | 20.6.139 |
| Java | JDK 21 |
| Build system | ModDevGradle 2.0.142 |
| GeckoLib | 4.5.4 |
| Mod ID | `terraforming_mars` |
| Package gốc | `com.marsproject.terraformingmars` |
| License | All Rights Reserved |

## Cài đặt môi trường phát triển

Yêu cầu:

- JDK 21.
- Git.
- IntelliJ IDEA được khuyến nghị, Eclipse vẫn có thể sử dụng.
- Không cần cài Gradle riêng vì project có Gradle Wrapper.

Clone repository và mở thư mục gốc bằng IDE. Sau khi Gradle import hoàn tất, sử dụng các lệnh PowerShell sau:

```powershell
.\gradlew.bat compileJava
.\gradlew.bat build
.\gradlew.bat runClient
```

Các lệnh bổ sung:

```powershell
.\gradlew.bat runServer
.\gradlew.bat runData
.\gradlew.bat --refresh-dependencies
```

File JAR sau khi build nằm trong:

```text
build/libs/
```

Nếu Gradle báo một NeoForge artifact đang bị khóa, hãy đóng Minecraft client hoặc cấu hình `runClient` đang sử dụng project rồi chạy lại. Không xóa thủ công cache hoặc artifact khi game vẫn đang mở.

## Cấu trúc source

```text
src/main/java/com/marsproject/terraformingmars/
├── block/          Block và các contract multiblock
├── block/entity/   BlockEntity và server ticker
├── client/         HUD, model, renderer, screen và client state
├── command/        Command của mod
├── effect/         Mob effect
├── environment/    Dữ liệu và tiến trình môi trường Sao Hỏa
├── event/          Oxygen, radiation, gravity, weather và sync handlers
├── gas/            Loại gas dùng bởi hệ thống machine
├── item/           Custom item
├── machine/        MachineType, operation, recipe, menu
├── network/        Custom payload
├── pipe/           Air pipe topology
├── power/          Cable topology và nguồn điện
├── registry/       Registry block, item, BlockEntity, menu và recipe
├── screen/         Intro screen và teleport flow
└── weather/        Mars weather
```

```text
src/main/resources/
├── assets/terraforming_mars/       Client assets và translation
├── data/terraforming_mars/         Recipe, loot table, dimension và worldgen
└── mars.png                         Logo của mod
```

BlockBench source có thể được giữ trong `assets/terraforming_mars/blockbench`, nhưng `build.gradle` loại trừ file `.bbmodel` khỏi JAR phát hành.

## Điểm khởi động và registry

Entry point là `TerraformingMarsMod`.

Khi thêm nội dung mới, ưu tiên các registry tập trung:

- `ModBlocks` cho block.
- `ModItems` cho item và BlockItem.
- `ModBlockEntities` cho BlockEntityType.
- `ModMenuTypes` cho menu.
- `ModRecipeTypes` cho recipe serializer và recipe type.
- `ModCreativeTabs` cho creative tab.

Không tạo registry thứ hai cho Machine hoặc từng loại generator. `TerraformingMarsMod.MARS_BEACON` là đăng ký cũ còn nằm trong entry point; nội dung mới vẫn nên đi qua package `registry`.

## Hệ thống môi trường Sao Hỏa

Các stage môi trường được load từ:

```text
data/terraforming_mars/mars_environment/
```

Mỗi stage có các thuộc tính chính:

- Tiến trình terraform.
- Radiation.
- Từ trường.
- Nhiệt độ.
- Nước.
- Áp suất khí quyển.
- Thành phần khí quyển.
- Sinh học.

Client nhận dữ liệu qua custom payload và hiển thị bằng Mars Environment HUD. Các handler trong package `event` áp dụng oxygen, radiation, gravity và weather lên người chơi.

Do áp suất ngoài trời hiện còn thấp, liquid water không thể được đặt trong Mars dimension:

- Water bucket bị tiêu thụ nhưng nước bay hơi ngay.
- Source và flowing water bị loại bỏ khi có physics update.
- Không thể tạo infinite water source.
- Block waterlogged được giữ lại nhưng mất phần nước.
- Có sound, particle và thông báo khi người chơi thử đặt water bucket.

Quy tắc này hiện áp dụng cho toàn bộ Mars dimension. Khi sealed-room atmosphere được triển khai, `MarsWaterEvaporationHandler.isLowPressureExposure(...)` cần được nối với room pressure để nước tồn tại bên trong base đủ áp suất.

## Hệ thống điện

Luồng điện hiện tại:

```text
Solar Array
    -> cable network
    -> UPS input port
    -> UPS storedEnergy
    -> UPS output port
    -> cable network
    -> Machine cable port
    -> Machine operation
```

Quy tắc quan trọng:

- Solar Array tạo watt dựa trên điều kiện hoạt động và mức bụi.
- UPS quét network phía input để nhận điện từ Solar Array.
- UPS lưu tối đa `500000` energy.
- Consumer chỉ được lấy điện từ output port của UPS.
- `tryConsumeEnergy()` là giao dịch nguyên tử: thiếu điện thì không trừ một phần.
- `PowerNetworkScanner` dùng BFS, visited set và giới hạn `4096` cable mỗi lần scan.
- Machine không lấy điện trực tiếp từ Solar Array và không tìm UPS theo khoảng cách.

UPS là multiblock 2 x 2 x 2. Không thay đổi anchor, part offsets hoặc input/output port nếu chưa kiểm tra world save và placement hiện hữu.

## Kiến trúc Machine

Các machine tiêu chuẩn dùng chung:

- `MachineBlock`.
- `MachineBlockEntity`.
- `MachineType`.
- `MachineOperation`.
- `MachineModel`.
- `MachineRenderer`.
- `MachineMenu` và `MachineScreen`.

Không tạo các class như `OxygenGeneratorBlock`, `NitrogenGeneratorBlock`, `AirCreatorBlock` hoặc BlockEntity riêng cho từng máy. Sự khác biệt phải được mô tả bằng `MachineType` và `MachineOperation`.

`ModBlocks.MACHINES` là danh sách dùng để đưa mọi MachineBlock vào cùng một `BlockEntityType`. Helper `machine(...)` tự thêm block mới vào danh sách này.

Các trạng thái machine hiện có:

| Trạng thái | Ý nghĩa |
| --- | --- |
| `IDLE` | Chưa có operation result |
| `ACTIVE` | Operation gần nhất thành công |
| `NO_POWER` | Không tìm thấy UPS output hợp lệ hoặc UPS thiếu stored energy |
| `NO_INPUT` | Thiếu Air Vent hoặc thiếu gas đúng loại |
| `OUTPUT_FULL` | Buffer gas output đã đầy |

Trạng thái được đánh giá lại theo `operationIntervalTicks`, lưu NBT và đồng bộ sang client khi thay đổi.

## Machine hiện tại

| Registry ID | Energy mỗi operation | Interval | Input | Output |
| --- | ---: | ---: | --- | --- |
| `oxygen_generator` | 300 | 120 tick | Raw atmosphere từ Air Vent | 100 O2 |
| `nitrogen_generator` | 150 | 120 tick | Raw atmosphere từ Air Vent | 100 N2 |
| `air_creator` | 450 | 120 tick | 21 O2 và 79 N2 | 100 breathable air |

120 tick tương đương khoảng 6 giây ở 20 TPS. Mỗi machine có buffer gas output tối đa `10000` đơn vị và buffer này được lưu trong NBT.

Oxygen Generator vẫn có recipe item CO2 canister thành O2 canister. Gas operation và item recipe hiện cùng tồn tại trong `MachineBlockEntity`.

## Quy ước hướng và port

Mọi MachineBlock dùng `BlockStateProperties.HORIZONTAL_FACING`.

Khi nhìn trực diện vào mặt trước máy:

```java
Direction front = state.getValue(FACING);
Direction back = front.getOpposite();
Direction left = front.getCounterClockWise();
Direction right = front.getClockWise();
```

Oxygen Generator và Nitrogen Generator:

| Mặt tương đối | Chức năng |
| --- | --- |
| Trái | Cable |
| Phải | Cable |
| Sau | Raw atmosphere input pipe |
| Trên | O2 hoặc N2 output pipe |
| Trước, dưới | Không kết nối |

Air Creator:

| Mặt tương đối | Chức năng |
| --- | --- |
| Trái | O2 input pipe |
| Phải | N2 input pipe |
| Trước | Cable |
| Sau | Cable |
| Trên | Cable |
| Dưới | Cable |

Mạng O2 và N2 phải tách đúng loại. Nếu cùng một pipe network chứa output source khác loại, Air Creator từ chối network đó.

Không hard-code NORTH, SOUTH, EAST hoặc WEST theo tọa độ world để xác định port.

## Air Vent, pipe và gas

`air_vent` là block thường, không phải Machine.

- Air Pipe kết nối được vào cả 6 hướng.
- `FACING` xác định mặt lấy mẫu môi trường cho hệ thống atmosphere sau này.
- Generator cần một pipe network nối tới Air Vent ở input phía sau.
- Air Vent không tiêu thụ điện.

`PipeNetworkScanner` hiện quét topology bằng BFS với giới hạn `4096` pipe. Pipe không lưu gas riêng. Gas được lưu trong buffer của source MachineBlockEntity và được consumer lấy trực tiếp qua network khi operation chạy.

Các loại gas hiện có:

```text
OXYGEN
NITROGEN
BREATHABLE_AIR
```

## GUI hiện tại

Mọi machine dùng chung một GUI cơ bản:

- Một input slot.
- Một output slot.
- Player inventory và hotbar.
- Progress bar.
- Màu trạng thái.

Khi chuột phải, server gửi trạng thái machine, energy mỗi operation và interval vào chat rồi mở GUI.

GUI chưa có:

- Gas tank bar.
- Stored gas counter.
- Pipe connection indicator.
- UPS connection indicator.
- Layout riêng cho Air Creator.

Không cho người chơi bật `ACTIVE` thủ công. `ACTIVE` luôn phản ánh operation gần nhất.

## GeckoLib và assets

Machine dùng chung:

```text
assets/terraforming_mars/geo/machine.geo.json
assets/terraforming_mars/animations/machine.animation.json
```

Mỗi machine có texture riêng trong:

```text
assets/terraforming_mars/textures/block/
```

`MachineModel` đọc model, texture và animation từ `MachineType`. Không chọn resource bằng cách so sánh chuỗi registry name.

Khi thêm block mới, kiểm tra đầy đủ:

```text
assets/terraforming_mars/blockstates/<id>.json
assets/terraforming_mars/models/block/<id>.json
assets/terraforming_mars/models/item/<id>.json
assets/terraforming_mars/textures/block/<id>.png
data/terraforming_mars/loot_tables/blocks/<id>.json
assets/terraforming_mars/lang/en_us.json
```

Không thêm PNG rỗng. Texture machine hiện dùng atlas 64 x 64 tương thích với `machine.geo.json` và fallback model `machine_atlas_cube.json`.

## Recipe machine

Machine recipe được định nghĩa trong:

```text
data/terraforming_mars/recipes/
```

Các field chính:

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

`machine_type` phải khớp `MachineType.machineTypeId`. Không xác định hành vi machine bằng `name.contains(...)` hoặc so sánh tên chuỗi.

Năng lượng của gas operation hiện lấy từ `MachineType.energyPerOperation`. Field `power_cost` vẫn thuộc định dạng recipe cũ và được giữ để tương thích dữ liệu, nhưng không quyết định giao dịch energy định kỳ của gas machine.

## Structure và base

Structure hoàn chỉnh nên được lưu tại:

```text
data/terraforming_mars/structures/<structure_name>.nbt
```

Có thể dùng WorldEdit để xây nhanh, sau đó dùng Structure Block vanilla để lưu `.nbt`. Với base lớn, nên chia thành các module như core, airlock, power room, habitat và greenhouse.

Hệ thống sealed room dự kiến sẽ dùng bounded BFS. Air Vent và Air Creator đã chuẩn bị contract cần thiết, nhưng việc kiểm tra phòng kín và điều áp chưa được triển khai.

## Giới hạn hiện tại

- Air Vent mới là nguồn input theo topology; chưa đọc thành phần atmosphere thực tế tại `samplePos`.
- Pipe chưa có tank, lưu lượng, pressure hoặc gas amount riêng.
- Air Creator tạo `BREATHABLE_AIR` vào buffer nhưng chưa bơm vào sealed room.
- Chưa có bounded BFS kiểm tra phòng kín.
- Chưa có room pressure, leak simulation hoặc atmosphere persistence theo từng phòng.
- Nước hiện bay hơi ở toàn bộ Mars dimension, kể cả bên trong công trình chưa có room-pressure record.
- Chưa miễn oxygen damage và radiation effect cho người chơi ở trong base.
- GUI machine vẫn là giao diện debug cơ bản.
- Project chưa có automated test source; Gradle hiện báo `test NO-SOURCE`.

## Quy trình thêm Machine mới

1. Xác định operation và gas contract trong `MachineOperation` hoặc mở rộng cấu trúc dữ liệu hiện có.
2. Tạo `MachineType` trong helper `machine(...)` của `ModBlocks`.
3. Không tạo Block hoặc BlockEntity riêng nếu shared Machine architecture xử lý được.
4. Đăng ký BlockItem trong `ModItems`.
5. Thêm blockstate, block model, item model, texture, loot table và translation.
6. Kiểm tra port tương đối theo `FACING`.
7. Kiểm tra NBT và client synchronization nếu thêm state.
8. Chạy `compileJava` và `build`.

## Quy tắc đóng góp

- Đọc code hiện tại và `git status` trước khi sửa.
- Working tree có thể chứa thay đổi chưa commit của thành viên khác. Không ghi đè hoặc xóa thay đổi không thuộc nhiệm vụ.
- Không dùng `git reset --hard` hoặc `git checkout --` để dọn working tree.
- Không đổi registry ID đã tồn tại.
- Không tạo power network, pipe network hoặc Machine registry song song.
- Hành vi gameplay phải được mô tả bằng type/configuration, không suy luận từ tên block.
- Logic tiêu thụ energy và gas phải nguyên tử: thiếu tài nguyên thì không trừ một phần.
- Chỉ thay đổi world state và resource storage phía server.
- Không gửi packet mỗi tick nếu dữ liệu không thay đổi.
- Code và identifier viết bằng tiếng Anh; translation và tài liệu có thể viết bằng ngôn ngữ phù hợp với nhóm.
- Mỗi thay đổi cần kèm resource tương ứng và được kiểm tra bằng Gradle.

Checklist trước khi bàn giao:

```powershell
git status --short
.\gradlew.bat compileJava
.\gradlew.bat build
```

Ngoài build, hãy test trực tiếp trong game các hướng placement, port connection, save/reload, trạng thái thiếu input, thiếu điện và output đầy.
