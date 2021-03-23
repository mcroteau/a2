package spike.config;

import n.annotate.Dependency;
import spike.model.ModelOne;
import spike.model.ModelTwo;

/**
 *
 * Because you passed SimpleConfig as
 * a config option to the application,
 * no additional annotations required.
 */
public class SimpleConfig {

    @Dependency
    public ModelOne modelOne(){
        ModelOne modelOne = new ModelOne();
        modelOne.setName("Model One");
        return modelOne;
    }

    @Dependency
    public ModelTwo modelTwo(){
        ModelTwo modelTwo = new ModelTwo();
        modelTwo.setName("Model Two");
        modelTwo.setModelOne(modelOne());
        return modelTwo;
    }

}
