package io.anyway.galaxy.message.serialization;

import com.alibaba.fastjson.JSON;
import io.anyway.galaxy.message.TransactionMessage;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by yangzz on 16/7/26.
 */
public class TransactionMessageSerializer implements Serializer<TransactionMessage> {

    private String encoding = "UTF8";

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

        String propertyName = isKey?"key.serializer.encoding":"value.serializer.encoding";
        Object encodingValue = configs.get(propertyName);
        if(encodingValue == null) {
            encodingValue = configs.get("serializer.encoding");
        }

        if(encodingValue != null && encodingValue instanceof String) {
            this.encoding = (String)encodingValue;
        }
    }

    @Override
    public byte[] serialize(String topic, TransactionMessage message) {
        try {
            return message == null?null:JSON.toJSON(message).toString().getBytes(this.encoding);
        } catch (UnsupportedEncodingException var4) {
            throw new SerializationException("Error when serializing "+message.getClass()+" to byte[] due to unsupported encoding " + this.encoding);
        }
    }

    @Override
    public void close() {

    }
}
