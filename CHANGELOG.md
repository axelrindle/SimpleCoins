# Changelog

All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased

### Added

- Localization
- `/sc sync` for synchronizing the database
- Dependency on [PocketKnife v2.0.0](https://github.com/axelrindle/PocketKnife/releases/tag/2.0.0)

### Fixed

- An error where the MySQL connection fails where there are differences between the used timezones.

## v0.4 (`20.07.2017`)

### Fixed

- Config won't be overwritten on every server startup anymore

## v0.3 (`30.12.2016`)

### Added

- Integration with [Vault](https://github.com/milkbowl/Vault)

## v0.2 (`27.12.2015`)

### Added

- Command to reload the configuration
- Two new messages (`NoPermission` and `Reload`)

### Changed

- Comments in the main config get removed

### Fixed

- Fixed a bug with a permission where you needed the simplecoins.get permission for the /sc set command

## v0.1 (`26.12.2015`)

### Added

- Balances can be stored either local or in a MySQL database
- Commands: `add`, `set`, `get`, `remote`
