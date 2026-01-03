package com.streetfoodgo.web.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for user profile page.
 */
@Controller
public class ProfileController {

    @GetMapping("/profile")
    public String showProfile() {
        return "profile";
    }
}