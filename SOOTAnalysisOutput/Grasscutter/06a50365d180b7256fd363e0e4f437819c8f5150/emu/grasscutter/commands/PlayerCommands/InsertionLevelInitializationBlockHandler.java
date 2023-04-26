package emu.grasscutter.commands;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GenshinData;
import emu.grasscutter.data.def.ItemData;
import emu.grasscutter.data.def.AvatarData;
import emu.grasscutter.data.def.AvatarSkillDepotData;
import emu.grasscutter.data.def.MonsterData;
import emu.grasscutter.game.GenshinPlayer;
import emu.grasscutter.game.GenshinScene;
import emu.grasscutter.game.World;
import emu.grasscutter.game.avatar.GenshinAvatar;
import emu.grasscutter.game.entity.EntityAvatar;
import emu.grasscutter.game.entity.EntityItem;
import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.game.inventory.GenshinItem;
import emu.grasscutter.game.inventory.Inventory;
import emu.grasscutter.game.inventory.ItemType;
import emu.grasscutter.game.props.ActionReason;
import emu.grasscutter.game.props.FightProperty;
import emu.grasscutter.game.props.PlayerProperty;
import emu.grasscutter.server.packet.send.PacketEntityFightPropUpdateNotify;
import emu.grasscutter.server.packet.send.PacketItemAddHintNotify;
import emu.grasscutter.utils.Position;
import java.util.LinkedList;
import java.util.List;

public final class PlayerCommands {

    @Command(label = "give", aliases = { "g", "item", "giveitem" }, usage = "Usage: give [player] <itemId|itemName> [amount]")
    public static class GiveCommand implements CommandHandler {

        @Override
        public void execute(GenshinPlayer player, List<String> args) {
            int target, item, amount = 1;
            switch(args.size()) {
                default:
                    CommandHandler.sendMessage(player, "Usage: give <player> <itemId|itemName> [amount]");
                    return;
                case 1:
                    try {
                        item = Integer.parseInt(args.get(0));
                        target = player.getAccount().getPlayerId();
                    } catch (NumberFormatException ignored) {
                        CommandHandler.sendMessage(player, "Invalid item id.");
                        return;
                    }
                    break;
                case 2:
                    try {
                        target = Integer.parseInt(args.get(0));
<<<<<<< MINE
                        if (Grasscutter.getGameServer().getPlayerById(target) == null) {
                            target = player.getId();
                            amount = Integer.parseInt(args.get(1));
                            item = Integer.parseInt(args.get(0));
                        } else {
                            item = Integer.parseInt(args.get(1));
                        }
                    } catch (NumberFormatException ignored) {
                        CommandHandler.sendMessage(player, "Invalid item or player ID.");
                        return;
                    }
                    break;
                case 3:
                    try {
                        target = Integer.parseInt(args.get(0));
                        if (Grasscutter.getGameServer().getPlayerById(target) == null) {
                            CommandHandler.sendMessage(player, "Invalid player ID.");
                            return;
                        }
                        item = Integer.parseInt(args.get(1));
                        amount = Integer.parseInt(args.get(2));
                    } catch (NumberFormatException ignored) {
                        CommandHandler.sendMessage(player, "Invalid item or player ID.");
                        return;
                    }
                    break;
            }
            GenshinPlayer targetPlayer = Grasscutter.getGameServer().getPlayerById(target);
=======
                        if (Grasscutter.getGameServer().getPlayerByUid(target) == null) {
                            target = player.getUid();
                            amount = Integer.parseInt(args.get(1));
                            item = Integer.parseInt(args.get(0));
                        } else {
                            item = Integer.parseInt(args.get(1));
                        }
                    } catch (NumberFormatException ignored) {
                        CommandHandler.sendMessage(player, "Invalid item or player ID.");
                        return;
                    }
                    break;
                case 3:
                    try {
                        target = Integer.parseInt(args.get(0));
                        if (Grasscutter.getGameServer().getPlayerByUid(target) == null) {
                            CommandHandler.sendMessage(player, "Invalid player ID.");
                            return;
                        }
                        item = Integer.parseInt(args.get(1));
                        amount = Integer.parseInt(args.get(2));
                    } catch (NumberFormatException ignored) {
                        CommandHandler.sendMessage(player, "Invalid item or player ID.");
                        return;
                    }
                    break;
            }
            GenshinPlayer targetPlayer = Grasscutter.getGameServer().getPlayerByUid(target);
>>>>>>> YOURS
            if (targetPlayer == null) {
                CommandHandler.sendMessage(player, "Player not found.");
                return;
            }
            ItemData itemData = GenshinData.getItemDataMap().get(item);
            if (itemData == null) {
                CommandHandler.sendMessage(player, "Invalid item id.");
                return;
            }
            this.item(targetPlayer, itemData, amount);
        }

        @Override
        public void execute(List<String> args) {
            if (args.size() < 2) {
                CommandHandler.sendMessage(null, "Usage: give <player> <itemId|itemName> [amount]");
                return;
            }
            try {
                int target = Integer.parseInt(args.get(0));
                int item = Integer.parseInt(args.get(1));
                int amount = 1;
                if (args.size() > 2)
                    amount = Integer.parseInt(args.get(2));
<<<<<<< MINE
                GenshinPlayer targetPlayer = Grasscutter.getGameServer().getPlayerById(target);
=======
                GenshinPlayer targetPlayer = Grasscutter.getGameServer().getPlayerByUid(target);
>>>>>>> YOURS
                if (targetPlayer == null) {
                    CommandHandler.sendMessage(null, "Player not found.");
                    return;
                }
                ItemData itemData = GenshinData.getItemDataMap().get(item);
                if (itemData == null) {
                    CommandHandler.sendMessage(null, "Invalid item id.");
                    return;
                }
                this.item(targetPlayer, itemData, amount);
            } catch (NumberFormatException ignored) {
                CommandHandler.sendMessage(null, "Invalid item or player ID.");
            }
        }

        private void item(GenshinPlayer player, ItemData itemData, int amount) {
            GenshinItem genshinItem = new GenshinItem(itemData);
            if (itemData.isEquip()) {
                List<GenshinItem> items = new LinkedList<>();
                for (int i = 0; i < amount; i++) {
                    items.add(genshinItem);
                }
                player.getInventory().addItems(items);
                player.sendPacket(new PacketItemAddHintNotify(items, ActionReason.SubfieldDrop));
            } else {
                genshinItem.setCount(amount);
                player.getInventory().addItem(genshinItem);
                player.sendPacket(new PacketItemAddHintNotify(genshinItem, ActionReason.SubfieldDrop));
            }
        }
    }

    @Command(label = "drop", aliases = { "d", "dropitem" }, usage = "Usage: drop <itemId|itemName> [amount]", execution = Command.Execution.PLAYER)
    public static class DropCommand implements CommandHandler {

        @Override
        public void execute(GenshinPlayer player, List<String> args) {
            if (args.size() < 1) {
                CommandHandler.sendMessage(player, "Usage: drop <itemId|itemName> [amount]");
                return;
            }
            try {
                int item = Integer.parseInt(args.get(0));
                int amount = 1;
                if (args.size() > 1)
                    amount = Integer.parseInt(args.get(1));
                ItemData itemData = GenshinData.getItemDataMap().get(item);
                if (itemData == null) {
                    CommandHandler.sendMessage(player, "Invalid item id.");
                    return;
                }
                if (itemData.isEquip()) {
                    float range = (5f + (.1f * amount));
                    for (int i = 0; i < amount; i++) {
                        Position pos = player.getPos().clone().addX((float) (Math.random() * range) - (range / 2)).addY(3f).addZ((float) (Math.random() * range) - (range / 2));
<<<<<<< MINE
                        EntityItem entity = new EntityItem(player.getWorld(), player, itemData, pos, 1);
                        player.getWorld().addEntity(entity);
                    }
                } else {
                    EntityItem entity = new EntityItem(player.getWorld(), player, itemData, player.getPos().clone().addY(3f), amount);
                    player.getWorld().addEntity(entity);
=======
                        EntityItem entity = new EntityItem(player.getScene(), player, itemData, pos, 1);
                        player.getScene().addEntity(entity);
                    }
                } else {
                    EntityItem entity = new EntityItem(player.getScene(), player, itemData, player.getPos().clone().addY(3f), amount);
                    player.getScene().addEntity(entity);
>>>>>>> YOURS
                }
            } catch (NumberFormatException ignored) {
                CommandHandler.sendMessage(player, "Invalid item or player ID.");
            }
        }
    }

    @Command(label = "givechar", aliases = { "givec" }, usage = "Usage: givechar <playerId> <avatarId> [level]")
    public static class GiveCharCommand implements CommandHandler {

        @Override
        public void execute(GenshinPlayer player, List<String> args) {
            int target, avatarID, level = 1, ascension = 1;
            if (args.size() < 2) {
                CommandHandler.sendMessage(null, "Usage: givechar <player> <avatarId> [level]");
                return;
            }
            switch(args.size()) {
                default:
                    CommandHandler.sendMessage(null, "Usage: givechar <player> <avatarId> [level]");
                    return;
                case 1:
                    try {
                        avatarID = Integer.parseInt(args.get(0));
                        target = player.getAccount().getPlayerId();
                    } catch (NumberFormatException ignored) {
                        CommandHandler.sendMessage(player, "Invalid avatar id.");
                        return;
                    }
                    break;
                case 2:
                    try {
                        target = Integer.parseInt(args.get(0));
                        if (Grasscutter.getGameServer().getPlayerById(target) == null) {
                            target = player.getId();
                            level = Integer.parseInt(args.get(1));
                            avatarID = Integer.parseInt(args.get(0));
                        } else {
                            avatarID = Integer.parseInt(args.get(1));
                        }
                    } catch (NumberFormatException ignored) {
                        CommandHandler.sendMessage(player, "Invalid avatar or player ID.");
                        return;
                    }
                    break;
                case 3:
                    try {
                        target = Integer.parseInt(args.get(0));
                        if (Grasscutter.getGameServer().getPlayerById(target) == null) {
                            CommandHandler.sendMessage(player, "Invalid player ID.");
                            return;
                        }
                        avatarID = Integer.parseInt(args.get(1));
                        level = Integer.parseInt(args.get(2));
                    } catch (NumberFormatException ignored) {
                        CommandHandler.sendMessage(player, "Invalid avatar or player ID.");
                        return;
                    }
                    break;
            }
            GenshinPlayer targetPlayer = Grasscutter.getGameServer().getPlayerById(target);
            if (targetPlayer == null) {
                CommandHandler.sendMessage(null, "Player not found.");
                return;
            }
            AvatarData avatarData = GenshinData.getAvatarDataMap().get(avatarID);
            if (avatarData == null) {
                CommandHandler.sendMessage(null, "Invalid avatar id.");
                return;
            }
            if (level <= 40) {
                ascension = (int) Math.ceil(level / 20);
            } else if (level > 20) {
                ascension = (int) Math.ceil(level / 10) - 3;
            }
            GenshinAvatar avatar = new GenshinAvatar(avatarID);
            avatar.setLevel(level);
            avatar.setPromoteLevel(ascension);
            avatar.recalcStats();
            targetPlayer.addAvatar(avatar);
        }

        @Override
        public void execute(List<String> args) {
            if (args.size() < 2) {
                CommandHandler.sendMessage(null, "Usage: givechar <player> <itemId|itemName> [amount]");
                return;
            }
            try {
                int target = Integer.parseInt(args.get(0));
                int avatarID = Integer.parseInt(args.get(1));
                int level = 1;
                if (args.size() > 2)
                    level = Integer.parseInt(args.get(2));
                int ascension = 1;
                GenshinPlayer targetPlayer = Grasscutter.getGameServer().getPlayerById(target);
                if (targetPlayer == null) {
                    CommandHandler.sendMessage(null, "Player not found.");
                    return;
                }
                AvatarData avatarData = GenshinData.getAvatarDataMap().get(avatarID);
                if (avatarData == null) {
                    CommandHandler.sendMessage(null, "Invalid avatar id.");
                    return;
                }
                if (level <= 40) {
                    ascension = (int) Math.ceil(level / 20);
                } else if (level > 20) {
                    ascension = (int) Math.ceil(level / 10) - 3;
                }
                GenshinAvatar avatar = new GenshinAvatar(avatarID);
                avatar.setLevel(level);
                avatar.setPromoteLevel(ascension);
                avatar.recalcStats();
                targetPlayer.addAvatar(avatar);
            } catch (NumberFormatException ignored) {
                CommandHandler.sendMessage(null, "Invalid item or player ID.");
            }
        }
    }

    @Command(label = "spawn", execution = Command.Execution.PLAYER, usage = "Usage: spawn <entityId|entityName> [level] [amount]")
    public static class SpawnCommand implements CommandHandler {

        @Override
        public void execute(GenshinPlayer player, List<String> args) {
            if (args.size() < 1) {
                CommandHandler.sendMessage(null, "Usage: spawn <entityId|entityName> [amount]");
                return;
            }
            try {
                int entity = Integer.parseInt(args.get(0));
                int level = 1;
                if (args.size() > 1)
                    level = Integer.parseInt(args.get(1));
                int amount = 1;
                if (args.size() > 2)
                    amount = Integer.parseInt(args.get(2));
                MonsterData entityData = GenshinData.getMonsterDataMap().get(entity);
                if (entityData == null) {
                    CommandHandler.sendMessage(null, "Invalid entity id.");
                    return;
                }
                float range = (5f + (.1f * amount));
                for (int i = 0; i < amount; i++) {
                    Position pos = player.getPos().clone().addX((float) (Math.random() * range) - (range / 2)).addY(3f).addZ((float) (Math.random() * range) - (range / 2));
<<<<<<< MINE
                    EntityMonster monster = new EntityMonster(player.getWorld(), entityData, pos, level);
                    player.getWorld().addEntity(monster);
=======
                    EntityMonster monster = new EntityMonster(player.getScene(), entityData, pos, level);
                    player.getScene().addEntity(monster);
>>>>>>> YOURS
                }
            } catch (NumberFormatException ignored) {
                CommandHandler.sendMessage(null, "Invalid item or player ID.");
            }
        }
    }

<<<<<<< MINE
@Command(label = "killall", usage = "Usage: killall [sceneId]")
=======
@Command(label = "killall", usage = "Usage: killall [playerUid] [sceneId]")
>>>>>>> YOURS
    public static class KillAllCommand implements CommandHandler {

        @Override
        public void execute(GenshinPlayer player, List<String> args) {
<<<<<<< MINE
            World world = player.getWorld();
            world.getEntities().values().stream().filter(entity -> entity instanceof EntityMonster).forEach(entity -> world.killEntity(entity, 0));
=======
            GenshinScene scene = player.getScene();
            scene.getEntities().values().stream().filter(entity -> entity instanceof EntityMonster).forEach(entity -> scene.killEntity(entity, 0));
            CommandHandler.sendMessage(null, "Killing all monsters in scene " + scene.getId());
>>>>>>> YOURS
        }

        @Override
        public void execute(List<String> args) {
<<<<<<< MINE
            if (args.size() < 1) {
                CommandHandler.sendMessage(null, "Usage: killall [sceneId]");
                return;
            }
            try {
                int sceneId = Integer.parseInt(args.get(0));
                CommandHandler.sendMessage(null, "Killing all monsters in scene " + sceneId);
            } catch (NumberFormatException ignored) {
                CommandHandler.sendMessage(null, "Invalid scene id.");
=======
            if (args.size() < 2) {
                CommandHandler.sendMessage(null, "Usage: killall [playerUid] [sceneId]");
                return;
            }
            try {
                int playerUid = Integer.parseInt(args.get(0));
                int sceneId = Integer.parseInt(args.get(1));
                GenshinPlayer player = Grasscutter.getGameServer().getPlayerByUid(playerUid);
                if (player == null) {
                    CommandHandler.sendMessage(null, "Player not found or offline.");
                    return;
                }
                GenshinScene scene = player.getWorld().getSceneById(sceneId);
                if (scene == null) {
                    CommandHandler.sendMessage(null, "Scene not found in player world");
                    return;
                }
                scene.getEntities().values().stream().filter(entity -> entity instanceof EntityMonster).forEach(entity -> scene.killEntity(entity, 0));
                CommandHandler.sendMessage(null, "Killing all monsters in scene " + scene.getId());
            } catch (NumberFormatException ignored) {
                CommandHandler.sendMessage(null, "Invalid arguments.");
>>>>>>> YOURS
            }
        }
    }

    @Command(label = "resetconst", aliases = { "resetconstellation" }, usage = "Usage: resetconst [all]", execution = Command.Execution.PLAYER)
    public static class ResetConstellationCommand implements CommandHandler {

        @Override
        public void execute(GenshinPlayer player, List<String> args) {
            if (args.size() > 0 && args.get(0).equalsIgnoreCase("all")) {
                player.getAvatars().forEach(this::resetConstellation);
                player.dropMessage("Reset all avatars' constellations.");
            } else {
                EntityAvatar entity = player.getTeamManager().getCurrentAvatarEntity();
                if (entity == null)
                    return;
                GenshinAvatar avatar = entity.getAvatar();
                this.resetConstellation(avatar);
                player.dropMessage("Constellations for " + avatar.getAvatarData().getName() + " have been reset. Please relog to see changes.");
            }
        }

        private void resetConstellation(GenshinAvatar avatar) {
            avatar.getTalentIdList().clear();
            avatar.setCoreProudSkillLevel(0);
            avatar.recalcStats();
            avatar.save();
        }
    }

    @Command(label = "godmode", usage = "Usage: godmode", execution = Command.Execution.PLAYER)
    public static class GodModeCommand implements CommandHandler {

        @Override
        public void execute(GenshinPlayer player, List<String> args) {
            player.setGodmode(!player.inGodmode());
            player.dropMessage("Godmode is now " + (player.inGodmode() ? "enabled" : "disabled") + ".");
        }
    }

    @Command(label = "sethealth", aliases = { "sethp" }, usage = "Usage: sethealth <hp>", execution = Command.Execution.PLAYER)
    public static class SetHealthCommand implements CommandHandler {

        @Override
        public void execute(GenshinPlayer player, List<String> args) {
            if (args.size() < 1) {
                CommandHandler.sendMessage(null, "Usage: sethealth <hp>");
                return;
            }
            try {
                int health = Integer.parseInt(args.get(0));
                EntityAvatar entity = player.getTeamManager().getCurrentAvatarEntity();
                if (entity == null)
                    return;
                entity.setFightProperty(FightProperty.FIGHT_PROP_CUR_HP, health);
                entity.getWorld().broadcastPacket(new PacketEntityFightPropUpdateNotify(entity, FightProperty.FIGHT_PROP_CUR_HP));
                player.dropMessage("Health set to " + health + ".");
            } catch (NumberFormatException ignored) {
                CommandHandler.sendMessage(null, "Invalid health value.");
            }
        }
    }

    @Command(label = "setworldlevel", aliases = { "setworldlvl" }, usage = "Usage: setworldlevel <level>", execution = Command.Execution.PLAYER)
    public static class SetWorldLevelCommand implements CommandHandler {

        @Override
        public void execute(GenshinPlayer player, List<String> args) {
            if (args.size() < 1) {
                CommandHandler.sendMessage(null, "Usage: setworldlevel <level>");
                return;
            }
            try {
                int level = Integer.parseInt(args.get(0));
                player.getWorld().setWorldLevel(level);
                player.setProperty(PlayerProperty.PROP_PLAYER_WORLD_LEVEL, level);
                player.dropMessage("World level set to " + level + ".");
            } catch (NumberFormatException ignored) {
                CommandHandler.sendMessage(null, "Invalid world level.");
            }
        }
    }

    @Command(label = "clearartifacts", aliases = { "clearart" }, usage = "Usage: clearartifacts", execution = Command.Execution.PLAYER)
    public static class ClearArtifactsCommand implements CommandHandler {

        @Override
        public void execute(GenshinPlayer player, List<String> args) {
            Inventory playerInventory = player.getInventory();
            playerInventory.getItems().values().stream().filter(item -> item.getItemType() == ItemType.ITEM_RELIQUARY).filter(item -> item.getLevel() == 1 && item.getExp() == 0).filter(item -> !item.isLocked() && !item.isEquipped()).forEach(item -> playerInventory.removeItem(item, item.getCount()));
        }
    }

    @Command(label = "changescene", aliases = { "scene" }, usage = "Usage: changescene <scene id>", execution = Command.Execution.PLAYER)
    public static class ChangeSceneCommand implements CommandHandler {

        @Override
        public void execute(GenshinPlayer player, List<String> args) {
            if (args.size() < 1) {
                CommandHandler.sendMessage(null, "Usage: changescene <scene id>");
                return;
            }
            int sceneId = 0;
            try {
                sceneId = Integer.parseInt(args.get(0));
            } catch (Exception e) {
                return;
            }
            boolean result = player.getWorld().transferPlayerToScene(player, sceneId, player.getPos());
            if (!result) {
                CommandHandler.sendMessage(null, "Scene does not exist or you are already in it");
            }
        }
    }
}