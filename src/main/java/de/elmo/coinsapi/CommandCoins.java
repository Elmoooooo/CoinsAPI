package de.elmo.coinsapi;


import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class CommandCoins
        implements CommandExecutor {
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if (player.isOp()) {
                for (int i = 1; i < args.length; i++) ;

                if (args.length == 0) {
                    player.sendMessage("§bCoins §8- §7Du hast §e" + CoinsAPI.getInstance().getCoins(player) + " §7Coins!");
                    return true;
                }
                switch (args[0]) {
                    case "add":
                        if (args.length == 2) {
                            int coins = 0;

                            try {
                                coins = Integer.valueOf(args[1]);
                            } catch (NumberFormatException ex) {
                                player.sendMessage("§cBitte gebe eine gültige Zahl an!");
                                return true;
                            }

                            CoinsAPI.getInstance().addCoins(player, coins);
                            player.sendMessage("§7Coins hinzugefügt!");
                        }
                        break;
                    case "set":
                        if (args.length == 2) {
                            int coins = 0;

                            try {
                                coins = Integer.valueOf(args[1]);
                            } catch (NumberFormatException ex) {
                                player.sendMessage("§cBitte gebe eine gültige Zahl an!");
                                return true;
                            }

                            CoinsAPI.getInstance().setCoins(player, coins);
                            player.sendMessage("§7Coins gesetzt!");
                        }
                        break;
                    case "remove":
                        if (args.length == 2) {
                            int coins = 0;

                            try {
                                coins = Integer.valueOf(args[1]);
                            } catch (NumberFormatException ex) {
                                player.sendMessage("§cBitte gebe eine gültige Zahl an!");
                                return true;
                            }

                            CoinsAPI.getInstance().removeCoins(player, coins);
                            player.sendMessage("§7Coins entfernt!");
                        }
                        break;
                }
            } else {
                player.sendMessage("§bCoins §8- §7Du hast §e" + CoinsAPI.getInstance().getCoins(player) + " §7Coins!");
            }
        } else {
            System.out.println("Was fällt dir ein die Console zu nutzen!");
        }
        return true;
    }
}