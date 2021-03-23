package renderer;

import spike.model.Todo;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Renderer {

    List<String> pathParts;
    Map<String, Object> data;

    public Renderer(Builder builder){
        this.pathParts = builder.pathParts;
        this.data = builder.data;
    }

    public String render() throws Exception {
        StringBuilder sb = new StringBuilder();
        String filePath = getViewPath(pathParts);
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);

        Object obj = null;
        List<Object> iterationObj = null;
        boolean inBoolean = false;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Matcher propertiesMatcher = Pattern
                    .compile("\\{\\{\\#(\\w*?)\\.(\\w*?)\\}\\}")
                    .matcher(line);

            while (propertiesMatcher.find()) {
                String key = propertiesMatcher.group(1);
                String valueKey = propertiesMatcher.group(2);
                System.out.println(propertiesMatcher.group(1) + "::" + propertiesMatcher.group(2));
                if(data.containsKey(key)){
                    Object object = data.get(key);
                    Field field = object.getClass().getDeclaredField(valueKey);
                    field.setAccessible(true);
                    if(!(boolean) field.get(object)){
                        inBoolean = false;
                        obj = object;
                    }else{
                        inBoolean = true;
                        obj = object;
                    }
                }
            }



            if(inBoolean){
                Matcher endingBooleanMatcher = Pattern
                        .compile("\\{\\{\\/(\\w*?)\\.(\\w*?)\\}\\}")
                        .matcher(line);

                while (endingBooleanMatcher.find()) {
                    inBoolean = false;
                    String replace = line.substring(endingBooleanMatcher.start(), endingBooleanMatcher.end());
                    System.out.println("replace " + replace);
                    line = line.replace(replace, "");
                }

                line = processField(obj, line);
            }

            if(!inBoolean) {

                line = processProperties(line);

                Matcher endingMatcher = Pattern
                        .compile("\\{\\{\\/(.*?)\\}\\}")
                        .matcher(line);

                if (iterationObj != null &&
                        !endingMatcher.matches()) {
                    line = processIterable(line, iterationObj);
                }

                while (endingMatcher.find()) {
                    String ending = endingMatcher.group(1);
                    line = line.replaceFirst("\\{\\{\\/(" + ending + ")\\}\\}", "");
                    iterationObj = null;
                    continue;
                }

                Matcher listMatcher = Pattern
                        .compile("\\{\\{\\^(.*?)\\}\\}")
                        .matcher(line);

                while (listMatcher.find()) {
                    String iterable = listMatcher.group(1);
                    if (data.containsKey(iterable)) {
                        System.out.println("is array");
                        line = line.replace("{{^" + iterable + "}}", "");
                        iterationObj = (ArrayList) data.get(iterable);
                    }
                }

                sb.append(line);
            }
        }

        return sb.toString();
    }




    protected String processProperties(String line) throws Exception {
        Matcher propertiesMatcher = Pattern
                .compile("\\{\\{(\\w*?)\\.(\\w*?)\\}\\}")
                .matcher(line);

        while (propertiesMatcher.find()) {
            String key = propertiesMatcher.group(1);
            String valueKey = propertiesMatcher.group(2);
            if(data.containsKey(key)){
                Object obj = data.get(key);
                Field field = obj.getClass().getDeclaredField(valueKey);
                field.setAccessible(true);
                String value = field.get(obj).toString();
                line = line.replaceFirst("\\{\\{(\\w*?)\\.(\\w*?)\\}\\}", value);
            }
        }
        return line;
    }



    protected String processField(Object obj, String line) throws Exception{
        Matcher itemMatcher = Pattern
                .compile("\\{\\{(\\w*?)\\}\\}")
                .matcher(line);

        while (itemMatcher.find()) {
            String item = itemMatcher.group(1);
            if(!item.contains("^") &&
                    !item.contains("!") &&
                    !item.contains("/")) {
                System.out.println("~ item " + item + "::" + obj.getClass());
                Field field = obj.getClass().getDeclaredField(item);
                field.setAccessible(true);
                String replace = field.get(obj).toString();
                line = line.replace("{{" + item + "}}", replace);
            }
        }
        return line;
    }


    protected String processBoolean(Object obj, String line) throws Exception{
        Matcher booleanMatcher = Pattern
                .compile("\\{\\{\\#(.*?[a-z])\\}\\}")
                .matcher(line);

        while (booleanMatcher.find()) {
            String item = booleanMatcher.group(1);
            Field field = obj.getClass().getDeclaredField(item);
            field.setAccessible(true);
            if((boolean) field.get(obj)){
                line = line.replaceFirst("\\{\\{\\#(" + item + ")\\}\\}", "");
                line = line.replaceFirst("\\{\\{\\/(" + item +")\\}\\}", "");
            }else{
                Pattern negativeReplace = Pattern.compile("\\{\\{\\#(.*?[a-z])\\}\\}.*?[a-z]\\{\\{\\/(.*?[a-z])\\}\\}");
                Matcher matcher = negativeReplace.matcher(line);
                while (matcher.find()) {
                    String replace = line.substring(matcher.start(), matcher.end());
                    line = line.replace(replace, "");
                }
            }
        }
        return line;
    }

    protected String processNegative(Object obj, String line) throws Exception{

        Matcher negativeMatcher = Pattern
                .compile("\\{\\{\\!(.*?[a-z])\\}\\}")
                .matcher(line);

        while (negativeMatcher.find()) {
            String item = negativeMatcher.group(1);
            System.out.println("negative :: "+ item);
            Field field = obj.getClass().getDeclaredField(item);
            field.setAccessible(true);
            if(!(boolean) field.get(obj)){
                System.out.println("remove line by line");
                line = line.replaceFirst("\\{\\{\\!(" + item + ")\\}\\}", "");
                line = line.replaceFirst("\\{\\{\\/(" + item +")\\}\\}", "");
            }else{
                Pattern negativeReplace = Pattern.compile("\\{\\{\\!(.*?[a-z])\\}\\}.*?[a-z]\\{\\{\\/(.*?[a-z])\\}\\}");
                Matcher matcher = negativeReplace.matcher(line);
                while (matcher.find()) {
                    String replace = line.substring(matcher.start(), matcher.end());
                    line = line.replace(replace, "");
                }
            }
        }
        return line;
    }


    protected String processIterable(String startingLine, List<Object> iterableObj) throws Exception {

        StringBuilder builder = new StringBuilder();

        for(int x = 0; x < iterableObj.size(); x++) {
            Object obj = iterableObj.get(x);
            System.out.println(obj.getClass());
            String line = startingLine;
            line = processField(obj, line);
            line = processBoolean(obj, line);
            line = processNegative(obj, line);
            builder.append(line);
        }

        return builder.toString();

    }


    protected String getViewPath(List<String> pathParts){
        StringBuilder builder = new StringBuilder();
        Path webPath = Paths.get("src", "main", "webapp");
        String basePath = webPath.toAbsolutePath().toString();
        for(String part : pathParts){
            builder.append(File.separator + part);
        }
        return basePath.concat(builder.toString());
    }

    public static class Builder{
        List<String> pathParts;
        Map<String, Object> data;

        public Builder path(List<String> pathParts){
            this.pathParts = pathParts;
            return this;
        }

        public Builder data(Map<String, Object> data){
            this.data = data;
            return this;
        }

        public Renderer build(){
            return new Renderer(this);
        }
    }
}
