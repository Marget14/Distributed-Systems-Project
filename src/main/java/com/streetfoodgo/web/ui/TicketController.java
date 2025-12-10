package com.streetfoodgo.web.ui;

import com.streetfoodgo.core.security.CurrentUserProvider;
import com.streetfoodgo.core.service.TicketBusinessLogicService;
import com.streetfoodgo.core.service.model.CompleteTicketRequest;
import com.streetfoodgo.core.service.model.OpenTicketRequest;
import com.streetfoodgo.core.service.model.StartTicketRequest;
import com.streetfoodgo.core.service.model.TicketView;

import com.streetfoodgo.web.ui.model.CompleteTicketForm;
import com.streetfoodgo.web.ui.model.OpenTicketForm;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatusCode;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * UI controller for managing tickets.
 */
@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final CurrentUserProvider currentUserProvider;
    private final TicketBusinessLogicService ticketBusinessLogicService;

    public TicketController(final CurrentUserProvider currentUserProvider,
                            final TicketBusinessLogicService ticketBusinessLogicService) {
        if (currentUserProvider == null) throw new NullPointerException();
        if (ticketBusinessLogicService == null) throw new NullPointerException();

        this.currentUserProvider = currentUserProvider;
        this.ticketBusinessLogicService = ticketBusinessLogicService;
    }

    @GetMapping("")
    public String list(final Model model) {
        final List<TicketView> ticketViewList = this.ticketBusinessLogicService.getTickets();
        model.addAttribute("tickets", ticketViewList);
        return "tickets";
    }

    @GetMapping("/{ticketId}")
    public String detail(@PathVariable final Long ticketId, final Model model) {
        final TicketView ticketView = this.ticketBusinessLogicService.getTicket(ticketId).orElse(null);
        if (ticketId == null) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(404), "Ticket not found");
        }
        final CompleteTicketForm completeTicketForm = new CompleteTicketForm("");
        model.addAttribute("ticket", ticketView);
        model.addAttribute("completeTicketForm", completeTicketForm);
        return "ticket";
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/new")
    public String showOpenForm(final Model model) {
        // form initial data
        final OpenTicketForm openTicketForm = new OpenTicketForm(null, "", "");
        model.addAttribute("form", openTicketForm);
        return "new_ticket";
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/new")
    public String handleOpenForm(
            @ModelAttribute("form") @Valid final OpenTicketForm openTicketForm,
            final BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "new_ticket";
        }
        final OpenTicketRequest openTicketRequest = new OpenTicketRequest(
                this.currentUserProvider.requiredStudentId(), // The current user must be Student. We need their ID.
                openTicketForm.teacherId(),
                openTicketForm.subject(),
                openTicketForm.studentContent()
        );
        final TicketView ticketView = this.ticketBusinessLogicService.openTicket(openTicketRequest);
        return "redirect:/tickets/" + ticketView.id();
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/{ticketId}/start")
    public String handleStartForm(@PathVariable final Long ticketId) {
        final StartTicketRequest startTicketRequest = new StartTicketRequest(ticketId);
        final TicketView ticketView = this.ticketBusinessLogicService.startTicket(startTicketRequest);
        return "redirect:/tickets/" + ticketView.id();
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/{ticketId}/complete")
    public String handleCompleteForm(
            @PathVariable final Long ticketId,
            @ModelAttribute("form") final CompleteTicketForm completeTicketForm,
            final BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "ticket";
        }
        final CompleteTicketRequest completeTicketRequest = new CompleteTicketRequest(
                ticketId,
                completeTicketForm.teacherContent()
        );
        final TicketView ticketView = this.ticketBusinessLogicService.completeTicket(completeTicketRequest);
        return "redirect:/tickets/" + ticketView.id();
    }
}
