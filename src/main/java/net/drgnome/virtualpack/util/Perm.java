// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.command.CommandSender;
import net.milkbowl.vault.permission.Permission;

import static net.drgnome.virtualpack.util.Global._log;

public class Perm
{
    private static Permission _perm;
    
    public static boolean init()
    {
        RegisteredServiceProvider perm = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        if(perm == null)
        {
            _log.warning(Lang.get("vpack.missperm"));
            return false;
        }
        _perm = (Permission)perm.getProvider();
        return true;
    }
    
    public static boolean has(CommandSender sender, String permission)
    {
        if(sender instanceof Player)
        {
            Player player = (Player)sender;
            return has(player.getWorld().getName(), player.getName(), permission);
        }
        return true;
    }
    
    public static boolean has(String world, String username, String permission)
    {
        try
        {
            if(_perm == null)
            {
                _log.warning("[VirtualPack] Permission instance is null!");
                return false;
            }
            boolean has = _perm.has(world, username, permission);
            if(!has && Config.bool("global-perms"))
            {
                has = _perm.has((String)null, username, permission);
            }
            return has;
        }
        catch(UnsupportedOperationException e) {}
        catch(NullPointerException e) {}
        catch(Throwable t)
        {
            warn();
            t.printStackTrace();
        }
        return false;
    }
    
    public static String[] getGroups(String world, String username)
    {
        try
        {
            if(_perm == null)
            {
                _log.warning("[VirtualPack] Permission instance is null!");
                return new String[0];
            }
            String[] groups = _perm.getPlayerGroups(world, username);
            if(Config.bool("global-perms"))
            {
                groups = Util.merge(groups, _perm.getPlayerGroups((String)null, username));
            }
            return groups;
        }
        catch(UnsupportedOperationException e) {}
        catch(NullPointerException e) {}
        catch(Throwable t)
        {
            warn();
            t.printStackTrace();
        }
        return new String[0];
    }
    
    public static boolean inGroup(String world, String username, String group)
    {
        try
        {
            if(_perm == null)
            {
                _log.warning("[VirtualPack] Permission instance is null!");
                return false;
            }
            boolean in = _perm.playerInGroup(world, username, group);
            if(Config.bool("global-perms") && !in)
            {
                in = _perm.playerInGroup((String)null, username, group);
            }
            return in;
        }
        catch(UnsupportedOperationException e) {}
        catch(NullPointerException e) {}
        catch(Throwable t)
        {
            warn();
            t.printStackTrace();
        }
        return false;
    }
}