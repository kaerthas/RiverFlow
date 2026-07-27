package com.riverflow.api.modules.workflow.validate;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 流程校验结果
 */
@Data
public class FlowValidationResult {

    /**
     * 是否通过（无 error 级别错误）
     */
    private boolean valid;

    /**
     * 错误信息（阻断保存/发布）
     */
    private List<String> errors = new ArrayList<>();

    /**
     * 警告信息（可忽略，但建议处理）
     */
    private List<String> warnings = new ArrayList<>();

    public static FlowValidationResult ok() {
        FlowValidationResult result = new FlowValidationResult();
        result.setValid(true);
        return result;
    }

    public static FlowValidationResult ofErrors(List<String> errors) {
        FlowValidationResult result = new FlowValidationResult();
        result.setValid(errors == null || errors.isEmpty());
        if (errors != null) {
            result.getErrors().addAll(errors);
        }
        return result;
    }

    public void addError(String error) {
        if (error != null && !error.isEmpty()) {
            this.errors.add(error);
            this.valid = false;
        }
    }

    public void addWarning(String warning) {
        if (warning != null && !warning.isEmpty()) {
            this.warnings.add(warning);
        }
    }

    public void merge(FlowValidationResult other) {
        if (other == null) {
            return;
        }
        if (!other.isValid()) {
            this.valid = false;
        }
        this.errors.addAll(other.getErrors());
        this.warnings.addAll(other.getWarnings());
    }
}
