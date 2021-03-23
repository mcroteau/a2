package n.processor;

import n.annotate.Dependency;
import n.data.BeanDetails;
import n.data.InjectionData;
import n.model.Bean;
import n.model.MethodDetails;
import n.support.Helper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class ConfigProcessor {

    Class[] configs;
    BeanDetails beanDetails;
    InjectionData injectionData;

    List<Class> unprocessedConfigs;

    Map<String, MethodDetails> methods;
    List<MethodDetails> iterableMethods;
    Set<MethodDetails> processedMethods;

    public ConfigProcessor(Class[] configs,
                           BeanDetails beanDetails,
                           InjectionData injectionData) throws Exception{
        this.configs = configs;
        this.unprocessedConfigs = Arrays.asList(configs);
        this.injectionData = injectionData;
        this.beanDetails = beanDetails;
        this.methods = new HashMap<>();
        this.processedMethods = new HashSet();
        this.iterableMethods = new ArrayList<>();
    }

    public ConfigProcessor run() throws Exception{
        setMapDependencyMethods();
        setIterableMethods(methods);
        while(!allDependenciesProcessed()){
            process(0);
        }
        return this;
    }


    protected void process(int idx){
        for(Integer z = 0; z < iterableMethods.size(); z++){
            MethodDetails methodDetails = iterableMethods.get(z);
            try{
                Method method = methodDetails.getMethod();
                Object object = methodDetails.getObject();
                Object dependency = method.invoke(object);

                String clazzName = Helper.getClazzName(dependency.getClass().getName());
                injectionData.getClasses().get(clazzName).setObject(dependency);

                createAddBean(method, dependency);
                processedMethods.add(methodDetails);
            }catch (Exception ex){
                ex.printStackTrace();
                process(z + 1);
            }
        }
    }

    private boolean setIterableMethods(Map<String, MethodDetails> methods) {
        for(Map.Entry<String, MethodDetails> entry : methods.entrySet()){
            iterableMethods.add(entry.getValue());
        }
        return true;
    }


    protected Boolean allDependenciesProcessed(){
        return processedMethods.size() == iterableMethods.size();
    }

    //143 is for everyone... now.

    protected void createAddBean(Method method, Object object){
        Bean bean = new Bean();
        bean.setBean(object);
        String classKey = Helper.getClazzName(method.getName());
        this.beanDetails.getBeans().put(classKey, bean);
    }

    protected void setMapDependencyMethods() throws Exception {
        for(Class config : unprocessedConfigs){
            List<Method> declaredMethods = Arrays.asList(config.getDeclaredMethods());
            for(Method method: declaredMethods){

                if(method.isAnnotationPresent(Dependency.class)) {
                    String methodKey = method.getName().toLowerCase();
                    if (methods.containsKey(methodKey)) {
                        throw new Exception("More than one dependency with the same name defined : " + method.getName());
                    }

                    Object object = null;
                    Constructor[] constructors = config.getConstructors();
                    for(Constructor constructor : constructors){
                        if(constructor.getParameterCount() == 0){
                            object = constructor.newInstance();
                            if(beanDetails.getBeans().containsKey(methodKey)){
                                System.out.println("\n\n");
                                System.out.println("Warning: you elements being injected twice, once by configuration, the other via @Inject.");
                                System.out.println("Take a look at " + config.getName() + " and @Inject for " + method.getName());
                                System.out.println("\n\n");
                                Bean existingBean = beanDetails.getBeans().get(methodKey);
                                existingBean.setBean(object);
                                beanDetails.getBeans().replace(methodKey, existingBean);
                            }
                        }
                    }

                    MethodDetails methodDetails = new MethodDetails();
                    methodDetails.setName(method.getName());
                    methodDetails.setMethod(method);
                    methodDetails.setObject(object);
                    methods.put(methodKey, methodDetails);
                }
            }
        }
    }

}
