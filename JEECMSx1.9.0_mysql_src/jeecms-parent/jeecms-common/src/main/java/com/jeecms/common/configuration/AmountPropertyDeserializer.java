package com.jeecms.common.configuration;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.math.BigDecimal;

/**
 *
 * @author: chenming
 * @date: 2021/3/16 11:02
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public class AmountPropertyDeserializer implements ObjectDeserializer {
    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        JSONLexer lexer = parser.getLexer();
        String mayBeStr = lexer.stringVal();
        if (StringUtils.isBlank(mayBeStr)) {
            return null;
        }
        Double d;
        if (mayBeStr.endsWith(",")) {
            d = lexer.decimalValue().multiply(BigDecimal.valueOf(10000)).doubleValue();
        } else {
            d = Double.parseDouble(mayBeStr) * 10000;
        }

        return (T) Long.valueOf(d.longValue());
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}
