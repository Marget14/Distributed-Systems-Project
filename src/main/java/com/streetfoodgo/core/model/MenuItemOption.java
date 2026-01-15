package com.streetfoodgo.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customization option group for a menu item.
 * Example: "Size", "Toppings", "Sauce Choice"
 */
@Entity
@Table(
        name = "menu_item_option",
        indexes = {
                @Index(name = "idx_option_menu_item", columnList = "menu_item_id")
        }
)
public class MenuItemOption {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_option_menu_item"))
    private MenuItem menuItem;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name; // e.g., "Size", "Extras", "Sauce"

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    @NotNull
    @Column(name = "allow_multiple", nullable = false)
    private Boolean allowMultiple = false; // Can select multiple choices

    @Column(name = "min_selections")
    private Integer minSelections = 0;

    @Column(name = "max_selections")
    private Integer maxSelections = 1;

    @NotNull
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<MenuItemChoice> choices = new ArrayList<>();

    // Constructors
    public MenuItemOption() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean required) {
        isRequired = required;
    }

    public Boolean getAllowMultiple() {
        return allowMultiple;
    }

    public void setAllowMultiple(Boolean allowMultiple) {
        this.allowMultiple = allowMultiple;
    }

    public Integer getMinSelections() {
        return minSelections;
    }

    public void setMinSelections(Integer minSelections) {
        this.minSelections = minSelections;
    }

    public Integer getMaxSelections() {
        return maxSelections;
    }

    public void setMaxSelections(Integer maxSelections) {
        this.maxSelections = maxSelections;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public List<MenuItemChoice> getChoices() {
        return choices;
    }

    public void setChoices(List<MenuItemChoice> choices) {
        this.choices = choices;
    }

    public void addChoice(MenuItemChoice choice) {
        choices.add(choice);
        choice.setOption(this);
    }

    public void removeChoice(MenuItemChoice choice) {
        choices.remove(choice);
        choice.setOption(null);
    }
}
