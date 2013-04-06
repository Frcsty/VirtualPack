// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.tmp;

import java.util.ArrayList;

import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.craftbukkit.v1_5_R2.inventory.CraftItemStack;

import net.drgnome.virtualpack.components.MatterInv;
import net.drgnome.virtualpack.components.VInv;
import net.drgnome.virtualpack.data.TransmutationHelper;
import net.drgnome.virtualpack.item.ComparativeItemStack;
import net.drgnome.virtualpack.item.ValuedItemStack;
import net.drgnome.virtualpack.util.Config;
import net.drgnome.virtualpack.util.Lang;
import net.drgnome.virtualpack.util.Util;
import net.minecraft.server.v1_5_R2.ItemStack;

public class TmpMatterInv extends VInv
{
    protected ArrayList<ComparativeItemStack> _unlocked = new ArrayList<ComparativeItemStack>();
    protected boolean _allUnlocked = false;
    public double _value = 0D;
    protected String _worldname;
    protected String _playername;
    
    public TmpMatterInv(String worldname, String playername)
    {
        super(54);
        _worldname = worldname;
        _playername = playername;
        if(Config.bool("transmutation.enabled"))
        {
            updateInfo();
            updateInv();
        }
    }
    
    public TmpMatterInv(String worldname, String playername, String[] data)
    {
        super(54);
        _worldname = worldname;
        _playername = playername;
        if(data.length > 0)
        {
            _value = Util.parseBigDouble(data[0]);
        }
        for(int i = 1; i < data.length; i++)
        {
            if(data[i].equals("[all]"))
            {
                _allUnlocked = true;
                break;
            }
            _unlocked.add(new ComparativeItemStack(Util.base64de(data[i])));
        }
        if(Config.bool("transmutation.enabled"))
        {
            updateInfo();
            updateInv();
        }
    }
    
    public String[] serialize()
    {
        ArrayList<String> list = new ArrayList<String>();
        list.add(Util.printDoublePlain(_value));
        if(_allUnlocked)
        {
            list.add("[all]");
        }
        else
        {
            for(ComparativeItemStack stack : _unlocked)
            {
                list.add(Util.base64en(stack.serialize()));
            }
        }
        return list.toArray(new String[0]);
    }
    
    public void setItem(int index, ItemStack item)
    {
        if((index > 0) && (index < 9))
        {
            if(index == 8)
            {
                super.setItem(index, item);
                updateInv();
                return;
            }
            org.bukkit.inventory.ItemStack mirror = CraftItemStack.asBukkitCopy(item);
            double value = TransmutationHelper.getValue(mirror);
            if((value > 0) && ((mirror.getType().getMaxDurability() <= 0) || (mirror.getDurability() == 0)))
            {
                if(unlock(item))
                {
                    _value += value * (double)item.count;
                }
                updateInfo();
            }
            else
            {
                super.setItem(index, item);
            }
        }
    }
    
    public void updateInfo()
    {
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(399, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MatterInv._prefix + Util.parseColors(Config.string("transmutation.color.stored-name")) + Lang.get("matter.stored"));
        meta.setLore(Util.createList(Util.parseColors(Config.string("transmutation.color.stored-value")) + Util.formatDouble(_value)));
        item.setItemMeta(meta);
        super.setItem(0, CraftItemStack.asNMSCopy(item));
    }
    
    public void updateInv()
    {
        ValuedItemStack[] items = _allUnlocked ? TransmutationHelper.getAllFiltered(_worldname, _playername) : TransmutationHelper.getAllFiltered(_worldname, _playername, _unlocked.toArray(new ComparativeItemStack[0]));
        int off = 0;
        if(super.getItem(8) != null)
        {
            ComparativeItemStack lock = new ComparativeItemStack(CraftItemStack.asBukkitCopy(super.getItem(8)));
            for(; off < items.length; off++)
            {
                if(lock.matches(items[off]))
                {
                    break;
                }
            }
        }
        for(int i = 9; i < getSize(); i++)
        {
            if((i + off - 9) >= items.length)
            {
                super.setItem(i, null);
                continue;
            }
            double value = TransmutationHelper.getValue(items[i + off - 9]);
            if(value <= 0)
            {
                super.setItem(i, null);
                continue;
            }
            super.setItem(i, CraftItemStack.asNMSCopy(items[i + off - 9].createStack(1)));
        }
    }
    
    private boolean unlock(ItemStack mcitem)
    {
        org.bukkit.inventory.ItemStack item = CraftItemStack.asBukkitCopy(mcitem);
        if(_allUnlocked)
        {
            return true;
        }
        if(Config.isGodItem(item))
        {
            _allUnlocked = true;
            updateInv();
            return false;
        }
        for(ComparativeItemStack stack : _unlocked)
        {
            if(stack.matches(item))
            {
                return true;
            }
        }
        _unlocked.add(new ComparativeItemStack(item));
        updateInv();
        return true;
    }
    
    public void reset()
    {
        _allUnlocked = false;
        _unlocked = new ArrayList<ComparativeItemStack>();
        _value = 0D;
    }
}