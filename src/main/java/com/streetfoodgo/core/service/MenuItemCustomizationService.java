package com.streetfoodgo.core.service;

import com.streetfoodgo.core.service.model.*;

import java.util.List;

public interface MenuItemCustomizationService {

    List<MenuItemOptionView> getOptions(Long menuItemId);

    MenuItemOptionView createOption(CreateMenuItemOptionRequest request);

    MenuItemOptionView updateOption(Long optionId, UpdateMenuItemOptionRequest request);

    void deleteOption(Long optionId);

    MenuItemChoiceView createChoice(Long optionId, CreateMenuItemChoiceRequest request);

    MenuItemChoiceView updateChoice(Long choiceId, UpdateMenuItemChoiceRequest request);

    void deleteChoice(Long choiceId);

    List<MenuItemIngredientView> getIngredients(Long menuItemId);

    MenuItemIngredientView createIngredient(Long menuItemId, CreateMenuItemIngredientRequest request);

    MenuItemIngredientView updateIngredient(Long ingredientId, UpdateMenuItemIngredientRequest request);

    void deleteIngredient(Long ingredientId);
}
