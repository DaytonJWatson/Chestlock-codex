# ChestLock

Secure player storage with lockable containers and flexible sharing controls for Spigot servers.

## Features (and how they work)

### Lockable containers
- **Config-driven lockables**: Only blocks enabled in `config.yml` under `lockables` can be locked. By default this includes chests (including copper variants), barrels, furnaces, hoppers, dispensers, droppers, blast furnaces, smokers, jukeboxes, anvils, and more. (`com.watsonllc.chestlock.Utils#lockableBlock`)
- **Double chest support**: When a chest is part of a double chest, ChestLock treats both halves together so the lock applies to the full inventory. (`com.watsonllc.chestlock.Utils#getConnectedChestBlocks`)

### Automatic lock creation
- **Auto-lock on placement**: When `settings.autoLock` is enabled, placing a lockable block automatically creates a lock after a short delay. Double chests are handled as a single lock. (`events.block.BlockPlace`)
- **Adjacency protection**: Players cannot place a lockable block adjacent to a lock they do not own unless they are bypassing. This prevents “locking around” someone else’s protected container. (`events.block.BlockPlace#adjacentToLock`)

### Access control
- **Owner + shared owners**: Every lock stores an ordered list of allowed players. The first entry is the owner; additional entries are shared owners. (`logic.LockController`)
- **Public mode**: Locks can be toggled public/private. Public locks allow anyone to interact without permission checks. (`logic.LockController#changePublicMode`, `commands.player.MakePublic`)
- **Claiming unowned containers**: `/chestlock claim` lets a player claim an unprotected lockable block and create a new lock. (`commands.player.ClaimLock`)
- **Destroying locks**: `/chestlock destroy` removes a lock from a block (and any connected chest halves). (`commands.player.DestroyLock`)
- **Add/remove owners**: `/chestlock add <player>` and `/chestlock remove <player>` update the lock’s allowed list. Removing the last owner destroys the lock. (`commands.player.AddOwner`, `commands.player.RemoveOwner`)
- **Bypass mode**: `/chestlock bypass` lets authorized players ignore lock protection and bypass auto-lock creation. A warning is shown when bypassing prevents an auto-lock. (`commands.admin.Bypass`, `logic.PlayerStateManager`, `events.block.BlockPlace`)

### Group sharing
- **Groups for shared access**: Owners can create groups and invite members. Players in the same group can access each other’s locks even if they aren’t explicitly listed as owners. (`logic.GroupController`, `commands.player.GroupCommands`, `logic.LockController#hasAccess`)
- **Invites workflow**: Group owners can invite players; invitees can accept/decline via `/chestlock group accept` or `/chestlock group decline`. (`commands.player.GroupCommands`)
- **Groups storage**: Group data is stored in `plugins/ChestLock/groups.yml`. (`config.GroupsData`)

### Hopper protection
- **Tagged hopper owners**: Hoppers and hopper minecarts are tagged with the placing player’s name. (`logic.HopperOwnerData`, `events.player.PlayerInteract#trackHopperMinecartPlacement`)
- **Filtered hopper transfers**: Item movement via hoppers is blocked if the hopper owner is not allowed to access a protected lock or if the transfer crosses locks owned by different players. (`events.block.InventoryMove`)

### Intrusion alerts
- **Per-lock alert toggle**: Shift + left-click a lock while holding redstone to enable/disable alerts for that lock. (`logic.IntrusionAlert`)
- **Owner notifications**: When someone without access attempts to open an alert-enabled lock, the owner receives a warning message (if they are outside the alert radius). (`logic.IntrusionAlert#alert`)

### Debug & utilities
- **Lock debug info**: Shift + left-click a lock with an empty hand to print the lock’s ID, type, public state, and owner list. (`logic.DebugAction`)
- **Inventory sorting**: Optional inventory sorting for chest inventories based on click type and sort mode configured in `config.yml`. (`events.player.InventoryClick`, `logic.ChestSorter`)
- **Explosion protection**: If `settings.tntProof` is enabled, lockable blocks are removed from explosion block lists (TNT/creeper proof). (`events.block.BlockExplode`, `events.block.EntityExplode`)

### Updates and config safety
- **Update checker**: If `settings.updateChecker` is enabled, the plugin checks for updates on startup and notifies players with `chestlock.updatechecker` permission on join. (`Main`, `events.player.PlayerJoin`)
- **Config version guard**: If the on-disk `config.yml` version doesn’t match the expected version, the plugin disables lock behavior and warns admins to regenerate the config. (`Main`, `ErrorBuild`)

## Commands

All commands use `/chestlock` as the root.

- `add <player> [toggle]` — Share a lock with a player by right-clicking the target lock.
- `remove <player> [toggle]` — Remove a player’s access from a lock by right-clicking the target lock.
- `claim [toggle]` — Claim an unprotected lockable block.
- `destroy [toggle]` — Remove a lock from a block.
- `public [toggle]` — Toggle a lock between public/private.
- `bypass` — Toggle bypass mode for administrators.

### Group commands

- `group create <group>` — Create a group (you become the owner).
- `group delete` — Delete your owned group.
- `group invite <player>` — Invite a player to your group.
- `group remove <player>` — Remove a player from your group.
- `group accept [group]` — Accept a group invite.
- `group decline [group]` — Decline a group invite.
- `group invites` — List your pending invites.
- `group leave` — Leave your current group.
- `group list` — List members of your group.

> Tip: The `toggle` option keeps the action active after each click until you run the command again or the action times out (15 seconds).

## Permissions

Permissions are only enforced if `settings.usePermissions` is enabled.

- `chestlock.add`
- `chestlock.remove`
- `chestlock.public`
- `chestlock.claim`
- `chestlock.destroy`
- `chestlock.updatechecker`
- `chestlock.bypass`
- `chestlock.bypass.*`
- `chestlock.group.create`
- `chestlock.group.delete`
- `chestlock.group.add`
- `chestlock.group.remove`
- `chestlock.group.leave`
- `chestlock.group.list`
- `chestlock.group.invite`
- `chestlock.group.accept`
- `chestlock.group.decline`
- `chestlock.group.invites`

## Configuration reference

Key settings in `plugins/ChestLock/config.yml`:

- `settings.usePermissions` — Enable permission checks for commands.
- `settings.updateChecker` — Enable update checking on startup and player join.
- `settings.autoLock` — Auto-create locks on placement.
- `settings.tntProof` — Prevent lockable blocks from being destroyed by explosions.
- `settings.sortInventoryEnabled` — Enable inventory sorting.
- `settings.sortBy` — Sorting mode (`alphabetical` or `type`).
- `settings.sortWith` — Click type to trigger sorting (e.g. `DOUBLE_CLICK`).
- `settings.intrusionAlerts` — Enable intrusion alert system.
- `settings.alertRadius` — Radius used to determine whether owners receive an alert.
- `settings.lockID-characters` / `settings.lockID-size` — Lock ID format.
- `lockables.*` — Toggle lock support per block type.

## Data files

- `plugins/ChestLock/locks.yml` — Stores all lock metadata (owner list, location, public flag, alerts).
- `plugins/ChestLock/groups.yml` — Stores group membership and invite data.

## Plugin metadata

- **Name**: ChestLock
- **API Version**: 1.21
- **Main class**: `com.watsonllc.chestlock.Main`
- **Command**: `/chestlock`
