# Asset Review Report: terraforming_mars

## Scope
- Đã đối chiếu các đăng ký trong [src/main/java/com/marsproject/terraformingmars/registry/ModBlocks.java](src/main/java/com/marsproject/terraformingmars/registry/ModBlocks.java) và [src/main/java/com/marsproject/terraformingmars/registry/ModItems.java](src/main/java/com/marsproject/terraformingmars/registry/ModItems.java).
- Đã kiểm tra blockstates, model JSON trong [src/main/resources/assets/terraforming_mars/models/block](src/main/resources/assets/terraforming_mars/models/block) và [src/main/resources/assets/terraforming_mars/models/item](src/main/resources/assets/terraforming_mars/models/item).
- Đã đối chiếu với các file PNG hiện có trong [src/main/resources/assets/terraforming_mars/textures/block](src/main/resources/assets/terraforming_mars/textures/block) và [src/main/resources/assets/terraforming_mars/textures/item](src/main/resources/assets/terraforming_mars/textures/item).

## Ghi chú chung
- Hầu hết block dùng 1 texture duy nhất, thường được map qua `textures.all` hoặc `textures.texture`.
- Các block dạng layer như `dust_layer` và `salt_layer` dùng cùng 1 texture cho các biến thể `layers=1..8`.
- Các item đều có model item riêng nhưng phần lớn trỏ thẳng về model block tương ứng (`parent: terraforming_mars:block/<name>`), nên không cần PNG riêng cho item.

## Bảng review block

| Tên block | Texture đường dẫn cần có | Trạng thái |
|---|---|---|
| dust_deposit | terraforming_mars:block/dust_deposit | OK |
| fine_dust | terraforming_mars:block/fine_dust | Thiếu |
| compacted_dust | terraforming_mars:block/compacted_dust | Thiếu |
| wind_crust | terraforming_mars:block/wind_crust | Thiếu |
| dirty_stone | terraforming_mars:block/dirty_stone | Thiếu |
| dust_layer | terraforming_mars:block/dust_layer | Thiếu |
| loose_regolith | terraforming_mars:block/loose_regolith | Thiếu |
| compacted_regolith | terraforming_mars:block/compacted_regolith | Thiếu |
| rocky_regolith | terraforming_mars:block/rocky_regolith | Thiếu |
| iron_rich_regolith | terraforming_mars:block/iron_rich_regolith | Thiếu |
| ice_rich_regolith | terraforming_mars:block/ice_rich_regolith | Thiếu |
| basaltic_rock | terraforming_mars:block/basaltic_rock | Thiếu |
| weathered_basalt | terraforming_mars:block/weathered_basalt | Thiếu |
| fractured_basalt | terraforming_mars:block/fractured_basalt | Thiếu |
| vesicular_basalt | terraforming_mars:block/vesicular_basalt | Thiếu |
| massive_basalt | terraforming_mars:block/massive_basalt | Thiếu |
| ferric_basalt | terraforming_mars:block/ferric_basalt | Thiếu |
| frost_basalt | terraforming_mars:block/frost_basalt | Thiếu |
| sulfate_rock | terraforming_mars:block/sulfate_rock | OK |
| layered_sulfate | terraforming_mars:block/layered_sulfate | Thiếu |
| carbonate_rock | terraforming_mars:block/carbonate_rock | Thiếu |
| layered_carbonate | terraforming_mars:block/layered_carbonate | Thiếu |
| claystone | terraforming_mars:block/claystone | Thiếu |
| mudstone | terraforming_mars:block/mudstone | Thiếu |
| evaporite | terraforming_mars:block/evaporite | Thiếu |
| ironstone | terraforming_mars:block/ironstone | OK |
| hematite_layer | terraforming_mars:block/hematite_layer | OK |
| oxidized_rock | terraforming_mars:block/oxidized_rock | Thiếu |
| magnetite_rock | terraforming_mars:block/magnetite_rock | Thiếu |
| cryotic_rock | terraforming_mars:block/cryotic_rock | OK |
| frozen_dust | terraforming_mars:block/frozen_dust | Thiếu |
| permafrost | terraforming_mars:block/permafrost | Thiếu |
| carbonate_vein | terraforming_mars:block/carbonate_vein | Thiếu |
| white_carbonate | terraforming_mars:block/white_carbonate | Thiếu |
| silica_deposit | terraforming_mars:block/silica_deposit | Thiếu |
| opaline_silica | terraforming_mars:block/opaline_silica | Thiếu |
| silica_crust | terraforming_mars:block/silica_crust | Thiếu |
| silica_vein | terraforming_mars:block/silica_vein | Thiếu |
| clay_rich_rock | terraforming_mars:block/clay_rich_rock | Thiếu |
| smectite | terraforming_mars:block/smectite | Thiếu |
| bentonite | terraforming_mars:block/bentonite | Thiếu |
| chloride_deposit | terraforming_mars:block/chloride_deposit | Thiếu |
| salt_crust | terraforming_mars:block/salt_crust | Thiếu |
| salt_layer | terraforming_mars:block/salt_layer | Thiếu |
| mars_iron_vein | terraforming_mars:block/mars_iron_vein | Thiếu |
| hematite_vein | terraforming_mars:block/hematite_vein | Thiếu |
| sulfate_vein | terraforming_mars:block/sulfate_vein | Thiếu |
| red_gravel | terraforming_mars:block/red_gravel | Thiếu |
| iron_gravel | terraforming_mars:block/iron_gravel | Thiếu |
| basalt_gravel | terraforming_mars:block/basalt_gravel | Thiếu |
| basalt_cobble | terraforming_mars:block/basalt_cobble | Thiếu |
| ironstone_cobble | terraforming_mars:block/ironstone_cobble | Thiếu |
| sulfate_cobble | terraforming_mars:block/sulfate_cobble | Thiếu |
| basalt_boulder | terraforming_mars:block/basalt_boulder | Thiếu |
| iron_boulder | terraforming_mars:block/iron_boulder | Thiếu |
| cryotic_boulder | terraforming_mars:block/cryotic_boulder | Thiếu |
| sulfate_boulder | terraforming_mars:block/sulfate_boulder | Thiếu |

## Bảng review item

| Tên item | Texture đường dẫn cần có | Trạng thái |
|---|---|---|
| dust_deposit | parent -> terraforming_mars:block/dust_deposit | OK |
| fine_dust | parent -> terraforming_mars:block/fine_dust | OK |
| compacted_dust | parent -> terraforming_mars:block/compacted_dust | OK |
| wind_crust | parent -> terraforming_mars:block/wind_crust | OK |
| dirty_stone | parent -> terraforming_mars:block/dirty_stone | OK |
| dust_layer | parent -> terraforming_mars:block/dust_layer_1 (qua block model layer) | OK |
| loose_regolith | parent -> terraforming_mars:block/loose_regolith | OK |
| compacted_regolith | parent -> terraforming_mars:block/compacted_regolith | OK |
| rocky_regolith | parent -> terraforming_mars:block/rocky_regolith | OK |
| iron_rich_regolith | parent -> terraforming_mars:block/iron_rich_regolith | OK |
| ice_rich_regolith | parent -> terraforming_mars:block/ice_rich_regolith | OK |
| basaltic_rock | parent -> terraforming_mars:block/basaltic_rock | OK |
| weathered_basalt | parent -> terraforming_mars:block/weathered_basalt | OK |
| fractured_basalt | parent -> terraforming_mars:block/fractured_basalt | OK |
| vesicular_basalt | parent -> terraforming_mars:block/vesicular_basalt | OK |
| massive_basalt | parent -> terraforming_mars:block/massive_basalt | OK |
| ferric_basalt | parent -> terraforming_mars:block/ferric_basalt | OK |
| frost_basalt | parent -> terraforming_mars:block/frost_basalt | OK |
| sulfate_rock | parent -> terraforming_mars:block/sulfate_rock | OK |
| layered_sulfate | parent -> terraforming_mars:block/layered_sulfate | OK |
| carbonate_rock | parent -> terraforming_mars:block/carbonate_rock | OK |
| layered_carbonate | parent -> terraforming_mars:block/layered_carbonate | OK |
| claystone | parent -> terraforming_mars:block/claystone | OK |
| mudstone | parent -> terraforming_mars:block/mudstone | OK |
| evaporite | parent -> terraforming_mars:block/evaporite | OK |
| ironstone | parent -> terraforming_mars:block/ironstone | OK |
| hematite_layer | parent -> terraforming_mars:block/hematite_layer | OK |
| oxidized_rock | parent -> terraforming_mars:block/oxidized_rock | OK |
| magnetite_rock | parent -> terraforming_mars:block/magnetite_rock | OK |
| cryotic_rock | parent -> terraforming_mars:block/cryotic_rock | OK |
| frozen_dust | parent -> terraforming_mars:block/frozen_dust | OK |
| permafrost | parent -> terraforming_mars:block/permafrost | OK |
| carbonate_vein | parent -> terraforming_mars:block/carbonate_vein | OK |
| white_carbonate | parent -> terraforming_mars:block/white_carbonate | OK |
| silica_deposit | parent -> terraforming_mars:block/silica_deposit | OK |
| opaline_silica | parent -> terraforming_mars:block/opaline_silica | OK |
| silica_crust | parent -> terraforming_mars:block/silica_crust | OK |
| silica_vein | parent -> terraforming_mars:block/silica_vein | OK |
| clay_rich_rock | parent -> terraforming_mars:block/clay_rich_rock | OK |
| smectite | parent -> terraforming_mars:block/smectite | OK |
| bentonite | parent -> terraforming_mars:block/bentonite | OK |
| chloride_deposit | parent -> terraforming_mars:block/chloride_deposit | OK |
| salt_crust | parent -> terraforming_mars:block/salt_crust | OK |
| salt_layer | parent -> terraforming_mars:block/salt_layer_1 (qua block model layer) | OK |
| mars_iron_vein | parent -> terraforming_mars:block/mars_iron_vein | OK |
| hematite_vein | parent -> terraforming_mars:block/hematite_vein | OK |
| sulfate_vein | parent -> terraforming_mars:block/sulfate_vein | OK |
| red_gravel | parent -> terraforming_mars:block/red_gravel | OK |
| iron_gravel | parent -> terraforming_mars:block/iron_gravel | OK |
| basalt_gravel | parent -> terraforming_mars:block/basalt_gravel | OK |
| basalt_cobble | parent -> terraforming_mars:block/basalt_cobble | OK |
| ironstone_cobble | parent -> terraforming_mars:block/ironstone_cobble | OK |
| sulfate_cobble | parent -> terraforming_mars:block/sulfate_cobble | OK |
| basalt_boulder | parent -> terraforming_mars:block/basalt_boulder | OK |
| iron_boulder | parent -> terraforming_mars:block/iron_boulder | OK |
| cryotic_boulder | parent -> terraforming_mars:block/cryotic_boulder | OK |
| sulfate_boulder | parent -> terraforming_mars:block/sulfate_boulder | OK |

## Texture dư / không dùng
- Không thấy file PNG dư nào trong [src/main/resources/assets/terraforming_mars/textures/block](src/main/resources/assets/terraforming_mars/textures/block): các PNG hiện có đều đang được model tham chiếu.
- Thư mục [src/main/resources/assets/terraforming_mars/textures/item](src/main/resources/assets/terraforming_mars/textures/item) chưa tồn tại, nên chưa có PNG item riêng nào.

## Danh sách PNG cần tạo mới

| Đường dẫn cần tạo | Kích thước gợi ý |
|---|---|
| src/main/resources/assets/terraforming_mars/textures/block/fine_dust.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/compacted_dust.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/wind_crust.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/dirty_stone.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/dust_layer.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/loose_regolith.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/compacted_regolith.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/rocky_regolith.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/iron_rich_regolith.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/ice_rich_regolith.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/basaltic_rock.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/weathered_basalt.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/fractured_basalt.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/vesicular_basalt.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/massive_basalt.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/ferric_basalt.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/frost_basalt.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/layered_sulfate.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/carbonate_rock.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/layered_carbonate.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/claystone.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/mudstone.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/evaporite.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/oxidized_rock.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/magnetite_rock.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/frozen_dust.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/permafrost.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/carbonate_vein.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/white_carbonate.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/silica_deposit.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/opaline_silica.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/silica_crust.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/silica_vein.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/clay_rich_rock.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/smectite.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/bentonite.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/chloride_deposit.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/salt_crust.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/salt_layer.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/mars_iron_vein.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/hematite_vein.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/sulfate_vein.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/red_gravel.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/iron_gravel.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/basalt_gravel.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/basalt_cobble.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/ironstone_cobble.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/sulfate_cobble.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/basalt_boulder.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/iron_boulder.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/cryotic_boulder.png | 16x16 |
| src/main/resources/assets/terraforming_mars/textures/block/sulfate_boulder.png | 16x16 |

## Kết luận ngắn
- Phần lớn các block hiện đang thiếu file PNG tương ứng với tên model.
- Các item không cần tạo PNG riêng nếu vẫn dùng `parent` về model block; chỉ cần tạo texture item riêng khi có model item độc lập và trỏ vào `textures/item/...`.
