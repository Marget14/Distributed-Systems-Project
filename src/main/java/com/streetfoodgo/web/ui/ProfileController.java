package com.streetfoodgo.web.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    @GetMapping("/edit")
    public String editProfile(Model model) {
        return "profile/edit";
    }
}