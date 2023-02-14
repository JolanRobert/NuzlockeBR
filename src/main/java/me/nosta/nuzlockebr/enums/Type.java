package me.nosta.nuzlockebr.enums;

import org.bukkit.ChatColor;

public enum Type {
    None(ChatColor.WHITE,""),
    Acier(ChatColor.GRAY,"[ACI] "),
    Combat(ChatColor.DARK_RED,"[COM] "),
    Dragon(ChatColor.DARK_BLUE,"[DRA] "),
    Eau(ChatColor.BLUE,"[EAU] "),
    Electrique(ChatColor.YELLOW,"[ELE] "),
    Fee(ChatColor.LIGHT_PURPLE,"[FEE] "),
    Feu(ChatColor.RED,"[FEU] "),
    Glace(ChatColor.AQUA,"[GLA] "),
    Insecte(ChatColor.DARK_GREEN,"[INS] "),
    Normal(ChatColor.GOLD,"[NOR] "),
    Plante(ChatColor.GREEN,"[PLA] "),
    Poison(ChatColor.DARK_PURPLE,"[POI] "),
    Psy(ChatColor.LIGHT_PURPLE,"[PSY] "),
    Spectre(ChatColor.DARK_GRAY,"[SPE] "),
    Tenebres(ChatColor.BLACK,"[TEN] "),
    Vol(ChatColor.WHITE,"[VOL] ");

    private ChatColor chatColor;
    private String prefix;

    Type(ChatColor chatColor, String prefix) {
        this.chatColor = chatColor;
        this.prefix = chatColor+prefix;
    }

    public ChatColor getColor() {return this.chatColor;}
    public String getPrefix() {return this.prefix;}
}
