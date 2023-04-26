package org.broadleafcommerce.openadmin.client.validation;

import com.smartgwt.client.widgets.form.validator.RegExpValidator;
import com.smartgwt.client.widgets.form.validator.Validator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationFactoryManager extends ArrayList<ValidationFactory> {

    private static final long serialVersionUID = 1L;

    public static final Map<String, Map<String, String>> EMAIL_CONFIG = new HashMap<String, Map<String, String>>();

    static {
        Map<String, String> items = new HashMap<String, String>();
        items.put("regularExpression", "^([a-zA-Z0-9_.\\-+])+@(([a-zA-Z0-9\\-])+\\.)+[a-zA-Z0-9]{2,4}$");
        items.put("errorMessage", "Invalid email address");
        EMAIL_CONFIG.put(RegExpValidator.class.getName(), items);
    }

    public static final Map<String, Map<String, String>> US_CURRENCY_CONFIG = new HashMap<String, Map<String, String>>();

    static {
        Map<String, String> items = new HashMap<String, String>();
        items.put("regularExpression", "^\\d+(\\.\\d{1,2})?$");
        items.put("errorMessage", "Invalid currency amount");
        US_CURRENCY_CONFIG.put(RegExpValidator.class.getName(), items);
    }

    private static ValidationFactoryManager manager = null;

    public static ValidationFactoryManager getInstance() {
        if (manager == null) {
            ValidationFactoryManager.manager = new ValidationFactoryManager();
            ValidationFactoryManager.manager.add(new PasswordMatchValidationFactory());
            ValidationFactoryManager.manager.add(new FieldMatchValidationFactory());
        }
        return ValidationFactoryManager.manager;
    }

    protected ValidationFactory defaultFactory = new BroadleafDefaultValidationFactory();

    public Validator[] createValidators(Map<String, Map<String, String>> validatorConfiguration, String fieldName) {
        List<Validator> validators = new ArrayList<Validator>();
        for (String validationClassname : validatorConfiguration.keySet()) {
            boolean factoryFound = false;
            for (ValidationFactory factory : this) {
                if (factory.isValidFactory(validationClassname, validatorConfiguration.get(validationClassname))) {
                    Validator validator = factory.createValidator(validationClassname, validatorConfiguration.get(validationClassname), fieldName);
                    validators.add(validator);
                    factoryFound = true;
                }
            }
            if (!factoryFound) {
                Validator validator = defaultFactory.createValidator(validationClassname, validatorConfiguration.get(validationClassname), fieldName);
                validators.add(validator);
            }
        }
        return validators.toArray(new Validator[] {});
    }

    public ValidationFactory getDefaultFactory() {
        return defaultFactory;
    }

    public void setDefaultFactory(ValidationFactory defaultFactory) {
        this.defaultFactory = defaultFactory;
    }
}