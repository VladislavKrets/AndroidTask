package com.test.testapplication;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Test {
    public static void main(String[] args) {
        String line = "{token=\"ioppppp\"}";
        Gson gson = new Gson();
        String token;
        JsonObject jsonObject = gson.fromJson(line, JsonObject.class);
        try {
            token = jsonObject.get("token").getAsString();
        } catch (Exception e) {
            token = null;
            //if exception was thrown during parsing token it means data was incorrect
        }
        System.out.println(token);
    }
}
