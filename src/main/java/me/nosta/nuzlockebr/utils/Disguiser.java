package me.nosta.nuzlockebr.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.enums.Skin;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.managers.PlayerManager;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.network.protocol.game.PacketPlayOutRespawn;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.level.EnumGamemode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class Disguiser {

    private static Map<NZPlayer,Property> disguisement = new HashMap<>();

    public static void setSkin(NZPlayer nzPlayer, Skin skin) {

        EntityPlayer playerNMS = ((CraftPlayer) nzPlayer.getPlayer()).getHandle();
        GameProfile profile = playerNMS.getBukkitEntity().getProfile();

        Property newSkin;

        if (skin == Skin.Reset) {
            if (!disguisement.containsKey(nzPlayer)) return;
            newSkin = disguisement.get(nzPlayer);
            disguisement.remove(nzPlayer);
        }

        else {
            newSkin = getSkinTextureValue(skin);

            //Save base skin
            if (!disguisement.containsKey(nzPlayer)) {
                Property property = profile.getProperties().get("textures").iterator().next();
                disguisement.put(nzPlayer, property);
            }
        }

        if (newSkin == null) return;

        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerConnection connection = ((CraftPlayer) p).getHandle().b;
            connection.a(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e, playerNMS));
            profile.getProperties().removeAll("textures");
            profile.getProperties().put("textures",newSkin);
            connection.a(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, playerNMS));
        }

        refreshPlayer(nzPlayer);
    }

    private static void refreshPlayer(NZPlayer nzPlayer) {
        Player player = nzPlayer.getPlayer();

        Location loc = player.getLocation();
        loc.setYaw(player.getLocation().getYaw());
        loc.setPitch(player.getLocation().getPitch());
        long seed = player.getWorld().getSeed();

        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        WorldServer world = ep.x();
        new BukkitRunnable() {
            @Override
            public void run() {
                ep.b.a(new PacketPlayOutRespawn(world.Z(),world.ab(),seed,EnumGamemode.a,EnumGamemode.a,false,false,true,ep.ga()));
                player.teleport(loc);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.hidePlayer(Main.getInstance(),player);
                    p.showPlayer(Main.getInstance(),player);
                }

                player.updateInventory();
            }
        }.runTaskLater(Main.getInstance(), 1);
    }

    private static Property getSkinTextureValue(Skin skin) {
        System.out.println(skin);
        if (skin == Skin.Asuka)
            return new Property("textures",
                    "ewogICJ0aW1lc3RhbXAiIDogMTYxNjM2Mzk2MDgyNywKICAicHJvZmlsZUlkIiA6ICI5MzZmMTA3MTEzOGM0YjMyYTg0OGY2NmE5Nzc2NDJhMiIsCiAgInByb2ZpbGVOYW1lIiA6ICIwMDAwMDAwMDAwMDAwMDB4IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzlmZDAzZDZiYjY3ZmQzZTVlZDA4NGQ3OTFjYjdlZjk5NjJmZTRhMTE5NjEzZmU5OWExZTBjMWQwZTMyMDEwNGIiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                    "DqgTY3BX4nFFyc97ds8LupYSHP//YBT7ll3LgAHCZR0sNUB0hmrC3DXLXyzg1MtIXgIVa8dbVi2ead2XEV3wdYRzgZ87W/9NdQT353Z6skDkgnlXlE0lFTe9L4xsJzVNqDmPPSCpy5wP7YkljxS81aDsrksOTRqGU1gZNM/KcXta56Ja3L1fIDTr3Cm09b9YLcHGbqGYMiW4TGCJ2ixYTN2c4gw+Kx1RNEuPmWO1AeveeEz3OReRCiQa6Ckmdbgr2XmecKLerOZhxT68IlIOpzQmz/JWA1cKKbVmQkoMJJcNjdTL58rr97T2JE+NuBnxnQ/2JA8ZH20o3pOwZR+hfKVFPIXpS68PYyal0bRw40+Q6wLHtnup/n9ptXgIymUpVeqy876IfT5+5ulnGXe6z8DCl9svZ7cdqs/3/0W0L3nDwHA1kNwWPX3N4Mc2fIZctr6TcKVoMZM6XGrf4ndfz/o8r0x31vvuaaLAo3eXwj8zAtaGOW2xFk4jrTS4DBxGLx9Z2J2c9MaPXmOPi/pToJaCuCrz6/lqWBWHMF/kx8TiVd5dKNRqv7QqunEvIgIRhpkPqpBV19OTT8GbVAKELFDlOHtR55+DDzjY/e8O6FnmKlQDwDZBzVFVCMXUroPTCF6b74keadLrmRe4VtH0Bi64FCneajdKaDs3VbGgfMg=");
        else if (skin == Skin.Spy)
            return new Property("textures",
                    "ewogICJ0aW1lc3RhbXAiIDogMTYxNjk1NDAxNzU1NSwKICAicHJvZmlsZUlkIiA6ICI0NTRiYjJjYTA0NzU0ZTIyYTc2MGZlMWQ2ZjU0ZTVhMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJvbGxpZTUxNCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85NGIyMjc2MjViMTk4YzIyMDlhM2IwYzViZGJjZGFkNGQzOWUxNmMxNjFhZTM3YTc0MzQ0NTcyNWIwZTE3Nzk0IgogICAgfQogIH0KfQ==",
                    "h0yXGBhL3hcyo+8a6k0XD5vsL2rPnlwgIs0+YmXKaWhYNA9R/+sN5+MkYHN0t8CeSgkCmLsi7JbrhSGGBJ/t+xz7+pW9V639B6C1tIEHsKkBEhbtIgFd1Wzxw+vXZa2wV7+KAnK3BmmgwPTqqcbZk/YMZauiRh4AZkxyok2gwSijMCYJkZdwTqjpMJoZfT6Ezp6RMURjRWF1QcqjUeF2tNLvJ7hbh9ZMhIcdzPTIVp0EPYcopLIL8sSvCGfkL+cCEWeQuoMwzlGBKEuJ8ePTWpxmBPneXMWN9Y80Rz8fwIiMOtJoipUeF6PVR4ioiugpwZ9DmXIM0n9wZC5DuTcCYr9FOoS7rgHzxPcNHimMErCtRup356h+FE8KXYssusWf+8LWNhz/Epinh6DBtbUtd6Jojl7t25nZVxOF0Toe9Qu/9CAPW99rtrKss8qSuFZQbYXQpysyAoqcdTluH6Qtl4qrummW9kTTSPB0N3f6OPt8D3j/xFcVjujZIfxUlOdObT2v8v/vl63Zf2EhciyDZ4O7u2OcGfOv8eGgMeYlV6MI0nbEB7IsAo0s3gYC/BnhEs7BpnZFHjTJL232+fozZEd9gFfogTHOT4L+fpwQzFtNsfl1T5/dJZTSsGEN7qomK44IMZJ5CARqr1zNf6i1IfYB7Del4LzPoZpra1JACFg=");
        else if (skin == Skin.Flyde)
            return new Property("textures",
                    "ewogICJ0aW1lc3RhbXAiIDogMTY1OTI2NjEzMjA1NywKICAicHJvZmlsZUlkIiA6ICIxNDU1MDNhNDRjZmI0NzcwYmM3NWNjMTRjYjUwMDE4NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJMaWtlbHlFcmljIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzRiOTZmZTJmNzNjMWU0YWMxYmNhZjM5OTgwOTUyN2E1YjNmYjZiNDMwZWEzODJjMmM4ZDljM2JmMTc0OWEwMWYiCiAgICB9CiAgfQp9",
                    "wXRcEf/QIc3r6S0sfnhOdyB9XeV4u1rNFtgxMQv5rBSM0VpWOdrI8JQ7TJn/AZl519wFgkOubmJdEWCzMYnq7nfun5wyDu2erlEJ7fM+S4qNtgLd4FXNRlk0q1R0BP3+RMYooKgoBmYgOfxa0OGMGE7gzFlAOiu0fg2k9E6ORIXE+/jTIkb8Rn8NewHYL4zuYYQ41EzLiMs7tcFIpJgQ9vTu9jiT8/t0D1sbV0KEMj/WDoTgLl6cMIMBHVWO4BneCT/dvtSJTTaCZIqiCHJUII8DpKQv2ZM/QFTW5LA5lBIbyYnJjdZD3UxxnU09Kb2lNXPxdA9wbt7NVBOp7AtEwbABC7HhyIyTRPs+PhrUYTUk7xj66V7N+bpfoFIlDfM6uTcfdbXRRElv6IDb9gm/kzZSMwG1UJBPyeKFmF94BBEdSV165VxjhmCtLhCTuWH/41ksnAzT+M02f9iLmfTaS+r5Dgo+SOWNTKYxX7+8mDmVEDblzZgezhCW+Kjc236rheiQ+vI9AJCiq6Jyh3D2e/HJZosbyr0INprOKGoXjx3URIdFJqNv5CwdQwkz/8Wub9W22TxjU9kmyU/6JGrns/2lrMwkLpxHn8sO0hI8LFP03g7JgGWZZN08Jfl6aBFKDHFJyw7JDKv63RWj4E1pKNdB3+TXzL/8XDZKi9RalFk=");
        else if (skin == Skin.Waluigi)
            return new Property("textures",
                    "ewogICJ0aW1lc3RhbXAiIDogMTU5NTg1NzEzOTY3NywKICAicHJvZmlsZUlkIiA6ICI4ZmMzMzU5MTljMzk0ODFhYTRiNGM1NzQ1ZDkzNTdkNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJXYWx1aWdpIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzU3Y2QzZDFlZTAzYjJkZGQyNTA5OGZmYTVmNGFiMTA1N2JjNDNlNGVjZDE0N2VjNDFhYThkZjMwMmUzZWIxMDkiCiAgICB9CiAgfQp9",
                    "ZwXi8ZeM40tMI8PUcJUzyZoc/pKx46ATxJISu7bFKIpGbPaGPfDw9atjuzEhW2bHY7ww+XyOaNOlT09ZUVntuPDBx95JtxcjcoWiNfyxIUWf7dcPslk73ivzDm+w94vEqA+LJS+U5OzM41ipJPWZn3cy9QUIjm6oMaHMY0Oi/6tbVObrSXYRyX5pILyrpnnCpLSNhDXr+dYgvQegpdSAs7Ldzc7dKrqdH8MTTsAVf5L7gJFQfb19znBjLpdyvHlVpNvQzS2XU/OBnu3fe7zz/vvbmWI3WTJfhLcNSoauCfDtUFXBPoghzftGG75V8kKfzu3/o7CrEwCm2BJe0cCCz+GZcHM+FBit792mctPX+yVptBn1Qk4RaZvjd0zmgE50Bt4BPu5FRuY0SiwwrBWoxkTpSAklnjht1LeoZZvuUsSOPC7IyUMs5R+MUhvfZy4bd89RARUPlxnwUSm6C0ZRwLmxQ/AVF4S+37nbADMbQkRgY+sxhjvgjDpxPiJGbOh4Swee1DC+EzR+tJVErJNn6mTFwrqP282mjxU+TDg1EDtcUKMo4jL0ghlkyPIbYP0g+B2yTDm780jKOgr0MpgOgnAKEcTT1xHyQjj+OVjhiDlce7wQ31pCpOpfU2CwTQKxf+RABog/H4BnJw+N8Xo5cRKv+6YXxQNdJfbF01ngT0I=");


        return null;
    }

    public static void clearAllDisguise() {
        for (NZPlayer nzPlayer : PlayerManager.getInstance().playerList) {
            if (!disguisement.containsKey(nzPlayer)) continue;
            setSkin(nzPlayer,Skin.Reset);
        }
    }

    public static String anonymizeMessage(String message) {
        for (NZPlayer nzPlayer : PlayerManager.getInstance().playerList) {
            if (message.contains(nzPlayer.getColoredName())) {
                message = message.replace(nzPlayer.getColoredName(), ""+ChatColor.WHITE+ChatColor.MAGIC+"aaaaa");
            }
        }

        return message;
    }
}
