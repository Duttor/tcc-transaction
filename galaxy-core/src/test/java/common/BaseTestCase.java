package common;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.MockitoAnnotations;

/**
 * Created by xiong.j on 2016/8/16.
 */
@Slf4j
public abstract class BaseTestCase {

    @BeforeClass
    public static void beforeClass() {
        System.out.println("---------------------------------------");
        System.out.println("-                                     -");
        System.out.println("-           Startup test              -");
        System.out.println("-                                     -");
        System.out.println("---------------------------------------");
    }

    @AfterClass
    public static void afterClass() {
        System.out.println("---------------------------------------");
        System.out.println("-                                     -");
        System.out.println("-           Shutdown test             -");
        System.out.println("-                                     -");
        System.out.println("---------------------------------------");
    }

    @Before
    public void before() throws Exception {
        MockitoAnnotations.initMocks(this);
        setup();
    }

    @After
    public void after() throws Exception {
        tearDown();
    }

    public void setup() throws Exception {}

    public void tearDown() throws Exception {}
}