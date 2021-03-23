package n.model;

import java.util.List;

public class Dependencies {

    List<Dependency> dependencies;

    public List<Dependency> getSources() {
        return dependencies;
    }

    public void setSources(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }
}
