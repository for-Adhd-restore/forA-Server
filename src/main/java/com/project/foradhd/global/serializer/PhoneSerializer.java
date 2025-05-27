package com.project.foradhd.global.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.project.foradhd.global.exception.InternalSystemException;
import java.io.IOException;

public class PhoneSerializer extends JsonSerializer<String> {

    private static final String phoneRegex = "^(\\d{2,4})(\\d{3,4})(\\d{4})$";
    private static final String phoneFormat = "$1-$2-$3";

    @Override
    public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        // value가 null이거나 빈 문자열이면 null로 출력
        if (value == null || value.trim().isEmpty()) {
            jsonGenerator.writeNull();
            return;
        }

        if (!value.matches(phoneRegex)) {
            throw new InternalSystemException(
                    new JsonParseException("Phone Number Format does not match the regex: " + phoneRegex));
        }
        jsonGenerator.writeString(value.replaceFirst(phoneRegex, phoneFormat));
    }
}
