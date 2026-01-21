package com.streetfoodgo.web.ui;

import com.streetfoodgo.core.model.Person;
import com.streetfoodgo.core.repository.PersonRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class VerificationController {

    private final PersonRepository personRepository;

    public VerificationController(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @GetMapping("/auth/verify")
    public String verifyEmail(@RequestParam("token") String token, Model model) {
        Optional<Person> personOpt = personRepository.findByVerificationToken(token);

        if (personOpt.isPresent()) {
            Person person = personOpt.get();
            if (!person.getEmailVerified()) {
                person.setEmailVerified(true);
                person.setVerificationToken(null); // Consume token
                personRepository.save(person);
                model.addAttribute("success", "Email verified successfully! You can now login.");
            } else {
                model.addAttribute("info", "Email is already verified.");
            }
        } else {
            model.addAttribute("error", "Invalid verification token.");
        }

        return "auth/login";
    }
}
