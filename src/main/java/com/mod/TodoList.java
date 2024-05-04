package com.mod;

import com.mod.util.JsonReader;
import com.mod.util.TodoItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TodoList
{
    private static final Map<Integer, TodoList> lists = new HashMap<>();
    private static int currentListID = 0;
    private static final Map<String, Integer> colorCodes = new HashMap<>();

    static
    {
        colorCodes.put("§0", 0x000000); // black
        colorCodes.put("§1", 0x0000AA); // dark_blue
        colorCodes.put("§2", 0x00AA00); // dark_green
        colorCodes.put("§3", 0x00AAAA); // dark_aqua
        colorCodes.put("§4", 0xAA0000); // dark_red
        colorCodes.put("§5", 0xAA00AA); // dark_purple
        colorCodes.put("§6", 0xFFAA00); // gold
        colorCodes.put("§7", 0xAAAAAA); // gray
        colorCodes.put("§8", 0x555555); // dark_gray
        colorCodes.put("§9", 0x5555FF); // blue
        colorCodes.put("§a", 0x55FF55); // green
        colorCodes.put("§b", 0x55FFFF); // aqua
        colorCodes.put("§c", 0xFF5555); // red
        colorCodes.put("§d", 0xFF55FF); // light_purple
        colorCodes.put("§e", 0xFFFF55); // yellow
        colorCodes.put("§f", 0xFFFFFF); // white
    }
    public ArrayList<TodoItem> list = new ArrayList<>();

    public TodoList(ArrayList<String> strings)
    {
        for (String str : strings)
        {
            if (str.startsWith("§"))
            {
                list.add(new TodoItem(str.substring(2), colorCodes.get(str.substring(0, 2))));
            }
            else
            {
                list.add(new TodoItem(str, 0xFFFFFF));
            }
        }
    }

    public TodoList()
    {
        list.add(new TodoItem("", 0xFFFFFF));
    }

    public static void saveListsToDisk()
    {
        Init.sendMsgToPlayer("saving lists");
        HashMap<String, ArrayList<String>> map = new HashMap<>();
        for (Integer id : lists.keySet())
        {
            ArrayList<String> rawList = new ArrayList<>();
            for (TodoItem item : lists.get(id).list)
            {
                rawList.add(item.getItem());
            }
            map.put(id.toString(), rawList);
        }
        JsonReader.saveToJson(map, "./Todo/lists.json");
    }

    public static void readListsFromDisk()
    {
        Map<String, ArrayList<String>> listsFromJson = JsonReader.readJsonFileOutsideJar("./Todo/lists.json");
        if (listsFromJson == null || listsFromJson.size() < 1)
        {
            //there are no lists
            listsFromJson = new HashMap<>();
            listsFromJson.put("0", new ArrayList<>(Arrays.asList("Hello", "World")));
        }
        for (String key : listsFromJson.keySet())
        {
            int id = 0;
            try
            {
                id = Integer.parseInt(key);
            }
            catch (NumberFormatException e)
            {
                id = lists.size();
            }
            finally
            {
                addList(id, new TodoList(listsFromJson.get(key)));
            }
        }
    }

    public static void reloadLists()
    {
        lists.clear();
        readListsFromDisk();
    }

    public void addEntryToList(String item, int color)
    {
        list.add(new TodoItem(item, color));
    }

    public void addEntryToList(String item, int color, int i)
    {
        list.add(i, new TodoItem(item, color));
    }

    public void  removeEntryFromList(int i)
    {
        list.remove(i);
    }

    public static TodoList getCurrent()
    {
        return lists.get(currentListID);
    }

    public static ArrayList<TodoItem> getCurrentList()
    {
        return lists.get(currentListID).list;
    }

    public static void incrementCurrentListI(int x)
    {
        currentListID += x;
        currentListID %= lists.size();
        if (currentListID < 0)
        {
            goToLatestList();
        }
    }

    public static void goToLatestList()
    {
        currentListID = lists.size() - 1;
    }

    private static void addList(Integer id, TodoList list)
    {
        lists.put(id, list);
        goToLatestList();
    }

    public static void createNewList()
    {
        addList(lists.size(), new TodoList());
    }

}
