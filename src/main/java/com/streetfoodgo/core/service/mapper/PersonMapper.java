package com.streetfoodgo.core.service.mapper;

import com.streetfoodgo.core.model.Person;
import com.streetfoodgo.core.service.model.PersonView;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert Person entity to PersonView DTO.
 */
@Component
public class PersonMapper {

    public PersonView toView(final Person person) {
        if (person == null) return null;

        return new PersonView(
                person.getId(),
                person.getFirstName(),
                person.getLastName(),
                person.getMobilePhoneNumber(),
                person.getEmailAddress(),
                person.getType(),
                person.getCreatedAt(),
                person.getEmailVerified()
        );
    }
}