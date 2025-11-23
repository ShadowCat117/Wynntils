## [2.0.0-beta.20](https://github.com/Wynntils/Wynntils/compare/v2.0.0-beta.19...v2.0.0-beta.20) (2024-08-10)


### Bug Fixes

* Fix TCC boss scoreboard pattern [skip ci] ([#2719](https://github.com/Wynntils/Wynntils/issues/2719)) ([d06812e](https://github.com/Wynntils/Wynntils/commit/d06812ef8a35a30f3e1d35016c98e62627c9f357))


### Miscellaneous Chores

* [auto-generated] Update urls.json [ci skip] ([#2720](https://github.com/Wynntils/Wynntils/issues/2720)) ([f2aae55](https://github.com/Wynntils/Wynntils/commit/f2aae55bc53ad706126408254acdadb1927bad1e))

## [2.0.0-beta.19](https://github.com/Wynntils/Wynntils/compare/v2.0.0-beta.18...v2.0.0-beta.19) (2024-08-09)


### Bug Fixes

* Change ReplaceRecipeBookFeature to DisableRecipeBookFeature [skip ci] ([#2718](https://github.com/Wynntils/Wynntils/issues/2718)) ([2bf815c](https://github.com/Wynntils/Wynntils/commit/2bf815c758e3d4af1d3515fa4dd2ebaf54de442e))
* Fix RevealNicknamesFeature by accounting for Wynn's inconsistent behavior [skip ci] ([#2716](https://github.com/Wynntils/Wynntils/issues/2716)) ([18667a5](https://github.com/Wynntils/Wynntils/commit/18667a592a9427ebc793d15b8105784095c8d123))
* Fix TradeMarketAutoOpenChatFeature (as 2.1 changed its chat message) [skip ci] ([#2717](https://github.com/Wynntils/Wynntils/issues/2717)) ([0adb694](https://github.com/Wynntils/Wynntils/commit/0adb6941fa7c55eb71da4ae66b8be44438fc6b8d))


### Miscellaneous Chores

* **release:** v2.0.0-beta.19 [skip ci] ([4d42930](https://github.com/Wynntils/Wynntils/commit/4d42930bdbc2f97fa01d5a4fb059d0ecb7ab6917))

## [2.0.0-beta.18](https://github.com/Wynntils/Wynntils/compare/v2.0.0-beta.17...v2.0.0-beta.18) (2024-08-09)


### Bug Fixes

* Fix ChatHandler not detecting slowdown effect correctly [skip ci] ([#2715](https://github.com/Wynntils/Wynntils/issues/2715)) ([02c2000](https://github.com/Wynntils/Wynntils/commit/02c200060b3ce80ba1c8c70cc107fe462fdcec5a))


### Miscellaneous Chores

* rawtypes need not be suppressed in LabelHandler [skip ci] ([#2712](https://github.com/Wynntils/Wynntils/issues/2712)) ([54d1f72](https://github.com/Wynntils/Wynntils/commit/54d1f72df972cf64eb24da458708ca430a83ffec))
* Rebrand to Wynntils (move away from using the Artemis codename) ([#2711](https://github.com/Wynntils/Wynntils/issues/2711)) ([8f3f081](https://github.com/Wynntils/Wynntils/commit/8f3f0819f7fcce1fd90611a223f42b583dfcc977))
* **release:** v2.0.0-beta.18 [skip ci] ([eb020e4](https://github.com/Wynntils/Wynntils/commit/eb020e490af28273fb285502ecef55a1b9f58aeb))
* Sunset the old gear chat encoding [skip ci] ([#2713](https://github.com/Wynntils/Wynntils/issues/2713)) ([2aa5694](https://github.com/Wynntils/Wynntils/commit/2aa5694eeb3ee5645ea8b3e535bbd0f2f1131b54))


### Code Refactoring

* Java 21 related refactors [skip ci] ([#2714](https://github.com/Wynntils/Wynntils/issues/2714)) ([2e283fa](https://github.com/Wynntils/Wynntils/commit/2e283fa28502f8f8e4a34d8a24db281449c807da))

## [2.0.0-beta.17](https://github.com/Wynntils/Wynntils/compare/v2.0.0-beta.16...v2.0.0-beta.17) (2024-08-09)


### Miscellaneous Chores

* rawtypes need not be suppressed in LabelHandler ([adf02d6](https://github.com/Wynntils/Wynntils/commit/adf02d6f0343e676fd1090cbd7bb591e17f7ec15))
* **release:** v2.0.0-beta.17 [skip ci] ([63c92e0](https://github.com/Wynntils/Wynntils/commit/63c92e0fec78cf29092d4afd5955401f843a35d6))

## [2.0.0-beta.16](https://github.com/Wynntils/Wynntils/compare/v2.0.0-beta.15...v2.0.0-beta.16) (2024-08-08)


### New Features

* Add ModMenu support for Fabric and configuration screen for NeoForge [skip ci] ([#2703](https://github.com/Wynntils/Wynntils/issues/2703)) ([ab79a4d](https://github.com/Wynntils/Wynntils/commit/ab79a4d3071fb3acdcf42d5defc4c0620740bb92))
* Localize Wynntils to 13 languages using GPT4-o (hu, nl, pl, es, fr, de, it, pt, ru, ja, ko, zh_CN, zh_TW) ([#2709](https://github.com/Wynntils/Wynntils/issues/2709)) ([227f1df](https://github.com/Wynntils/Wynntils/commit/227f1df79ca89f3bc1db9f69e50c080da99907f4))
* Parse World Event items as activities, prepare WorldEventModel/WorldEventInfo [skip ci] ([#2706](https://github.com/Wynntils/Wynntils/issues/2706)) ([925feb6](https://github.com/Wynntils/Wynntils/commit/925feb65ebabfa431943bed6c82c28264f288261))


### Bug Fixes

* Don't render overlays when F1/Hide Gui is enabled [skip ci] ([#2710](https://github.com/Wynntils/Wynntils/issues/2710)) ([f5e44a5](https://github.com/Wynntils/Wynntils/commit/f5e44a58884da78f21db82efceb57f75287c9dc8))
* Try to parse a crafted items' name from the item, if it's not in the lore [skip ci] ([#2708](https://github.com/Wynntils/Wynntils/issues/2708)) ([8d74aea](https://github.com/Wynntils/Wynntils/commit/8d74aeacd9fdd16b499d6f756ccb9c6de0d26713))


### Miscellaneous Chores

* **release:** v2.0.0-beta.16 [skip ci] ([c26ef55](https://github.com/Wynntils/Wynntils/commit/c26ef557bb699901e2bc5b55849b7c7ec44b1f89))

