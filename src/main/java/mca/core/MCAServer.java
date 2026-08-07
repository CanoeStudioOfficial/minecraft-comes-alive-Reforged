package mca.core;

import mca.core.minecraft.ItemsMCA;
import mca.core.minecraft.VillageHelper;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.PlayerSaveData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;

public class MCAServer {
    private static MCAServer instance;
    // Maps a player's UUID to a list of UUIDs that have proposed to them with /mca propose
    private static Map<UUID, List<UUID>> proposals;
    // List of UUIDs that initiated procreation mapped to the time the request expires.
    private static Map<UUID, Long> procreateMap;
    private int serverTicks = 0;

    private MCAServer() {
        proposals = new HashMap<>();
        procreateMap = new HashMap<>();
    }

    public static MCAServer get() {
        if (instance == null) {
            instance = new MCAServer();
        }
        return instance;
    }

    public void tick() {
        serverTicks++;

        if (serverTicks >= 100) {
            World overworld = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);
            VillageHelper.tick(overworld);
            serverTicks = 0;
        }

        // Collect all expired procreate requests and remove them.
        List<UUID> removals = new ArrayList<>();
        for (UUID k : procreateMap.keySet()) {
            if (procreateMap.get(k) < System.currentTimeMillis()) {
                removals.add(k);
            }
        }
        for (UUID k : removals) {
            procreateMap.remove(k);
        }
    }

    /**
     * Returns true if receiver has a proposal from sender.
     *
     * @param sender   Command sender
     * @param receiver Player whose name was entered by the sender
     * @return boolean
     */
    private boolean hasProposalFrom(EntityPlayer sender, EntityPlayer receiver) {
        return getProposalsFor(receiver).contains(sender.getUniqueID());
    }

    /**
     * Returns all proposals for the provided player
     *
     * @param player Player whose proposals should be returned.
     * @return List<UUID>
     */
    private List<UUID> getProposalsFor(EntityPlayer player) {
        return proposals.getOrDefault(player.getUniqueID(), new ArrayList<>());
    }

    /**
     * Removes the provided proposer from the target's list of proposals.
     *
     * @param target   Target player who's proposal list will be modified.
     * @param proposer The proposer to the target player.
     */
    private void removeProposalFor(EntityPlayer target, EntityPlayer proposer) {
        List<UUID> list = getProposalsFor(target);
        list.remove(proposer.getUniqueID());
        proposals.put(target.getUniqueID(), list);
    }

    private void removeAllProposalsFor(UUID uuid) {
        proposals.remove(uuid);
        proposals.values().forEach(list -> list.remove(uuid));
    }

    /**
     * Lists all proposals for the given player.
     *
     * @param sender Player whose active proposals will be listed.
     */
    public void listProposals(EntityPlayer sender) {
        List<UUID> proposals = getProposalsFor(sender);

        if (proposals.size() == 0) {
            infoMessage(sender, "You have no active proposals.");
        } else {
            infoMessage(sender, "You have active proposals from: ");
        }

        // Send the name of all online players to the command sender.
        proposals.forEach((uuid -> {
            EntityPlayer player = getOnlinePlayer(uuid);
            if (player != null) {
                infoMessage(sender, "- " + player.getName());
            }
        }));
    }

    /**
     * Sends a proposal from the sender to the receiver.
     *
     * @param sender   The player sending the proposal.
     * @param receiver The player being proposed to.
     */
    public void sendProposal(EntityPlayer sender, EntityPlayer receiver) {
        if (receiver == null) {
            failMessage(sender, "Player not found on the server.");
            return;
        }

        // A proposal is only valid for players who have no active relationship.
        if (PlayerSaveData.get(sender).isMarriedOrEngaged()) {
            failMessage(sender, "You cannot send a proposal since you are already married or engaged.");
            return;
        }

        // Ensure the sender isn't himself.
        if (sender.getUniqueID().equals(receiver.getUniqueID())) {
            failMessage(sender, "You cannot propose to yourself.");
            return;
        }

        if (PlayerSaveData.get(receiver).isMarriedOrEngaged()) {
            failMessage(sender, receiver.getName() + " is already married or engaged.");
            return;
        }

        // Ensure the receiver hasn't already been proposed to by this player.
        if (hasProposalFrom(sender, receiver)) {
            failMessage(sender, "You have already sent a proposal to " + receiver.getName());
        } else {
            // Send the proposal messages.
            successMessage(sender, "Your proposal to " + receiver.getName() + " has been sent!");
            infoMessage(receiver, sender.getName() + " has proposed marriage. To accept, type /mca accept " + sender.getName());

            // Add the proposal to the receiver's proposal list.
            List<UUID> list = getProposalsFor(receiver);
            list.add(sender.getUniqueID());
            proposals.put(receiver.getUniqueID(), list);
        }
    }

    /**
     * Rejects and removes a proposal from the receiver to the sender.
     *
     * @param sender   The person rejecting the proposal.
     * @param receiver The initial proposer.
     */
    public void rejectProposal(EntityPlayer sender, EntityPlayer receiver) {
        // Ensure a proposal existed.
        if (!hasProposalFrom(receiver, sender)) {
            failMessage(sender, receiver.getName() + " hasn't proposed to you.");
        } else {
            // Notify of the proposal failure and remove it.
            successMessage(sender, "Your rejection has been sent.");
            failMessage(receiver, sender.getName() + " rejected your proposal.");
            removeProposalFor(sender, receiver);
        }
    }

    /**
     * Accepts and removes a proposal from the receiver to the sender.
     *
     * @param sender   The person accepting the proposal.
     * @param receiver The initial proposer.
     */
    public void acceptProposal(EntityPlayer sender, EntityPlayer receiver) {
        // Ensure a proposal is active.
        if (!hasProposalFrom(receiver, sender)) {
            failMessage(sender, receiver.getName() + " hasn't proposed to you.");
            return;
        }

        PlayerSaveData senderData = PlayerSaveData.get(sender);
        PlayerSaveData receiverData = PlayerSaveData.get(receiver);

        // Re-check both sides when the proposal is accepted. A proposal can
        // outlive a logout, reset, or another relationship change.
        if (senderData.isMarriedOrEngaged() || receiverData.isMarriedOrEngaged()) {
            failMessage(sender, "This proposal is no longer valid because one of you is already married or engaged.");
            failMessage(receiver, "Your proposal to " + sender.getName() + " is no longer valid.");
            removeProposalFor(sender, receiver);
            return;
        }

        // Notify of acceptance and set both player datas atomically on the
        // server thread before sending the success messages.
        senderData.marry(receiver.getUniqueID(), receiver.getName());
        receiverData.marry(sender.getUniqueID(), sender.getName());
        removeAllProposalsFor(sender.getUniqueID());
        removeAllProposalsFor(receiver.getUniqueID());
        successMessage(receiver, sender.getName() + " has accepted your proposal!");
        successMessage(sender, "You and " + receiver.getName() + " are now married.");
        successMessage(receiver, "You and " + sender.getName() + " are now married.");
    }

    /**
     * Ends the sender's marriage and notifies their spouse if the spouse is online.
     *
     * @param sender The person ending their marriage.
     */
    public void endMarriage(EntityPlayer sender) {
        // Retrieve all data instances and an instance of the ex-spouse if they are present.
        PlayerSaveData senderData = PlayerSaveData.get(sender);

        // Only a completed marriage can be ended by this command.
        if (!senderData.isMarried()) {
            failMessage(sender, "You are not married.");
            return;
        }

        UUID spouseUUID = senderData.getSpouseUUID();

        // Lookup the spouse, if it's a villager, we can't continue
        EntityPlayer onlineSpouse = getOnlinePlayer(spouseUUID);
        Optional<Entity> spouse = sender.world.loadedEntityList.stream()
                .filter(e -> e.getUniqueID().equals(spouseUUID))
                .findFirst();
        if (onlineSpouse != null) {
            spouse = Optional.of(onlineSpouse);
        }
        if (spouse.isPresent() && spouse.get() instanceof EntityVillagerMCA) {
            failMessage(sender, "You cannot use this command when married to a villager.");
            return;
        }

        PlayerSaveData receiverData = onlineSpouse == null
                ? PlayerSaveData.getExisting(sender.world, spouseUUID)
                : PlayerSaveData.get(onlineSpouse);

        // Notify the sender of the success and end both marriages.
        successMessage(sender, "Your marriage to " + senderData.getSpouseName() + " has ended.");
        senderData.endMarriage();
        if (receiverData == null && onlineSpouse != null) {
            receiverData = PlayerSaveData.get(onlineSpouse);
        }
        if (receiverData != null) {
            receiverData.endMarriage();
        }
        removeAllProposalsFor(sender.getUniqueID());
        removeAllProposalsFor(spouseUUID);

        // Notify the ex if they are online.
        if (onlineSpouse != null) {
            failMessage(onlineSpouse, sender.getName() + " has ended their marriage with you.");
        }
    }

    /**
     * Initiates procreation with a married player.
     *
     * @param sender The person requesting procreation.
     */
    public void procreate(EntityPlayer sender) {
        // Ensure the sender is married.
        PlayerSaveData senderData = PlayerSaveData.get(sender);
        if (!senderData.isMarried()) {
            failMessage(sender, "You cannot procreate if you are not married.");
            return;
        }

        // Ensure we don't already have a baby
        if (senderData.isBabyPresent()) {
            failMessage(sender, "You already have a baby.");
            return;
        }

        if (!senderData.mayProcreateAgain(sender.world.getTotalWorldTime())) {
            failMessage(sender, "Maybe later...");
            return;
        }

        // Ensure the spouse is online.
        EntityPlayer spouse = getOnlinePlayer(senderData.getSpouseUUID());
        if (spouse != null) {
            PlayerSaveData spouseData = PlayerSaveData.get(spouse);
            if (!spouseData.isMarriedTo(sender.getUniqueID())) {
                failMessage(sender, "Your marriage data is incomplete. Both players must be married to each other.");
                return;
            }

            if (spouseData.isBabyPresent()) {
                failMessage(sender, "Your spouse already has a baby.");
                return;
            }

            if (!spouseData.mayProcreateAgain(sender.world.getTotalWorldTime())) {
                failMessage(sender, "Maybe later...");
                return;
            }

            // If the spouse is online and has previously sent a procreation request that hasn't expired, we can continue.
            // Otherwise we notify the spouse that they must also enter the command.
            if (!procreateMap.containsKey(spouse.getUniqueID())) {
                procreateMap.put(sender.getUniqueID(), System.currentTimeMillis() + 10000);
                infoMessage(spouse, sender.getName() + " has requested procreation. To accept, type /mca procreate within 10 seconds.");
            } else {
                // On success, add a randomly generated baby to the original requester.
                successMessage(sender, "Procreation successful!");
                successMessage(spouse, "Procreation successful!");
                spouse.addItemStackToInventory(new ItemStack(sender.world.rand.nextBoolean() ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL));

                spouseData.setBabyPresent(true);
                senderData.setBabyPresent(true);
                spouseData.markProcreated(sender.world.getTotalWorldTime());
                senderData.markProcreated(sender.world.getTotalWorldTime());
                procreateMap.remove(sender.getUniqueID());
                procreateMap.remove(spouse.getUniqueID());
            }
        } else {
            failMessage(sender, "Your spouse is not present on the server.");
        }
    }

    private void successMessage(EntityPlayer player, String message) {
        player.sendMessage(new TextComponentString(Constants.Color.GREEN + message));
    }

    private void failMessage(EntityPlayer player, String message) {
        player.sendMessage(new TextComponentString(Constants.Color.RED + message));
    }

    private void infoMessage(EntityPlayer player, String message) {
        player.sendMessage(new TextComponentString(Constants.Color.YELLOW + message));
    }

    private EntityPlayer getOnlinePlayer(UUID uuid) {
        if (uuid == null || Constants.ZERO_UUID.equals(uuid)) {
            return null;
        }

        return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(uuid);
    }
}
