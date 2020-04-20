package me.davidml16.acubelets.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public final class NBTEditor {
    private static final Map< String, Class<?> > classCache;
    private static final Map< String, Method > methodCache;
    private static final Map< Class< ? >, Constructor< ? > > constructorCache;
    private static final Map< Class< ? >, Class< ? > > NBTClasses;
    private static final Map< Class< ? >, Field > NBTTagFieldCache;
    private static Field NBTListData;
    private static Field NBTCompoundMap;
    private static final String VERSION;
    private static final MinecraftVersion LOCAL_VERSION;

    static {
        VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        LOCAL_VERSION = MinecraftVersion.get( VERSION );

        classCache = new HashMap< String, Class<?> >();
        try {
            classCache.put( "NBTBase", Class.forName( "net.minecraft.server." + VERSION + "." + "NBTBase" ) );
            classCache.put( "NBTTagCompound", Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagCompound" ) );
            classCache.put( "NBTTagList", Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagList" ) );
            classCache.put( "MojangsonParser", Class.forName( "net.minecraft.server." + VERSION + "." + "MojangsonParser" ) );

            classCache.put( "ItemStack", Class.forName( "net.minecraft.server." + VERSION + "." + "ItemStack" ) );
            classCache.put( "CraftItemStack", Class.forName( "org.bukkit.craftbukkit." + VERSION + ".inventory." + "CraftItemStack" ) );
            classCache.put( "CraftMetaSkull", Class.forName( "org.bukkit.craftbukkit." + VERSION + ".inventory." + "CraftMetaSkull" ) );

            classCache.put( "Entity", Class.forName( "net.minecraft.server." + VERSION + "." + "Entity" ) );
            classCache.put( "CraftEntity", Class.forName( "org.bukkit.craftbukkit." + VERSION + ".entity." + "CraftEntity" ) );
            classCache.put( "EntityLiving", Class.forName( "net.minecraft.server." + VERSION + "." + "EntityLiving" ) );

            classCache.put( "CraftWorld", Class.forName( "org.bukkit.craftbukkit." + VERSION + "." + "CraftWorld" ) );
            classCache.put( "CraftBlockState", Class.forName( "org.bukkit.craftbukkit." + VERSION + ".block." + "CraftBlockState" ) );
            classCache.put( "BlockPosition", Class.forName( "net.minecraft.server." + VERSION + "." + "BlockPosition" ) );
            classCache.put( "TileEntity", Class.forName( "net.minecraft.server." + VERSION + "." + "TileEntity" ) );
            classCache.put( "World", Class.forName( "net.minecraft.server." + VERSION + "." + "World" ) );

            classCache.put( "TileEntitySkull", Class.forName( "net.minecraft.server." + VERSION + "." + "TileEntitySkull" ) );
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        NBTClasses = new HashMap< Class< ? >, Class< ? > >();
        try {
            NBTClasses.put( Byte.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagByte" ) );
            NBTClasses.put( String.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagString" ) );
            NBTClasses.put( Double.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagDouble" ) );
            NBTClasses.put( Integer.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagInt" ) );
            NBTClasses.put( Long.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagLong" ) );
            NBTClasses.put( Short.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagShort" ) );
            NBTClasses.put( Float.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagFloat" ) );
            NBTClasses.put( Class.forName( "[B" ), Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagByteArray" ) );
            NBTClasses.put( Class.forName( "[I" ), Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagIntArray" ) );
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        methodCache = new HashMap< String, Method >();
        try {
            methodCache.put( "get", getNMSClass( "NBTTagCompound" ).getMethod( "get", String.class ) );
            methodCache.put( "set", getNMSClass( "NBTTagCompound" ).getMethod( "set", String.class, getNMSClass( "NBTBase" ) ) );
            methodCache.put( "hasKey", getNMSClass( "NBTTagCompound" ).getMethod( "hasKey", String.class ) );
            methodCache.put( "setIndex", getNMSClass( "NBTTagList" ).getMethod( "a", int.class, getNMSClass( "NBTBase" ) ) );
            if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_14 ) ) {
                methodCache.put( "getTypeId", getNMSClass( "NBTBase" ).getMethod( "getTypeId" ) );
                methodCache.put( "add", getNMSClass( "NBTTagList" ).getMethod( "add", int.class, getNMSClass( "NBTBase" ) ) );
                methodCache.put( "size", getNMSClass( "NBTTagList" ).getMethod( "size" ) );
            } else {
                methodCache.put( "add", getNMSClass( "NBTTagList" ).getMethod( "add", getNMSClass( "NBTBase" ) ) );
            }

            if ( LOCAL_VERSION == MinecraftVersion.v1_8 ) {
                methodCache.put( "listRemove", getNMSClass( "NBTTagList" ).getMethod( "a", int.class )  );
            } else {
                methodCache.put( "listRemove", getNMSClass( "NBTTagList" ).getMethod( "remove", int.class )  );
            }
            methodCache.put( "remove", getNMSClass( "NBTTagCompound" ).getMethod( "remove", String.class ) );

            methodCache.put( "hasTag", getNMSClass( "ItemStack" ).getMethod( "hasTag" ) );
            methodCache.put( "getTag", getNMSClass( "ItemStack" ).getMethod( "getTag" ) );
            methodCache.put( "setTag", getNMSClass( "ItemStack" ).getMethod( "setTag", getNMSClass( "NBTTagCompound" ) ) );
            methodCache.put( "asNMSCopy", getNMSClass( "CraftItemStack" ).getMethod( "asNMSCopy", ItemStack.class ) );
            methodCache.put( "asBukkitCopy", getNMSClass( "CraftItemStack" ).getMethod( "asBukkitCopy", getNMSClass( "ItemStack" ) ) );

            methodCache.put( "getEntityHandle", getNMSClass( "CraftEntity" ).getMethod( "getHandle" ) );
            methodCache.put( "getEntityTag", getNMSClass( "Entity" ).getMethod( "c", getNMSClass( "NBTTagCompound" ) ) );
            methodCache.put( "setEntityTag", getNMSClass( "Entity" ).getMethod( "f", getNMSClass( "NBTTagCompound" ) ) );

            methodCache.put( "save", getNMSClass( "ItemStack" ).getMethod( "save", getNMSClass( "NBTTagCompound" ) ) );

            if ( LOCAL_VERSION.lessThanOrEqualTo( MinecraftVersion.v1_10 ) ) {
                methodCache.put( "createStack", getNMSClass( "ItemStack" ).getMethod( "createStack", getNMSClass( "NBTTagCompound" ) ) );
            } else if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_13 ) ) {
                methodCache.put( "createStack", getNMSClass( "ItemStack" ).getMethod( "a", getNMSClass( "NBTTagCompound" ) ) );
            }

            if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_12 )) {
                methodCache.put( "setTileTag", getNMSClass( "TileEntity" ).getMethod( "load", getNMSClass( "NBTTagCompound" ) ) );
            } else {
                methodCache.put( "setTileTag", getNMSClass( "TileEntity" ).getMethod( "a", getNMSClass( "NBTTagCompound" ) ) );
            }
            methodCache.put( "getTileEntity", getNMSClass( "World" ).getMethod( "getTileEntity", getNMSClass( "BlockPosition" ) ) );
            methodCache.put( "getWorldHandle", getNMSClass( "CraftWorld" ).getMethod( "getHandle" ) );

            methodCache.put( "setGameProfile", getNMSClass( "TileEntitySkull" ).getMethod( "setGameProfile", GameProfile.class ) );

            methodCache.put( "loadNBTTagCompound", getNMSClass( "MojangsonParser" ).getMethod( "parse", String.class ) );
        } catch( Exception e ) {
            e.printStackTrace();
        }

        try {
            methodCache.put( "getTileTag", getNMSClass( "TileEntity" ).getMethod( "save", getNMSClass( "NBTTagCompound" ) ) );
        } catch( NoSuchMethodException exception ) {
            try {
                methodCache.put( "getTileTag", getNMSClass( "TileEntity" ).getMethod( "b", getNMSClass( "NBTTagCompound" ) ) );
            } catch ( Exception exception2 ) {
                exception2.printStackTrace();
            }
        } catch( Exception exception ) {
            exception.printStackTrace();
        }

        try {
            methodCache.put( "setProfile", getNMSClass( "CraftMetaSkull" ).getDeclaredMethod( "setProfile", GameProfile.class ) );
            methodCache.get( "setProfile" ).setAccessible( true );
        } catch( NoSuchMethodException exception ) {
            // The method doesn't exist, so it's before 1.15.2
        }

        constructorCache = new HashMap< Class< ? >, Constructor< ? > >();
        try {
            constructorCache.put( getNBTTag( Byte.class ), getNBTTag( Byte.class ).getDeclaredConstructor( byte.class ) );
            constructorCache.put( getNBTTag( String.class ), getNBTTag( String.class ).getDeclaredConstructor( String.class ) );
            constructorCache.put( getNBTTag( Double.class ), getNBTTag( Double.class ).getDeclaredConstructor( double.class ) );
            constructorCache.put( getNBTTag( Integer.class ), getNBTTag( Integer.class ).getDeclaredConstructor( int.class ) );
            constructorCache.put( getNBTTag( Long.class ), getNBTTag( Long.class ).getDeclaredConstructor( long.class ) );
            constructorCache.put( getNBTTag( Float.class ), getNBTTag( Float.class ).getDeclaredConstructor( float.class ) );
            constructorCache.put( getNBTTag( Short.class ), getNBTTag( Short.class ).getDeclaredConstructor( short.class ) );
            constructorCache.put( getNBTTag( Class.forName( "[B" ) ), getNBTTag( Class.forName( "[B" ) ).getDeclaredConstructor( Class.forName( "[B" ) ) );
            constructorCache.put( getNBTTag( Class.forName( "[I" ) ), getNBTTag( Class.forName( "[I" ) ).getDeclaredConstructor( Class.forName( "[I" ) ) );

            // This is for 1.15 since Mojang decided to make the constructors private
            for ( Constructor< ? > cons : constructorCache.values() ) {
                cons.setAccessible( true );
            }

            constructorCache.put( getNMSClass( "BlockPosition" ), getNMSClass( "BlockPosition" ).getConstructor( int.class, int.class, int.class ) );

            if ( LOCAL_VERSION == MinecraftVersion.v1_11 || LOCAL_VERSION == MinecraftVersion.v1_12 ) {
                constructorCache.put( getNMSClass( "ItemStack" ), getNMSClass( "ItemStack" ).getConstructor( getNMSClass( "NBTTagCompound" ) ) );
            }
        } catch( Exception e ) {
            e.printStackTrace();
        }

        NBTTagFieldCache = new HashMap< Class< ? >, Field >();
        try {
            for ( Class< ? > clazz : NBTClasses.values() ) {
                Field data = clazz.getDeclaredField( "data" );
                data.setAccessible( true );
                NBTTagFieldCache.put( clazz, data );
            }
        } catch( Exception e ) {
            e.printStackTrace();
        }

        try {
            NBTListData = getNMSClass( "NBTTagList" ).getDeclaredField( "list" );
            NBTListData.setAccessible( true );
            NBTCompoundMap = getNMSClass( "NBTTagCompound" ).getDeclaredField( "map" );
            NBTCompoundMap.setAccessible( true );
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }

    private static Class< ? > getNBTTag( Class< ? > primitiveType ) {
        if ( NBTClasses.containsKey( primitiveType ) )
            return NBTClasses.get( primitiveType );
        return primitiveType;
    }

    private static Object getNBTVar( Object object ) {
        if ( object == null ) {
            return null;
        }
        Class< ? > clazz = object.getClass();
        try {
            if ( NBTTagFieldCache.containsKey( clazz ) ) {
                return NBTTagFieldCache.get( clazz ).get( object );
            }
        } catch ( Exception exception ) {
            exception.printStackTrace();
        }
        return null;
    }

    private static Method getMethod( String name ) {
        return methodCache.containsKey( name ) ? methodCache.get( name ) : null;
    }

    private static Constructor< ? > getConstructor( Class< ? > clazz ) {
        return constructorCache.containsKey( clazz ) ? constructorCache.get( clazz ) : null;
    }

    private static Class<?> getNMSClass(String name) {
        if ( classCache.containsKey( name ) ) {
            return classCache.get( name );
        }

        try {
            return Class.forName("net.minecraft.server." + VERSION + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getMatch( String string, String regex ) {
        Pattern pattern = Pattern.compile( regex );
        Matcher matcher = pattern.matcher( string );
        if ( matcher.find() ) {
            return matcher.group( 1 );
        } else {
            return null;
        }
    }

    private static Object createItemStack( Object compound ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
        if ( LOCAL_VERSION == MinecraftVersion.v1_11 || LOCAL_VERSION == MinecraftVersion.v1_12 ) {
            return getConstructor( getNMSClass( "ItemStack" ) ).newInstance( compound );
        }
        return getMethod( "createStack" ).invoke( null, compound );
    }

    public static String getVersion() {
        return VERSION;
    }

    public static ItemStack getHead( String skinURL ) {
        Material material = Material.getMaterial( "SKULL_ITEM" );
        if ( material == null ) {
            // Most likely 1.13 materials
            material = Material.getMaterial( "PLAYER_HEAD" );
        }
        ItemStack head = new ItemStack( material, 1, ( short ) 3 );
        if ( skinURL == null || skinURL.isEmpty() ) {
            return head;
        }
        ItemMeta headMeta = head.getItemMeta();
        GameProfile profile = new GameProfile( UUID.randomUUID(), null);
        byte[] encodedData = Base64.getEncoder().encode( String.format( "{textures:{SKIN:{\"url\":\"%s\"}}}", skinURL ).getBytes() );
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));

        if ( methodCache.containsKey( "setProfile" ) ) {
            try {
                getMethod( "setProfile" ).invoke( headMeta, profile );
            } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
                e.printStackTrace();
            }
        } else {
            Field profileField = null;
            try {
                profileField = headMeta.getClass().getDeclaredField("profile");
            } catch ( NoSuchFieldException | SecurityException e ) {
                e.printStackTrace();
            }
            profileField.setAccessible(true);
            try {
                profileField.set(headMeta, profile);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        head.setItemMeta(headMeta);
        return head;
    }

    public static String getTexture( ItemStack head ) {
        ItemMeta meta = head.getItemMeta();
        Field profileField = null;
        try {
            profileField = meta.getClass().getDeclaredField("profile");
        } catch ( NoSuchFieldException | SecurityException e ) {
            e.printStackTrace();
            throw new IllegalArgumentException( "Item is not a player skull!" );
        }
        profileField.setAccessible(true);
        try {
            GameProfile profile = ( GameProfile ) profileField.get( meta );
            if ( profile == null ) {
                return null;
            }

            for ( Property prop : profile.getProperties().values() ) {
                if ( prop.getName().equals( "textures" ) ) {
                    String texture = new String( Base64.getDecoder().decode( prop.getValue() ) );
                    return getMatch( texture, "\\{\"url\":\"(.*?)\"\\}" );
                }
            }
            return null;
        } catch ( IllegalArgumentException | IllegalAccessException | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object getItemTag( ItemStack item, Object... keys ) {
        if ( item == null ) {
            return null;
        }
        try {
            Object stack = null;
            stack = getMethod( "asNMSCopy" ).invoke( null, item );

            Object tag = null;

            if ( getMethod( "hasTag" ).invoke( stack ).equals( true ) ) {
                tag = getMethod( "getTag" ).invoke( stack );
            } else {
                tag = getNMSClass( "NBTTagCompound" ).newInstance();
            }

            return getTag( tag, keys );
        } catch ( Exception exception ) {
            exception.printStackTrace();
            return null;
        }
    }

    public static NBTCompound getItemNBTTag( ItemStack item, Object... keys ) {
        if ( item == null ) {
            return null;
        }
        try {
            Object stack = null;
            stack = getMethod( "asNMSCopy" ).invoke( null, item );

            Object tag = getNMSClass( "NBTTagCompound" ).newInstance();

            tag = getMethod( "save" ).invoke( stack, tag );

            return getNBTTag( tag, keys );
        } catch ( Exception exception ) {
            exception.printStackTrace();
            return null;
        }
    }

    public static ItemStack setItemTag( ItemStack item, Object value, Object... keys ) {
        if ( item == null ) {
            return null;
        }
        try {
            Object stack = getMethod( "asNMSCopy" ).invoke( null, item );

            Object tag = null;

            if ( getMethod( "hasTag" ).invoke( stack ).equals( true ) ) {
                tag = getMethod( "getTag" ).invoke( stack );
            } else {
                tag = getNMSClass( "NBTTagCompound" ).newInstance();
            }

            if ( keys.length == 0 && value instanceof NBTCompound ) {
                tag = ( ( NBTCompound ) value ).tag;
            } else {
                setTag( tag, value, keys );
            }

            getMethod( "setTag" ).invoke( stack, tag );
            return ( ItemStack ) getMethod( "asBukkitCopy" ).invoke( null, stack );
        } catch ( Exception exception ) {
            exception.printStackTrace();
            return null;
        }
    }

    public static ItemStack getItemFromTag( NBTCompound compound ) {
        if ( compound == null ) {
            return null;
        }
        try {
            Object tag = compound.tag;
            Object count = getTag( tag, "Count" );
            Object id = getTag( tag, "id" );
            if ( count == null || id == null ) {
                System.out.println( "Missing count and id" );
                return null;
            }
            if ( count instanceof Byte && id instanceof String ) {
                return ( ItemStack ) getMethod( "asBukkitCopy" ).invoke( null, createItemStack( tag ) );
            }
            System.out.println( count.getClass() + ":" + id.getClass() );
            return null;
        } catch ( Exception exception ) {
            exception.printStackTrace();
            return null;
        }
    }

    public static Object getEntityTag( Entity entity, Object... keys ) {
        if ( entity == null ) {
            return entity;
        }
        try {
            Object NMSEntity = getMethod( "getEntityHandle" ).invoke( entity );

            Object tag = getNMSClass( "NBTTagCompound" ).newInstance();

            getMethod( "getEntityTag" ).invoke( NMSEntity, tag );

            return getTag( tag, keys );
        } catch ( Exception exception ) {
            exception.printStackTrace();
            return null;
        }
    }

    public static NBTCompound getEntityNBTTag( Entity entity, Object...keys ) {
        if ( entity == null ) {
            return null;
        }
        try {
            Object NMSEntity = getMethod( "getEntityHandle" ).invoke( entity );

            Object tag = getNMSClass( "NBTTagCompound" ).newInstance();

            getMethod( "getEntityTag" ).invoke( NMSEntity, tag );

            return getNBTTag( tag, keys );
        } catch ( Exception exception ) {
            exception.printStackTrace();
            return null;
        }
    }

    public static void setEntityTag( Entity entity, Object value, Object... keys ) {
        if ( entity == null ) {
            return;
        }
        try {
            Object NMSEntity = getMethod( "getEntityHandle" ).invoke( entity );

            Object tag = getNMSClass( "NBTTagCompound" ).newInstance() ;

            getMethod( "getEntityTag" ).invoke( NMSEntity, tag );

            if ( keys.length == 0 && value instanceof NBTCompound ) {
                tag = ( ( NBTCompound ) value ).tag;
            } else {
                setTag( tag, value, keys );
            }

            getMethod( "setEntityTag" ).invoke( NMSEntity, tag );
        } catch ( Exception exception ) {
            exception.printStackTrace();
            return;
        }
    }

    public static Object getBlockTag( Block block, Object... keys ) {
        try {
            if ( block == null || !getNMSClass( "CraftBlockState" ).isInstance( block.getState() ) ) {
                return null;
            }
            Location location = block.getLocation();

            Object blockPosition = getConstructor( getNMSClass( "BlockPosition" ) ).newInstance( location.getBlockX(), location.getBlockY(), location.getBlockZ() );

            Object nmsWorld = getMethod( "getWorldHandle" ).invoke( location.getWorld() );

            Object tileEntity = getMethod( "getTileEntity" ).invoke( nmsWorld, blockPosition );

            Object tag = getNMSClass( "NBTTagCompound" ).newInstance();

            getMethod( "getTileTag" ).invoke( tileEntity, tag );

            return getTag( tag, keys );
        } catch( Exception exception ) {
            exception.printStackTrace();
            return null;
        }
    }

    public static Object getBlockNBTTag( Block block, Object... keys ) {
        try {
            if ( block == null || !getNMSClass( "CraftBlockState" ).isInstance( block.getState() ) ) {
                return null;
            }
            Location location = block.getLocation();

            Object blockPosition = getConstructor( getNMSClass( "BlockPosition" ) ).newInstance( location.getBlockX(), location.getBlockY(), location.getBlockZ() );

            Object nmsWorld = getMethod( "getWorldHandle" ).invoke( location.getWorld() );

            Object tileEntity = getMethod( "getTileEntity" ).invoke( nmsWorld, blockPosition );

            Object tag = getNMSClass( "NBTTagCompound" ).newInstance();

            getMethod( "getTileTag" ).invoke( tileEntity, tag );

            return getNBTTag( tag, keys );
        } catch( Exception exception ) {
            exception.printStackTrace();
            return null;
        }
    }

    public static void setBlockTag( Block block, Object value, Object... keys ) {
        try {
            if ( block == null || !getNMSClass( "CraftBlockState" ).isInstance( block.getState() ) ) {
                return;
            }
            Location location = block.getLocation();

            Object blockPosition = getConstructor( getNMSClass( "BlockPosition" ) ).newInstance( location.getBlockX(), location.getBlockY(), location.getBlockZ() );

            Object nmsWorld = getMethod( "getWorldHandle" ).invoke( location.getWorld() );

            Object tileEntity = getMethod( "getTileEntity" ).invoke( nmsWorld, blockPosition );

            Object tag = getNMSClass( "NBTTagCompound" ).newInstance();

            getMethod( "getTileTag" ).invoke( tileEntity, tag );

            if ( keys.length == 0 && value instanceof NBTCompound ) {
                tag = ( ( NBTCompound ) value ).tag;
            } else {
                setTag( tag, value, keys );
            }

            getMethod( "setTileTag" ).invoke( tileEntity, tag );
        } catch( Exception exception ) {
            exception.printStackTrace();
            return;
        }
    }

    public static void setSkullTexture( Block block, String texture ) {
        GameProfile profile = new GameProfile( UUID.randomUUID(), null );
        profile.getProperties().put( "textures", new com.mojang.authlib.properties.Property( "textures", new String( Base64.getEncoder().encode( String.format( "{textures:{SKIN:{\"url\":\"%s\"}}}", texture ).getBytes() ) ) ) );

        try {
            Location location = block.getLocation();

            Object blockPosition = getConstructor( getNMSClass( "BlockPosition" ) ).newInstance( location.getBlockX(), location.getBlockY(), location.getBlockZ() );

            Object nmsWorld = getMethod( "getWorldHandle" ).invoke( location.getWorld() );

            Object tileEntity = getMethod( "getTileEntity" ).invoke( nmsWorld, blockPosition );

            getMethod( "setGameProfile" ).invoke( tileEntity, profile );
        } catch( Exception exception ) {
            exception.printStackTrace();
        }
    }

    private static Object getValue( Object object, Object... keys ) {
        if ( object instanceof ItemStack ) {
            return getItemTag( ( ItemStack ) object, keys );
        } else if ( object instanceof Entity ) {
            return getEntityTag( ( Entity ) object, keys );
        } else if ( object instanceof Block ) {
            return getBlockTag( ( Block ) object, keys );
        } else if ( object instanceof NBTCompound ) {
            try {
                return getTag( ( ( NBTCompound ) object ).tag, keys );
            } catch ( Exception e ) {
                return null;
            }
        } else {
            throw new IllegalArgumentException( "Object provided must be of type ItemStack, Entity, Block, or NBTCompound!" );
        }
    }

    public static String getString( Object object, Object... keys ) {
        Object result = getValue( object, keys );
        return result instanceof String ? ( String ) result : null;
    }

    public static int getInt( Object object, Object... keys ) {
        Object result = getValue( object, keys );
        return result instanceof Integer ? ( int ) result : 0;
    }

    public static double getDouble( Object object, Object... keys ) {
        Object result = getValue( object, keys );
        return result instanceof Double ? ( double ) result : 0;
    }

    public static long getLong( Object object, Object... keys ) {
        Object result = getValue( object, keys );
        return result instanceof Long ? ( long ) result : 0;
    }

    public static float getFloat( Object object, Object... keys ) {
        Object result = getValue( object, keys );
        return result instanceof Float ? ( float ) result : 0;
    }

    public static short getShort( Object object, Object... keys ) {
        Object result = getValue( object, keys );
        return result instanceof Short ? ( short ) result : 0;
    }

    public static byte getByte( Object object, Object... keys ) {
        Object result = getValue( object, keys );
        return result instanceof Byte ? ( byte ) result : 0;
    }

    public static byte[] getByteArray( Object object, Object... keys ) {
        Object result = getValue( object, keys );
        return result instanceof byte[] ? ( byte[] ) result : null;
    }

    public static int[] getIntArray( Object object, Object... keys ) {
        Object result = getValue( object, keys );
        return result instanceof int[] ? ( int[] ) result : null;
    }

    public static boolean contains( Object object, Object... keys ) {
        Object result = getValue( object, keys );
        return result != null;
    }

    public static < T > T set( T object, Object value, Object... keys ) {
        if ( object instanceof ItemStack ) {
            return ( T ) setItemTag( ( ItemStack ) object, value, keys );
        } else if ( object instanceof Entity ) {
            setEntityTag( ( Entity ) object, value, keys );
        } else if ( object instanceof Block ) {
            setBlockTag( ( Block ) object, value, keys );
        } else {
            throw new IllegalArgumentException( "Object provided must be of type ItemStack, Entity, Block, or NBTCompound!" );
        }
        return object;
    }

    public static NBTCompound getNBTCompound( String json ) {
        return NBTCompound.fromJson( json );
    }

    private static void setTag( Object tag, Object value, Object... keys ) throws Exception {
        Object notCompound;
        if ( value != null ) {
            if ( value instanceof NBTCompound ) {
                notCompound = ( ( NBTCompound ) value ).tag;
            } else if ( getNMSClass( "NBTTagList" ).isInstance( value ) || getNMSClass( "NBTTagCompound" ).isInstance( value ) ) {
                notCompound = value;
            } else {
                notCompound = getConstructor( getNBTTag( value.getClass() ) ).newInstance( value );
            }
        } else {
            notCompound = null;
        }

        Object compound = tag;
        for ( int index = 0; index < keys.length - 1; index++ ) {
            Object key = keys[ index ];
            Object oldCompound = compound;
            if ( key instanceof Integer ) {
                compound = ( ( List< ? > ) NBTListData.get( compound ) ).get( ( int ) key );
            } else if ( key != null ) {
                compound = getMethod( "get" ).invoke( compound, ( String ) key );
            }
            if ( compound == null || key == null ) {
                if ( keys[ index + 1 ] == null || keys[ index + 1 ] instanceof Integer ) {
                    compound = getNMSClass( "NBTTagList" ).newInstance();
                } else {
                    compound = getNMSClass( "NBTTagCompound" ).newInstance();
                }
                if ( oldCompound.getClass().getSimpleName().equals( "NBTTagList" ) ) {
                    if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_14 ) ) {
                        getMethod( "add" ).invoke( oldCompound, getMethod( "size" ).invoke( oldCompound ), compound );
                    } else {
                        getMethod( "add" ).invoke( oldCompound, compound );
                    }
                } else {
                    getMethod( "set" ).invoke( oldCompound, ( String ) key, compound );
                }
            }
        }
        if ( keys.length > 0 ) {
            Object lastKey = keys[ keys.length - 1 ];
            if ( lastKey == null ) {
                if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_14 ) ) {
                    getMethod( "add" ).invoke( compound, getMethod( "size" ).invoke( compound ), notCompound );
                } else {
                    getMethod( "add" ).invoke( compound, notCompound );
                }
            } else if ( lastKey instanceof Integer ) {
                if ( notCompound == null ) {
                    getMethod( "listRemove" ).invoke( compound, ( int ) lastKey );
                } else {
                    getMethod( "setIndex" ).invoke( compound, ( int ) lastKey, notCompound );
                }
            } else {
                if ( notCompound == null ) {
                    getMethod( "remove" ).invoke( compound, ( String ) lastKey );
                } else {
                    getMethod( "set" ).invoke( compound, ( String ) lastKey, notCompound );
                }
            }
        } else {
            if ( notCompound != null ) {
            }
        }
    }

    private static NBTCompound getNBTTag( Object tag, Object...keys ) throws Exception {
        Object compound = tag;

        for ( Object key : keys ) {
            if ( compound == null ) {
                return null;
            } else if ( getNMSClass( "NBTTagCompound" ).isInstance( compound ) ) {
                compound = getMethod( "get" ).invoke( compound, ( String ) key );
            } else if ( getNMSClass( "NBTTagList" ).isInstance( compound ) ) {
                compound = ( ( List< ? > ) NBTListData.get( compound ) ).get( ( int ) key );
            }
        }
        return new NBTCompound( compound );
    }

    private static Object getTag( Object tag, Object... keys ) throws Exception {
        if ( keys.length == 0 ) {
            return getTags( tag );
        }

        Object notCompound = tag;

        for ( Object key : keys ) {
            if ( notCompound == null ) {
                return null;
            } else if ( getNMSClass( "NBTTagCompound" ).isInstance( notCompound ) ) {
                notCompound = getMethod( "get" ).invoke( notCompound, ( String ) key );
            } else if ( getNMSClass( "NBTTagList" ).isInstance( notCompound ) ) {
                notCompound = ( ( List< ? > ) NBTListData.get( notCompound ) ).get( ( int ) key );
            } else {
                return getNBTVar( notCompound );
            }
        }
        if ( notCompound == null ) {
            return null;
        } else if ( getNMSClass( "NBTTagList" ).isInstance( notCompound ) ) {
            return getTags( notCompound );
        } else if ( getNMSClass( "NBTTagCompound" ).isInstance( notCompound ) ) {
            return getTags( notCompound );
        } else {
            return getNBTVar( notCompound );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static Object getTags( Object tag ) {
        Map< Object, Object > tags = new HashMap< Object, Object >();
        try {
            if ( getNMSClass( "NBTTagCompound" ).isInstance( tag ) ) {
                Map< String, Object > tagCompound = ( Map< String, Object > ) NBTCompoundMap.get( tag );
                for ( String key : tagCompound.keySet() ) {
                    Object value = tagCompound.get( key );
                    if ( getNMSClass( "NBTTagEnd" ).isInstance( value ) ) {
                        continue;
                    }
                    tags.put( key, getTag( value ) );
                }
            } else if ( getNMSClass( "NBTTagList" ).isInstance( tag ) ) {
                List< Object > tagList = ( List< Object > ) NBTListData.get( tag );
                for ( int index = 0; index < tagList.size(); index++ ) {
                    Object value = tagList.get( index );
                    if ( getNMSClass( "NBTTagEnd" ).isInstance( value ) ) {
                        continue;
                    }
                    tags.put( index, getTag( value ) );
                }
            } else {
                return getNBTVar( tag );
            }
            return tags;
        } catch ( Exception e ) {
            e.printStackTrace();
            return tags;
        }
    }

    public static final class NBTCompound {
        protected final Object tag;

        protected NBTCompound( @Nonnull Object tag ) {
            this.tag = tag;
        }

        public void set( Object value, Object... keys ) {
            try {
                setTag( tag, value, keys );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }

        public String toJson() {
            return tag.toString();
        }

        public static NBTCompound fromJson( String json ) {
            try {
                return new NBTCompound( getMethod( "loadNBTTagCompound" ).invoke( null, json ) );
            } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public String toString() {
            return tag.toString();
        }

        @Override
        public int hashCode() {
            return tag.hashCode();
        }

        @Override
        public boolean equals( Object obj ) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            NBTCompound other = (NBTCompound) obj;
            if (tag == null) {
                if (other.tag != null)
                    return false;
            } else if (!tag.equals(other.tag))
                return false;
            return true;
        }
    }

    private enum MinecraftVersion {
        v1_8( "1_8", 0 ),
        v1_9( "1_9", 1 ),
        v1_10( "1_10", 2 ),
        v1_11( "1_11", 3 ),
        v1_12( "1_12", 4 ),
        v1_13( "1_13", 5 ),
        v1_14( "1_14", 6 ),
        v1_15( "1_15", 7 );

        private int order;
        private String key;

        MinecraftVersion( String key, int v ) {
            this.key = key;
            order = v;
        }

        // Would be really cool if we could overload operators here
        public boolean greaterThanOrEqualTo( MinecraftVersion other ) {
            return order >= other.order;
        }

        public boolean lessThanOrEqualTo( MinecraftVersion other ) {
            return order <= other.order;
        }

        public static MinecraftVersion get( String v ) {
            for ( MinecraftVersion k : MinecraftVersion.values() ) {
                if ( v.contains( k.key ) ) {
                    return k;
                }
            }
            return null;
        }
    }
}