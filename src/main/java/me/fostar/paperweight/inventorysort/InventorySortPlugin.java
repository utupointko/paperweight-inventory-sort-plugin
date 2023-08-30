package me.fostar.paperweight.inventorysort;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import java.util.Collection;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.EntityArgument.players;

@DefaultQualifier(NonNull.class)
public final class InventorySortPlugin extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);

        this.registerPluginBrigadierCommand(
            "sort",
            literal -> literal.requires(stack -> stack.getBukkitSender().hasPermission("inventorysort"))
                .executes(ctx -> {
                    if (ctx.getSource().getBukkitSender() instanceof Player) {
                        Player player = (Player) ctx.getSource().getBukkitSender();
                        sortInventory(player.getInventory());
                        player.sendMessage("Inventory sorted!");
                    }
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    private void sortInventory(Inventory inventory) {
        // Get the contents of the inventory
        ItemStack[] inv = inventory.getStorageContents();

        // Create a new array to hold the items to be sorted
        ItemStack[] newInv = new ItemStack[inv.length - 9];

        // Copy the items to be sorted to the new array
        for (int i = 0; i < newInv.length; i++) {
            newInv[i] = inv[i + 9];
        }

        // Sort the new array using bubble sort
        bubbleSort(newInv);

        // Copy the sorted items back to the original inventory
        for (int i = 0; i < newInv.length; i++) {
            inv[i + 9] = newInv[i];
        }

        // Update the inventory with the sorted contents
        inventory.setStorageContents(inv);
    }

    private void bubbleSort(ItemStack[] arr) {
        int n = arr.length;

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] == null || (arr[j + 1] != null && arr[j].getType().compareTo(arr[j + 1].getType()) < 0)) {
                    // Swap arr[j+1] and arr[j]
                    ItemStack temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                } else if (arr[j] != null && arr[j + 1] != null && arr[j].getType().compareTo(arr[j + 1].getType()) == 0) {
                    // Handle stacking logic for items with the same type
                    int maxStackSize = arr[j].getMaxStackSize();
                    int combinedAmount = arr[j].getAmount() + arr[j + 1].getAmount();

                    if (combinedAmount <= maxStackSize) {
                        // Combine items into one stack
                        arr[j].setAmount(combinedAmount);
                        arr[j + 1] = null; // Set the second stack to null
                    } else {
                        // Split stacks and adjust amounts
                        arr[j].setAmount(maxStackSize);
                        arr[j + 1].setAmount(combinedAmount - maxStackSize);
                    }
                }
            }
        }
    }


    private PluginBrigadierCommand registerPluginBrigadierCommand(final String label, final Consumer<LiteralArgumentBuilder<CommandSourceStack>> command) {
        final PluginBrigadierCommand pluginBrigadierCommand = new PluginBrigadierCommand(this, label, command);
        this.getServer().getCommandMap().register(this.getName(), pluginBrigadierCommand);
        ((CraftServer) this.getServer()).syncCommands();
        return pluginBrigadierCommand;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @EventHandler
    public void onCommandRegistered(final CommandRegisteredEvent<BukkitBrigadierCommandSource> event) {
        if (!(event.getCommand() instanceof PluginBrigadierCommand pluginBrigadierCommand)) {
            return;
        }
        final LiteralArgumentBuilder<CommandSourceStack> node = literal(event.getCommandLabel());
        pluginBrigadierCommand.command().accept(node);
        event.setLiteral((LiteralCommandNode) node.build());
    }
}
