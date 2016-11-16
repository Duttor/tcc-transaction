package common;

import io.anyway.galaxy.message.producer.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by xiong.j on 2016/8/16.
 */
@Component
@Slf4j
public class MockMessageProducer implements MessageProducer {
    @Override
    public void sendMessage(Object message) throws Throwable {
        log.info("Send message OK, message=" + message);
    }
}
