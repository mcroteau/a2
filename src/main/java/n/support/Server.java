package n.support;

import com.sun.net.httpserver.HttpServer;
import n.data.BeanDetails;
import n.data.InjectionData;
import n.http.DispatchHandler;
import n.model.HttpMappings;
import n.processor.HttpProcessor;
import n.processor.LayoutProcessor;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    Class[] configs;
    String[] webResources;
    BeanDetails beanDetails;
    InjectionData injectionData;

    Integer port;
    HttpServer httpServer;
    HttpProcessor httpProcessor;
    HttpMappings httpMappings;


    Server(Builder builder) throws Exception {
        this.configs = builder.configs;
        this.webResources = builder.webResources;
        this.beanDetails = builder.beanDetails;
        this.injectionData = builder.injectionData;

        this.port = builder.port;
        this.httpServer = builder.httpServer;

        Initializer initializer = new Initializer(configs, beanDetails, injectionData);
        initializer.start();

        this.httpProcessor = initializer.getHttpProcessor();
        this.httpMappings = httpProcessor.getMappings();
    }


    public boolean run() throws IOException {
        System.out.println("run....");
        try {
            DispatchHandler dispatcher = new DispatchHandler();
            dispatcher.setHttpMappings(httpMappings);
            dispatcher.setContext("/");
            dispatcher.setLayoutProcessor(new LayoutProcessor.Builder()
                                            .layouts(new String[]{"default.html"})
                                            .build());
            dispatcher.setWebResources(webResources);
            httpServer.createContext("/", dispatcher);
            httpServer.start();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return true;
    }


    public Object get(String name){
        String beanName = name.toLowerCase();
        if(beanDetails.getBeans().containsKey(beanName)){
            return beanDetails.getBeans().get(beanName).getBean();
        }
        return null;
    }

    public static class Builder{
        Integer port;
        HttpServer httpServer;
        ExecutorService executors;
        Class[] configs;
        String[] webResources;
        BeanDetails beanDetails;
        InjectionData injectionData;

        public Builder(){ this.beanDetails = new BeanDetails(); }

        public Builder withPort(Integer port){
            this.port = port;
            return this;
        }

        public Builder withConfigs(Class[] configs){
            this.configs = configs;
            return this;
        }

        public Builder withResources(String[] webResources){
            this.webResources = webResources;
            return this;
        }

        public Builder threads(int nThreads) throws IOException {
            this.executors = Executors.newFixedThreadPool(nThreads);
            this.httpServer = HttpServer.create(new InetSocketAddress(this.port), 0);;
            return this;
        }

        public Server configure() throws Exception {
            if(this.configs == null){
                throw new Exception("No configurations found! Either set configurations as a @M1 config class or configure a dependencies file in the resources directory.");
            }
            this.injectionData = new InjectionData.Builder()
                    .withBeanData(beanDetails)
                    .prepare()
                    .build();

            return new Server(this);
        }
    }

}
