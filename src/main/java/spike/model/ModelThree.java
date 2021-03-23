package spike.model;

import n.annotate.Element;
import n.annotate.Inject;

@Element
public class ModelThree {

    @Inject
    ModelOne modelOne;

    public ModelOne getModelOne() {
        return modelOne;
    }

}
