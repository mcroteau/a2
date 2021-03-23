package n.processor;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LayoutProcessor {

    Map<String, String> layoutsMap;

    public LayoutProcessor(Builder builder){
        this.layoutsMap = builder.layoutsMap;
    }

    public String render(File view) throws Exception{
        StringBuilder content = new StringBuilder();
        Scanner scanner = new Scanner(view);
        String layoutName = "";
        File layoutFile = null;
        String pageTitle = null;
        while(scanner.hasNext()){
            String line = scanner.nextLine();

            Matcher pageTitleMatcher = Pattern
                    .compile("\\{\\{(title)\\:([a-zA-Z ]*?)\\}\\}")
                    .matcher(line);

            while(pageTitleMatcher.find()){
                pageTitle = pageTitleMatcher.group(2);
                String replace = line.substring(pageTitleMatcher.start(), pageTitleMatcher.end());
                line = line.replace(replace, "");
            }

            Matcher matcher = Pattern
                    .compile("\\{\\{(layout)\\:(\\w*?.\\.html)\\}\\}")
                    .matcher(line);

            while (matcher.find()) {
                layoutName = matcher.group(2);
                String path = Paths.get("src", "main", "webapp", "layouts")
                        .toAbsolutePath().toString().concat(File.separator + layoutName);
                layoutFile = new File(path);
                String replace = line.substring(matcher.start(), matcher.end());
                line = line.replace(replace, "");
            }

            content.append(line);
        }

        if(layoutFile != null &&
                layoutFile.exists() &&
                    layoutsMap.containsKey(layoutName)) {

            System.out.println("*******");
            System.out.println("we found a layout");
            System.out.println("*******");

            StringBuilder html = new StringBuilder();
            Scanner layoutScanner = new Scanner(layoutsMap.get(layoutName));
            while (layoutScanner.hasNext()) {

                String line = layoutScanner.nextLine();

                Matcher titleMatcher = Pattern
                        .compile("\\{\\{(title)\\:([a-zA-Z ]*?)\\}\\}")
                        .matcher(line);

                while (titleMatcher.find()) {
                    String replace = line.substring(titleMatcher.start(), titleMatcher.end());
                    if (pageTitle != null) {
                        line = line.replace(replace, pageTitle);
                    } else {
                        String defaultTitle = titleMatcher.group(2);
                        line = line.replace(replace, defaultTitle);
                    }
                }

                Matcher matcher = Pattern
                        .compile("\\{\\{(layout)\\:(body)\\}\\}")
                        .matcher(line);

                while (matcher.find()) {
                    System.out.println("matches remove tag");
                    String replace = line.substring(matcher.start(), matcher.end());
                    line = line.replace(replace, content.toString());
                }

                html.append(line);
            }

            return html.toString();

        }else{
            return content.toString();
        }
    }

    public static class Builder{
        String[] layouts;
        Map<String, String> layoutsMap;

        public Builder(){
            layoutsMap = new HashMap<>();
        }

        public LayoutProcessor.Builder layouts(String[] layouts){
            this.layouts = layouts;
            return this;
        }

        protected void processLayouts() throws Exception {
            for(String layout : layouts){
                String path = Paths.get("src", "main", "webapp", "layouts", layout)
                        .toAbsolutePath().toString();
                File layoutFile = new File(path);
                if(!layoutFile.exists()){
                    throw new Exception("Layout not found at " + path);
                }
                StringBuilder sb = new StringBuilder();
                Scanner scanner = new Scanner(layoutFile);
                while(scanner.hasNext()) {
                    sb.append(scanner.nextLine());
                }
                layoutsMap.put(layout, sb.toString());
            }
        }

        public LayoutProcessor build() throws Exception {
            processLayouts();
            return new LayoutProcessor(this);
        }
    }
}
