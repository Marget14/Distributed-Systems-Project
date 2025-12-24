package com.streetfoodgo.core.service;

import com.streetfoodgo.core.model.Client;
import com.streetfoodgo.core.model.PersonType;
import com.streetfoodgo.core.repository.ClientRepository;
import com.streetfoodgo.core.service.model.CreatePersonRequest;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Initializes application.
 */
@Service
public class InitializationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitializationService.class);

    private final ClientRepository clientRepository;
    private final PersonBusinessLogicService personBusinessLogicService;
    private final OrderBusinessLogicService orderBusinessLogicService;
    private final AtomicBoolean initialized;

    public InitializationService(final ClientRepository clientRepository,
                                 final PersonBusinessLogicService personBusinessLogicService,
                                 final OrderBusinessLogicService orderBusinessLogicService) {
        if (clientRepository == null) throw new NullPointerException();
        if (personBusinessLogicService == null) throw new NullPointerException();
        if (orderBusinessLogicService == null) throw new NullPointerException();
        this.clientRepository = clientRepository;
        this.personBusinessLogicService = personBusinessLogicService;
        this.orderBusinessLogicService = orderBusinessLogicService;
        this.initialized = new AtomicBoolean(false);
    }

    @PostConstruct
    public void populateDatabaseWithInitialData() {
        final boolean alreadyInitialized = this.initialized.getAndSet(true);
        if (alreadyInitialized) {
            LOGGER.warn("Database initialization skipped: initial data has already been populated.");
            return;
        }
        LOGGER.info("Starting database initialization with initial data...");
        final List<Client> clientList = List.of(
                new Client(null, "client01", "s3cr3t", "INTEGRATION_READ,INTEGRATION_WRITE"),
                new Client(null, "client02", "s3cr3t", "INTEGRATION_READ")
        );
        this.clientRepository.saveAll(clientList);
        final List<CreatePersonRequest> createPersonRequestList = List.of(
                // User with ID 1
                new CreatePersonRequest(
                        PersonType.WAITER,
                        "t0001",
                        "Dimitris",
                        "Margetis",
                        "it2023141@hua.gr",
                        "+306976679684",
                        "1234"
                ),
                // User with ID 2
                new CreatePersonRequest(
                        PersonType.CUSTOMER,
                        "it2023089",
                        "Test 1",
                        "Test 1",
                        "it2023089@hua.gr",
                        "+306900000001",
                        "1234"
                ),
                // User with ID 3
                new CreatePersonRequest(
                        PersonType.STUDENT,
                        "it2023124",
                        "Test 2",
                        "Test 2",
                        "it2023124@hua.gr",
                        "+306900000002",
                        "1234"
                )
        );
        for (final var createPersonRequest : createPersonRequestList) {
            this.personBusinessLogicService.createPerson(createPersonRequest, false); // do not send SMS
        }
        // TODO Not working: requires authenticated user!

        /*
        final List<OpenTicketRequest> openTicketRequestList = List.of(
            new OpenTicketRequest(
                2L,
                1L,
                "Test Subject",
                "Test Student Content"
            )
        );
        for (final var openTicketRequest : openTicketRequestList) {
            this.ticketService.openTicket(openTicketRequest, false); // do not send SMS
        }
        */
        LOGGER.info("Database initialization completed successfully.");
    }
}
