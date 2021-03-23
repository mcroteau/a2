package n.support;

import n.processor.LayoutProcessor;
import spike.config.DbConfig;
import spike.config.SimpleConfig;
import spike.model.Todo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Runner {

    static Logger log = Logger.getLogger("Runner");

    public static void main(String[] args) {

        log.info("runner ...");
        try {

            Class[] configs = new Class[]{SimpleConfig.class, DbConfig.class};
            String[] resources = new String[]{ "assets" };

            new Server.Builder()
                    .withResources(resources)
                    .withConfigs(configs)
                    .withPort(8080)
                    .threads(1760)
                    .configure()
                    .run();

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}