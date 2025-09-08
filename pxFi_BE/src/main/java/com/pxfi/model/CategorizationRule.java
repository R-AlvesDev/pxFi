package com.pxfi.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categorization_rules")
public class CategorizationRule {

    public enum RuleField {
        REMITTANCE_INFO,
        AMOUNT
    }

    public enum RuleOperator {
        CONTAINS,
        EQUALS,
        STARTS_WITH,
        ENDS_WITH,
        AMOUNT_EQUALS,      
        AMOUNT_GREATER_THAN,
        AMOUNT_LESS_THAN 
    }

    @Id
    private String id;
    private ObjectId userId; 

    private RuleField fieldToMatch; 
    private RuleOperator operator;   
    private String valueToMatch;     
    
    private String categoryId;
    private String subCategoryId; // Can be null

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public ObjectId getUserId() { return userId; }
    public void setUserId(ObjectId userId) { this.userId = userId; }
    
    public RuleField getFieldToMatch() { return fieldToMatch; }
    public void setFieldToMatch(RuleField fieldToMatch) { this.fieldToMatch = fieldToMatch; }

    public RuleOperator getOperator() { return operator; }
    public void setOperator(RuleOperator operator) { this.operator = operator; }

    public String getValueToMatch() { return valueToMatch; }
    public void setValueToMatch(String valueToMatch) { this.valueToMatch = valueToMatch; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getSubCategoryId() { return subCategoryId; }
    public void setSubCategoryId(String subCategoryId) { this.subCategoryId = subCategoryId; }
}