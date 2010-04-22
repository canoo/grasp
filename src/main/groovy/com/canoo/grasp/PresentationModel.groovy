package com.canoo.grasp

class PresentationModel {

    long id
    long version

    PresentationModel() {

        if (properties.containsKey("scaffold")) {
            def emc = new ExpandoMetaClass(this.getClass(), false)
            this.scaffold.metaClass.properties.each { MetaBeanProperty property ->
                
                def fieldname = property.name
                if (!(fieldname in "metaClass class".tokenize())) {
                    try {
                        def pmClassName =  this.getClass().getPackage().getName() + "." + property.type.getSimpleName() + "PM"
                        def pmClass = Class.forName(pmClassName)
                        def instance = pmClass.newInstance()
                        def modelSwitch = new PresentationModelSwitch(instance)
                        emc."$fieldname" = modelSwitch
                    } catch (ClassNotFoundException e) {
                        emc."$fieldname" = null
                    }

                }
            }
            emc.initialize()
            this.metaClass = emc
        }
    }

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
            if (key in 'class scaffold metaClass id version'.tokenize()) return
            this[key] = new Attribute(model, key, this.getClass().name)
        }
    }
}
