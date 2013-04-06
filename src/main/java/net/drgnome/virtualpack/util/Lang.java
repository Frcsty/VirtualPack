// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.util;

import static net.drgnome.virtualpack.util.Global.warn;
import static net.drgnome.virtualpack.util.Global._plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.bukkit.configuration.file.YamlConfiguration;

public class Lang
{
    public static final String _langVersion = "5";
    private static YamlConfiguration _file = new YamlConfiguration();
    private static File _dir;
    
    public static void init()
    {
        _dir = _plugin.getDataFolder();
        reload();
    }
    
    public static void reload()
    {
        try
        {
            File file = new File(_dir, "lang.yml");
            boolean create = !file.exists();
            if(create)
            {
                file.getParentFile().mkdirs();
                PrintStream writer = new PrintStream(new FileOutputStream(file));
                writer.close();
            }
            _file.load(file);
            if(!create)
            {
                String lv = _file.getString("langv");
                String lc = _file.getString("langc");
                if((lv == null) || (!lv.equalsIgnoreCase(_langVersion)) || (lc == null) || (!lc.equalsIgnoreCase(Config.string("language"))))
                {
                    _file.save(new File(_dir, "lang_" + Config.string("language") + "_" + _langVersion + ".yml"));
                    _file = new YamlConfiguration();
                }
            }
            setDefs();
            _file.save(file);
        }
        catch(Throwable t)
        {
            warn();
            t.printStackTrace();
        }
    }
    
    // Set all default values
    private static void setDefs()
    {
        setDef("langv", _langVersion);
        setDef("langc", Config.string("language"));
        try
        {
            for(String[] s : Util.readIni(_plugin.getResource("lang/" + Config.string("language") + ".lang")))
            {
                setDef(s[0], s[1]);
            }
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
    }
    
    // Set a default value
    private static void setDef(String path, String value)
    {
        if(!_file.isSet(path))
        {
            _file.set(path, value);
        }
    }
    
    public static String get(String string, String... replacements)
    {
        string = get(string);
        if(replacements != null)
        {
            for(int i = 1; i <= replacements.length; i++)
            {
                string = string.replaceAll("%" + i, replacements[i - 1]);
            }
        }
        return string;
    }
    
    public static String get(String string)
    {
        if((_file != null) && (_file.isSet(string)))
        {
            return _file.getString(string);
        }
        return "[VirtualPack] STRING NOT FOUND";
    }
}