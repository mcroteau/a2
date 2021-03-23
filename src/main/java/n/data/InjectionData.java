package n.data;

import n.annotate.*;
import n.model.Bean;
import n.model.ClassDetails;
import n.support.Helper;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InjectionData {

    Integer jdbcCount;
    Integer serviceCount;
    Integer elementCount;
    Map<String, ClassDetails> classes;
    Map<String, ClassDetails> httpClasses;
    Map<String, ClassDetails> annotatedClasses;

    public InjectionData(Builder builder){
        this.jdbcCount = builder.jdbcCount;
        this.serviceCount = builder.serviceCount;
        this.elementCount = builder.elementCount;
        this.classes = builder.classes;
        this.httpClasses = builder.httpClasses;
        this.annotatedClasses = builder.annotatedClasses;
    }

    public Map<String, ClassDetails> getClasses() {
        return this.classes;
    }

    public Map<String, ClassDetails> getHttpClasses() {
        return httpClasses;
    }

    public Map<String, ClassDetails> getAnnotatedClasses() {
        return annotatedClasses;
    }

    public Integer getJdbcCount() {
        return jdbcCount;
    }

    public Integer getServiceCount() {
        return serviceCount;
    }

    public Integer getElementCount() {
        return elementCount;
    }

    public static class Builder{
        BeanDetails beanDetails;
        Integer jdbcCount;
        Integer serviceCount;
        Integer elementCount;
        ClassLoader classLoader;
        Map<String, ClassDetails> classes;
        Map<String, ClassDetails> annotatedClasses;
        Map<String, ClassDetails> httpClasses;

        public Builder(){
            this.jdbcCount = 0;
            this.serviceCount = 0;
            this.elementCount = 0;
            this.classes = new HashMap<>();
            this.annotatedClasses = new HashMap<>();
            this.httpClasses = new HashMap<>();
            this.classLoader = this.getClass().getClassLoader();
        }

        protected String getTopPath(){
            Path classPath = Paths.get("src", "main", "java");
            String absolutePath = classPath.toFile().getAbsolutePath();
            return absolutePath;
        }

        protected void setClasses(String classesPath){
            File pathFile = new File(classesPath);
            List<File> files = Arrays.asList(pathFile.listFiles());
            for (File file : files) {

                if (file.isDirectory()) {
                    setClasses(file.getPath());
                    continue;
                }

//                System.out.println(file.getName());
                String separator = System.getProperty("file.separator");
                String regex = "java\\" + separator;
                String[] pathParts = file.getPath().split(regex);

                String clazzPath = pathParts[1]
                        .replace("\\", ".")
                        .replace("/",".")
                        .replace(".java", "");

                try {
                    Class clazz = classLoader.loadClass(clazzPath);

                    if (clazz.isAnnotation() ||
                            clazz.isInterface() ||
                            clazz.getName().equals("n.support.Runner") ||
                            (clazz.getName() == this.getClass().getName())) {
//                        System.out.println("is annotation, interface or this");
                        continue;
                    }

                    ClassDetails classDetails = new ClassDetails();
                    classDetails.setClazz(clazz);
                    classDetails.setName(Helper.getClazzName(clazz.getName()));
                    classDetails.setPath(clazz.getName());

                    Constructor[] constructors = clazz.getDeclaredConstructors();
                    for(Constructor constructor : constructors){
                        if(constructor.getParameterCount() == 0){
                            Object object = constructor.newInstance();
                            classDetails.setObject(object);
                        }
                    }

                    classes.put(classDetails.getName(), classDetails);//TODO: expects that no classes are named the same within your program.

                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }

        protected void buildAddBean(Map.Entry<String, ClassDetails> entry){
            System.out.println(entry.getKey() + "::" + entry.getValue());
            Bean bean = new Bean();
            String key = entry.getKey();
            Object object = entry.getValue().getObject();
            bean.setBean(object);
            this.beanDetails.getBeans().put(key, bean);
        }

        protected void setBeanData() throws Exception {
            for(Map.Entry<String, ClassDetails> entry : classes.entrySet()){
                Class clazz = entry.getValue().getClazz();

                if(clazz.isAnnotationPresent(Element.class)){
                    buildAddBean(entry);
                    elementCount++;
                }
                if(clazz.isAnnotationPresent(Jdbc.class)){
                    buildAddBean(entry);
                    jdbcCount++;
                }
                if(clazz.isAnnotationPresent(Service.class)){
                    buildAddBean(entry);
                    serviceCount++;
                }
                if(clazz.isAnnotationPresent(HttpHandler.class)){
                    httpClasses.put(entry.getKey(), entry.getValue());
                }

                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(Inject.class)) {
                        annotatedClasses.put(entry.getKey(), entry.getValue());
                        continue;
                    }
                }
            }
        }

        public Builder withBeanData(BeanDetails beanDetails){
            this.beanDetails = beanDetails;
            return this;
        }

//        protected void setApp(){
//            Bean bean = new Bean();
//            bean.setBean(new App(beanDetails));
//            beanDetails.getBeans().put("app", bean);
//        }

        public Builder prepare() throws Exception {
            setClasses(getTopPath());
            setBeanData();
//            setApp();
            return this;
        }

        public InjectionData build(){
            return new InjectionData(this);
        }
    }
}
