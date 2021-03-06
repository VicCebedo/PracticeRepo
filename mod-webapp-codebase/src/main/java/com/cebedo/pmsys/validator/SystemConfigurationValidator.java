package com.cebedo.pmsys.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.cebedo.pmsys.helper.ValidationHelper;
import com.cebedo.pmsys.model.SystemConfiguration;

@Component
public class SystemConfigurationValidator implements Validator {

    private ValidationHelper validationHelper = new ValidationHelper();

    @Override
    public boolean supports(Class<?> clazz) {
	return SystemConfiguration.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
	if (errors.hasErrors()) {
	    this.validationHelper.rejectMissingRequiredFields(errors);
	    return;
	}
	SystemConfiguration systemConfiguration = (SystemConfiguration) target;

	String name = systemConfiguration.getName();
	String value = systemConfiguration.getValue();

	if (!this.validationHelper.stringLengthIsLessThanMax(name, 32)) {
	    this.validationHelper.rejectGreaterThanMaxLength(errors, "name", 32);
	}
	if (!this.validationHelper.stringLengthIsLessThanMax(value, 255)) {
	    this.validationHelper.rejectGreaterThanMaxLength(errors, "value", 255);
	}
	// You cannot create a configuration with an empty name.
	if (this.validationHelper.stringIsBlank(name)) {
	    this.validationHelper.rejectInvalidProperty(errors, "name");
	}
    }

}
