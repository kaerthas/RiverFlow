package com.riverflow.api.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置验证结果
 */
@Data
@AllArgsConstructor
public class ValidationResult {

    private boolean valid;
    private List<String> errors;

    public static ValidationResult success() {
        return new ValidationResult(true, new ArrayList<>());
    }

    public static ValidationResult fail(String error) {
        List<String> errors = new ArrayList<>();
        errors.add(error);
        return new ValidationResult(false, errors);
    }

    public static ValidationResult fail(List<String> errors) {
        return new ValidationResult(false, errors);
    }

    public ValidationResult addError(String error) {
        this.errors.add(error);
        this.valid = false;
        return this;
    }
}
