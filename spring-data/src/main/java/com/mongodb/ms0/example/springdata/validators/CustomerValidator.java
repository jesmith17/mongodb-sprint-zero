package com.mongodb.ms0.example.springdata.validators;

import com.mongodb.ms0.example.springdata.models.Customer;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class CustomerValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Customer.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmpty(errors, "firstName", "firstName.empty");
        ValidationUtils.rejectIfEmpty(errors, "lastName", "lastName.empty");
        ValidationUtils.rejectIfEmpty(errors, "title", "title.empty");
        ValidationUtils.rejectIfEmpty(errors, "address", "address.empty");
        Customer cust = (Customer) target;
        if (cust.getPhones() == null || cust.getPhones().size() < 1){
            errors.rejectValue("phones", "Empty Phones");
        }

    }
}
