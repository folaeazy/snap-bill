package com.infrastructure.email.Components;

import com.domain.valueObjects.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryTypeResolver {
    public Category resolve(String rawCategory) {

        if (rawCategory == null) {
            return Category.of("Others");
        }

        String normalized = rawCategory.trim().toLowerCase();

        return switch (normalized) {

            case "food", "restaurant", "dining", "coffee" ->
                    Category.of("Food & Dining");

            case "subscription", "entertainment", "streaming" ->
                    Category.of("Entertainment");

            case "transport", "uber", "ride", "taxi", "bolt" ->
                    Category.of("Transport");

            case "shopping", "ecommerce", "retail" ->
                    Category.of("Shopping");

            case "bills", "utility", "electricity", "water" ->
                    Category.of("Utilities");

            default ->
                    Category.of("Others");
        };
    }
}
