// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.FieldAccessException;
import net.drgnome.virtualpack.components.MatterInv;
import net.drgnome.virtualpack.util.Config;
import net.drgnome.virtualpack.util.Lang;
import net.drgnome.virtualpack.util.Util;
import static net.drgnome.virtualpack.util.Global._plugin;
import static net.drgnome.virtualpack.util.Global._log;

public class TransmutationListener extends PacketAdapter
{
    public static final String _prefix = Util.parseColors("backdoor");
    
    public static void register()
    {
        HashSet<Integer> set = new HashSet<Integer>();
        try
        {
            set.addAll(Packets.getServerRegistry().getSupported());
            set.addAll(Packets.getClientRegistry().getSupported());
        }
        catch(FieldAccessException e)
        {
            set.addAll(Packets.getServerRegistry().values());
            set.addAll(Packets.getClientRegistry().values());
        }
        ProtocolLibrary.getProtocolManager().addPacketListener(new TransmutationListener(set));
    }
    
    public static void unregister()
    {
        ProtocolLibrary.getProtocolManager().removePacketListeners(_plugin);
    }
    
    public TransmutationListener(Set<Integer> set)
    {
        super(_plugin, ConnectionSide.BOTH, ListenerPriority.HIGHEST, set);
    }
    
    public void onPacketReceiving(PacketEvent event)
    {
        handlePacket(event, false);
    }
    
    public void onPacketSending(PacketEvent event)
    {
        handlePacket(event, true);
    }
    
    private void handlePacket(PacketEvent event, boolean send)
    {
        if((event == null) || event.isCancelled() || (event.getPacket() == null))
        {
            return;
        }
        StructureModifier<ItemStack> mod1 = event.getPacket().getItemModifier();
        if(mod1 != null)
        {
            try
            {
                for(int i = 0; i < mod1.size(); i++)
                {
                    mod1.writeSafely(i, applyChange(mod1.readSafely(i), send));
                }
            }
            catch(Throwable t)
            {
                _log.warning("[VirtualPack] Nothing severe, but can't modify outgoing item stacks. (1)");
                t.printStackTrace();
            }
        }
        StructureModifier<ItemStack[]> mod2 = event.getPacket().getItemArrayModifier();
        if(mod2 != null)
        {
            try
            {
                for(int i = 0; i < mod2.size(); i++)
                {
                    mod2.writeSafely(i, applyChange(mod2.readSafely(i), send));
                }
            }
            catch(Throwable t)
            {
                _log.warning("[VirtualPack] Nothing severe, but can't modify outgoing item stacks. (2)");
                t.printStackTrace();
            }
        }
    }
    
    private ItemStack[] applyChange(ItemStack[] origItems, boolean send)
    {
        if(origItems == null)
        {
            return null;
        }
        ItemStack[] items = new ItemStack[origItems.length];
        for(int i = 0; i < items.length; i++)
        {
            items[i] = applyChange(origItems[i], send);
        }
        return items;
    }
    
    private ItemStack applyChange(ItemStack origItem, boolean send)
    {
        if(origItem == null)
        {
            return null;
        }
        ItemStack item = origItem.clone();
        ItemMeta meta = item.getItemMeta().clone();
        List<String> lore = meta.hasLore() ? meta.getLore() : (new ArrayList<String>());
        if(send)
        {
            if(!meta.hasDisplayName() || !meta.getDisplayName().startsWith(MatterInv._prefix))
            {
                double value = TransmutationHelper.getValue(item);
                if(value > 0)
                {
                    lore.add(_prefix + Lang.get("matter.iteminfo", Util.parseColors(Config.string("transmutation.color.name")), ChatColor.RESET.toString(), Util.parseColors(Config.string("transmutation.color.value")), Util.formatDouble(value)));
                }
            }
        }
        else
        {
            for(int i = 0; i < lore.size(); i++)
            {
                if(lore.get(i).startsWith(_prefix))
                {
                    lore.remove(i);
                    i--;
                }
            }
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}