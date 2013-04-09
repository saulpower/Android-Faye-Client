package main.java.com.moneydesktop.finance.data;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import main.java.com.moneydesktop.finance.ApplicationContext;

public class Serialization {

    public static String serialize(Object object) throws JsonGenerationException, JsonMappingException, IOException {

        StringWriter sw = new StringWriter();
        ApplicationContext.getObjectMapper().writeValue(sw, object);

        return sw.toString();
    }

    public static Object deserialize(String json, Class<?> objectClass) throws JsonParseException, JsonMappingException, IOException {

        return ApplicationContext.getObjectMapper().readValue(json, objectClass);
    }

}
