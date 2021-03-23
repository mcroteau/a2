package n.data;

import java.util.HashMap;
import java.util.Map;

public class ExchangeData {

    Map<String, Object> d;

    public ExchangeData(){
        this.d = new HashMap<>();
    }

    public void put(String key, Object value){
        this.d.put(key, value);
    }

    public Object get(String key){
        if(this.d.containsKey(key)){
            return this.d.get(key);
        }
        return null;
    }

    public Map<String, Object> getD(){
        return this.d;
    }

}
