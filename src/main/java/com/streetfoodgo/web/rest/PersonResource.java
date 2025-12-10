package com.streetfoodgo.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.streetfoodgo.core.service.PersonDataService;
import com.streetfoodgo.core.service.model.PersonView;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing {@code Person} resource.
 */
@RestController
@RequestMapping(value = "/api/v1/person", produces = MediaType.APPLICATION_JSON_VALUE)
public class PersonResource {

    private final PersonDataService personDataService;

    public PersonResource(final PersonDataService personDataService) {
        if (personDataService == null) throw new NullPointerException();
        this.personDataService = personDataService;
    }

    @PreAuthorize("hasRole('INTEGRATION_READ')")
    @GetMapping("")
    public List<PersonView> people() {
        final List<PersonView> personViewList = this.personDataService.getAllPeople();
        return personViewList;
    }
}
