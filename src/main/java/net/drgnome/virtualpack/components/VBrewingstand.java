// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import org.bukkit.craftbukkit.v1_5_R2.inventory.CraftItemStack;
import net.drgnome.virtualpack.util.Config;
import net.minecraft.server.v1_5_R2.ContainerBrewingStand;
import net.minecraft.server.v1_5_R2.EntityHuman;
import net.minecraft.server.v1_5_R2.EntityPlayer;
import net.minecraft.server.v1_5_R2.ItemStack;
import net.minecraft.server.v1_5_R2.TileEntityBrewingStand;

public class VBrewingstand extends ContainerBrewingStand implements VGUI
{
    protected EntityPlayer player;
    protected TileEntityBrewingStand _data;
    
    public VBrewingstand(EntityPlayer player, TileEntityBrewingStand data)
    {
        super(player.inventory, data);
        this.checkReachable = false;
        this._data = data;
        this.player = player;
    }
    
    public final ItemStack clickItem(int slot, int mouse, int shift, EntityHuman human)
    {
        ItemStack item;
        if(allowClick(slot, mouse, shift, human))
        {
            item = super.clickItem(slot, mouse, shift, human);
        }
        else
        {
            item = human.inventory.getCarried();
        }
        update();
        return item;
    }
    
    public boolean allowClick(int slot, int mouse, int shift, EntityHuman human)
    {
        if(shift == 1)
        {
            if(slot >= _data.getSize())
            {
                return isItemAllowed(human, human.inventory.getItem(toInventorySlot(slot - _data.getSize())));
            }
            return true;
        }
        else if((slot >= 0) && (slot < _data.getSize()))
        {
            return isItemAllowed(human, human.inventory.getCarried());
        }
        return true;
    }
    
    protected int toInventorySlot(int slot)
    {
        return (slot >= 27) ? (slot - 27) : (slot + 9);
    }
    
    private boolean isItemAllowed(EntityHuman human, ItemStack item)
    {
        return !Config.isBlacklisted(human.world.getWorld().getName(), human.name, "store", CraftItemStack.asBukkitCopy(item));
    }
    
    protected void update()
    {
        player.updateInventory(player.activeContainer);
    }
}