# Changelog

## [0.2.0](https://github.com/Indemnity83/configory/compare/configory-v0.1.0...configory-v0.2.0) (2026-07-08)


### ⚠ BREAKING CHANGES

* A config's id now maps to its file location and dotted paths are pure JSON nesting (previously the first path segment selected the file). The on-disk layout is now config/<id>.json (plus config/<id>/<name>.json for extra configs), ConfigPath dropped file()/DEFAULT_FILE, and Config.save(String) / dirtyFiles() were removed in favor of save() / isDirty(). See #43.

### Added

* Add a type-agnostic display-string accessor to ConfigValue ([#45](https://github.com/Indemnity83/configory/issues/45)) ([8b29a41](https://github.com/Indemnity83/configory/commit/8b29a4199eb909083881f55f533c123a972cd4a1))
* Add defineX shorthand for typed config definitions ([#19](https://github.com/Indemnity83/configory/issues/19)) ([4361b1c](https://github.com/Indemnity83/configory/commit/4361b1c6472d2b01d094822d44284dd1b349551b))
* Add presence checks to ConfigValue for dynamic path reads ([#23](https://github.com/Indemnity83/configory/issues/23)) ([122a1cd](https://github.com/Indemnity83/configory/commit/122a1cd8410856a9042e18ffe2c825459321f033))
* Add strict-bound and finite-float numeric constraints ([#44](https://github.com/Indemnity83/configory/issues/44)) ([14b07e1](https://github.com/Indemnity83/configory/commit/14b07e1e3b8d072ffac4496d0574ab15eebd2c8e))
* Add trySet for validating dynamic writes without throwing ([#24](https://github.com/Indemnity83/configory/issues/24)) ([6c213c0](https://github.com/Indemnity83/configory/commit/6c213c046e91982ce7f3bffd5dd84894de08aa1e))
* Allow injecting ConfigStorage through the registry and bootstrap ([#41](https://github.com/Indemnity83/configory/issues/41)) ([744e80f](https://github.com/Indemnity83/configory/commit/744e80fc441fe67d46b6797fd70cee29ab888628))
* Config id maps to the file location; paths are pure nesting ([806721e](https://github.com/Indemnity83/configory/commit/806721e7b88186def82924682ac3dccb6c0853f5))
* Declare per-loader Minecraft compatibility and add Forge ([#55](https://github.com/Indemnity83/configory/issues/55)) ([0592ebd](https://github.com/Indemnity83/configory/commit/0592ebd5295b35ad1e3a5204cb8114ec6708c920))
* Generate a Brigadier command surface for config keys ([#53](https://github.com/Indemnity83/configory/issues/53)) ([b166660](https://github.com/Indemnity83/configory/commit/b1666604c22292ad31660a39af0185c9e211ed46))
* Map the config id to the file location and make paths pure nesting ([#43](https://github.com/Indemnity83/configory/issues/43)) ([d7bcf3c](https://github.com/Indemnity83/configory/commit/d7bcf3c20438d806406a42bab66b3cd2da8811fc))
* Publish to Modrinth Maven and document installation ([#20](https://github.com/Indemnity83/configory/issues/20)) ([fbb76f7](https://github.com/Indemnity83/configory/commit/fbb76f79c31b9f34eb5855729fc62ee766c4c123))
* Ship Fabric and NeoForge metadata so Configory installs as a library mod ([#22](https://github.com/Indemnity83/configory/issues/22)) ([1b1a24f](https://github.com/Indemnity83/configory/commit/1b1a24fab663a3da26aebc25b7d3173927d4b752))
* Support enum-typed config keys ([#46](https://github.com/Indemnity83/configory/issues/46)) ([d08cee0](https://github.com/Indemnity83/configory/commit/d08cee02dd7dd38989f292cbb4cb6dfae4b539c0))
* Write defaults to disk on first bootstrap load ([#48](https://github.com/Indemnity83/configory/issues/48)) ([4eef472](https://github.com/Indemnity83/configory/commit/4eef4726d379a09f28e6f64ffad201ad48864aad))


### Fixed

* Heal inverted min/max pairs when keys also carry cross-field validators ([#52](https://github.com/Indemnity83/configory/issues/52)) ([43bb2eb](https://github.com/Indemnity83/configory/commit/43bb2ebfe7e8a87e9668da9ebe08529a58bd23e8))
* Prevent infinite recursion in mutual cross-field validators ([#38](https://github.com/Indemnity83/configory/issues/38)) ([3cd00fb](https://github.com/Indemnity83/configory/commit/3cd00fb9bd8606cd904c0e9ca4d18f677ef9ae54))

## 0.1.0 (2026-07-06)


### Added

* Initial release of Configory — a convention-based configuration library for Minecraft mods, with typed config keys, dot-notation paths, fluent validation, JSON-backed files, and per-mod isolation.
