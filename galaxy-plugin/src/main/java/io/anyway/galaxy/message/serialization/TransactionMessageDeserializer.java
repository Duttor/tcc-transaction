package io.anyway.galaxy.message.serialization;

import com.alibaba.fastjson.JSON;
import io.anyway.galaxy.message.TransactionMessage;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by yangzz on 16/7/26.
 */
public class TransactionMessageDeserializer implements Deserializer<TransactionMessage> {

    private String encoding = "UTF8";

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

        String propertyName = isKey?"key.deserializer.encoding":"value.deserializer.encoding";
        Object encodingValue = configs.get(propertyName);
        if(encodingValue == null) {
            encodingValue = configs.get("deserializer.encoding");
        }

        if(encodingValue != null && encodingValue instanceof String) {
            this.encoding = (String)encodingValue;
        }

    }

    @Override
    public TransactionMessage deserialize(String topic, byte[] bytes) {
        try {
            return bytes == null?null: JSON.parseObject(new String(bytes,encoding),TransactionMessage.class);
        } catch (UnsupportedEncodingException var4) {
            throw new SerializationException("Error when deserializing byte[] to string due to unsupported encoding " + this.encoding);
        }
    }

    @Override
    public void close() {

    }
}
