package com.ebp.openQuarterMaster.lib.core.jackson;

import com.ebp.openQuarterMaster.lib.core.UnitUtils;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bson.types.ObjectId;

import javax.measure.Unit;
import java.io.IOException;

/**
 * Jackson module to handle the Mongodb ObjectId in a reasonable manner
 */
public class UnitModule extends SimpleModule {
    public UnitModule() {
        super();
        addSerializer(Unit.class, new ObjectIdSerializer());
        addDeserializer(Unit.class, new ObjectIdDeserializer());
    }

    public static class ObjectIdSerializer extends JsonSerializer<Unit> {
        @Override
        public void serialize(Unit value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if(!UnitUtils.ALLOWED_UNITS.contains(value)){
                serializers.findValueSerializer(value.getClass()).serialize(value, gen, serializers);
            } else {
                gen.writeString(UnitUtils.stringFromUnit(value));
            }
        }
    }
    public static class ObjectIdDeserializer extends JsonDeserializer<Unit> {
        @Override
        public Unit deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            Unit output = UnitUtils.unitFromString(p.getValueAsString());

//            if(output == null) { //TODO
//                output = ctxt.findNonContextualValueDeserializer(Unit.class).deserialize(p, ctxt);
//            }
            return output;
        }
    }


}
