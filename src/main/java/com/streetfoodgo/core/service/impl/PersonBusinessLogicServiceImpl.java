package com.streetfoodgo.core.service.impl;

import com.streetfoodgo.core.model.Person;
import com.streetfoodgo.core.port.PhoneNumberPort;
import com.streetfoodgo.core.port.SmsNotificationPort;
import com.streetfoodgo.core.port.impl.dto.PhoneNumberValidationResult;
import com.streetfoodgo.core.repository.PersonRepository;
import com.streetfoodgo.core.service.PersonBusinessLogicService;
import com.streetfoodgo.core.service.mapper.PersonMapper;
import com.streetfoodgo.core.service.model.CreatePersonRequest;
import com.streetfoodgo.core.service.model.CreatePersonResult;
import com.streetfoodgo.core.service.model.PersonView;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Implementation of PersonBusinessLogicService for StreetFoodGo.
 */
@Service
public class PersonBusinessLogicServiceImpl implements PersonBusinessLogicService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonBusinessLogicServiceImpl.class);

    private final Validator validator;
    private final PasswordEncoder passwordEncoder;
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final PhoneNumberPort phoneNumberPort;
    private final SmsNotificationPort smsNotificationPort;

    public PersonBusinessLogicServiceImpl(
            final Validator validator,
            final PasswordEncoder passwordEncoder,
            final PersonRepository personRepository,
            final PersonMapper personMapper,
            final PhoneNumberPort phoneNumberPort,
            final SmsNotificationPort smsNotificationPort) {

        if (validator == null) throw new NullPointerException();
        if (passwordEncoder == null) throw new NullPointerException();
        if (personRepository == null) throw new NullPointerException();
        if (personMapper == null) throw new NullPointerException();
        if (phoneNumberPort == null) throw new NullPointerException();
        if (smsNotificationPort == null) throw new NullPointerException();

        this.validator = validator;
        this.passwordEncoder = passwordEncoder;
        this.personRepository = personRepository;
        this.personMapper = personMapper;
        this.phoneNumberPort = phoneNumberPort;
        this.smsNotificationPort = smsNotificationPort;
    }

    @Transactional
    @Override
    public CreatePersonResult createPerson(final CreatePersonRequest request, final boolean notify) {
        if (request == null) throw new NullPointerException();

        // Validate request
        final Set<ConstraintViolation<CreatePersonRequest>> violations = this.validator.validate(request);
        if (!violations.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            for (final ConstraintViolation<CreatePersonRequest> violation : violations) {
                sb.append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("\n");
            }
            return CreatePersonResult.fail(sb.toString());
        }

        // Unpack
        final String firstName = request.firstName().strip();
        final String lastName = request.lastName().strip();
        final String emailAddress = request.emailAddress().strip();
        String mobilePhoneNumber = request.mobilePhoneNumber().strip();
        final String rawPassword = request.rawPassword();

        // Validate phone number
        final PhoneNumberValidationResult phoneResult = this.phoneNumberPort.validate(mobilePhoneNumber);
        if (!phoneResult.isValidMobile()) {
            return CreatePersonResult.fail("Mobile Phone Number is not valid");
        }
        mobilePhoneNumber = phoneResult.e164();

        // Check duplicates
        if (this.personRepository.existsByEmailAddressIgnoreCase(emailAddress)) {
            return CreatePersonResult.fail("Email Address already registered");
        }

        if (this.personRepository.existsByMobilePhoneNumber(mobilePhoneNumber)) {
            return CreatePersonResult.fail("Mobile Phone Number already registered");
        }

        // Hash password
        final String hashedPassword = this.passwordEncoder.encode(rawPassword);

        // Create Person entity
        Person person = new Person();
        person.setId(null);
        person.setType(request.type());
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setEmailAddress(emailAddress);
        person.setMobilePhoneNumber(mobilePhoneNumber);
        person.setPasswordHash(hashedPassword);
        person.setCreatedAt(null);

        // Validate entity
        final Set<ConstraintViolation<Person>> personViolations = this.validator.validate(person);
        if (!personViolations.isEmpty()) {
            throw new RuntimeException("Invalid Person instance - programmer error");
        }

        // Save to database
        person = this.personRepository.save(person);

        // Send SMS notification
        if (notify) {
            final String content = String.format(
                    "Welcome to StreetFoodGo! Your account has been created. Use %s to log in.",
                    emailAddress
            );
            final boolean sent = this.smsNotificationPort.sendSms(mobilePhoneNumber, content);
            if (!sent) {
                LOGGER.warn("SMS notification failed for {}", mobilePhoneNumber);
            }
        }

        // Map to view
        final PersonView personView = this.personMapper.toView(person);

        return CreatePersonResult.success(personView);
    }
}