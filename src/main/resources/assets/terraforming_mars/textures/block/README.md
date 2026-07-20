# Mars geology texture targets

No PNG textures are included by design. Each registered block model resolves its
texture from this directory using its registry name, for example:

`textures/block/basaltic_rock.png` → `terraforming_mars:block/basaltic_rock`

Create one 16×16 PNG per block registry name in `ModBlocks`. `dust_layer` and
`salt_layer` also use their matching texture on all eight stackable layer models.
