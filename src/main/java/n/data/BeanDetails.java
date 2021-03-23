package n.data;

import n.model.Bean;

import java.util.HashMap;
import java.util.Map;

public class BeanDetails {

    Map<String, Bean> beans;

    public BeanDetails(){
        this.beans = new HashMap<>();
    }

    public Map<String, Bean> getBeans(){
        return this.beans;
    }

}
