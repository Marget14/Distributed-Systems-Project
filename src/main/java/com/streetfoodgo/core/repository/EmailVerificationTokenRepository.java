package com.streetfoodgo.core.repository;

import com.streetfoodgo.core.model.EmailVerificationToken;
import com.streetfoodgo.core.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for EmailVerificationToken entity.
 */
@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findByPerson(Person person);

    void deleteByPerson(Person person);
}
