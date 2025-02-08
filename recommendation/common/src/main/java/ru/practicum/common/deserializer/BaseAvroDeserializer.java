package ru.practicum.common.deserializer;

import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

public class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {
    private final Schema schema;
    private final DecoderFactory decoderFactory;

    public BaseAvroDeserializer(Schema schema) {
        this(DecoderFactory.get(), schema);
    }

    public BaseAvroDeserializer(DecoderFactory decoderFactory, Schema schema) {
        this.decoderFactory = decoderFactory;
        this.schema = schema;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) return null;

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            Decoder decoder = decoderFactory.binaryDecoder(inputStream, null);
            DatumReader<T> reader = new SpecificDatumReader<>(schema);
            return reader.read(null, decoder);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize Avro data", e);
        }
    }

    @Override
    public void close() {
    }
}
