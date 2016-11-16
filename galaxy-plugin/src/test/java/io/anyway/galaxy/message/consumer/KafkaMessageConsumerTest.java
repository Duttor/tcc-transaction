package io.anyway.galaxy.message.consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ServiceLoader;

/**
 * KafkaMessageConsumer Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>22, 2016</pre>
 */
public class KafkaMessageConsumerTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: handleMessage(Object message)
     * @throws Throwable 
     */
    @Test
    public void testHandleMessage() throws Throwable {
//        ServiceLoader<MessageConsumer> serviceLoader = ServiceLoader.load(MessageConsumer.class);
//        for (MessageConsumer consumer : serviceLoader) {
//            consumer.handleMessage("test!!");
//        }
    }


} 
