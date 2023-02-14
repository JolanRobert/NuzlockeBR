package me.nosta.nuzlockebr.enums;

import org.bukkit.ChatColor;

public enum NZColor {
    None(ChatColor.WHITE),
    Red(ChatColor.RED),
    Blue(ChatColor.BLUE);

    private ChatColor chatColor;

    NZColor(ChatColor chatColor) {
        this.chatColor = chatColor;
    }

    public ChatColor getColor() {return this.chatColor;}
}
