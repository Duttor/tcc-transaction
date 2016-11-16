package io.anyway.galaxy.message.consumer;

import io.anyway.galaxy.message.TransactionMessage;
import io.anyway.galaxy.message.TransactionMessageService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by xiong.j on 2016/7/21.
 */
@Component
public class KafkaMessageConsumer implements InitializingBean,DisposableBean{

    private final static Logger logger = LoggerFactory.getLogger(KafkaMessageConsumer.class);

    @Value("${kafka.servers}")
    private String servers;

    @Value("${kafka.consumer.group}")
    private String group;

    @Value("${kafka.client.id}")
    private String client;

    @Value("${kafka.consumer.timeout}")
    private int timeout= 30;

    @Autowired
    private TransactionMessageService transactionMessageService;

    private volatile boolean running= true;

    private KafkaConsumer<String, TransactionMessage> consumer;

    @Override
    public void afterPropertiesSet() throws Exception {

        Properties props = new Properties();
        props.put("bootstrap.servers", servers);
        props.put("group.id", group);
        props.put("client.id",client);
        props.put("enable.auto.commit", "false");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "io.anyway.galaxy.message.serialization.TransactionMessageDeserializer");

        consumer = new KafkaConsumer<String, TransactionMessage>(props);
        //定义事务处理的topic
        consumer.subscribe(Arrays.asList("galaxy-tx-message"));

        if(logger.isInfoEnabled()){
            logger.info("crete kafka consumer: "+consumer+" ,subscribe topic: galaxy-tx-message");
        }

        final Thread thread= new Thread(){
            @Override
            public void run() {
                for (; running; ) {
                    try {
                        ConsumerRecords<String, TransactionMessage> records = consumer.poll(timeout);
                        for (TopicPartition partition : records.partitions()) {
                            List<ConsumerRecord<String, TransactionMessage>> partitionRecords = records.records(partition);
                            for (ConsumerRecord<String, TransactionMessage> each : partitionRecords) {
                                if(logger.isInfoEnabled()){
                                    logger.info("kafka receive message: "+"{topic:"+each.topic()+",partition:"+partition.partition()+",offset:"+each.offset()+",value:"+each.value()+"}");
                                }
                                if (transactionMessageService.isValidMessage(each.value())) {
                                    transactionMessageService.asyncHandleMessage(each.value());
                                }
                            }
                            //同步设置offset
                            long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                            Map<TopicPartition, OffsetAndMetadata> offsets = Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1));
                            consumer.commitSync(offsets);
                            if (logger.isInfoEnabled()) {
                                logger.info("application group: " + group + " has committed offset: " + offsets);
                            }
                        }
                    } catch (Throwable e) {
                        logger.error("Consumer message failed ", e);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e1) {
                            // e1.printStackTrace();
                        }
                    }
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void destroy() throws Exception {
        running= false;
        consumer.close();
        if(logger.isInfoEnabled()){
            logger.info("destroy kafka consumer: "+consumer);
        }
    }
}
