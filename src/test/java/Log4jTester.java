import org.apache.log4j.Logger;

/**
 * Created by win-7 on 2016/2/5.
 */
public class Log4jTester {
    public static void main (String [] args ){
        Logger logger = Logger.getLogger(Log4jTester.class) ;

        logger.info("start a test");

        logger.error("error info into two files");

        logger.debug("debug hehe");

    }

}
