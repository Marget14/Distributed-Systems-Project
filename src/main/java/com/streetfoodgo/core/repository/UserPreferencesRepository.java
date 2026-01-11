package com.streetfoodgo.core.repository;

import com.streetfoodgo.core.model.Person;
import com.streetfoodgo.core.model.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {

    Optional<UserPreferences> findByPerson(Person person);

    Optional<UserPreferences> findByPersonId(Long personId);
}