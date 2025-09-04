package com.pxfi.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.pxfi.model.Category;
import com.pxfi.repository.CategoryRepository;

import jakarta.annotation.PostConstruct;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @PostConstruct
    public void init() {
        if (categoryRepository.count() == 0) {
            createDefaultCategories();
        }
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Arrays.stream(input.split("\\s+"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    private void createDefaultCategories() {
        Map<String, String> parentCategories = Map.of(
            "Income", "salary,meal-allowance,other-income",
            "Housing", "rent-mortgage,utilities,internet-tv-phone",
            "Transportation", "public-transport,tolls,fuel,ride-sharing,car-maintenance",
            "Food & Dining", "groceries,restaurants-cafes,takeaway-delivery",
            "Shopping", "clothing-accessories,electronics-gadgets,hobbies,general-merchandise",
            "Health & Wellness", "healthcare-pharmacy,gym-fitness,therapy",
            "Entertainment", "subscriptions,events-movies,books-music",
            "Financial", "bank-fees,transfers,savings-investments",
            "Pets","veterinary,food",
            "Others","withdrawals"
        );

        parentCategories.forEach((parentName, subNames) -> {
            Category parent = new Category();
            parent.setName(parentName);
            categoryRepository.save(parent);

            for (String subName : subNames.split(",")) {
                Category sub = new Category();
                String formattedSubName = toTitleCase(subName.replace('-', ' '));
                sub.setName(formattedSubName);
                sub.setParentId(parent.getId());

                if ("Savings Investments".equalsIgnoreCase(formattedSubName)) {
                    sub.setAssetTransfer(true);
                }

                categoryRepository.save(sub);
            }
        });

        Category uncategorized = new Category();
        uncategorized.setName("Uncategorized");
        categoryRepository.save(uncategorized);
    }

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Optional<Category> updateCategory(String id, Category categoryDetails) {
        return categoryRepository.findById(id).map(category -> {
            category.setName(categoryDetails.getName());
            category.setParentId(categoryDetails.getParentId());
            category.setAssetTransfer(categoryDetails.isAssetTransfer());
            return categoryRepository.save(category);
        });
    }

    public void deleteCategory(String id) {
        // Check if the category is a parent to any subcategories
        if (categoryRepository.existsByParentId(id)) {
            // If it is, throw an exception to prevent deletion
            throw new IllegalStateException("Cannot delete a category that has subcategories.");
        }
        
        categoryRepository.deleteById(id);
    }
    
}