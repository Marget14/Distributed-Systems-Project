package com.streetfoodgo.core.service.impl;

import com.streetfoodgo.core.model.*;
import com.streetfoodgo.core.repository.*;
import com.streetfoodgo.core.security.CurrentUserProvider;
import com.streetfoodgo.core.service.MenuItemCustomizationService;
import com.streetfoodgo.core.service.model.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class MenuItemCustomizationServiceImpl implements MenuItemCustomizationService {

    private final MenuItemRepository menuItemRepository;
    private final MenuItemOptionRepository optionRepository;
    private final MenuItemChoiceRepository choiceRepository;
    private final MenuItemIngredientRepository ingredientRepository;
    private final CurrentUserProvider currentUserProvider;

    public MenuItemCustomizationServiceImpl(
            final MenuItemRepository menuItemRepository,
            final MenuItemOptionRepository optionRepository,
            final MenuItemChoiceRepository choiceRepository,
            final MenuItemIngredientRepository ingredientRepository,
            final CurrentUserProvider currentUserProvider) {
        this.menuItemRepository = Objects.requireNonNull(menuItemRepository);
        this.optionRepository = Objects.requireNonNull(optionRepository);
        this.choiceRepository = Objects.requireNonNull(choiceRepository);
        this.ingredientRepository = Objects.requireNonNull(ingredientRepository);
        this.currentUserProvider = Objects.requireNonNull(currentUserProvider);
    }

    @Override
    public List<MenuItemOptionView> getOptions(final Long menuItemId) {
        if (menuItemId == null || menuItemId <= 0) throw new IllegalArgumentException();
        final MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
        requireOwner(menuItem.getStore());

        return optionRepository.findByMenuItemIdOrderByDisplayOrderAsc(menuItemId)
                .stream()
                .map(this::toOptionView)
                .toList();
    }

    @Transactional
    @Override
    public MenuItemOptionView createOption(final CreateMenuItemOptionRequest request) {
        if (request == null) throw new NullPointerException();
        final MenuItem menuItem = menuItemRepository.findById(request.menuItemId())
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
        requireOwner(menuItem.getStore());

        final MenuItemOption option = new MenuItemOption();
        option.setMenuItem(menuItem);
        option.setName(request.name());
        option.setDescription(request.description());
        option.setIsRequired(Boolean.TRUE.equals(request.isRequired()));
        option.setAllowMultiple(Boolean.TRUE.equals(request.allowMultiple()));
        option.setMinSelections(request.minSelections() != null ? request.minSelections() : 0);
        option.setMaxSelections(request.maxSelections() != null ? request.maxSelections() : 1);
        option.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : 0);

        final MenuItemOption saved = optionRepository.save(option);
        return toOptionView(saved);
    }

    @Transactional
    @Override
    public MenuItemOptionView updateOption(final Long optionId, final UpdateMenuItemOptionRequest request) {
        if (optionId == null || optionId <= 0) throw new IllegalArgumentException();
        if (request == null) throw new NullPointerException();

        final MenuItemOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("Option not found"));
        requireOwner(option.getMenuItem().getStore());

        if (request.name() != null) option.setName(request.name());
        if (request.description() != null) option.setDescription(request.description());
        if (request.isRequired() != null) option.setIsRequired(request.isRequired());
        if (request.allowMultiple() != null) option.setAllowMultiple(request.allowMultiple());
        if (request.minSelections() != null) option.setMinSelections(request.minSelections());
        if (request.maxSelections() != null) option.setMaxSelections(request.maxSelections());
        if (request.displayOrder() != null) option.setDisplayOrder(request.displayOrder());

        return toOptionView(optionRepository.save(option));
    }

    @Transactional
    @Override
    public void deleteOption(final Long optionId) {
        if (optionId == null || optionId <= 0) throw new IllegalArgumentException();
        final MenuItemOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("Option not found"));
        requireOwner(option.getMenuItem().getStore());
        optionRepository.delete(option);
    }

    @Transactional
    @Override
    public MenuItemChoiceView createChoice(final Long optionId, final CreateMenuItemChoiceRequest request) {
        if (optionId == null || optionId <= 0) throw new IllegalArgumentException();
        if (request == null) throw new NullPointerException();

        final MenuItemOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("Option not found"));
        requireOwner(option.getMenuItem().getStore());

        final MenuItemChoice choice = new MenuItemChoice();
        choice.setOption(option);
        choice.setName(request.name());
        choice.setDescription(request.description());
        choice.setAdditionalPrice(request.additionalPrice());
        choice.setIsAvailable(Boolean.TRUE.equals(request.isAvailable()));
        choice.setIsDefault(Boolean.TRUE.equals(request.isDefault()));
        choice.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : 0);

        final MenuItemChoice saved = choiceRepository.save(choice);
        return toChoiceView(saved);
    }

    @Transactional
    @Override
    public MenuItemChoiceView updateChoice(final Long choiceId, final UpdateMenuItemChoiceRequest request) {
        if (choiceId == null || choiceId <= 0) throw new IllegalArgumentException();
        if (request == null) throw new NullPointerException();

        final MenuItemChoice choice = choiceRepository.findById(choiceId)
                .orElseThrow(() -> new IllegalArgumentException("Choice not found"));
        requireOwner(choice.getOption().getMenuItem().getStore());

        if (request.name() != null) choice.setName(request.name());
        if (request.description() != null) choice.setDescription(request.description());
        if (request.additionalPrice() != null) choice.setAdditionalPrice(request.additionalPrice());
        if (request.isAvailable() != null) choice.setIsAvailable(request.isAvailable());
        if (request.isDefault() != null) choice.setIsDefault(request.isDefault());
        if (request.displayOrder() != null) choice.setDisplayOrder(request.displayOrder());

        final MenuItemChoice saved = choiceRepository.save(choice);
        return toChoiceView(saved);
    }

    @Transactional
    @Override
    public void deleteChoice(final Long choiceId) {
        if (choiceId == null || choiceId <= 0) throw new IllegalArgumentException();
        final MenuItemChoice choice = choiceRepository.findById(choiceId)
                .orElseThrow(() -> new IllegalArgumentException("Choice not found"));
        requireOwner(choice.getOption().getMenuItem().getStore());
        choiceRepository.delete(choice);
    }

    @Override
    public List<MenuItemIngredientView> getIngredients(final Long menuItemId) {
        if (menuItemId == null || menuItemId <= 0) throw new IllegalArgumentException();
        final MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
        requireOwner(menuItem.getStore());

        return ingredientRepository.findByMenuItemIdOrderByDisplayOrderAsc(menuItemId)
                .stream()
                .map(this::toIngredientView)
                .toList();
    }

    @Transactional
    @Override
    public MenuItemIngredientView createIngredient(final Long menuItemId, final CreateMenuItemIngredientRequest request) {
        if (menuItemId == null || menuItemId <= 0) throw new IllegalArgumentException();
        if (request == null) throw new NullPointerException();

        final MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
        requireOwner(menuItem.getStore());

        final MenuItemIngredient ingredient = new MenuItemIngredient();
        ingredient.setMenuItem(menuItem);
        ingredient.setName(request.name());
        ingredient.setDescription(request.description());
        ingredient.setIsRemovable(Boolean.TRUE.equals(request.isRemovable()));
        ingredient.setIsAllergen(Boolean.TRUE.equals(request.isAllergen()));
        ingredient.setAllergenInfo(request.allergenInfo());
        ingredient.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : 0);

        final MenuItemIngredient saved = ingredientRepository.save(ingredient);
        return toIngredientView(saved);
    }

    @Transactional
    @Override
    public MenuItemIngredientView updateIngredient(final Long ingredientId, final UpdateMenuItemIngredientRequest request) {
        if (ingredientId == null || ingredientId <= 0) throw new IllegalArgumentException();
        if (request == null) throw new NullPointerException();

        final MenuItemIngredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found"));
        requireOwner(ingredient.getMenuItem().getStore());

        if (request.name() != null) ingredient.setName(request.name());
        if (request.description() != null) ingredient.setDescription(request.description());
        if (request.isRemovable() != null) ingredient.setIsRemovable(request.isRemovable());
        if (request.isAllergen() != null) ingredient.setIsAllergen(request.isAllergen());
        if (request.allergenInfo() != null) ingredient.setAllergenInfo(request.allergenInfo());
        if (request.displayOrder() != null) ingredient.setDisplayOrder(request.displayOrder());

        final MenuItemIngredient saved = ingredientRepository.save(ingredient);
        return toIngredientView(saved);
    }

    @Transactional
    @Override
    public void deleteIngredient(final Long ingredientId) {
        if (ingredientId == null || ingredientId <= 0) throw new IllegalArgumentException();
        final MenuItemIngredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found"));
        requireOwner(ingredient.getMenuItem().getStore());
        ingredientRepository.delete(ingredient);
    }

    private void requireOwner(final Store store) {
        final var current = currentUserProvider.requireCurrentUser();
        if (current.type() != PersonType.OWNER) {
            throw new SecurityException("Only owners can manage menu customizations");
        }
        if (!store.getOwner().getId().equals(current.id())) {
            throw new SecurityException("Cannot manage another store's menu");
        }
    }

    private MenuItemOptionView toOptionView(final MenuItemOption option) {
        return new MenuItemOptionView(
                option.getId(),
                option.getName(),
                option.getDescription(),
                option.getIsRequired(),
                option.getAllowMultiple(),
                option.getMinSelections(),
                option.getMaxSelections(),
                option.getDisplayOrder(),
                choiceRepository.findByOptionIdOrderByDisplayOrderAsc(option.getId()).stream().map(this::toChoiceView).toList()
        );
    }

    private MenuItemChoiceView toChoiceView(final MenuItemChoice choice) {
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

    private MenuItemIngredientView toIngredientView(final MenuItemIngredient ingredient) {
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
