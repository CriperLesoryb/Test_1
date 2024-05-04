package com.mod;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class UserInputListener
{
    public static boolean isShowTodo;

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event)
    {
        if (Init.keyTodoToggleVisibility.isPressed())
        {
            isShowTodo = !isShowTodo;
        }
    }



}
