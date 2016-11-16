import io.anyway.galaxy.console.dal.rdao.TransactionInfoDao;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by xiong.j on 2016/8/2.
 */
public class main {

    @Test
    public void testInsert(){
        ClassPathXmlApplicationContext ctx= new ClassPathXmlApplicationContext("classpath:spring/appcontext-web-servlet.xml");
        ctx.refresh();
        TransactionInfoDao transactionInfoDao = ctx.getBean(TransactionInfoDao.class);
        transactionInfoDao.get(1);
    }
}
