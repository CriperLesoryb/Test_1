package com.mod;

import com.mod.util.GuiHitbox;
import com.mod.util.TodoItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;

public class EventHandler
{
//  this is true when the overlay is temporarily hidden.

    public static GuiHitbox todoEditorHitbox;
    public static int[] todoEditorGuiPosCash;
    public static boolean draggingEditorGui;
    public static int currentLine = -1;
    public static boolean holdingDownLMB;
    public static boolean focusedOnEditor;
    public static int[] overlayCoords = {20, 20};
    public static int guiScaleCache = Minecraft.getMinecraft().gameSettings.guiScale;


    @SubscribeEvent
    public void onGameRender(TickEvent.RenderTickEvent event)
    {
//        to-do list overlay
        if (UserInputListener.isShowTodo)
        {
            renderList(TodoList.getCurrent().list);
        }

    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event)
    {
        if (event.gui instanceof GuiInventory)
        {
            int newGuiScale = Minecraft.getMinecraft().gameSettings.guiScale;
            if (guiScaleCache != newGuiScale)
            {
                guiScaleCache = newGuiScale;
                resetGuiLocation();
            }
        }
        if (event.gui instanceof GuiIngameMenu)
        {
            TodoList.saveListsToDisk();
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        TodoList.saveListsToDisk();
    }

    @SubscribeEvent
    public void drawInventoryGui(GuiScreenEvent.DrawScreenEvent.Post event)
    {

        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if (!(gui instanceof GuiInventory))
        {
            return;
        }

        /* to-do list editor */
        ResourceLocation texture = new ResourceLocation("euphaasmod:textures/gui/longBlankGui.png");
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

        if (todoEditorHitbox == null)
        {
            resetGuiLocation();
        }

        //Size to be cut from texture
        int width = 176;
        int height = 256;

        //Stretch the texture to this size
        //If drawTexturedModalRect(), this value will be 256 (i.e. 256x256 png is recommended when using drawTexturedModalRect())
        int textureWidth = 256;
        int textureHeight = 256;

        //Position to start cutting from the texture
        int u = 0;
        int v = 0;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(todoEditorHitbox.objectX, todoEditorHitbox.objectY, u, v, width, height, textureWidth, textureHeight);

        // draw to-do text
        ArrayList<TodoItem> list = TodoList.getCurrent().list;
        for (int i = 0; i < list.size(); i++)
        {
            if (i == currentLine && System.currentTimeMillis() % 700 < 350)
            {
                renderTextToScreen(list.get(i).getItem() + "_", todoEditorHitbox.objectX+10, todoEditorHitbox.objectY+10 + 10*i, list.get(i).getColor());
            }
            else
            {
                renderTextToScreen(list.get(i).getItem(), todoEditorHitbox.objectX+10, todoEditorHitbox.objectY+10 + 10*i, list.get(i).getColor());
            }
        }
    }

    private static void resetGuiLocation()
    {
        int guiScale = getGuiScale();
        todoEditorHitbox = new GuiHitbox(
                (Minecraft.getMinecraft().displayWidth / guiScale - 176) / 2 - 184,
                (Minecraft.getMinecraft().displayHeight / guiScale - 256) / 2,
                176,
                256
        );
    }

    private static int getGuiScale()
    {
        return Math.max(1, Minecraft.getMinecraft().gameSettings.guiScale);
    }

    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent event)
    {
        if (!(event.gui instanceof GuiInventory))
        {
            return;
        }

        GuiInventory guiInventory = (GuiInventory) event.gui;
        int scaleFactor = getGuiScale();
        int mouseX = Mouse.getEventX() / scaleFactor;
        int mouseY = (guiInventory.mc.displayHeight - Mouse.getEventY()) / scaleFactor;

        boolean isLeftButtonDown = org.lwjgl.input.Mouse.isButtonDown(0);
        boolean isRightButtonDown = org.lwjgl.input.Mouse.isButtonDown(1);
        if (todoEditorHitbox == null) return;

        if (draggingEditorGui)
        {
            if (!isLeftButtonDown)
            {
                draggingEditorGui = false;
            }
            if (todoEditorGuiPosCash != null)
            {
                //we have position data, so this is not first iteration.
                moveTodoEditorBox(mouseX, mouseY);
            }
            // update position data
            todoEditorGuiPosCash = new int[]{mouseX, mouseY};
            return;
        }

        boolean mouseInsideEditor = todoEditorHitbox.isPointInsideHitbox(mouseX, mouseY);

        //mouse is outside of inventory
        if (todoEditorHitbox.isPointInsideBorder(mouseX, mouseY, 8) && isLeftButtonDown)
        {
            draggingEditorGui = true;

        }
        else if (mouseInsideEditor && isLeftButtonDown)
        {
            focusedOnEditor = true;
            if (!holdingDownLMB)
            {
                currentLine = (mouseY - (todoEditorHitbox.objectY + 10)) / 10;
                currentLine = Math.min(currentLine, TodoList.getCurrent().list.size() - 1);

                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && TodoList.getCurrent().list.size() > currentLine)
                {
                    removeLine();
                }
                checkForIllegalCurrentLine();
            }
        }
        else if (!mouseInsideEditor && isLeftButtonDown)
        {
            focusedOnEditor = false;
            currentLine = -1;
        }
        else
        {
            //border is unclicked, stop dragging box
            todoEditorGuiPosCash = null;
        }

        if (Mouse.isButtonDown(0) && !holdingDownLMB)
        {
            holdingDownLMB = true;
        }
        else if (!Mouse.isButtonDown(0) && holdingDownLMB)
        {
            holdingDownLMB = false;
        }
    }

    private static void checkForIllegalCurrentLine()
    {
        if (currentLine > TodoList.getCurrent().list.size() - 1)
        {
            currentLine = TodoList.getCurrent().list.size() - 1;
        }
    }

    @SubscribeEvent
    public void onKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event)
    {
        // Check if the open GUI is the inventory screen
        if (!(event.gui instanceof GuiInventory))
        {
            return;
        }
        if (currentLine < 0)
        {
            return;
        }

        // Check if a key is pressed
        if (!Keyboard.getEventKeyState())
        {
            return;
        }
        char key = Keyboard.getEventCharacter();
        int code = Keyboard.getEventKey();

        //handling the input

        /* for ctrl commands */
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
        {
            switch (code)
            {
                case Keyboard.KEY_N:
                {
                    //add a blank list and then go to it
                    TodoList.createNewList();
                    TodoList.goToLatestList();
                    currentLine = 0;
                    break;
                }
                case Keyboard.KEY_S:
                {
                    TodoList.saveListsToDisk();
                    break;
                }
            }
        }

        /* while shift is being held */
        else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
        {
            switch (code)
            {
                case Keyboard.KEY_BACK:
                {
                    removeLine();
                    break;
                }
            }
        }

        /* normal controls */
        else
        {
            switch (code)
            {
                case Keyboard.KEY_RETURN:
                {
                    TodoList.getCurrent().list.add(currentLine + 1, new TodoItem("", TodoList.getCurrent().list.get(currentLine).getColor()));
                    currentLine += 1;
                    break;
                }
                case Keyboard.KEY_BACK:
                {
                    TodoList.getCurrent().list.get(currentLine).removeLastChar();
                    break;
                }
                case Keyboard.KEY_LEFT:
                {
                    TodoList.incrementCurrentListI(-1);
                    currentLine = 0;
                    break;
                }
                case Keyboard.KEY_RIGHT:
                {
                    TodoList.incrementCurrentListI(1);
                    currentLine = 0;
                    break;
                }
                case Keyboard.KEY_UP:
                {
                    if (currentLine > 0)
                    {
                        currentLine--;
                    }
                    break;
                }
                case Keyboard.KEY_DOWN:
                {
                    if (currentLine < TodoList.getCurrent().size() - 1)
                    {
                        currentLine++;
                    }
                    break;
                }
                case Keyboard.KEY_ESCAPE:
                case Keyboard.KEY_LSHIFT:
                case Keyboard.KEY_RSHIFT:
                case Keyboard.KEY_LCONTROL:
                {
                    return;
                }
                default:
                {
                    TodoList.getCurrent().list.get(currentLine).appendChar(key);
                }
            }
        }

        event.setCanceled(true);
    }

    private static void removeLine()
    {
        if (TodoList.getCurrent().list.size() <= 1)
        {
            //if this line is the last line, set to empty instead of removing
            TodoList.getCurrent().list.set(0, new TodoItem("", TodoList.getCurrent().list.get(0).getColor()));
            return;
        }
        TodoList.getCurrent().list.remove(currentLine);
        if (currentLine != 0)
        {
            //bring currentLine to something that exists
            currentLine -= 1;
        }
    }

    private void moveTodoEditorBox(int mouseX, int mouseY)
    {
        int diffX = mouseX - todoEditorGuiPosCash[0];
        int diffY = mouseY - todoEditorGuiPosCash[1];
        todoEditorHitbox.objectX += diffX;
        todoEditorHitbox.objectY += diffY;
    }


    public void renderList(ArrayList<TodoItem> list)
    {
        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i).getItem().length() < 1) continue;
            renderTextToScreen("- " + list.get(i).getItem(), overlayCoords[0], overlayCoords[1] + 10*i, list.get(i).getColor());
        }
    }

    private void renderTextToScreen(String text, int x, int y, int color)
    {
        Minecraft mc = Minecraft.getMinecraft();
        mc.fontRendererObj.drawStringWithShadow(text, x, y, color);
    }

}
