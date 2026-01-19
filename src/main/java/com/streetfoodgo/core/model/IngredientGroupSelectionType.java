package com.streetfoodgo.core.model;

/**
 * Selection type for ingredient groups.
 * Determines how customers can select ingredients from a group.
 */
public enum IngredientGroupSelectionType {

    /**
     * Customer must select exactly one ingredient (e.g., size selection).
     */
    SINGLE_SELECT,

    /**
     * Customer can select multiple ingredients (e.g., toppings, extras).
     */
    MULTI_SELECT,

    /**
     * Customer can choose to remove ingredients (e.g., "No onions", "No pickles").
     */
    REMOVAL
}
