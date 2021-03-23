package n.support;

import com.sun.net.httpserver.HttpExchange;
import n.model.Dependency;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Helper {

    public static String removeLast(String s) {
        return (s == null || s.length() == 0)
                ? null
                : (s.substring(0, s.length() - 1));
    }

    public static String getSetter(String name){
        StringBuilder setterBuilder = new StringBuilder();
        setterBuilder.append("set");
        setterBuilder.append(name.toLowerCase());
        return setterBuilder.toString().toLowerCase();
    }


    public static String getTypeName(String typeName) {
        int index = typeName.lastIndexOf(".");
        if(index > 0){
            typeName = typeName.substring(index + 1);
        }
        return typeName;
    }

    public static String getClazzName(String clazzNameWithExtions){
        int index = clazzNameWithExtions.lastIndexOf(".");
        String qualifiedName = clazzNameWithExtions;
        if(index > 0){
            qualifiedName = qualifiedName.substring(index + 1);
        }
        return qualifiedName.toLowerCase();
    }

    public static String getClazzName(Dependency dependency){
        String clazzName = dependency.getName();
        if(clazzName == null){
            clazzName = getClazzName(dependency.getPojo());
        }else{
            clazzName = clazzName.toLowerCase();
        }
        return clazzName;
    }

    public static <T> Collector<T, ?, T> toSingleton() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1) {
                        throw new IllegalStateException();
                    }
                    return list.get(0);
                }
        );
    }

    public static Map<String, String> parse(HttpExchange exchange) throws Exception {
        String query = "";
        InputStream in = exchange.getRequestBody();
        try {

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte buf[] = new byte[4096];

            for (int n = in.read(buf); n > 0; n = in.read(buf)) {
                out.write(buf, 0, n);
            }

            query = new String(out.toByteArray(), "utf-8");
            query = java.net.URLDecoder.decode(query, StandardCharsets.UTF_8.name());

        } catch (Exception ex){
            ex.printStackTrace();
        }finally {
            in.close();
        }

        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            }else{
                result.put(entry[0], "");
            }
        }
        return result;

    }

}
