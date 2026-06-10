package com.example.surveycollector.model.remote.dto;

import com.google.gson.annotations.SerializedName;

public class FirebaseKey {
    @SerializedName("name")
    private String name;

    public FirebaseKey() {}

    public FirebaseKey(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
