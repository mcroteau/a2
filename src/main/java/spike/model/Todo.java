package spike.model;

public class Todo {

    public Todo(){}

    public Todo(Integer id, String todo, Boolean finished){
        this.id = id;
        this.todo = todo;
        this.finished = finished;
    }

    Integer id;
    String todo;
    Boolean finished;
    Boolean onTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTodo() {
        return todo;
    }

    public void setTodo(String todo) {
        this.todo = todo;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public Boolean getOnTime() {
        return onTime;
    }

    public void setOnTime(Boolean onTime) {
        this.onTime = onTime;
    }

}
