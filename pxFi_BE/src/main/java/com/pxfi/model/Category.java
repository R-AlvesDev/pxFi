package com.pxfi.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "categories")
public class Category {
    @Id
    private String id;

    private String name;

    private String parentId; 
    private String userId; 

    private boolean isAssetTransfer = false; 

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @JsonProperty("isAssetTransfer")
    public boolean isAssetTransfer() {
        return isAssetTransfer;
    }

    public void setAssetTransfer(boolean isAssetTransfer) {
        this.isAssetTransfer = isAssetTransfer;
    }
}