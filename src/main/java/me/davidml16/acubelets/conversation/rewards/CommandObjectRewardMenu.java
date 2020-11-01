package me.davidml16.acubelets.conversation.rewards;

import me.davidml16.acubelets.Main;
import me.davidml16.acubelets.conversation.CommonPrompts;
import me.davidml16.acubelets.objects.CommandObject;
import me.davidml16.acubelets.objects.CubeletType;
import me.davidml16.acubelets.objects.rewards.CommandReward;
import me.davidml16.acubelets.objects.rewards.Reward;
import me.davidml16.acubelets.utils.Sounds;
import me.davidml16.acubelets.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandObjectRewardMenu implements ConversationAbandonedListener, CommonPrompts {

    private Main main;
    public CommandObjectRewardMenu(Main main) {
        this.main = main;
    }

    public Conversation getConversation(Player paramPlayer, CubeletType cubeletType, Reward reward) {
        Conversation conversation = (new ConversationFactory(main)).withModality(true).withLocalEcho(false).withFirstPrompt(new RewardMenuOptions()).withTimeout(3600).thatExcludesNonPlayersWithMessage("").addConversationAbandonedListener(this).buildConversation(paramPlayer);
        conversation.getContext().setSessionData("player", paramPlayer);
        conversation.getContext().setSessionData("reward", reward);
        conversation.getContext().setSessionData("cubeletType", cubeletType);

        main.getGuiHandler().addConversation(paramPlayer);

        return conversation;
    }

    public Conversation getConversation(Player paramPlayer) { return getConversation(paramPlayer, null, null); }

    public void conversationAbandoned(ConversationAbandonedEvent paramConversationAbandonedEvent) {}

    public class RewardMenuOptions extends FixedSetPrompt {
        RewardMenuOptions() { super("1", "2", "3"); }

        protected Prompt acceptValidatedInput(ConversationContext param1ConversationContext, String param1String) {
            CubeletType cubeletType = (CubeletType) param1ConversationContext.getSessionData("cubeletType");
            switch (param1String) {
                case "1":
                    return new CommonStringPrompt(main,this, true,ChatColor.YELLOW + "  Enter reward command, \"cancel\" to return.\n  Available variables: %player%\n\n ", "rewardCommand");
                case "2":
                    if(param1ConversationContext.getSessionData("rewardCommand") != null) {
                        String rewardCommand = (String) param1ConversationContext.getSessionData("rewardCommand");

                        Reward commandReward = (Reward) param1ConversationContext.getSessionData("reward");
                        ((CommandReward) commandReward).getCommands().add(
                                new CommandObject("command-" + ((CommandReward) commandReward).getCommands().size(), rewardCommand));

                        cubeletType.saveType();

                        param1ConversationContext.getForWhom().sendRawMessage("\n" + Utils.translate(main.getLanguageHandler().getPrefix()
                                + " &aYou added &e" + commandReward.getId() + " &ato commands of cubelet type &e" + cubeletType.getId()));

                        Sounds.playSound((Player) param1ConversationContext.getSessionData("player"),
                                ((Player) param1ConversationContext.getSessionData("player")).getLocation(), Sounds.MySound.ANVIL_USE, 10, 3);

                        main.getEditRewardCommandsGUI().reloadGUI(commandReward);
                        main.getEditRewardCommandsGUI().open((Player) param1ConversationContext.getSessionData("player"), commandReward);
                        main.getGuiHandler().removeConversation((Player) param1ConversationContext.getSessionData("player"));
                        return Prompt.END_OF_CONVERSATION;
                    } else {
                        return new ErrorPrompt(main, this, "\n" + ChatColor.RED + "  You need to setup COMMAND to save command reward!\n  Write anything to continue\n ");
                    }
                case "3":
                    return new ConfirmExitPrompt(main, this);
            }
            return null;
        }


        public String getPromptText(ConversationContext param1ConversationContext) {
            String cadena = "";
            cadena += ChatColor.GOLD + "" + ChatColor.BOLD + "\n  CUBELET COMMAND REWARD CREATION MENU\n";
            cadena += ChatColor.GREEN + " \n";

            if (param1ConversationContext.getSessionData("rewardCommand") == null) {
                cadena += ChatColor.RED + "    1 " + ChatColor.GRAY + "- Set reward command (" + ChatColor.RED + "none" + ChatColor.GRAY + ")\n";
            } else {
                cadena += ChatColor.GREEN + "    1 " + ChatColor.GRAY + "- Set reward command (" + ChatColor.YELLOW + param1ConversationContext.getSessionData("rewardCommand") + ChatColor.GRAY + ")\n";
            }

            cadena += ChatColor.GREEN + "    2 " + ChatColor.GRAY + "- Save\n";
            cadena += ChatColor.GREEN + "    3 " + ChatColor.GRAY + "- Exit and discard\n";
            cadena += ChatColor.GREEN + " \n";
            cadena += ChatColor.GOLD + "" + ChatColor.YELLOW + "  Choose the option: \n";
            cadena += ChatColor.GREEN + " \n";
            return cadena;
        }
    }

}