
package com.mujin.commons.lang.jsonserial;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mujin.commons.lang.constants.DateConstants;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 自定义日期类型序列化器
 *
 * @author chenglin.wu
 * @date 2021/4/19
 */
@SuppressWarnings("unused")
public class CustomDateSerializer extends JsonSerializer<Date> {

    /**
     * 日期格式化
     */
    private final SimpleDateFormat sdf = new SimpleDateFormat(DateConstants.DEFAULT_DATE_FORMAT);


    @Override
    public void serialize(Date date, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        String strDate = "";
        if (date != null) {
            strDate = sdf.format(date);
        }
        gen.writeString(strDate);

    }

}
