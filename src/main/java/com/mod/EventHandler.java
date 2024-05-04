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
//    public static GuiHitbox vanillaInventory = new GuiHitbox(
//            (Minecraft.getMinecraft().displayWidth - 176) / 2 + 6,
//            (Minecraft.getMinecraft().displayHeight - 176) / 2,
//            176,
//            166
//    );

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
            int scale = Minecraft.getMinecraft().gameSettings.guiScale;
//            vanillaInventory = new GuiHitbox(
//                    (Minecraft.getMinecraft().displayWidth / scale - 176) / 2 + 6,
//                    (Minecraft.getMinecraft().displayHeight / scale - 176) / 2,
//                    176,
//                    166
//            );
        }
        else if (event.gui instanceof GuiIngameMenu)
        {
            TodoList.saveListsToDisk();
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        TodoList.saveListsToDisk();
    }

//    @SubscribeEvent
//    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
//        // Check if the inventory GUI was previously opened and now it's closed
//        if (isPlayerInInventory && event.gui instanceof GuiInventory && Minecraft.getMinecraft().currentScreen == null) {
//            // The inventory GUI is being closed
//            isPlayerInInventory = false;
//            System.out.println("Player has closed inventory GUI");
//            // Add your code here to perform actions when the player closes the inventory GUI
//        }
//    }

    @SubscribeEvent

    public void drawInventoryGui(GuiScreenEvent.DrawScreenEvent.Post event)
    {
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if (gui instanceof GuiInventory)
        {
            /* to-do list editor */
            ResourceLocation texture = new ResourceLocation("euphaasmod:textures/gui/longBlankGui.png");
            Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

            if (todoEditorHitbox == null)
            {
                int guiScale = Minecraft.getMinecraft().gameSettings.guiScale;
                todoEditorHitbox = new GuiHitbox(
                        (Minecraft.getMinecraft().displayWidth / guiScale - 176) / 2 - 184,
                        (Minecraft.getMinecraft().displayHeight / guiScale - 256) / 2,
                        176,
                        256
                );
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

    }

    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent event)
    {
        if (event.gui instanceof GuiInventory)
        {
            GuiInventory guiInventory = (GuiInventory) event.gui;
            int scaleFactor = guiInventory.mc.gameSettings.guiScale;
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
        if (event.gui instanceof GuiInventory)
        {
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
            if (code == Keyboard.KEY_RETURN)
            {
                TodoList.getCurrent().list.add(currentLine + 1, new TodoItem("", TodoList.getCurrent().list.get(currentLine).getColor()));
                currentLine += 1;
            }
            else if (code == Keyboard.KEY_BACK)
            {
                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && TodoList.getCurrent().list.size() > currentLine)
                {
                    removeLine();
                }
                else
                {
                    TodoList.getCurrent().list.get(currentLine).removeLastChar();
                }
            }
            else if (code == Keyboard.KEY_ESCAPE || code == Keyboard.KEY_LSHIFT
                    || code == Keyboard.KEY_RSHIFT || code == Keyboard.KEY_LCONTROL)
            {
                return;
            }
            else if (code == Keyboard.KEY_LEFT)
            {
                TodoList.incrementCurrentListI(-1);
                currentLine = 0;
            }
            else if (code == Keyboard.KEY_RIGHT)
            {
                TodoList.incrementCurrentListI(1);
                currentLine = 0;
            }
            else if (code == Keyboard.KEY_N && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
            {
                //add a blank list and then go to it
                TodoList.createNewList();
                TodoList.goToLatestList();
                currentLine = 0;
            }
            else if (code == Keyboard.KEY_S && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
            {
                TodoList.saveListsToDisk();
            }
            else
            {
                TodoList.getCurrent().list.get(currentLine).appendChar(key);
            }
            event.setCanceled(true);
        }
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
