package io.anyway.galaxy.message;

import common.DalTestCase;
import io.anyway.galaxy.common.TransactionStatusEnum;
import io.anyway.galaxy.context.TXContext;
import io.anyway.galaxy.context.support.TXContextSupport;
import io.anyway.galaxy.message.producer.MessageProducer;
import io.anyway.galaxy.util.AopTargetUtil;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

public class TransactionMessageServiceImplTest extends DalTestCase {

    @InjectMocks
    @Autowired
    private TransactionMessageService transactionMessageService;

    @Autowired
    private MessageProducer messageProducer;

    @Test
    public void testSendMessageError() throws Throwable {
        ReflectionTestUtils.setField(AopTargetUtil.getTarget(transactionMessageService), "messageProducer", null);

        TXContext ctx = new TXContextSupport(0L, 15099498831872L, "serial_86965", "purchase");
        transactionMessageService.sendMessage(ctx, TransactionStatusEnum.CANCELLING);

        ReflectionTestUtils.setField(AopTargetUtil.getTarget(transactionMessageService), "messageProducer", messageProducer);
    }

    @Test
    public void testSendMessage() throws Throwable {
        TXContext ctx = new TXContextSupport(0L, 15099498831872L, "serial_86965", "purchase");
        transactionMessageService.sendMessage(ctx, TransactionStatusEnum.CANCELLING);
    }
}
