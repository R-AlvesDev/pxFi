package com.pxfi.service;

import com.pxfi.model.Category;
import com.pxfi.model.User;
import com.pxfi.repository.CategoryRepository;
import com.pxfi.security.SecurityConfiguration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            return List.of();
        }
        return categoryRepository.findByUserId(currentUser.getId());
    }

    public Category createCategory(Category category) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Cannot create a category without a logged in user.");
        }
        category.setUserId(currentUser.getId());
        return categoryRepository.save(category);
    }

    public Optional<Category> updateCategory(String id, Category categoryDetails) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            return Optional.empty();
        }
        return categoryRepository
                .findByIdAndUserId(id, currentUser.getId())
                .map(
                        category -> {
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

        categoryRepository
                .findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Category not found or you do not have permission to delete it."));

        if (categoryRepository.countByParentIdAndUserId(id, currentUser.getId()) > 0) {
            throw new IllegalStateException("Cannot delete a category that has subcategories.");
        }

        categoryRepository.deleteById(id);
    }

    public void createDefaultCategoriesForUser(ObjectId userId) {
        if (userId == null) {
            return;
        }

        Map<String, String> parentCategories = Map.of(
                "Income",
                "salary,meal-allowance,other-income",
                "Housing",
                "rent-mortgage,utilities,internet-tv-phone",
                "Transportation",
                "public-transport,tolls,fuel,ride-sharing,car-maintenance",
                "Food & Dining",
                "groceries,restaurants-cafes,takeaway-delivery",
                "Shopping",
                "clothing-accessories,electronics-gadgets,hobbies,general-merchandise",
                "Health & Wellness",
                "healthcare-pharmacy,gym-fitness,therapy",
                "Entertainment",
                "subscriptions,events-movies,books-music",
                "Financial",
                "bank-fees,transfers,savings-investments",
                "Pets",
                "veterinary,food",
                "Others",
                "withdrawals");

        parentCategories.forEach(
                (parentName, subNames) -> {
                    Category parent = new Category();
                    parent.setName(parentName);
                    parent.setUserId(userId);
                    categoryRepository.save(parent);

                    for (String subName : subNames.split(",")) {
                        Category sub = new Category();
                        String formattedSubName = toTitleCase(subName.replace('-', ' '));
                        sub.setName(formattedSubName);
                        sub.setParentId(parent.getId());
                        sub.setUserId(userId);

                        if ("Savings Investments".equalsIgnoreCase(formattedSubName)) {
                            sub.setAssetTransfer(true);
                        }
                        categoryRepository.save(sub);
                    }
                });

        Category uncategorized = new Category();
        uncategorized.setName("Uncategorized");
        uncategorized.setUserId(userId);
        categoryRepository.save(uncategorized);
    }

    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Arrays.stream(input.split("\\s+"))
                .map(
                        word -> Character.toUpperCase(word.charAt(0))
                                + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
