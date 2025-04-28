## BlockFaker - Paper 1.19.3
Send fake blocks and player heads/skulls to specific players.  
Create custom player head textures from existing player heads.  
Based on paperweight 1.19.3-R0.1-SNAPSHOT to access NMS.

#### Requirements
* Paper 1.19.3
* ProtcolLib 5.1.0

#### Features
* Create fake blocks with a material.
* Create fake heads with custom textures.
* Keeps track of who can see what.
* Event based updates to keep fakes: logon, worldchange, chunkloads.
* Ignores player interactions to avoid client side updates.
* Fake blocks/skulls stored persistently in *.yml files.
* Supports permissions.
* Somewhat optimized.

#### Missing
* Refactoring and de-duplication.
* Command overhaul.
* More informative commands.

#### Usage
```mclang
## Fake blocks
/createfakeblock <block_name> <x> <y> <z> <world> <material>
/togglefakeblock <block_name> <player> <hide|show>
/showfakeblocks <player> <block_names...>
/hidefakeblocks ...
/hideallfakeblocks ...
/listfakeblocks
/removefakeblock <block_name>

## While looking at a placed player head with texture
/createtexturefromskull <texture_name> <x> <y> <z> [world]
/removetexture <name>
/listtextures

## Fake player heads
/createfakeskull <skull_name> <x> <y> <z> <world> <texture_name> [ground|wall] [rotation]
# While looking at a placed player head (vanilla too), gets it's placement and rotation
/createfakeskullfromblock <name> <x> <y> <z> <world> <texture_name>

/togglefakeskull <skull_name> <player> <hide|show>
/showfakeskulls <player> <skull_names...>
/hidefakeskulls ...
/hideallfakeskulls ...
/listfakeskulls
/removefakeskull <skull_name>
```