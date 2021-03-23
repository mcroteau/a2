package spike.service;

import n.annotate.Service;
import spike.model.Todo;

import java.util.ArrayList;
import java.util.List;

@Service
public class TodoService {

    public List<Todo> index() {
        String sql = "select * from todos";
        return new ArrayList<>();
    }
}
