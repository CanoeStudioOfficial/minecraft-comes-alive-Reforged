package mca.command;

import mca.core.Constants;
import mca.core.MCA;
import mca.core.MCAServer;
import mca.entity.data.PlayerSaveData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import java.util.Arrays;

public class CommandMCA extends CommandBase {
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
            if (!MCA.getConfig().allowPlayerMarriage) {
                sendMessage(commandSender, "MCA commands have been disabled by the server administrator.");
                return;
            }

            if (input.length == 0) {
                throw new WrongUsageException("");
            }

            final EntityPlayer player = (EntityPlayer) commandSender;
            String subcommand = input[0].toLowerCase();
            String[] arguments = Arrays.copyOfRange(input, 1, input.length);
            MCA.getLog().info(player.getName() + " entered command " + Arrays.toString(input));

            switch (subcommand) {
                case "help":
                    displayHelp(commandSender);
                    break;
                case "propose":
                    EntityPlayer target = player.world.getPlayerEntityByName(arguments[0]);
                    if (target != null) {
                        MCAServer.get().sendProposal(player, target);
                    } else {
                        player.sendMessage(new TextComponentString("Player not found on the server."));
                    }
                    break;
                case "accept":
                    target = player.world.getPlayerEntityByName(arguments[0]);
                    if (target != null) {
                        MCAServer.get().acceptProposal(player, target);
                    } else {
                        player.sendMessage(new TextComponentString("Player not found on the server."));
                    }
                    break;
                case "proposals":
                    MCAServer.get().listProposals(player);
                    break;
                case "procreate":
                    MCAServer.get().procreate(player);
                    break;
                case "separate":
                    MCAServer.get().endMarriage(player);
                    break;
                case "reject":
                    target = player.world.getPlayerEntityByName(arguments[0]);
                    if (target != null) {
                        MCAServer.get().rejectProposal(player, target);
                    } else {
                        player.sendMessage(new TextComponentString("Player not found on the server."));
                    }
                    break;
                case "mail":
                    handleMailCommand(player);
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
        sendMessage(commandSender, Constants.Color.WHITE + " /mca propose <PlayerName>" + Constants.Color.GOLD + " - Proposes marriage to the given player.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca proposals " + Constants.Color.GOLD + " - Shows all active proposals.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca accept <PlayerName>" + Constants.Color.GOLD + " - Accepts the player's marriage request.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca reject <PlayerName>" + Constants.Color.GOLD + " - Rejects the player's marriage request.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca procreate " + Constants.Color.GOLD + " - Starts procreation.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca separate " + Constants.Color.GOLD + " - Ends your marriage.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca mail " + Constants.Color.GOLD + " - Collects your mail from villagers.", true);
        sendMessage(commandSender, Constants.Color.DARKRED + "--- " + Constants.Color.GOLD + "GLOBAL COMMANDS" + Constants.Color.DARKRED + " ---", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca help " + Constants.Color.GOLD + " - Shows this list of commands.", true);
    }

    private void handleMailCommand(EntityPlayer player) {
        PlayerSaveData data = PlayerSaveData.get(player);
        if (data.hasMail()) {
            int count = 0;
            while (data.hasMail()) {
                ItemStack mail = data.getMail();
                if (mail != null) {
                    if (!player.inventory.addItemStackToInventory(mail)) {
                        // 如果背包满了，掉落物品
                        player.dropItem(mail, false);
                    }
                    count++;
                }
            }
            player.sendMessage(new TextComponentString(Constants.Color.GREEN + "You received " + count + " letter(s) from villagers!"));
        } else {
            player.sendMessage(new TextComponentString(Constants.Color.YELLOW + "You have no mail."));
        }
    }
}
