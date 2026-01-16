package com.streetfoodgo.web.ui;

import com.streetfoodgo.core.service.MenuItemCustomizationService;
import com.streetfoodgo.core.service.MenuItemService;
import com.streetfoodgo.core.service.model.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/owner/menu-items")
@PreAuthorize("hasRole('OWNER')")
public class OwnerMenuCustomizationController {

    private final MenuItemService menuItemService;
    private final MenuItemCustomizationService customizationService;

    public OwnerMenuCustomizationController(
            final MenuItemService menuItemService,
            final MenuItemCustomizationService customizationService) {
        this.menuItemService = menuItemService;
        this.customizationService = customizationService;
    }

    // =============== OPTIONS / CHOICES ===============

    @GetMapping("/{itemId}/options")
    public String listOptions(@PathVariable Long itemId, final Model model) {
        final MenuItemView item = menuItemService.getMenuItem(itemId).orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
        final List<MenuItemOptionView> options = customizationService.getOptions(itemId);
        model.addAttribute("item", item);
        model.addAttribute("options", options);
        return "owner/menu/options";
    }

    @GetMapping("/{itemId}/options/new")
    public String newOption(@PathVariable Long itemId, final Model model) {
        final MenuItemView item = menuItemService.getMenuItem(itemId).orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
        model.addAttribute("item", item);
        model.addAttribute("optionForm", new CreateMenuItemOptionRequest(itemId, "", "", false, false, 0, 1, 0));
        return "owner/menu/option-form";
    }

    @PostMapping("/{itemId}/options")
    public String createOption(@PathVariable Long itemId,
                               @Valid @ModelAttribute("optionForm") CreateMenuItemOptionRequest request,
                               BindingResult bindingResult,
                               final Model model) {
        if (bindingResult.hasErrors()) {
            final MenuItemView item = menuItemService.getMenuItem(itemId).orElseThrow();
            model.addAttribute("item", item);
            return "owner/menu/option-form";
        }
        customizationService.createOption(request);
        return "redirect:/owner/menu-items/" + itemId + "/options";
    }

    @GetMapping("/{itemId}/options/{optionId}/edit")
    public String editOption(@PathVariable Long itemId, @PathVariable Long optionId, final Model model) {
        final MenuItemView item = menuItemService.getMenuItem(itemId).orElseThrow();
        final List<MenuItemOptionView> options = customizationService.getOptions(itemId);
        final MenuItemOptionView opt = options.stream().filter(o -> o.id().equals(optionId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Option not found"));

        model.addAttribute("item", item);
        model.addAttribute("optionId", optionId);
        model.addAttribute("optionForm", new UpdateMenuItemOptionRequest(
                opt.name(), opt.description(), opt.isRequired(), opt.allowMultiple(), opt.minSelections(), opt.maxSelections(), opt.displayOrder()
        ));
        return "owner/menu/option-form";
    }

    @PostMapping("/{itemId}/options/{optionId}/edit")
    public String updateOption(@PathVariable Long itemId,
                               @PathVariable Long optionId,
                               @Valid @ModelAttribute("optionForm") UpdateMenuItemOptionRequest request,
                               BindingResult bindingResult,
                               final Model model) {
        if (bindingResult.hasErrors()) {
            final MenuItemView item = menuItemService.getMenuItem(itemId).orElseThrow();
            model.addAttribute("item", item);
            model.addAttribute("optionId", optionId);
            return "owner/menu/option-form";
        }
        customizationService.updateOption(optionId, request);
        return "redirect:/owner/menu-items/" + itemId + "/options";
    }

    @PostMapping("/{itemId}/options/{optionId}/delete")
    public String deleteOption(@PathVariable Long itemId, @PathVariable Long optionId) {
        customizationService.deleteOption(optionId);
        return "redirect:/owner/menu-items/" + itemId + "/options";
    }

    @GetMapping("/{itemId}/options/{optionId}/choices/new")
    public String newChoice(@PathVariable Long itemId, @PathVariable Long optionId, final Model model) {
        final MenuItemView item = menuItemService.getMenuItem(itemId).orElseThrow();
        model.addAttribute("item", item);
        model.addAttribute("optionId", optionId);
        model.addAttribute("choiceForm", new CreateMenuItemChoiceRequest("", "", java.math.BigDecimal.ZERO, true, false, 0));
        return "owner/menu/choice-form";
    }

    @PostMapping("/{itemId}/options/{optionId}/choices")
    public String createChoice(@PathVariable Long itemId,
                               @PathVariable Long optionId,
                               @Valid @ModelAttribute("choiceForm") CreateMenuItemChoiceRequest request,
                               BindingResult bindingResult,
                               final Model model) {
        if (bindingResult.hasErrors()) {
            final MenuItemView item = menuItemService.getMenuItem(itemId).orElseThrow();
            model.addAttribute("item", item);
            model.addAttribute("optionId", optionId);
            return "owner/menu/choice-form";
        }
        customizationService.createChoice(optionId, request);
        return "redirect:/owner/menu-items/" + itemId + "/options";
    }

    @GetMapping("/{itemId}/options/{optionId}/choices/{choiceId}/edit")
    public String editChoice(@PathVariable Long itemId, @PathVariable Long optionId, @PathVariable Long choiceId, final Model model) {
        final MenuItemView item = menuItemService.getMenuItem(itemId).orElseThrow();
        final List<MenuItemOptionView> options = customizationService.getOptions(itemId);
        final MenuItemChoiceView choice = options.stream()
                .filter(o -> o.id().equals(optionId))
                .flatMap(o -> o.choices().stream())
                .filter(c -> c.id().equals(choiceId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Choice not found"));

        model.addAttribute("item", item);
        model.addAttribute("optionId", optionId);
        model.addAttribute("choiceId", choiceId);
        model.addAttribute("choiceForm", new UpdateMenuItemChoiceRequest(
                choice.name(), choice.description(), choice.additionalPrice(), choice.isAvailable(), choice.isDefault(), choice.displayOrder()
        ));
        return "owner/menu/choice-form";
    }

    @PostMapping("/{itemId}/options/{optionId}/choices/{choiceId}/edit")
    public String updateChoice(@PathVariable Long itemId,
                               @PathVariable Long optionId,
                               @PathVariable Long choiceId,
                               @Valid @ModelAttribute("choiceForm") UpdateMenuItemChoiceRequest request,
                               BindingResult bindingResult,
                               final Model model) {
        if (bindingResult.hasErrors()) {
            final MenuItemView item = menuItemService.getMenuItem(itemId).orElseThrow();
            model.addAttribute("item", item);
            model.addAttribute("optionId", optionId);
            model.addAttribute("choiceId", choiceId);
            return "owner/menu/choice-form";
        }
        customizationService.updateChoice(choiceId, request);
        return "redirect:/owner/menu-items/" + itemId + "/options";
    }

    @PostMapping("/{itemId}/options/{optionId}/choices/{choiceId}/delete")
    public String deleteChoice(@PathVariable Long itemId, @PathVariable Long optionId, @PathVariable Long choiceId) {
        customizationService.deleteChoice(choiceId);
        return "redirect:/owner/menu-items/" + itemId + "/options";
    }

    // =============== INGREDIENTS ===============

    @GetMapping("/{itemId}/ingredients")
    public String listIngredients(@PathVariable Long itemId, final Model model) {
        final MenuItemView item = menuItemService.getMenuItem(itemId).orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
        final List<MenuItemIngredientView> ingredients = customizationService.getIngredients(itemId);
        model.addAttribute("item", item);
        model.addAttribute("ingredients", ingredients);
        return "owner/menu/ingredients";
    }

    @GetMapping("/{itemId}/ingredients/new")
    public String newIngredient(@PathVariable Long itemId, final Model model) {
        final MenuItemView item = menuItemService.getMenuItem(itemId).orElseThrow();
        model.addAttribute("item", item);
        model.addAttribute("ingredientForm", new CreateMenuItemIngredientRequest("", "", true, false, "", 0));
        return "owner/menu/ingredient-form";
    }

    @PostMapping("/{itemId}/ingredients")
    public String createIngredient(@PathVariable Long itemId,
                                   @Valid @ModelAttribute("ingredientForm") CreateMenuItemIngredientRequest request,
                                   BindingResult bindingResult,
                                   final Model model) {
        if (bindingResult.hasErrors()) {
            final MenuItemView item = menuItemService.getMenuItem(itemId).orElseThrow();
            model.addAttribute("item", item);
            return "owner/menu/ingredient-form";
        }
        customizationService.createIngredient(itemId, request);
        return "redirect:/owner/menu-items/" + itemId + "/ingredients";
    }

    @GetMapping("/{itemId}/ingredients/{ingredientId}/edit")
    public String editIngredient(@PathVariable Long itemId, @PathVariable Long ingredientId, final Model model) {
        final MenuItemView item = menuItemService.getMenuItem(itemId).orElseThrow();
        final List<MenuItemIngredientView> ingredients = customizationService.getIngredients(itemId);
        final MenuItemIngredientView ing = ingredients.stream().filter(i -> i.id().equals(ingredientId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found"));

        model.addAttribute("item", item);
        model.addAttribute("ingredientId", ingredientId);
        model.addAttribute("ingredientForm", new UpdateMenuItemIngredientRequest(
                ing.name(), ing.description(), ing.isRemovable(), ing.isAllergen(), ing.allergenInfo(), ing.displayOrder()
        ));
        return "owner/menu/ingredient-form";
    }

    @PostMapping("/{itemId}/ingredients/{ingredientId}/edit")
    public String updateIngredient(@PathVariable Long itemId,
                                   @PathVariable Long ingredientId,
                                   @Valid @ModelAttribute("ingredientForm") UpdateMenuItemIngredientRequest request,
                                   BindingResult bindingResult,
                                   final Model model) {
        if (bindingResult.hasErrors()) {
            final MenuItemView item = menuItemService.getMenuItem(itemId).orElseThrow();
            model.addAttribute("item", item);
            model.addAttribute("ingredientId", ingredientId);
            return "owner/menu/ingredient-form";
        }
        customizationService.updateIngredient(ingredientId, request);
        return "redirect:/owner/menu-items/" + itemId + "/ingredients";
    }

    @PostMapping("/{itemId}/ingredients/{ingredientId}/delete")
    public String deleteIngredient(@PathVariable Long itemId, @PathVariable Long ingredientId) {
        customizationService.deleteIngredient(ingredientId);
        return "redirect:/owner/menu-items/" + itemId + "/ingredients";
    }
}
