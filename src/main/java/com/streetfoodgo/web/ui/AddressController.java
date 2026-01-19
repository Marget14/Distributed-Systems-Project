package com.streetfoodgo.web.ui;

import com.streetfoodgo.core.security.CurrentUserProvider;
import com.streetfoodgo.core.service.DeliveryAddressService;
import com.streetfoodgo.core.service.model.CreateDeliveryAddressRequest;
import com.streetfoodgo.core.service.model.DeliveryAddressView;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing delivery addresses.
 */
@Controller
@RequestMapping("/profile/addresses")
@PreAuthorize("hasRole('CUSTOMER')")
public class AddressController {

    private final DeliveryAddressService deliveryAddressService;
    private final CurrentUserProvider currentUserProvider;

    public AddressController(
            final DeliveryAddressService deliveryAddressService,
            final CurrentUserProvider currentUserProvider) {

        if (deliveryAddressService == null) throw new NullPointerException();
        if (currentUserProvider == null) throw new NullPointerException();

        this.deliveryAddressService = deliveryAddressService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public String listAddresses(final Model model) {
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        final List<DeliveryAddressView> addresses =
                this.deliveryAddressService.getCustomerAddresses(currentUser.id());

        model.addAttribute("addresses", addresses);
        return "profile/addresses";
    }

    @GetMapping("/new")
    public String showCreateForm(@RequestParam(required = false) String redirect, final Model model) {
        model.addAttribute("addressForm", new CreateDeliveryAddressRequest("", "", "", "", "", null, null));
        if (redirect != null) {
            model.addAttribute("redirectUrl", redirect);
        }
        return "profile/address-form";
    }

    @PostMapping
    public String createAddress(
            @RequestParam(required = false) String redirectUrl,
            @Valid @ModelAttribute("addressForm") CreateDeliveryAddressRequest request,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            if (redirectUrl != null) {
                model.addAttribute("redirectUrl", redirectUrl);
            }
            return "profile/address-form";
        }

        this.deliveryAddressService.createAddress(request);

        if (redirectUrl != null && !redirectUrl.isBlank()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/profile/addresses";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, final Model model) {
        final DeliveryAddressView address = this.deliveryAddressService.getAddress(id)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        final CreateDeliveryAddressRequest form = new CreateDeliveryAddressRequest(
                address.label(),
                address.street(),
                address.city(),
                address.postalCode(),
                address.phoneNumber(),
                address.latitude(),
                address.longitude()
        );

        model.addAttribute("addressForm", form);
        model.addAttribute("addressId", id);
        return "profile/address-form";
    }

    @PostMapping("/{id}")
    public String updateAddress(
            @PathVariable Long id,
            @Valid @ModelAttribute("addressForm") CreateDeliveryAddressRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "profile/address-form";
        }

        this.deliveryAddressService.updateAddress(id, request);
        return "redirect:/profile/addresses";
    }

    @PostMapping("/{id}/set-default")
    public String setDefault(@PathVariable Long id) {
        this.deliveryAddressService.setDefaultAddress(id);
        return "redirect:/profile/addresses";
    }

    @PostMapping("/{id}/delete")
    public String deleteAddress(@PathVariable Long id) {
        this.deliveryAddressService.deleteAddress(id);
        return "redirect:/profile/addresses";
    }
}