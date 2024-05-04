package com.mod.util;

import com.google.gson.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JsonReader
{

    public static Map<String, ArrayList<String>> readJsonFileOutsideJar(String path)
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
            {
                stringBuilder.append(line);
            }
            reader.close();
            String json = stringBuilder.toString();
            Gson gson = new Gson();
            JsonElement jsonElement = new JsonParser().parse(json);
            return parseJsonElement(jsonElement);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, ArrayList<String>> readJsonFileInsideJar(String path)
    {
        try
        {
            InputStream inputStream = JsonReader.class.getResourceAsStream(path);
            if (inputStream == null)
            {
                return null;
            }

            java.util.Scanner scanner = new java.util.Scanner(inputStream).useDelimiter("\\A");
            String json = scanner.hasNext() ? scanner.next() : "";
            scanner.close();

            JsonElement jsonElement = new JsonParser().parse(json);

            inputStream.close();

            return parseJsonElement(jsonElement);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private static Map<String, ArrayList<String>> parseJsonElement(JsonElement jsonElement)
    {
        Map<String, ArrayList<String>> resultMap = new HashMap<>();
        if (!jsonElement.isJsonObject()) return null;
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet())
        {
            String entryName = entry.getKey();
            JsonElement currentList = jsonObject.get(entryName);
            if (!currentList.isJsonArray()) continue;

            resultMap.put(entryName, new ArrayList<>());

            for (JsonElement element : currentList.getAsJsonArray())
            {
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString())
                {
                    resultMap.get(entryName).add(element.getAsString());
                }
            }
        }
        return resultMap;
    }

    public static void saveToJson(Map<String, ArrayList<String>> map, String path) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(map);

        try (FileWriter writer = new FileWriter(path)) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveArrayListToJson(ArrayList<String> arrayList, String filePath) {
        try {
            // Create a Gson instance
            Gson gson = new Gson();

            // Convert ArrayList to JSON array
            JsonArray jsonArray = new JsonArray();
            for (String item : arrayList) {
                JsonElement element = new JsonPrimitive(item);
                jsonArray.add(element);
            }

            // Write JSON array to file
            try (FileWriter writer = new FileWriter(filePath)) {
                gson.toJson(jsonArray, writer);
            }

            System.out.println("ArrayList saved to JSON file successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
