package com.ie.pdf2.ztools;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


//Gson转Map时，Int会变成double解决方法

public class GsonDoubleInteger {

    public static Gson getGson() {
        Gson gson = new GsonBuilder().registerTypeAdapter(HashMap.class, new JsonDeserializer<HashMap>() {
                    @Override
                    public HashMap<String, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        HashMap<String, Object> resultMap = new HashMap<>();
                        JsonObject jsonObject = json.getAsJsonObject();
                        Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                        for (Map.Entry<String, JsonElement> entry : entrySet) {
                            resultMap.put(entry.getKey(), entry.getValue());
                        }
                        return resultMap;
                    }

                })
                .disableHtmlEscaping()
                .create();
        return gson;
    }


}
