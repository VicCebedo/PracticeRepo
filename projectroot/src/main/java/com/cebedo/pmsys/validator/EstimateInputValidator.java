package com.cebedo.pmsys.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import com.cebedo.pmsys.bean.EstimateComputationInputBean;
import com.cebedo.pmsys.constants.RegistryResponseMessage;

@Component
public class EstimateInputValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
	return EstimateComputationInputBean.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
	EstimateComputationInputBean targetObj = (EstimateComputationInputBean) target;
	MultipartFile file = targetObj.getEstimationFile();
	// If file is null, or file is empty.
	if (file == null || file.isEmpty()) {
	    errors.reject("", RegistryResponseMessage.ERROR_PROJECT_ESTIMATION_EMPTY_FILE);
	}
	// TODO Handle case when other file types are uploaded.
	// Filter only Excel files.
	// TODO Test if code works in *.xlsx
    }

}
