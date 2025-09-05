package com.pxfi.service;

import com.pxfi.model.Category;
import com.pxfi.model.User;
import com.pxfi.repository.CategoryRepository;
import com.pxfi.repository.TransactionRepository; // You might need this for future validation
import com.pxfi.security.SecurityConfiguration;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository; // For validating deletes

    public CategoryService(CategoryRepository categoryRepository, TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    // The @PostConstruct init() method has been removed as it's not suitable for a multi-user system.
    // Default categories should be created when a new user registers.

    public List<Category> getAllCategories() {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            return List.of(); // Return an empty list if no user is logged in
        }
        return categoryRepository.findByUserId(currentUser.getId());
    }

    public Category createCategory(Category category) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Cannot create a category without a logged in user.");
        }
        category.setUserId(currentUser.getId()); // Set the owner of the new category
        return categoryRepository.save(category);
    }

    public Optional<Category> updateCategory(String id, Category categoryDetails) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            return Optional.empty();
        }
        // Find the category by its ID AND the current user's ID to ensure ownership
        return categoryRepository.findByIdAndUserId(id, currentUser.getId()).map(category -> {
            category.setName(categoryDetails.getName());
            category.setParentId(categoryDetails.getParentId());
            category.setAssetTransfer(categoryDetails.isAssetTransfer());
            return categoryRepository.save(category);
        });
    }

    public void deleteCategory(String id) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated.");
        }

        // First, verify the category belongs to the current user
        categoryRepository.findByIdAndUserId(id, currentUser.getId())
            .orElseThrow(() -> new IllegalStateException("Category not found or you do not have permission to delete it."));

        // Check if the category is a parent to any subcategories for this user
        if (categoryRepository.countByParentIdAndUserId(id, currentUser.getId()) > 0) {
            throw new IllegalStateException("Cannot delete a category that has subcategories.");
        }
        
        // Optional: Add a check here to see if any transactions use this category before deleting.
        
        categoryRepository.deleteById(id);
    }

    // This method is now designed to be called when a new user registers.
    public void createDefaultCategoriesForUser(String userId) {
        if (userId == null) return;
    
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
            parent.setUserId(userId); // Assign to the new user
            categoryRepository.save(parent);

            for (String subName : subNames.split(",")) {
                Category sub = new Category();
                String formattedSubName = toTitleCase(subName.replace('-', ' '));
                sub.setName(formattedSubName);
                sub.setParentId(parent.getId());
                sub.setUserId(userId); // Assign to the new user

                if ("Savings Investments".equalsIgnoreCase(formattedSubName)) {
                    sub.setAssetTransfer(true);
                }
                categoryRepository.save(sub);
            }
        });

        Category uncategorized = new Category();
        uncategorized.setName("Uncategorized");
        uncategorized.setUserId(userId); // Assign to the new user
        categoryRepository.save(uncategorized);
    }

    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return input;
        return Arrays.stream(input.split("\\s+"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}