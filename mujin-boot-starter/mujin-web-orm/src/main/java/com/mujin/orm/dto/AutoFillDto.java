package com.mujin.orm.dto;


import cn.hutool.core.util.ObjectUtil;
import lombok.Data;

/**
 * 自动注入的配置信息
 *
 * @author chenglin.wu
 * @date 2025/12/27 21:31
 */
@Data
public class AutoFillDto {
    /**
     * 自动注入的列名
     */
    private String fillColumnName;
    /**
     * 自动注入的值
     */
    private Object fillVal;
    /**
     * 自动注入数据的class对象
     */
    private Class<Object> fillClass;

    /**
     * value值是否能为null
     */
    private boolean nullVal;


    private AutoFillDto() {
    }

    public static Builder builder() {
        return new Builder(new AutoFillDto());
    }


    public static class Builder {

        private final AutoFillDto fillDto;

        public Builder(AutoFillDto fillDto) {
            this.fillDto = fillDto;
        }

        public Builder fillColumnName(String fillColumnName) {
            this.fillDto.fillColumnName = fillColumnName;
            return this;
        }

        public Builder fillVal(Object fillVal) {
            this.fillDto.fillVal = fillVal;
            return this;
        }

        @SuppressWarnings("ALL")
        public Builder fillClass(Class fillClass) {
            this.fillDto.fillClass = fillClass;
            return this;
        }

        public Builder nullVal(boolean nullVal) {
            this.fillDto.nullVal = nullVal;
            return this;
        }

        public AutoFillDto build() {
            if (ObjectUtil.hasNull(this.fillDto.fillClass, this.fillDto.fillColumnName) && !this.fillDto.nullVal && ObjectUtil.hasNull(this.fillDto.fillVal)) {
                throw new RuntimeException("create fill dto fill,please check your handler,column name is : " + this.fillDto.fillColumnName);
            }
            return this.fillDto;
        }
    }

}
