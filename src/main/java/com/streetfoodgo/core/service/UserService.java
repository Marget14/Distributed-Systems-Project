package com.streetfoodgo.core.service;

import com.streetfoodgo.web.rest.model.UserInfo;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    // Αυτή η μέθοδος πρέπει να επιστρέφει UserInfo για να μην βγάζει λάθος ο Controller
    public UserInfo getUserInfo(String email) {
        return new UserInfo(email, "ROLE_ADMIN");
    }
}
