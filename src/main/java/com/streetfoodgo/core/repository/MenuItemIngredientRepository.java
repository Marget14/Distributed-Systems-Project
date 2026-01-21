package com.streetfoodgo.core.repository;

import com.streetfoodgo.core.model.MenuItemIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for MenuItemIngredient entity.
 */
@Repository
public interface MenuItemIngredientRepository extends JpaRepository<MenuItemIngredient, Long> {

    List<MenuItemIngredient> findByMenuItemIdOrderByDisplayOrderAsc(Long menuItemId);
}
