package com.streetfoodgo.web.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for user profile page.
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    @GetMapping("/profile")
    public String showProfile() {
        return "/profile/profile";
    }

    @GetMapping("/edit")
    public String editProfile(Model model) {
        return "/profile/edit";
    }
}