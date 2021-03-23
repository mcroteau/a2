package n.processor;

import n.annotate.Variable;
import n.annotate.verbs.Delete;
import n.annotate.verbs.Get;
import n.annotate.verbs.Post;
import n.annotate.verbs.Put;
import n.data.BeanDetails;
import n.data.InjectionData;
import n.model.ClassDetails;
import n.model.HttpMapping;
import n.model.HttpMappings;
import n.model.TypeDetails;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpProcessor {

    public static final String GET    = "get";
    public static final String POST   = "post";
    public static final String PUT    = "put";
    public static final String DELETE = "delete";

    InjectionData injectionData;
    BeanDetails beanDetails;
    Map<String, ClassDetails> processed;
    HttpMappings httpMappings;

    public HttpProcessor(BeanDetails beanDetails,
                         InjectionData injectionData){
        this.beanDetails = beanDetails;
        this.injectionData = injectionData;
        this.processed = new HashMap<>();
        this.httpMappings = new HttpMappings();
    }

    public HttpProcessor run() throws Exception{
        while(!allAnnotationsProcessed()){
            processWebAnnotations();
        }
        return this;
    }

    private boolean allAnnotationsProcessed(){
        return this.processed.size() == this.injectionData.getHttpClasses().size();
    }

    private void processWebAnnotations() throws Exception{
        for(Map.Entry<String, ClassDetails> entry : this.injectionData.getHttpClasses().entrySet()){
            Class clazz = entry.getValue().getClazz();
            Method[] methods = clazz.getDeclaredMethods();
            for(Method method: methods){
                if(method.isAnnotationPresent(Get.class)){
                    setGetMapping(method, entry.getValue());
                    processed.put(entry.getKey(), entry.getValue());
                }
                if(method.isAnnotationPresent(Post.class)){
                    setPostMapping(method, entry.getValue());
                    processed.put(entry.getKey(), entry.getValue());
                }
                if(method.isAnnotationPresent(Put.class)){
                    setPutMapping(method, entry.getValue());
                    processed.put(entry.getKey(), entry.getValue());
                }
                if(method.isAnnotationPresent(Delete.class)){
                    setDeleteMapping(method, entry.getValue());
                    processed.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    protected void setGetMapping(Method method, ClassDetails classDetails) throws Exception{
        Get get = method.getAnnotation(Get.class);
        String path = get.value();
        HttpMapping mapping = new HttpMapping();
        mapping.setVerb(GET);
        setBaseDetailsAdd(path, mapping, method, classDetails);
    }

    protected void setPostMapping(Method method, ClassDetails classDetails) throws Exception{
        Post post = method.getAnnotation(Post.class);
        String path = post.value();
        HttpMapping mapping = new HttpMapping();
        mapping.setVerb(POST);
        setBaseDetailsAdd(path, mapping, method, classDetails);
    }

    protected void setPutMapping(Method method, ClassDetails classDetails) throws Exception{
        Put put = method.getAnnotation(Put.class);
        String path = put.value();
        HttpMapping mapping = new HttpMapping();
        mapping.setVerb(PUT);
        setBaseDetailsAdd(path, mapping, method, classDetails);
    }

    protected void setDeleteMapping(Method method, ClassDetails classDetails) throws Exception{
        Delete delete = method.getAnnotation(Delete.class);
        String path = delete.value();
        HttpMapping mapping = new HttpMapping();
        mapping.setVerb(DELETE);
        setBaseDetailsAdd(path, mapping, method, classDetails);
    }

    protected void setBaseDetailsAdd(String path, HttpMapping mapping, Method method, ClassDetails classDetails) throws Exception{

        mapping.setTypeNames(new ArrayList<>());

        List<TypeDetails> typeDetails = new ArrayList<>();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        Class[] paramTypes = method.getParameterTypes();
        for (int n = 0; n < paramAnnotations.length; n++) {
            for (Annotation a: paramAnnotations[n]) {
                if (a instanceof Variable) {
                    TypeDetails details = new TypeDetails();
                    details.setName(paramTypes[n].getTypeName());
                    details.setType(paramTypes[n].getTypeName());
                    typeDetails.add(details);
                }
            }
        }


//https://regex101.com/r/sYeDyN/1
//\/(post){1}\/[A-Za-z0-9]*\/(paul){1}$
//\/(get){1}\/[A-Za-z0-9]\/[A-Za-z0-9]\/[A-Za-z0-9]\/$

        StringBuilder regexPath = new StringBuilder();
        regexPath.append("\\/");
        int count = 0;
        String[] parts = path.split("/");
        for(String part: parts){
            count++;
            if(!part.equals("")) {
                if (part.matches("(\\{\\{[a-zA-Z]*\\}\\})")) {
                    regexPath.append("[A-Za-z0-9]*");
                } else {
                    regexPath.append("(" + part.toLowerCase() + "){1}");
                }
                if (count < parts.length) {
                    regexPath.append("\\/");
                }
            }
        }
        regexPath.append("$");

        mapping.setRegexedPath(regexPath.toString());
        mapping.setTypeDetails(typeDetails);
        mapping.setPath(path);
        mapping.setMethod(method);
        mapping.setClassDetails(classDetails);
        if(httpMappings.contains(regexPath.toString())){
            throw new Exception("Request path + " + path + " exists multiple times.");
        }
        httpMappings.add(path, mapping);
    }


    public HttpMappings getMappings() {
        return httpMappings;
    }
}
