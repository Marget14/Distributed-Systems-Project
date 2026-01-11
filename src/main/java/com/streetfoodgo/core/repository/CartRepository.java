package com.streetfoodgo.core.repository;

import com.streetfoodgo.core.model.Cart;
import com.streetfoodgo.core.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByPerson(Person person);

    Optional<Cart> findBySessionId(String sessionId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.person = :person")
    Optional<Cart> findByPersonWithItems(Person person);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.sessionId = :sessionId")
    Optional<Cart> findBySessionIdWithItems(String sessionId);
}