package n.model;

import java.lang.reflect.Method;
import java.util.List;

public class HttpMapping {

    String path;
    String regexedPath;
    String verb;
    Method method;
    List<TypeDetails> typeDetails;
    List<String> typeNames;
    List<String> variableNames;

    ClassDetails classDetails;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRegexedPath() {
        return regexedPath;
    }

    public void setRegexedPath(String regexedPath) {
        this.regexedPath = regexedPath;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public List<TypeDetails> getTypeDetails() {
        return typeDetails;
    }

    public void setTypeDetails(List<TypeDetails> typeDetails) {
        this.typeDetails = typeDetails;
    }

    public List<String> getTypeNames() {
        return typeNames;
    }

    public void setTypeNames(List<String> typeNames) {
        this.typeNames = typeNames;
    }

    public List<String> getVariableNames() {
        return variableNames;
    }

    public void setVariableNames(List<String> variableNames) {
        this.variableNames = variableNames;
    }

    public ClassDetails getClassDetails() {
        return classDetails;
    }

    public void setClassDetails(ClassDetails classDetails) {
        this.classDetails = classDetails;
    }

}
