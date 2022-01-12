# Changelog

## [Unreleased]
### Fixed
- Screen getting spammed by update notifications

## [1.2.1+1.18]
### Added
- Update all button. Update all your mods in one go! [#100](https://github.com/DeathsGun/ModManager/issues/100)
- Support for 1.16

### Fixed
- Race condition when showing the update notification [#108](https://github.com/DeathsGun/ModManager/issues/108)
- Mods showing up that are actually up-to-date [#101](https://github.com/DeathsGun/ModManager/issues/101)

### Changed
- Updated to 1.18.1
- Updated Russian translation (thanks to Felix14-v2) [#97](https://github.com/DeathsGun/ModManager/pull/97)

## [1.2.0+1.18-pre] - 22.11.2021
### Added
- Compatibility with 1.18-pre releases

### Fixed
- Essentials compatibility [#84](https://github.com/DeathsGun/ModManager/issues/84)
- preLaunch errors [#95](https://github.com/DeathsGun/ModManager/issues/95)

## [1.2.0-alpha] - 05.11.2021
### Added
- A back button [#87](https://github.com/DeathsGun/ModManager/issues/87)
- New browsing experience by allowing more detailed filters [#79](https://github.com/DeathsGun/ModManager/issues/79)
- Hide mods being shown in the updatable mods section [#77](https://github.com/DeathsGun/ModManager/issues/77)

### Fixed
- Delete mods on preLaunch which should avoid the update
  problem [#91](https://github.com/DeathsGun/ModManager/issues/91)

### Changed
- Minimum ```fabric-loader``` version is now 0.12

## [1.1.1-alpha] - 06.10.2021
### Fixed
- Old versions not being deleted (Now really) [#51](https://github.com/DeathsGun/ModManager/issues/51)
- Whitespaces producing errors [#70](https://github.com/DeathsGun/ModManager/issues/70)
- Tabs producing errors [#67](https://github.com/DeathsGun/ModManager/issues/67)

### Changed
- Updated turkish translation (thanks to kuzeeeyk) [#75](https://github.com/DeathsGun/ModManager/pull/75)

## [1.1.0-alpha] - 01.10.2021
### Added
- Allows mods to specify their Modrinth project id [#8](https://github.com/DeathsGun/ModManager/issues/8)
- Icons are now cached through restarts (Max: 10 MB) [#24](https://github.com/DeathsGun/ModManager/issues/24)
- Restart notification when mods get updated, removed or
  installed [#30](https://github.com/DeathsGun/ModManager/issues/30)
- Continue scrolling in the mod list [#38](https://github.com/DeathsGun/ModManager/issues/38)
- Show changelog, current version and target version before
  update [#41](https://github.com/DeathsGun/ModManager/issues/41)
- Sort mods by relevance, downloads, updated and newest [#45](https://github.com/DeathsGun/ModManager/issues/45)
- Allows mods to disable update checking for their mod [#62](https://github.com/DeathsGun/ModManager/issues/62)

### Fixed
- NullPointerException when updating mods [#42](https://github.com/DeathsGun/ModManager/issues/42)
- NullPointerException when mods not follow SemVer [#61](https://github.com/DeathsGun/ModManager/issues/64)
- Forge versions shown as update [#56](https://github.com/DeathsGun/ModManager/issues/56)
- Old versions not being deleted [#51](https://github.com/DeathsGun/ModManager/issues/51)
- Mods shown outdated but there actually up to date [#52](https://github.com/DeathsGun/ModManager/issues/52)

### Changed
- Rewrite in Kotlin [#44](https://github.com/DeathsGun/ModManager/pull/44)

## [1.0.2-alpha] - 03.09.2021
### Added
- New loading icon [#40](https://github.com/DeathsGun/ModManager/pull/40)
- Chinese translation (Special thanks to MineCommanderCN) [#36](https://github.com/DeathsGun/ModManager/pull/36)
- Korean translation (Special thanks to arlytical#1) [#32](https://github.com/DeathsGun/ModManager/pull/32)
- Russian translation (Special thanks to Felix14-v2) [#31](https://github.com/DeathsGun/ModManager/pull/31)

### Fixed
- CPU overload when using ModManager [#48](https://github.com/DeathsGun/ModManager/issues/48)
- Forge artifacts being downloaded [#37](https://github.com/DeathsGun/ModManager/pull/37)
- NullPointerException's while updating a mod [#34](https://github.com/DeathsGun/ModManager/issues/34)

### Changed
- Improved Turkish translation (Special thanks to kuzeeeyk) [#39](https://github.com/DeathsGun/ModManager/pull/39)

## [1.0.1-alpha] - 24.08.2021
### Added
- Turkish translation (Special thanks to kuzeeeyk) [#21](https://github.com/DeathsGun/ModManager/pull/21)
- Only show "Updatable mods" category when there are updatable
  mods [#10](https://github.com/DeathsGun/ModManager/issues/10)

### Fixed
- Crashes when opening ModMenu [#13](https://github.com/DeathsGun/ModManager/issues/13)
- Update error on Windows because of file locks [#17](https://github.com/DeathsGun/ModManager/issues/13)
- Search only when enter key was hit for improved performance [#7](https://github.com/DeathsGun/ModManager/issues/7)
- Crashes when ModManager loses connection while opening a more detailed
  view [#16](https://github.com/DeathsGun/ModManager/issues/16)
- Icons being mixed up [#22](https://github.com/DeathsGun/ModManager/issues/22)
- Unknown mods showing up [#18](https://github.com/DeathsGun/ModManager/issues/18)

## [1.0.0-alpha] - 23.08.2021
### Added
- Browsing through Modrinth
- Install, remove and update mods
- Notification about updates
- Overview about mods that can be updated