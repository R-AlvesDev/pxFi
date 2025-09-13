package com.pxfi.model;

public class CategorizeSimilarRequest {
    private String remittanceInfo;
    private String categoryId;
    private String subCategoryId;
    private boolean isAddingSubcategory;

    public boolean isAddingSubcategory() {
        return isAddingSubcategory;
    }

    public void setAddingSubcategory(boolean addingSubcategory) {
        isAddingSubcategory = addingSubcategory;
    }

    // Getters and Setters
    public String getRemittanceInfo() {
        return remittanceInfo;
    }

    public void setRemittanceInfo(String remittanceInfo) {
        this.remittanceInfo = remittanceInfo;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getSubCategoryId() {
        return subCategoryId;
    }

    public void setSubCategoryId(String subCategoryId) {
        this.subCategoryId = subCategoryId;
    }
}