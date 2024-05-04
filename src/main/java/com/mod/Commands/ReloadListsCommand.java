package com.mod.Commands;

import com.mod.TodoList;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ReloadListsCommand extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "reloadlists";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/reloadlists";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        TodoList.reloadLists();
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }
}
