package com.example.evcharging.models;

import com.google.gson.annotations.SerializedName;

public class CancellationReason {

    @SerializedName("reason")
    private String reason;

    public CancellationReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
