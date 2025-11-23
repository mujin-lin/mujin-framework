
package com.mujin.commons.lang.jsonserial;


import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.mujin.commons.lang.constants.DateConstants;
import com.mujin.commons.lang.constants.IntConstants;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 自定义反序列化器
 *
 * @author chenglin.wu
 * @date 2021/4/19
 */
@SuppressWarnings("unused")
public class DateDeserializer extends JsonDeserializer<Date> {

    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        if (StrUtil.isBlank(jp.getText())) {
            return null;
        }

        String strDate = jp.getText().trim();
        Date dtDate;

        try {
            // 初始化大写T和Z
            char capitalT = IntConstants.INT_84;
            char capitalZ = IntConstants.INT_90;
            // 初始化短横线
            char hyphen = IntConstants.INT_45;
            // 初始化冒号
            char doppelpunkt = IntConstants.INT_58;
            // 初始化斜线
            char diagonal = IntConstants.INT_47;

            // 格式化器
            SimpleDateFormat formatter;
            if (strDate.contains(String.valueOf(capitalT)) && strDate.endsWith(String.valueOf(capitalZ))) {
                formatter = new SimpleDateFormat(DateConstants.DATE_TIME_UTC_FORMAT);
                dtDate = formatter.parse(strDate.replace(String.valueOf(capitalZ), DateConstants.UTC_STR));
                return dtDate;
            } else if (strDate.contains(String.valueOf(hyphen))) {
                if (strDate.contains(String.valueOf(doppelpunkt))) {
                    formatter = new SimpleDateFormat(DateConstants.DEFAULT_DATE_TIME_FORMAT);
                } else {
                    formatter = new SimpleDateFormat(DateConstants.DEFAULT_DATE_FORMAT);
                }
                dtDate = formatter.parse(strDate);
                return dtDate;
            } else if (strDate.contains(String.valueOf(diagonal))) {
                if (strDate.contains(String.valueOf(doppelpunkt))) {
                    formatter = new SimpleDateFormat(DateConstants.DATE_TIME_OBLIQUE_FORMAT);
                } else {
                    formatter = new SimpleDateFormat(DateConstants.DATE_OBLIQUE_FORMAT);
                }
                dtDate = formatter.parse(strDate);
                return dtDate;
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("parser %s to Date fail", strDate));
        }

        throw new RuntimeException(String.format("parser %s to Date fail", strDate));
    }

}
