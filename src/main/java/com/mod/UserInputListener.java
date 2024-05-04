package com.mod;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class UserInputListener
{
    private static float originalSensitivity;
    public static boolean isLowSenseRN = false;
    public static boolean isShowTodo;
    public static int[] todoEditorGuiPosCash;

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event)
    {
        if (Init.keyTodoToggleVisibility.isPressed())
        {
            isShowTodo = !isShowTodo;
        }
    }



}
