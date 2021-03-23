package n.processor;

import n.annotate.Inject;
import n.data.BeanDetails;
import n.data.InjectionData;
import n.model.ClassDetails;
import n.support.Helper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationProcessor {

    InjectionData injectionData;
    BeanDetails beanDetails;
    Map<String, ClassDetails> processed;
    List<ClassDetails> annotations;

    public AnnotationProcessor(BeanDetails beanDetails,
                               InjectionData injectionData){
        this.beanDetails = beanDetails;
        this.injectionData = injectionData;
        this.processed = new HashMap<>();
        this.annotations = new ArrayList<>();
        map();
    }

    public void run() throws Exception{

        while(!allAnnotationsProcessed()){
            processAnnotations(0);
            break;
        }
    }

    private void processAnnotations(int idx) throws Exception {
        for(Integer z = idx; z < annotations.size(); z++){
            ClassDetails classDetails = annotations.get(z);
            Integer fieldsCount = getAnnotatedFieldsCount(classDetails.getClazz());
            Integer processedFieldsCount = 0;

            Object object = classDetails.getObject();
            Field[] fields = classDetails.getClazz().getDeclaredFields();
            for(Field field: fields) {
                if(field.isAnnotationPresent(Inject.class)) {
                    String fieldKey = field.getName().toLowerCase();
                    if(beanDetails.getBeans().containsKey(fieldKey)){
                        Object bean = beanDetails.getBeans().get(fieldKey).getBean();
                        field.setAccessible(true);
                        field.set(object, bean);
                        processedFieldsCount++;
                    }else{
                        throw new Exception(field.getName() + " is missing on " + object.getClass().getName());
                    }
                }
            }

            if(fieldsCount !=
                    processedFieldsCount){
                processAnnotations( z + 1);
            }else{
                String key = Helper.getClazzName(classDetails.getName());
                processed.put(key, classDetails);
            }
        }
    }

    protected Integer getAnnotatedFieldsCount(Class clazz) throws Exception{
        Integer count = 0;
        Field[] fields = clazz.getDeclaredFields();
        for(Field field: fields){
            if(field.isAnnotationPresent(Inject.class)){
                count++;
            }
        }
        return count;
    }

    private void map(){
        for(Map.Entry<String, ClassDetails> entry: this.injectionData.getAnnotatedClasses().entrySet()){
            ClassDetails classDetails = entry.getValue();
            if(!annotations.contains(classDetails))annotations.add(classDetails);
        }
    }


    protected Boolean allAnnotationsProcessed(){
        return this.processed.size() == this.injectionData.getAnnotatedClasses().size();
    }
}
