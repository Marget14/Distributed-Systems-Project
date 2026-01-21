package com.streetfoodgo.web.rest;

import com.streetfoodgo.core.service.PersonDataService;
import com.streetfoodgo.core.service.model.PersonView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing {@code Person} resource.
 * Requires INTEGRATION_READ role for access.
 */
@RestController
@RequestMapping(value = "/api/v1/person", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Person Management", description = "APIs for retrieving person data (integration endpoints)")
public class PersonResource {

    private final PersonDataService personDataService;

    public PersonResource(final PersonDataService personDataService) {
        if (personDataService == null) throw new NullPointerException();
        this.personDataService = personDataService;
    }

    /**
     * Retrieve all persons in the system.
     *
     * @return list of all persons
     */
    @PreAuthorize("hasRole('INTEGRATION_READ')")
    @GetMapping
    @Operation(summary = "Get all persons",
               description = "Retrieve a list of all persons in the system. Requires INTEGRATION_READ role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved persons",
                     content = @Content(mediaType = "application/json", schema = @Schema(type = "array"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - user lacks INTEGRATION_READ role")
    })
    public List<PersonView> getAllPersons() {
        return this.personDataService.getAllPeople();
    }
}
