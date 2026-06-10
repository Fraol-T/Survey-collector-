package com.example.surveycollector.model.remote.dto;

import com.google.gson.annotations.SerializedName;

public class RespondentDto {
    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("registeredAt")
    private String registeredAt;

    private transient String firebaseKey;

    public RespondentDto() {}

    public RespondentDto(String name, String email, String registeredAt) {
        this.name = name;
        this.email = email;
        this.registeredAt = registeredAt;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(String registeredAt) { this.registeredAt = registeredAt; }

    public String getFirebaseKey() { return firebaseKey; }
    public void setFirebaseKey(String firebaseKey) { this.firebaseKey = firebaseKey; }
}
