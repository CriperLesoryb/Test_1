package com.mod.util;

public class TodoItem
{
        private String item;
        private int color;

    public TodoItem(String item, int color)
    {
        this.item = item;
        this.color = color;
    }

    public String getItem()
    {
        return item;
    }

    public int getColor()
    {
        return color;
    }

    public void removeLastChar()
    {
        if (item.length() < 1)
        {
            return;
        }
        item = item.substring(0, item.length() - 1);
    }

    public void appendChar(char c)
    {
        item += c;
    }
}
