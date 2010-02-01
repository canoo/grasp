package com.canoo.grasp

class PresentationModel {

    long id
    long version

    /**
     * Setting a model automatically attaches new Attribute objects to all properties of this
     * Presentation model backed by the model.
     * @param model can be anything that exposes properties, e.g. Grails domain objects or a simple map
     * @throws MissingPropertyException if the model has no property that the presentation model claims to reflect
     */
    void setModel(Object model) {
        properties.each { key , value ->
            if (key in 'class metaClass id version'.tokenize()) return
            this[key] = new Attribute(model, key, this.getClass().name)
        }
    }

    // only for the moment to allow instance delete method
    void delete() {
        getClass().delete this // relay over dynamic static method
    }

}
