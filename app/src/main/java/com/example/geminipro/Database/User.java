package com.example.geminipro.Database;

import android.net.Uri;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.HashMap;
import java.util.List;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private List<String> stringUris;
    private List<String> userOrGemini;
    private HashMap<Integer, List<Uri>> imageHashMap;
    private String date;
    private String title;
    private boolean pin;
    private String funcType;

    public User(String title, String date, List<String> stringUris, List<String> userOrGemini, HashMap<Integer, List<Uri>> imageHashMap, boolean pin, String funcType) {
        this.title = title;
        this.date = date;
        this.stringUris = stringUris;
        this.userOrGemini = userOrGemini;
        this.imageHashMap = imageHashMap;
        this.pin = pin;
        this.funcType = funcType;
    }

    // Getters and setters

    public boolean isPin() {
        return pin;
    }

    public void setPin(boolean pin) {
        this.pin = pin;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<String> getStringUris() {
        return stringUris;
    }

    public void setStringUris(List<String> stringUris) {
        this.stringUris = stringUris;
    }

    public List<String> getUserOrGemini() {
        return userOrGemini;
    }

    public void setUserOrGemini(List<String> userOrGemini) {
        this.userOrGemini = userOrGemini;
    }

    public HashMap<Integer, List<Uri>> getImageHashMap() {
        return imageHashMap;
    }

    public void setImageHashMap(HashMap<Integer, List<Uri>> imageHashMap) {
        this.imageHashMap = imageHashMap;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFuncType() {
        return funcType;
    }

    public void setFuncType(String funcType) {
        this.funcType = funcType;
    }

}