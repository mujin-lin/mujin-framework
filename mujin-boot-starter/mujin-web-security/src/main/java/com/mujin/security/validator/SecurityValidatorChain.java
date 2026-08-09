package com.mujin.security.validator;

import com.mujin.security.validator.context.AfterHandlerValidatorContext;
import com.mujin.security.validator.context.PreHandleValidatorContext;

import java.util.Objects;

/**
 * validator 的链
 *
 * @author chenglin.wu
 * @date 2025/12/12
 */
public class SecurityValidatorChain implements SecurityValidator {
    /**
     * 下一个链节点
     */
    private SecurityValidatorChain nextValidatorChain;
    /**
     * 当前验证器
     */
    private SecurityValidator validator;
    /**
     * 当前类是否有验证器
     */
    private boolean hasValidator;

    public SecurityValidatorChain() {
        this.addValidator(null);
    }

    @Override
    public void validateBefore(PreHandleValidatorContext context) {
        if (hasValidator) {
            this.validator.validateBefore(context);
        }
        if (Objects.nonNull(this.nextValidatorChain)) {
            this.nextValidatorChain.validateBefore(context);
        }
    }

    @Override
    public void validateAfter(AfterHandlerValidatorContext context) {
        if (Objects.nonNull(this.nextValidatorChain)) {
            this.nextValidatorChain.validateAfter(context);
        }
        if (hasValidator) {
            this.validator.validateAfter(context);
        }
    }

    /**
     * 添加验证器
     *
     * @param validator 验证器
     * @date 2025/12/12
     */
    protected void addValidator(SecurityValidator validator) {
        this.validator = validator;
        this.hasValidator = Objects.nonNull(validator);
    }

    /**
     * 添加下一个节点
     *
     * @param chain 下一个节点
     * @date 2025/12/12
     */
    protected void addNext(SecurityValidatorChain chain) {
        this.nextValidatorChain = chain;
    }


}
