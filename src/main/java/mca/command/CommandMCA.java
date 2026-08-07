package mca.command;

import com.google.common.base.Optional;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.MCAServer;
import mca.core.minecraft.ItemsMCA;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.PlayerSaveData;
import mca.items.ItemBaby;
import mca.util.Util;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CommandMCA extends CommandBase {
    private enum Subcommand {
        HELP("help", 0, false, "/mca help", "Shows this list of commands."),
        PROPOSE("propose", 1, true, "/mca propose <PlayerName>", "Proposes marriage to the given player."),
        PROPOSALS("proposals", 0, false, "/mca proposals", "Shows all active proposals."),
        ACCEPT("accept", 1, true, "/mca accept <PlayerName>", "Accepts the player's marriage request."),
        REJECT("reject", 1, true, "/mca reject <PlayerName>", "Rejects the player's marriage request."),
        PROCREATE("procreate", 0, false, "/mca procreate", "Starts procreation."),
        SEPARATE("separate", 0, false, "/mca separate", "Ends your marriage."),
        ADMIN("admin", -1, false, "/mca admin <subcommand>", "Runs an administrative MCA subcommand.");

        private final String name;
        private final int argumentCount;
        private final boolean playerArgument;
        private final String usage;
        private final String description;

        Subcommand(String name, int argumentCount, boolean playerArgument, String usage, String description) {
            this.name = name;
            this.argumentCount = argumentCount;
            this.playerArgument = playerArgument;
            this.usage = usage;
            this.description = description;
        }

        private static Subcommand byName(String name) {
            for (Subcommand command : values()) {
                if (command.name.equalsIgnoreCase(name)) {
                    return command;
                }
            }
            return null;
        }

        private static String[] names() {
            Subcommand[] commands = values();
            String[] names = new String[commands.length];
            for (int i = 0; i < commands.length; i++) {
                names[i] = commands[i].name;
            }
            return names;
        }

        private void validate(String[] arguments) throws WrongUsageException {
            if (argumentCount >= 0 && arguments.length != argumentCount) {
                throw new WrongUsageException(usage);
            }
        }
    }

    private enum AdminSubcommand {
        HELP("help", 0, "/mca admin help", "Shows this list of commands."),
        FFH("ffh", 0, "/mca admin ffh", "Force all hearts on all villagers."),
        FBG("fbg", 0, "/mca admin fbg", "Force your baby to grow up."),
        FCG("fcg", 0, "/mca admin fcg", "Force nearby children to grow."),
        CLV("clv", 0, "/mca admin clv", "Clear all loaded villagers."),
        INH("inh", 0, "/mca admin inh", "Increase hearts by 10."),
        DEH("deh", 0, "/mca admin deh", "Decrease hearts by 10."),
        SGR("sgr", 0, "/mca admin sgr", "Spawn a Grim Reaper."),
        KGR("kgr", 0, "/mca admin kgr", "Kill all Grim Reapers in the world."),
        DPD("dpd", 0, "/mca admin dpd", "Dump player data to chat."),
        RVD("rvd", 1, "/mca admin rvd <UUID>", "Reset the given villager."),
        RPD("rpd", 1, "/mca admin rpd <PlayerName>", "Reset the given player's MCA data."),
        CVE("cve", 0, "/mca admin cve", "Remove all villager editors from the game.");

        private final String name;
        private final int argumentCount;
        private final String usage;
        private final String description;

        AdminSubcommand(String name, int argumentCount, String usage, String description) {
            this.name = name;
            this.argumentCount = argumentCount;
            this.usage = usage;
            this.description = description;
        }

        private static AdminSubcommand byName(String name) {
            for (AdminSubcommand command : values()) {
                if (command.name.equalsIgnoreCase(name)) {
                    return command;
                }
            }
            return null;
        }

        private static String[] names() {
            AdminSubcommand[] commands = values();
            String[] names = new String[commands.length];
            for (int i = 0; i < commands.length; i++) {
                names[i] = commands[i].name;
            }
            return names;
        }

        private void validate(String[] arguments) throws WrongUsageException {
            if (arguments.length != argumentCount) {
                throw new WrongUsageException(usage);
            }
        }
    }

    @Override
    public String getName() {
        return "mca";
    }

    @Override
    public String getUsage(ICommandSender commandSender) {
        return "/mca <subcommand> <arguments>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] input) throws CommandException {
        try {
            if (input.length == 0) {
                throw new WrongUsageException("");
            }

            String subcommandName = input[0].toLowerCase(Locale.ROOT);
            Subcommand subcommand = Subcommand.byName(subcommandName);
            if (subcommand == null) {
                throw new WrongUsageException("");
            }

            if (subcommand == Subcommand.ADMIN) {
                executeAdmin(server, commandSender, Arrays.copyOfRange(input, 1, input.length));
                return;
            }

            if (!MCA.getConfig().allowPlayerMarriage) {
                sendMessage(commandSender, "MCA commands have been disabled by the server administrator.");
                return;
            }

            final EntityPlayer player = (EntityPlayer) commandSender;
            String[] arguments = Arrays.copyOfRange(input, 1, input.length);
            MCA.getLog().info(player.getName() + " entered command " + Arrays.toString(input));
            subcommand.validate(arguments);

            switch (subcommand) {
                case HELP:
                    displayHelp(commandSender);
                    break;
                case PROPOSE:
                    EntityPlayer target = server.getPlayerList().getPlayerByUsername(arguments[0]);
                    if (target != null) {
                        MCAServer.get().sendProposal(player, target);
                    } else {
                        player.sendMessage(new TextComponentString("Player not found on the server."));
                    }
                    break;
                case ACCEPT:
                    target = server.getPlayerList().getPlayerByUsername(arguments[0]);
                    if (target != null) {
                        MCAServer.get().acceptProposal(player, target);
                    } else {
                        player.sendMessage(new TextComponentString("Player not found on the server."));
                    }
                    break;
                case PROPOSALS:
                    MCAServer.get().listProposals(player);
                    break;
                case PROCREATE:
                    MCAServer.get().procreate(player);
                    break;
                case SEPARATE:
                    MCAServer.get().endMarriage(player);
                    break;
                case REJECT:
                    target = server.getPlayerList().getPlayerByUsername(arguments[0]);
                    if (target != null) {
                        MCAServer.get().rejectProposal(player, target);
                    } else {
                        player.sendMessage(new TextComponentString("Player not found on the server."));
                    }
                    break;
                default:
                    throw new WrongUsageException("");
            }
        } catch (ClassCastException e) {
            throw new CommandException("MCA commands cannot be used through rcon.");
        } catch (WrongUsageException e) {
            throw new CommandException("Your command was invalid or improperly formatted. Usage: " + getUsage(commandSender));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 0) {
            return Arrays.asList(Subcommand.names());
        }

        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, Subcommand.names());
        }

        if (Subcommand.ADMIN.name.equalsIgnoreCase(args[0])) {
            return getAdminTabCompletions(server, Arrays.copyOfRange(args, 1, args.length));
        }

        Subcommand subcommand = Subcommand.byName(args[0]);
        if (subcommand != null && subcommand.playerArgument && args.length == 2) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }

        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        if (index == 1 && args.length > 0) {
            Subcommand subcommand = Subcommand.byName(args[0]);
            return subcommand != null && subcommand.playerArgument;
        }

        if (index >= 2 && args.length > 0 && Subcommand.ADMIN.name.equalsIgnoreCase(args[0])) {
            String[] adminArgs = Arrays.copyOfRange(args, 1, args.length);
            return index - 1 == 1 && adminArgs.length > 0 && AdminSubcommand.RPD.name.equalsIgnoreCase(adminArgs[0]);
        }

        return false;
    }

    private void executeAdmin(MinecraftServer server, ICommandSender commandSender, String[] input) throws CommandException {
        try {
            if (!commandSender.canUseCommand(4, getName())) {
                throw new CommandException("You do not have permission to use MCA admin commands.");
            }

            if (!MCA.getConfig().enableAdminCommands) {
                sendMessage(commandSender, "MCA admin commands have been disabled by the server administrator.");
                return;
            }

            if (input.length == 0) {
                throw new WrongUsageException("/mca admin <subcommand> <arguments>");
            }

            final EntityPlayer player = (EntityPlayer) commandSender;
            AdminSubcommand subcommand = AdminSubcommand.byName(input[0]);
            if (subcommand == null) {
                throw new WrongUsageException("/mca admin <subcommand> <arguments>");
            }

            String[] arguments = Arrays.copyOfRange(input, 1, input.length);
            MCA.getLog().info(player.getName() + " entered admin command " + Arrays.toString(input));
            subcommand.validate(arguments);

            switch (subcommand) {
                case HELP:
                    displayAdminHelp(commandSender);
                    break;
                case FFH:
                    forceFullHearts(player);
                    break;
                case FBG:
                    forceBabyGrow(player);
                    break;
                case FCG:
                    forceChildGrow(player);
                    break;
                case CLV:
                    clearLoadedVillagers(player);
                    break;
                case INH:
                    incrementHearts(player);
                    break;
                case DEH:
                    decrementHearts(player);
                    break;
                case SGR:
                    spawnGrimReaper(player);
                    break;
                case KGR:
                    killGrimReaper(player);
                    break;
                case DPD:
                    dumpPlayerData(player);
                    break;
                case RVD:
                    resetVillagerData(player, arguments);
                    break;
                case RPD:
                    resetPlayerData(server, player, arguments);
                    break;
                case CVE:
                    clearVillagerEditors(player);
                    break;
                default:
                    throw new WrongUsageException("/mca admin <subcommand> <arguments>");
            }
        } catch (ClassCastException e) {
            throw new CommandException("MCA commands cannot be used through rcon.");
        } catch (WrongUsageException e) {
            throw new CommandException("Your admin command was invalid or improperly formatted. Usage: /mca admin <subcommand> <arguments>");
        }
    }

    private List<String> getAdminTabCompletions(MinecraftServer server, String[] args) {
        if (args.length == 0) {
            return Arrays.asList(AdminSubcommand.names());
        }

        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, AdminSubcommand.names());
        }

        if (args.length == 2 && AdminSubcommand.RPD.name.equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }

        return Collections.emptyList();
    }

    private void forceFullHearts(EntityPlayer player) {
        for (Entity entity : player.world.loadedEntityList) {
            if (entity instanceof EntityVillagerMCA) {
                EntityVillagerMCA villager = (EntityVillagerMCA) entity;
                villager.getPlayerHistoryFor(player.getUniqueID()).setHearts(100);
            }
        }
        sendMessage(player, Constants.Color.GREEN + "Forced full hearts on all villagers.");
    }

    private void forceBabyGrow(EntityPlayer player) {
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack.getItem() instanceof ItemBaby) {
                stack.getTagCompound().setInteger("age", MCA.getConfig().babyGrowUpTime);
            }
        }
        sendMessage(player, Constants.Color.GREEN + "Forced any held babies to grow up age.");
    }

    private void forceChildGrow(EntityPlayer player) {
        player.world.loadedEntityList.stream()
                .filter(e -> e instanceof EntityVillagerMCA && ((EntityVillagerMCA) e).isChild())
                .forEach(e -> ((EntityVillagerMCA) e).addGrowth(999999));
        sendMessage(player, Constants.Color.GREEN + "Forced any children to grow to adults.");
    }

    private void clearLoadedVillagers(EntityPlayer player) {
        int count = 0;
        for (Entity entity : player.world.loadedEntityList) {
            if (entity instanceof EntityVillagerMCA) {
                entity.setDead();
                count++;
            }
        }
        sendMessage(player, Constants.Color.GREEN + "Cleared " + count + " villagers from the world.");
    }

    private void incrementHearts(EntityPlayer player) {
        for (Entity entity : player.world.loadedEntityList) {
            if (entity instanceof EntityVillagerMCA) {
                ((EntityVillagerMCA) entity).getPlayerHistoryFor(player.getUniqueID()).changeHearts(10);
            }
        }
        sendMessage(player, Constants.Color.GREEN + "Increased hearts for all villagers by 10.");
    }

    private void decrementHearts(EntityPlayer player) {
        for (Entity entity : player.world.loadedEntityList) {
            if (entity instanceof EntityVillagerMCA) {
                ((EntityVillagerMCA) entity).getPlayerHistoryFor(player.getUniqueID()).changeHearts(-10);
            }
        }
        sendMessage(player, Constants.Color.GREEN + "Decreased hearts for all villagers by 10.");
    }

    private void spawnGrimReaper(EntityPlayer player) {
        EntityGrimReaper reaper = new EntityGrimReaper(player.world);
        reaper.setPosition(player.posX, player.posY, player.posZ);
        player.world.spawnEntity(reaper);
    }

    private void killGrimReaper(EntityPlayer player) {
        player.world.loadedEntityList.stream()
                .filter(e -> e instanceof EntityGrimReaper)
                .forEach(Entity::setDead);
    }

    private void dumpPlayerData(EntityPlayer player) {
        PlayerSaveData.get(player).dump(player);
    }

    private void resetVillagerData(EntityPlayer sender, String[] arguments) {
        Optional<EntityVillagerMCA> target = Util.getEntityByUUID(sender.world, UUID.fromString(arguments[0]), EntityVillagerMCA.class);
        if (!target.isPresent()) {
            sendMessage(sender, "Target villager was not found.");
        } else {
            target.get().reset();
            sendMessage(sender, target.get().getDisplayName().getUnformattedText() + " has been reset successfully.");
        }
    }

    private void resetPlayerData(MinecraftServer server, EntityPlayer sender, String[] arguments) {
        EntityPlayer target = server.getPlayerList().getPlayerByUsername(arguments[0]);
        if (target == null) {
            sendMessage(sender, "Player not found on the server.");
        } else {
            PlayerSaveData.get(target).reset();
            sendMessage(sender, "Player data for " + target.getName() + " has been reset successfully.");
            sendMessage(target, "Your player data has been reset by " + sender.getName() + ".");
        }
    }

    private void clearVillagerEditors(EntityPlayer sender) {
        ItemStack editorStack = new ItemStack(ItemsMCA.VILLAGER_EDITOR);
        sender.world.playerEntities.stream()
                .filter(player -> player.inventory.hasItemStack(editorStack))
                .forEach(player -> {
                    int slot = 0;
                    while (slot < player.inventory.getSizeInventory() - 1) {
                        if (player.inventory.getStackInSlot(slot).getItem() == ItemsMCA.VILLAGER_EDITOR) {
                            player.inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
                        }
                        slot++;
                    }
                });
        sendMessage(sender, "All villager editors cleared from inventories.");
    }

    private void sendMessage(ICommandSender commandSender, String message) {
        commandSender.sendMessage(new TextComponentString(Constants.Color.GOLD + "[MCA] " + Constants.Format.RESET + message));
    }

    private void sendMessage(ICommandSender commandSender, String message, boolean noPrefix) {
        if (noPrefix) {
            commandSender.sendMessage(new TextComponentString(message));
        } else {
            sendMessage(commandSender, message);
        }
    }

    private void displayHelp(ICommandSender commandSender) {
        sendMessage(commandSender, Constants.Color.DARKRED + "--- " + Constants.Color.GOLD + "PLAYER COMMANDS" + Constants.Color.DARKRED + " ---", true);
        for (Subcommand command : Subcommand.values()) {
            if (command == Subcommand.ADMIN || command == Subcommand.HELP) {
                continue;
            }
            sendMessage(commandSender, Constants.Color.WHITE + " " + command.usage + Constants.Color.GOLD + " - " + command.description, true);
        }
        sendMessage(commandSender, Constants.Color.DARKRED + "--- " + Constants.Color.GOLD + "GLOBAL COMMANDS" + Constants.Color.DARKRED + " ---", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca admin <subcommand> " + Constants.Color.GOLD + " - Runs an administrative MCA command.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca help " + Constants.Color.GOLD + " - " + Subcommand.HELP.description, true);
    }

    private void displayAdminHelp(ICommandSender commandSender) {
        sendMessage(commandSender, Constants.Color.DARKRED + "--- " + Constants.Color.GOLD + "OP COMMANDS" + Constants.Color.DARKRED + " ---", true);
        for (AdminSubcommand command : AdminSubcommand.values()) {
            if (command == AdminSubcommand.HELP) {
                continue;
            }
            String warning = command == AdminSubcommand.CLV ? " " + Constants.Color.RED + "(IRREVERSIBLE)" : "";
            sendMessage(commandSender, Constants.Color.WHITE + " " + command.usage + Constants.Color.GOLD + " - " + command.description + warning, true);
        }
        sendMessage(commandSender, Constants.Color.DARKRED + "--- " + Constants.Color.GOLD + "GLOBAL COMMANDS" + Constants.Color.DARKRED + " ---", true);
        sendMessage(commandSender, Constants.Color.WHITE + " " + AdminSubcommand.HELP.usage + Constants.Color.GOLD + " - " + AdminSubcommand.HELP.description, true);
    }
}
