package com.mujin.commons.web.utils;


import com.mujin.commons.lang.exception.BusinessException;
import com.mujin.commons.web.enums.error.DataError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.HibernateValidator;

import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * validator 校验框架工具用于上传文件时判断必填项
 *
 * @author chenglin.wu
 * @date 2026/05/06
 */
public final class ValidatorUtils {
    /**
     * 快速验证
     */
    private final static Validator VALIDATOR_FAST;
    /**
     * 验证所有的信息
     */
    private final static Validator VALIDATOR_ALL;

    static {
        try (ValidatorFactory validatorFactoryFast = Validation.byProvider(HibernateValidator.class).configure().failFast(true).buildValidatorFactory();
             ValidatorFactory validatorFactoryAll = Validation.byProvider(HibernateValidator.class).configure().failFast(false).buildValidatorFactory()) {
            VALIDATOR_FAST = validatorFactoryFast.getValidator();
            VALIDATOR_ALL = validatorFactoryAll.getValidator();
        }
    }

    private ValidatorUtils() {
    }

    /**
     * 校验遇到第一个不合法的字段直接返回不合法字段，后续字段不再校验
     *
     * @param domain 检验的数据
     * @throws Exception 检验异常
     * @date 2026/05/06
     */
    public static <T> void validateFast(T domain, Class<?>... groups) throws Exception {
        Set<ConstraintViolation<T>> validateResult = VALIDATOR_FAST.validate(domain, groups);
        if (!validateResult.isEmpty()) {
            throw new BusinessException(validateResult.iterator().next().getMessage());
        }
    }

    /**
     * 校验遇到第一个不合法的字段直接返回不合法字段，后续字段不再校验
     *
     * @param domains 检验的数据集合
     * @throws Exception 检验异常
     * @date 2026/05/06
     */
    public static <T> void validateFast(List<T> domains, Class<?>... groups) throws Exception {
        for (T domain : domains) {
            Set<ConstraintViolation<T>> validateResult = VALIDATOR_FAST.validate(domain, groups);
            if (!validateResult.isEmpty()) {
                throw new BusinessException(DataError.DATA_CHECK, validateResult.iterator().next().getMessage());
            }
        }

    }

    /**
     * 校验所有字段并返回不合法字段
     *
     * @param domain 任何需要检验的数据
     * @throws Exception 检验异常
     * @date 2026/05/06
     */
    public static <T> Set<ConstraintViolation<T>> validateAll(T domain, Class<?>... groups) throws Exception {
        Set<ConstraintViolation<T>> validateResult = VALIDATOR_ALL.validate(domain, groups);
        if (!validateResult.isEmpty()) {
            Iterator<ConstraintViolation<T>> it = validateResult.iterator();
            StringBuilder stringBuilder = new StringBuilder();
            while (it.hasNext()) {
                ConstraintViolation<T> cv = it.next();
                stringBuilder.append(cv.getMessage());
            }
            throw new BusinessException(DataError.DATA_CHECK, stringBuilder.toString());
        }
        return validateResult;
    }

    /**
     * 校验所有字段并返回不合法字段
     *
     * @param domains 任何需要检验的数据集合
     * @throws Exception 检验异常
     * @date 2026/05/06
     */
    public static <T> void validateAll(List<T> domains, Class<?>... groups) throws Exception {
        for (T domain : domains) {
            validateAll(domain,groups);
        }
    }


}
