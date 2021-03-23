package spike.web;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import n.annotate.HttpHandler;
import n.annotate.Inject;
import n.annotate.Json;
import n.annotate.Variable;
import n.annotate.verbs.Get;
import n.annotate.verbs.Post;
import n.data.ExchangeData;
import n.jdbc.Q;
import n.support.Helper;
import spike.model.Todo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@HttpHandler
public class TodoHandler {

    @Inject
    Q q;

    Gson gson = new Gson();

    @Get("/")
    public String index(HttpExchange exchange, Map<String, Object> data){
        return "pages/index.html";
    }

    @Get("/todos")
    public String todos(HttpExchange exchange,
                         Map<String, Object> data){

        String sql = "select * from todos";
        List<Todo> todos = (ArrayList) q.list(sql, new Object[]{}, Todo.class);
        data.put("todos", todos);
        if(todos.size() > 0)data.put("todosExist", true);
        return "pages/todo/index.html";
    }


    @Get("/todos/create")
    public String create(HttpExchange exchange,
                          Map<String, Object> data){
        return "pages/todo/create.html";
    }


    @Post("/todos/save")
    public String save(HttpExchange exchange,
                        Map<String, Object> data) throws Exception{

        Map<String, String> attributes = Helper.parse(exchange);
        String todo = attributes.get("todo").replace("'", "''");

        if(todo == null ||
                todo.equals("")){
            data.put("message", "Todo was empty.");
            return "redirect:/todos/create";
        }

        String sql = "insert into todos (todo) values ('{}')";
        q.save(sql, new Object[]{ todo });

        String retrieveSql = "select * from todos order by id desc limit 1";
        Todo storedTodo = (Todo) q.get(retrieveSql, new Object[]{}, Todo.class);

        data.put("message", "Successfully saved Todo");
        return "redirect:/todos/edit/" + storedTodo.getId();
    }

    @Get("/todos/edit/{{id}}")
    public String get(HttpExchange exchange,
                       Map<String, Object> data,
                       @Variable Integer id){
        String sql = "select * from todos where id = {}";
        Object[] parameters = new Object[]{id};
        Todo todo = (Todo) q.get(sql, parameters, Todo.class);
        data.put("t", todo);
        return "pages/todo/edit.html";
    }

    @Post("/todos/update/{{id}}")
    public String update(HttpExchange exchange,
                          Map<String, Object> data,
                          @Variable Integer id) throws Exception{

        String sql = "select * from todos where id = {}";
        Object[] parameters = new Object[]{id};

        Todo storedTodo = (Todo) q.get(sql, parameters, Todo.class);

        Map<String, String> attributes = Helper.parse(exchange);
        String finishedOnOff =  attributes.get("finished");

        Boolean finished = false;
        if(finishedOnOff != null && finishedOnOff.equals("on"))finished = true;
        storedTodo.setFinished(finished);
        storedTodo.setTodo(attributes.get("todo"));

        storedTodo.setTodo(storedTodo.getTodo().replace("'", "''"));
        String updateSql = "update todos set todo = '{}', finished = {} where id = {}";
        Object[] updateParams = new Object[]{
                storedTodo.getTodo(), storedTodo.getFinished(), storedTodo.getId()
        };
        q.update(updateSql, updateParams);
        data.put("message", "Successfully updated todo");
        return "redirect:/todos/edit/" + id;
    }


    @Post("/todos/delete/{{id}}")//Jetty does not support Put or Delete
    public String delete(HttpExchange exchange,
                         Map<String, Object> data,
                         @Variable Integer id){
        String sql = "delete from todos where id = {}";
        q.delete(sql, new Object[]{id});
        data.put("message", "Successfully deleted Todo id:" + id);
        return "redirect:/todos";
    }

    @Json
    @Get("/json")
    public String json(HttpExchange exchange, Map<String, Object> data){
        System.out.println("hi");
        List<Todo> todos = Arrays.asList(
                new Todo(1, "Uno", false),
                new Todo(2, "Dos", false),
                new Todo(3, "Tres", true)
        );
        return gson.toJson(todos);
    }
}
