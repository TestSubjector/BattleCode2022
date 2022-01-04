package utility;

import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
import java.util.logging.Handler;
import java.io.IOException;

public class Blog {

    public static Logger logger = Logger.getLogger("baco"); 
    public static Handler initBlog(Handler fh) {
        try {
            fh = new FileHandler("logs/baco.log");
            fh.setFormatter(new SimpleFormatter()); 
            Logger.getLogger("").addHandler(fh);
            Logger.getLogger("baco").setLevel(Level.FINEST);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fh;
    }

    public static void log(String msg, String type) {
        switch (type) {
            case "FINE":
                logger.fine(msg);
                break;
            case "FINER":
                logger.finer(msg);
                break;
            case "INFO":
                logger.info(msg);
                break;
            case "WARNING":
                logger.warning(msg);
                break;
            case "SEVERE":
                logger.severe(msg);
                break;
            default:
                logger.finest(msg);
                break;
        }
    }
}
