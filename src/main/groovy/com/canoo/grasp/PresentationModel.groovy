package com.canoo.grasp

import com.canoo.grasp.demo.PublisherPM

class PresentationModel {

    long id
    long version

    /**
     * Setting a model automatically attaches new Attribute objects to all properties of this
     * Presentation model backed by the model.
     * @param model can be anything that exposes properties, e.g. Grails domain objects or a simple map
     * @throws MissingPropertyException if the model has no property that the presentation model claims to reflect
     */
    void setModel(Object model) { // todo: check. This is probably called erroneously with a PM, not a backing model...
        properties.each { key, value ->
            if (value in PresentationModelSwitch) { // todo: check (Andres)
                def newPM = value.adaptee.getClass().newInstance()
                newPM.model = model[key]
                value.adaptee = newPM
                return
            }
            if (key in 'class metaClass id version'.tokenize()) return
            this[key] = new Attribute(model, key, this.getClass().name)
        }
    }
}
