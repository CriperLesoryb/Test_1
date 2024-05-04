package com.mod;

import com.mod.Commands.ReloadListsCommand;
import com.mod.util.JsonReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.IOException;

@Mod(modid = "TodoList", useMetadata=true)
public class Init
{
    public static final String MODID = "TodoList";

    public static final KeyBinding keyTodoToggleVisibility = new KeyBinding("Toggle to-do overlay", Keyboard.KEY_LBRACKET, "euphaa's mods");

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        /* make dirs */
        checkForAndMakeDir("./Todo");
        createFile("./Todo/lists.json");

        /* keybinds */
        ClientRegistry.registerKeyBinding(keyTodoToggleVisibility);

        /* event handlers */
        MinecraftForge.EVENT_BUS.register(new Renderer());
        MinecraftForge.EVENT_BUS.register(new UserInputListener());

        /* commands */
        ClientCommandHandler.instance.registerCommand(new ReloadListsCommand());

        /* import to-do lists */
        TodoList.readListsFromDisk();


    }


    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {

    }

    public static void sendMsgToPlayer(String msg)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        player.addChatComponentMessage(new ChatComponentText(msg));
    }

    //makes a directory only if it doesn't exist already
    public static void checkForAndMakeDir(String path)
    {
        File resourcesDir = new File(path);
        if (!resourcesDir.isDirectory()) {
            //dir doens't exist, time to make one
            resourcesDir.mkdirs();
        }
    }

    private static boolean createFile(String path)
    {
        File f = new File(path);
        if (!f.exists())
        {
            try
            {
                f.createNewFile();
                return true;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }

}
