package com.example.evcharging.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.Map;

// This class now accurately reflects the backend's Notification.cs model
public class Notification {

    @SerializedName("id")
    public String id;

    @SerializedName("recipientNIC")
    public String recipientNIC;

    @SerializedName("title")
    public String title;

    @SerializedName("message")
    public String message;

    // Backend sends 'type' as a number (enum)
    @SerializedName("type")
    public int type;

    @SerializedName("relatedEntityId")
    public String relatedEntityId;

    @SerializedName("isRead")
    public boolean isRead;

    // Backend sends 'priority' as a number (enum)
    @SerializedName("priority")
    public int priority;

    @SerializedName("createdAt")
    public Date createdAt;

    // Optional fields
    @SerializedName("metadata")
    public Map<String, Object> metadata;

    // Default constructor for Gson
    public Notification() {}
}
