package com.streetfoodgo.core.repository;

import com.streetfoodgo.core.model.MenuItemChoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for MenuItemChoice entity.
 */
@Repository
public interface MenuItemChoiceRepository extends JpaRepository<MenuItemChoice, Long> {

    List<MenuItemChoice> findByOptionIdOrderByDisplayOrderAsc(Long optionId);
}
