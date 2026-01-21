package com.streetfoodgo.core.repository;

import com.streetfoodgo.core.model.Ingredient;
import com.streetfoodgo.core.model.IngredientGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link Ingredient} entity.
 * Handles ingredient management and lookup.
 */
@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    /**
     * Find all ingredients in a group.
     * Used for customization modal display.
     */
    List<Ingredient> findByIngredientGroupOrderByDisplayOrderAsc(IngredientGroup ingredientGroup);

    /**
     * Find available ingredients in a group.
     * Used to hide out-of-stock options.
     */
    List<Ingredient> findByIngredientGroupAndIsAvailableTrueOrderByDisplayOrderAsc(IngredientGroup ingredientGroup);

    /**
     * Find default ingredients in a group.
     * Used for pre-selection in customization modal.
     */
    List<Ingredient> findByIngredientGroupAndIsDefaultTrue(IngredientGroup ingredientGroup);

    /**
     * Count available ingredients in a group.
     * Used to validate selection count.
     */
    long countByIngredientGroupAndIsAvailableTrue(IngredientGroup ingredientGroup);

    /**
     * Find ingredients by group ID.
     * Used when group object is not loaded.
     */
    List<Ingredient> findByIngredientGroupIdOrderByDisplayOrderAsc(Long ingredientGroupId);
}
