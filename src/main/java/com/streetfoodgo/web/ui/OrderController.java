package com.streetfoodgo.web.ui;

import com.streetfoodgo.core.security.CurrentUserProvider;
import com.streetfoodgo.core.service.OrderBusinessLogicService;

import com.streetfoodgo.core.service.model.CompleteOrderRequest;
import com.streetfoodgo.core.service.model.OpenOrderRequest;
import com.streetfoodgo.core.service.model.OrderView;
import com.streetfoodgo.core.service.model.StartOrderRequest;
import com.streetfoodgo.web.ui.model.CompleteOrderForm;
import com.streetfoodgo.web.ui.model.OpenOrderForm;
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
 * UI controller for managing orders.
 */
@Controller
@RequestMapping("/orders")
public class OrderController {

    private final CurrentUserProvider currentUserProvider;
    private final OrderBusinessLogicService orderBusinessLogicService;

    public OrderController(final CurrentUserProvider currentUserProvider,
                           final OrderBusinessLogicService orderBusinessLogicService) {
        if (currentUserProvider == null) throw new NullPointerException();
        if (orderBusinessLogicService == null) throw new NullPointerException();

        this.currentUserProvider = currentUserProvider;
        this.orderBusinessLogicService = orderBusinessLogicService;
    }

    @GetMapping("")
    public String list(final Model model) {
        final List<OrderView> orderViewList = this.orderBusinessLogicService.getOrders();
        model.addAttribute("orders", orderViewList);
        return "orders";
    }

    @GetMapping("/{orderId}")
    public String detail(@PathVariable final Long orderId, final Model model) {
        final OrderView orderView = this.orderBusinessLogicService.getOrder(orderId).orElse(null);
        if (orderId == null) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(404), "Order not found");
        }
        final CompleteOrderForm completeOrderForm = new CompleteOrderForm("");
        model.addAttribute("order", orderView);
        model.addAttribute("completeOrderForm", completeOrderForm);
        return "order";
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/new")
    public String showOpenForm(final Model model) {
        // form initial data
        final OpenOrderForm openOrderForm = new OpenOrderForm(null, "", "");
        model.addAttribute("form", openOrderForm);
        return "new_order";
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/new")
    public String handleOpenForm(
        @ModelAttribute("form") @Valid final OpenOrderForm openOrderForm,
        final BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "new_order";
        }
        final OpenOrderRequest openOrderRequest = new OpenOrderRequest(
            this.currentUserProvider.requiredStudentId(), // The current user must be Customer. We need their ID.
            openOrderForm.waiterId(),
            openOrderForm.subject(),
            openOrderForm.customerContent()
        );
        final OrderView orderView = this.orderBusinessLogicService.openOrder(openOrderRequest);
        return "redirect:/orders/" + orderView.id();
    }

    @PreAuthorize("hasRole('WAITER')")
    @PostMapping("/{orderId}/start")
    public String handleStartForm(@PathVariable final Long orderId) {
        final StartOrderRequest startOrderRequest = new StartOrderRequest(orderId);
        final OrderView orderView = this.orderBusinessLogicService.startOrder(startOrderRequest);
        return "redirect:/orders/" + orderView.id();
    }

    @PreAuthorize("hasRole('WAITER')")
    @PostMapping("/{orderId}/complete")
    public String handleCompleteForm(
        @PathVariable final Long orderId,
        @ModelAttribute("form") final CompleteOrderForm completeOrderForm,
        final BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "order";
        }
        final CompleteOrderRequest completeOrderRequest = new CompleteOrderRequest(
            orderId,
            completeOrderForm.waiterContent()
        );
        final OrderView orderView = this.orderBusinessLogicService.completeOrder(completeOrderRequest);
        return "redirect:/orders/" + orderView.id();
    }
}
