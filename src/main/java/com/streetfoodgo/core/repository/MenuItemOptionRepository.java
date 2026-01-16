package com.streetfoodgo.core.repository;

import com.streetfoodgo.core.model.MenuItemOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for MenuItemOption entity.
 */
@Repository
public interface MenuItemOptionRepository extends JpaRepository<MenuItemOption, Long> {

    List<MenuItemOption> findByMenuItemIdOrderByDisplayOrderAsc(Long menuItemId);
}
