package com.streetfoodgo.core.service.mapper;

import com.streetfoodgo.core.model.*;
import com.streetfoodgo.core.service.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper to convert MenuItem entity to MenuItemView DTO.
 */
@Component
public class MenuItemMapper {

    public MenuItemView toView(final MenuItem menuItem) {
        if (menuItem == null) return null;

        return new MenuItemView(
                menuItem.getId(),
                menuItem.getStore().getId(),
                menuItem.getStore().getName(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getCategory(),
                menuItem.getAvailable(),
                menuItem.getImageUrl(),
                menuItem.getCreatedAt(),
                toOptionViews(menuItem.getOptions()),
                toIngredientViews(menuItem.getIngredients())
        );
    }

    private List<MenuItemOptionView> toOptionViews(List<MenuItemOption> options) {
        if (options == null) return List.of();
        return options.stream()
                .map(this::toOptionView)
                .collect(Collectors.toList());
    }

    private MenuItemOptionView toOptionView(MenuItemOption option) {
        if (option == null) return null;
        return new MenuItemOptionView(
                option.getId(),
                option.getName(),
                option.getDescription(),
                option.getIsRequired(),
                option.getAllowMultiple(),
                option.getMinSelections(),
                option.getMaxSelections(),
                option.getDisplayOrder(),
                toChoiceViews(option.getChoices())
        );
    }

    private List<MenuItemChoiceView> toChoiceViews(List<MenuItemChoice> choices) {
        if (choices == null) return List.of();
        return choices.stream()
                .map(this::toChoiceView)
                .collect(Collectors.toList());
    }

    private MenuItemChoiceView toChoiceView(MenuItemChoice choice) {
        if (choice == null) return null;
        return new MenuItemChoiceView(
                choice.getId(),
                choice.getName(),
                choice.getDescription(),
                choice.getAdditionalPrice(),
                choice.getIsAvailable(),
                choice.getIsDefault(),
                choice.getDisplayOrder()
        );
    }

    private List<MenuItemIngredientView> toIngredientViews(List<MenuItemIngredient> ingredients) {
        if (ingredients == null) return List.of();
        return ingredients.stream()
                .map(this::toIngredientView)
                .collect(Collectors.toList());
    }

    private MenuItemIngredientView toIngredientView(MenuItemIngredient ingredient) {
        if (ingredient == null) return null;
        return new MenuItemIngredientView(
                ingredient.getId(),
                ingredient.getName(),
                ingredient.getDescription(),
                ingredient.getIsRemovable(),
                ingredient.getIsAllergen(),
                ingredient.getAllergenInfo(),
                ingredient.getDisplayOrder()
        );
    }
}