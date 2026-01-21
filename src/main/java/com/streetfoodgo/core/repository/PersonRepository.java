package com.streetfoodgo.core.repository;

import com.streetfoodgo.core.model.Person;
import com.streetfoodgo.core.model.PersonType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Person} entity.
 */
@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    Optional<Person> findByEmailAddressIgnoreCase(String emailAddress);

    List<Person> findAllByTypeOrderByLastName(PersonType type);

    boolean existsByEmailAddressIgnoreCase(String emailAddress);

    boolean existsByMobilePhoneNumber(String mobilePhoneNumber);

    Optional<Person> findByVerificationToken(String verificationToken);
}