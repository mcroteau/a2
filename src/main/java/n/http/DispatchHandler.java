package n.http;


import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import n.annotate.Json;
import n.data.ExchangeData;
import n.model.HttpMapping;
import n.model.HttpMappings;
import n.model.TypeDetails;
import n.processor.LayoutProcessor;
import n.support.Helper;

import java.io.*;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatchHandler implements HttpHandler {

    String contextPath;
    HttpMappings httpMappings;
    ExchangeData flashScope;
    LayoutProcessor layoutProcessor;
    String[] webResources;

    public void setContext(String contextPath){
        this.contextPath = contextPath;
    }

    public void setHttpMappings(HttpMappings httpMappings){
        this.httpMappings = httpMappings;
    }

    public void setLayoutProcessor(LayoutProcessor layoutProcessor){ this.layoutProcessor = layoutProcessor; }

    public void setWebResources(String[] webResources){ this.webResources = webResources; }

    //Headers h = exchange.getResponseHeaders();
    //h.add("Location", "/o/hi");
    //exchange.sendResponseHeaders(302, -1);
    //exchange.close();
    //return;

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        Map<String, Object> exchangeData = new HashMap<>();
        if(exchange.getAttribute("message") != null){
            exchangeData.put("message", exchange.getAttribute("message"));
            exchange.setAttribute("message", "");
        }

        OutputStream outputStream = exchange.getResponseBody();

        String uri = getUri(exchange);
        String verb = exchange.getRequestMethod().toLowerCase();
        if(isResource(uri)){
            serveResource(verb, uri, exchange);
            return;
        }

        if(!isResource(uri)){

            try {

                HttpMapping httpMapping = getHttpMapping(verb, uri);
                List<Object> good = getGood(uri, httpMapping, exchange, exchangeData);

                Method method = httpMapping.getMethod();
                Object obj = httpMapping.getClassDetails().getObject();
                System.out.println("method::" + method.getName() + " : " + obj.getClass().getTypeName());

                String response = (String) method.invoke(obj, good.toArray());

                System.out.println("uri " + response);
                if(isRedirect(response)){
                    exchange.setAttribute("message", exchangeData.get("message"));
                    Headers headers = exchange.getResponseHeaders();
                    String redirect = getRedirect(response);
                    headers.add("Location", redirect);
                    exchange.sendResponseHeaders(302, -1);
                    exchange.close();
                    return;
                }


                if(method.isAnnotationPresent(Json.class)){

                    Headers headers = exchange.getResponseHeaders();
                    headers.add("content-type", "application/json");
                    exchange.sendResponseHeaders(200, response.length());
                    outputStream.write(response.getBytes());

                }else {
                    Path webPath = Paths.get("src", "main", "webapp");
                    String viewPath = webPath.toFile().getAbsolutePath().concat(File.separator + response);
                    File viewFile = new File(viewPath);

                    String raw = layoutProcessor.render(viewFile);

                    Writer out = new StringWriter();
                    out.append(raw);

                    Template template = Mustache.compiler().compile(raw);
                    String html = template.execute(exchangeData);

    //                MustacheFactory mf = new DefaultMustacheFactory();
    //                Mustache mustache = mf.compile(new StringReader(out.toString()), "Template issue:");
    //                mustache.execute(out, exchangeData).flush();

                    exchange.sendResponseHeaders(200, html.length());
                    outputStream.write(html.getBytes());
                }

                outputStream.flush();
                outputStream.close();

            }catch(Exception ex){
                ex.printStackTrace();
                exchange.sendResponseHeaders(200, "We forgot an ampersand".length());
                outputStream.write("We forgot an ampersand".getBytes());
                outputStream.flush();
                outputStream.close();
            }
        }

    }

    protected boolean isRedirect(String uri){
        return uri.startsWith("redirect:");
    }

    protected String getRedirect(String uri){
        String[] parts = uri.split(":");
        return parts[1];
    }

    protected List<Object> getGood(String uri,
                                   HttpMapping httpMapping,
                                   HttpExchange exchange,
                                   Map<String, Object> exchangeData){
        List<String> values = getHttpValues(uri, httpMapping);
        List<Object> good = new ArrayList<>();
        good.add(exchange);
        good.add(exchangeData);
        for(int z = 0; z < httpMapping.getTypeDetails().size(); z++){
            TypeDetails details = httpMapping.getTypeDetails().get(z);
            Object preObj = values.get(z);
            Object obj = preObj;
//            System.out.println(details.getType());
            if (details.getType().equals("int")) {
                obj = Integer.parseInt(preObj.toString());
            } else if (details.getType().equals("double")) {
                obj = Double.parseDouble(preObj.toString());
            } else if (details.getType().equals("long")) {
                obj = Long.parseLong(preObj.toString());
            } else if (details.getType().equals("java.lang.Integer")) {
                obj = Integer.parseInt(preObj.toString());
            } else if (details.getType().equals("java.lang.Long")) {
                obj = Long.parseLong(preObj.toString());
            } else if (details.getType().equals("java.math.BigDecimal")) {
                obj = new BigDecimal(preObj.toString());
            }
            good.add(obj);
        }
        return good;
    }


    protected List<String> getHttpValues(String uri, HttpMapping mapping){
        List<String> pathParts = Arrays.asList(uri.split("/"));
        List<String> regexParts = Arrays.asList(mapping.getRegexedPath().split("/"));

        //System.out.println("same size " + pathParts.size() + " : " + regexParts.size());

        List<String> good = new ArrayList<>();
        for(int n = 0; n < regexParts.size(); n++){
            String regex = regexParts.get(n);
            if(regex.contains("A-Za-z0-9")){
                good.add(pathParts.get(n));
            }
        }
        return good;
    }

    protected String getUri(HttpExchange exchange){
        String uri = exchange.getRequestURI().toString().replaceFirst(contextPath, "");
        System.out.println("uri " + uri);

        if(uri.endsWith("/")){
            Helper.removeLast(uri);
        }
        if(uri.equals("")) {
            uri = uri.concat("/");
        }
        if(!uri.startsWith("/")){
            uri = "/".concat(uri);
        }
        return uri;
    }

    protected Boolean isResource(String uri){
        String[] parts = uri.split("/");
        List<String> resources = Arrays.asList(webResources);
        if(parts.length > 0) {
            String asset = parts[1];
            if (resources.contains(asset)) return true;
        }
        return false;
    }

    protected HttpMapping getHttpMapping(String verb, String uri){
        HttpMapping httpMapping = null;
        for (Map.Entry<String, HttpMapping> mappingEntry : httpMappings.getMappings().entrySet()) {
            HttpMapping mapping = mappingEntry.getValue();
            Pattern pattern = Pattern.compile(mapping.getRegexedPath());
            Matcher matcher = pattern.matcher(uri);
            if(matcher.matches() &&
                    mapping.getVerb().equals(verb)){
                httpMapping = mapping;
                break;
            }
        }
        return httpMapping;
    }


    public void serveResource(String verb, String uri, HttpExchange he) throws IOException {
        String webPath = Paths.get("src", "main", "webapp").toAbsolutePath().toString();
        String filePath = webPath.concat(uri);
        File file = new File(filePath);
        FileInputStream fis;
        try {
            fis = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            //Thank you: https://stackoverflow.com/users/35070/phihag
            // The file may also be forbidden to us instead of missing, but we're leaking less information this way
            sendError(he, 404, "File not found");
            return;
        }

        String mimeType = lookupMime(filePath);
        he.getResponseHeaders().set("Content-Type", mimeType);
        if ("get".equals(verb)) {
            he.sendResponseHeaders(200, file.length());
            OutputStream os = he.getResponseBody();
            copyStream(fis, os);
            os.close();
            os.flush();
        } else {
            assert("HEAD".equals(verb));
            he.sendResponseHeaders(200, -1);
        }
        fis.close();
    }

    private void copyStream(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[1024];
        int n;
        while ((n = is.read(buf)) >= 0) {
            os.write(buf, 0, n);
        }
    }

    private void sendError(HttpExchange he, int rCode, String description) throws IOException {
        String message = "HTTP error " + rCode + ": " + description;
        byte[] messageBytes = message.getBytes("UTF-8");

        he.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        he.sendResponseHeaders(rCode, messageBytes.length);
        OutputStream os = he.getResponseBody();
        os.write(messageBytes);
        os.close();
    }

    // This is one function to avoid giving away where we failed
    private void reportPathTraversal(HttpExchange he) throws IOException {
        sendError(he, 400, "Path traversal attempt detected");
    }

    private static String getExt(String path) {
        int slashIndex = path.lastIndexOf('/');
        String basename = (slashIndex < 0) ? path : path.substring(slashIndex + 1);

        int dotIndex = basename.lastIndexOf('.');
        if (dotIndex >= 0) {
            return basename.substring(dotIndex + 1);
        } else {
            return "";
        }
    }

    private static String lookupMime(String path) {
        String ext = getExt(path).toLowerCase();
        return MIME_MAP.getOrDefault(ext, "application/octet-stream");
    }


    private static final Map<String,String> MIME_MAP = new HashMap<>();

    static {
        MIME_MAP.put("appcache", "text/cache-manifest");
        MIME_MAP.put("css", "text/css");
        MIME_MAP.put("gif", "image/gif");
        MIME_MAP.put("html", "text/html");
        MIME_MAP.put("js", "application/javascript");
        MIME_MAP.put("json", "application/json");
        MIME_MAP.put("jpg", "image/jpeg");
        MIME_MAP.put("jpeg", "image/jpeg");
        MIME_MAP.put("mp4", "video/mp4");
        MIME_MAP.put("mp3", "audio/mp3");
        MIME_MAP.put("pdf", "application/pdf");
        MIME_MAP.put("png", "image/png");
        MIME_MAP.put("svg", "image/svg+xml");
        MIME_MAP.put("xlsm", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        MIME_MAP.put("xml", "application/xml");
        MIME_MAP.put("zip", "application/zip");
        MIME_MAP.put("md", "text/plain");
        MIME_MAP.put("txt", "text/plain");
        MIME_MAP.put("php", "text/plain");
    };
}
