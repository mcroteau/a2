package n.support;

import n.data.BeanDetails;
import n.data.InjectionData;
import n.processor.AnnotationProcessor;
import n.processor.ConfigProcessor;
import n.processor.HttpProcessor;

public class Initializer {

    Class[] configs;
    BeanDetails beanDetails;
    InjectionData injectionData;

    HttpProcessor httpProcessor;

    AnnotationProcessor annotationProcessor;
    ConfigProcessor configProcessor;

    public Initializer(Class[] configs,
                       BeanDetails beanDetails,
                       InjectionData injectionData){
        this.configs = configs;
        this.beanDetails = beanDetails;
        this.injectionData = injectionData;
    }

    public Initializer start() throws Exception{

        if(this.configs != null ||
                this.configs.length > 0){
            this.configProcessor = new ConfigProcessor(configs, beanDetails, injectionData);
            configProcessor.run();
        }

        this.annotationProcessor = new AnnotationProcessor(beanDetails, injectionData);
        annotationProcessor.run();

        this.httpProcessor = new HttpProcessor(beanDetails, injectionData);
        httpProcessor.run();

        System.out.println("http processor " + httpProcessor);

        return this;
    }

    public HttpProcessor getHttpProcessor() {
        return this.httpProcessor;
    }


}
