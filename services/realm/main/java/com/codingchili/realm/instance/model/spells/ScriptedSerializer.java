package com.codingchili.realm.instance.model.spells;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

/**
 * @author Robin Duda
 * <p>
 * Custom jackson serializer to unpack an AbstractMap.SimpleEntry.
 * Used to further simplify the format of scripts embedded into configuration.
 *
 * todo example
 */
public class ScriptedSerializer extends StdDeserializer<Scripted> {

    public ScriptedSerializer() {
        super(Scripted.class);
    }

    @Override
    public Scripted deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        String engine = node.fieldNames().next();
        String source = node.get(engine).textValue();
        return ScriptEngines.script(source, engine);
    }

}
