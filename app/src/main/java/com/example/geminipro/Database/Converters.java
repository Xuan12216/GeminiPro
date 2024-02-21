package com.example.geminipro.Database;

import android.net.Uri;

import androidx.room.TypeConverter;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Converters {
    @TypeConverter
    public static String fromList(List<String> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<String> toList(String string) {
        Type listType = new TypeToken<List<String>>() {}.getType();
        return new Gson().fromJson(string, listType);
    }

    @TypeConverter
    public static String fromUriList(HashMap<Integer, List<Uri>> hashMap) {
        Gson gson = new Gson();
        HashMap<Integer, List<String>> hashMapStr = new HashMap<>();
        for (Integer key : hashMap.keySet()) {
            List<String> uriStrList = new ArrayList<>();
            for (Uri uri : Objects.requireNonNull(hashMap.get(key))) {
                uriStrList.add(uri.toString());
            }
            hashMapStr.put(key, uriStrList);
        }
        return gson.toJson(hashMapStr);
    }

    @TypeConverter
    public static HashMap<Integer, List<Uri>> toUriList(String string) {
        Gson gson = new Gson();
        HashMap<Integer, List<String>> hashMapStr = gson.fromJson(string, new com.google.gson.reflect.TypeToken<HashMap<Integer, List<String>>>() {}.getType());
        HashMap<Integer, List<Uri>> hashMap = new HashMap<>();
        for (Integer key : hashMapStr.keySet()) {
            List<Uri> uriList = new ArrayList<>();
            for (String uriStr : Objects.requireNonNull(hashMapStr.get(key))) {
                uriList.add(Uri.parse(uriStr));
            }
            hashMap.put(key, uriList);
        }
        return hashMap;
    }
}
