package com.streetfoodgo.core.service;

import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

@Service
public class PersonDataService {

    public List<PersonView> getAllPeople() {
        // Επιστρέφουμε μια άδεια λίστα για να μην κρασάρει ο Controller
        return Collections.emptyList();
    }
}
