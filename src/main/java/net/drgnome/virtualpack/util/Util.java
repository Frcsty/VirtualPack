// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.util;

import java.io.*;
import java.nio.charset.Charset;
import java.net.*;
import java.util.*;
import java.math.BigDecimal;
import java.lang.reflect.*;
import javax.xml.bind.DatatypeConverter;
import net.minecraft.server.v1_5_R2.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import net.drgnome.virtualpack.components.BaseInv;
import net.drgnome.virtualpack.components.BaseView;
import static net.drgnome.virtualpack.util.Global.*;

public class Util
{
    private static boolean _lastStack;
    private static String[] _lastStackIds;
    
    // Math.round? Too damn slow!
    public static int round(double d)
    {
        int i = (int)d;
        d -= i;
        return i + (d >= 0.5 ? 1 : (d <= -0.5 ? -1 : 0));
    }
    
    public static double roundBig(double d)
    {
        long i = (long)d;
        d -= i;
        return i + (d >= 0.5 ? 1 : (d <= -0.5 ? -1 : 0));
    }
    
    // Same for Math.floor
    public static int floor(double d)
    {
        return d < 0 ? (int)d - 1 : (int)d;
    }
    
    public static boolean hasUpdate(String name, String version)
    {
        try
        {
            HttpURLConnection con = (HttpURLConnection)(new URL("http://dev.drgnome.net/version.php?t=" + name)).openConnection();            
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; JVM)");                        
            con.setRequestProperty("Pragma", "no-cache");
            con.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line = null;
            StringBuilder stringb = new StringBuilder();
            if((line = reader.readLine()) != null)
            {
                stringb.append(line);
            }
            String vdigits[] = version.toLowerCase().split("\\.");
            String cdigits[] = stringb.toString().toLowerCase().split("\\.");
            int max = vdigits.length > cdigits.length ? cdigits.length : vdigits.length;
            int a = 0;
            int b = 0;
            for(int i = 0; i < max; i++)
            {
                try
                {
                    a = Integer.parseInt(cdigits[i]);
                }
                catch(Throwable t1)
                {
                    char c[] = cdigits[i].toCharArray();
                    for(int j = 0; j < c.length; j++)
                    {
                        a += (c[j] << ((c.length - (j + 1)) * 8));
                    }
                }
                try
                {
                    b = Integer.parseInt(vdigits[i]);
                }
                catch(Throwable t1)
                {
                    char c[] = vdigits[i].toCharArray();
                    for(int j = 0; j < c.length; j++)
                    {
                        b += (c[j] << ((c.length - (j + 1)) * 8));
                    }
                }
                if(a > b)
                {
                    return true;
                }
                else if(a < b)
                {
                    return false;
                }
                else if((i == max - 1) && (cdigits.length > vdigits.length))
                {
                    return true;
                }
            }
        }
        catch(Throwable t)
        {
        }
        return false;
    }
    
    public static <T> T[] createGenericArray(Class<T> clazz)
    {
        return createGenericArray(clazz, 0);
    }
    
    public static <T> T[] createGenericArray(Class<T> clazz, int... size)
    {
        for(int i = 0; i < size.length; i++)
        {
            if(size[i] < 0)
            {
                size[i] = 0;
            }
        }
        try
        {
            return (T[])(Array.newInstance(clazz, size));
        }
        catch(Throwable t)
        {
            t.printStackTrace();
            return (T[])null;
        }
    }
    
    public static <T extends Cloneable> T copy(T object)
    {
        if(object != null)
        {
            try
            {
                Method m = object.getClass().getMethod("clone");
                m.setAccessible(true);
                return (T)m.invoke(object);
            }
            catch(Throwable t)
            {
                t.printStackTrace();
            }
        }
        return null;
    }
    
    public static <T extends Cloneable> T[] copy(T... objects)
    {
        ArrayList<T> list = new ArrayList<T>();
        for(T obj : objects)
        {
            list.add(copy(obj));
        }
        return list.toArray(createGenericArray((Class<T>)objects.getClass().getComponentType()));
    }
    
    public static net.minecraft.server.v1_5_R2.ItemStack copy_old(net.minecraft.server.v1_5_R2.ItemStack item)
    {
        return item == null ? null : item.cloneItemStack();
    }
    
    public static net.minecraft.server.v1_5_R2.ItemStack[] copy_old(net.minecraft.server.v1_5_R2.ItemStack item[])
    {
        net.minecraft.server.v1_5_R2.ItemStack it[] = new net.minecraft.server.v1_5_R2.ItemStack[item.length];
        for(int i = 0; i < it.length; i++)
        {
            it[i] = copy_old(item[i]);
        }
        return it;
    }
    
    public static <T> T[] cut(T[] objects, int start)
    {
        T[] array = createGenericArray((Class<T>)objects.getClass().getComponentType(), objects.length - start);
        for(int i = start; i < objects.length; i++)
        {
            array[i - start] = objects[i];
        }
        return array;
    }
    
    public static <T> T[] merge(T[]... objects)
    {
        ArrayList<T> list = new ArrayList<T>();
        for(T[] array : objects)
        {
            if(array == null)
            {
                continue;
            }
            for(T obj : array)
            {
                if(obj == null)
                {
                    continue;
                }
                if(!list.contains(obj))
                {
                    list.add(obj);
                }
            }
        }
        return list.toArray((T[])Array.newInstance(objects[0].getClass().getComponentType(), list.size()));
    }
    
    public static <T> ArrayList<T> createList(T... array)
    {
        ArrayList<T> list = new ArrayList<T>();
        for(T t : array)
        {
            list.add(t);
        }
        return list;
    }
    
    public static boolean areEqual(net.minecraft.server.v1_5_R2.ItemStack item1, net.minecraft.server.v1_5_R2.ItemStack item2)
    {
        return (item1.id == item2.id) && (item1.count == item2.count) && (item1.getData() == item2.getData());
    }
    
    public static int tryParse(String s, int i)
    {
        try
        {
            return Integer.parseInt(s);
        }
        catch(Throwable t)
        {
            return i;
        }
    }
    
    public static double tryParse(String s, double d)
    {
        try
        {
            return Double.parseDouble(s);
        }
        catch(Throwable t)
        {
            return d;
        }
    }
    
    /*public static double parseBigDouble(String s)
    {
        System.out.println(s);
        System.out.println((new BigDecimal(s)).toPlainString());
        double d = 0D;
        String[] parts = s.split("\\.");
        for(int i = 0; i < parts[0].length(); i++)
        {
            try
            {
                d += Double.parseDouble(parts[0].substring(parts[0].length() - (i + 1), parts[0].length() - i)) * (double)Math.pow(10, i);
            }
            catch(NumberFormatException e)
            {
                e.printStackTrace();
                return 0D;
            }
        }
        if(parts.length > 1)
        {
            for(int i = 0; i < parts[1].length(); i++)
            {
                try
                {
                    d += Double.parseDouble(parts[1].substring(i, i + 1)) * (double)Math.pow(10, -(i + 1));
                }
                catch(NumberFormatException e)
                {
                    e.printStackTrace();
                    return 0D;
                }
            }
        }
        return d;
    }*/
    
    public static String implode(String glue, String... parts)
    {
        if((glue == null) || (parts.length <= 0))
        {
            return "";
        }
        String string = parts[0];
        for(int i = 1; i < parts.length; i++)
        {
            string += glue + parts[i];
        }
        return string;
    }
    
    public static double smooth(double d, int digits)
    {
        double factor = Math.pow(10, digits);
        return round(d * factor) / factor;
    }
    
    public static double smoothBig(double d, int digits)
    {
        double factor = Math.pow(10, digits);
        return roundBig(d * factor) / factor;
    }
    
    public static String printDoublePlain(double d)
    {
        return BigDecimal.valueOf(d).toPlainString();
    }
    
    public static String printDouble(double d)
    {
        return BigDecimal.valueOf(smoothBig(d, 3)).toPlainString();
    }
    
    public static String formatDouble(double d)
    {
        String[] plain = printDouble(d).split("\\.");
        String formatted = "";
        while(plain[0].length() >= 4)
        {
            formatted = "'" + plain[0].substring(plain[0].length() - 3) + formatted;
            plain[0] = plain[0].substring(0, plain[0].length() - 3);
        }
        formatted = plain[0] + formatted;
        if((plain.length > 1) && (tryParse(plain[1], 0) != 0))
        {
            formatted += "." + plain[1];
        }
        return formatted;
    }
    
    public static double parseBigDouble(String s)
    {
        try
        {
            return (new BigDecimal(s)).doubleValue();
        }
        catch(Throwable t)
        {
            return 0D;
        }
        // return (new BigDecimal(s)).setScale(2).doubleValue();
    }
    
    public static int max(int... values)
    {
        int tmp = values[0];
        for(int i = 1; i < values.length; i++)
        {
            if(values[i] > tmp)
            {
                tmp = values[i];
            }
        }
        return tmp;
    }
    
    public static int min(int... values)
    {
        int tmp = values[0];
        for(int i = 1; i < values.length; i++)
        {
            if(values[i] < tmp)
            {
                tmp = values[i];
            }
        }
        return tmp;
    }
    
    public static net.minecraft.server.v1_5_R2.ItemStack stringToItemStack(String string)
    {
        if((string == null) || (string.length() == 0))
        {
            return null;
        }
        return net.minecraft.server.v1_5_R2.ItemStack.createStack(NBTCompressedStreamTools.a(DatatypeConverter.parseBase64Binary(string)));
    }
    
    public static String itemStackToString(net.minecraft.server.v1_5_R2.ItemStack item)
    {
        if(item == null)
        {
            return "";
        }
        return DatatypeConverter.printBase64Binary(NBTCompressedStreamTools.a(item.save(new NBTTagCompound())));
    }
    
    public static net.minecraft.server.v1_5_R2.ItemStack[] stack(net.minecraft.server.v1_5_R2.ItemStack item1, net.minecraft.server.v1_5_R2.ItemStack item2)
    {
        _lastStack = false;
        if(item2 == null)
        {
            return new net.minecraft.server.v1_5_R2.ItemStack[]{item1, null};
        }
        if(item1 == null)
        {
            _lastStack = true;
            return new net.minecraft.server.v1_5_R2.ItemStack[]{item2, null};
        }
        if(!areEqual(item1, item2))
        {
            return new net.minecraft.server.v1_5_R2.ItemStack[]{item1, item2};
        }
        int max = (item2.count > (item1.getMaxStackSize() - item1.count)) ? (item1.getMaxStackSize() - item1.count) : item2.count;
        if(max <= 0)
        {
            return new net.minecraft.server.v1_5_R2.ItemStack[]{item1, item2};
        }
        _lastStack = true;
        item1.count += max;
        item2.count -= max;
        return new net.minecraft.server.v1_5_R2.ItemStack[]{item1, (item2.count <= 0) ? null : item2};
    }
    
    public static net.minecraft.server.v1_5_R2.ItemStack[] stack(IInventory[] invs, net.minecraft.server.v1_5_R2.ItemStack... items)
    {
        boolean[] stacked = new boolean[invs.length];
        ArrayList<net.minecraft.server.v1_5_R2.ItemStack> left = new ArrayList<net.minecraft.server.v1_5_R2.ItemStack>();
        for(net.minecraft.server.v1_5_R2.ItemStack item : items)
        {
            for(int j = 0; j < invs.length; j++)
            {
                IInventory inv = invs[j];
                net.minecraft.server.v1_5_R2.ItemStack[] contents = inv.getContents();
                stacked[j] = false;
                for(int i = 0; i < contents.length; i++)
                {
                    net.minecraft.server.v1_5_R2.ItemStack[] tmp = stack(contents[i], item);
                    inv.setItem(i, tmp[0]);
                    item = tmp[1];
                    stacked[j] = stacked[j] || _lastStack;
                    if(item == null)
                    {
                        break;
                    }
                }
                if(item == null)
                {
                    break;
                }
            }
            if(item != null)
            {
                left.add(item);
            }
        }
        ArrayList<String> touched = new ArrayList<String>();
        for(int i = 0; i < stacked.length; i++)
        {
            if(stacked[i])
            {
                touched.add("" + (i + 1));
            }
        }
        _lastStackIds = touched.toArray(new String[0]);
        return left.toArray(new net.minecraft.server.v1_5_R2.ItemStack[0]);
    }
    
    public static String[] getLastStackingIds()
    {
        return _lastStackIds;
    }
    
    public static void openWindow(EntityPlayer player, Container container, String name, int id, int size)
    {
        if(name.length() > 32)
        {
            name = name.substring(0, 32);
        }
        player.playerConnection.sendPacket(new Packet100OpenWindow(1, id, name, size, true));
        player.activeContainer = container;
        container.windowId = 1;
        container.addSlotListener((ICrafting)player);
    }
    
    public static void openInv(Player player, BaseInv inv)
    {
        player.openInventory(new BaseView(player, inv));
    }
    
    public static boolean loadJar(File file)
    {
        ClassLoader loader = _plugin.getClass().getClassLoader();
        if(loader instanceof URLClassLoader)
        {
            try
            {
                URLClassLoader cl = (URLClassLoader)loader;
                Method m = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                m.setAccessible(true);
                m.invoke(cl, file.toURI().toURL());
            }
            catch(Throwable t1)
            {
                warn();
                t1.printStackTrace();
                return false;
            }
        }
        else
        {
            warn();
            _log.severe("[VirtualPack] PluginClassLoader not URLClassLoader!");
            return false;
        }
        return true;
    }
    
    public static String base64en(String string)
    {
        return DatatypeConverter.printBase64Binary(string.getBytes());
    }
    
    public static String base64de(String string)
    {
        return new String(DatatypeConverter.parseBase64Binary(string));
    }
    
    public static String[][] readIni(File file) throws FileNotFoundException, IOException
    {
        return readIni(new FileInputStream(file));
    }
    
    public static String[][] readIni(InputStream stream) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
        ArrayList<String[]> list = new ArrayList<String[]>();
        String line;
        while((line = reader.readLine()) != null)
        {
            String[] parts = line.split("=");
            if(parts.length == 2)
            {
                list.add(parts);
            }
        }
        return list.toArray(new String[0][]);
    }
    
    public static String parseColors(String chars)
    {
        String colors = "";
        for(char c : chars.toCharArray())
        {
            colors += ChatColor.getByChar(c).toString();
        }
        return colors;
    }
    
    public static int getTick()
    {
        int tick = Config.getInt("tick.interval");
        return (tick > 0) ? tick : 10;
    }
}