package com.canoo.grasp

import groovy.beans.Bindable

class PresentationModelSwitch extends PresentationModel {

    @Bindable PresentationModel adaptee
    final Map<String, AttributeSwitch> proxyAttributePerName = [:] // propname to proxyAttribute

    private final PresentationModel defaultPM // in use when no real adaptee is set

    /**
     * @param defaultPM.model may be null
     */
    PresentationModelSwitch(PresentationModel defaultPM) {
        this.defaultPM = defaultPM
        setAdaptee defaultPM
    }

    /**
     * @param newAdaptee may be null, in which case the defaultPM is used
     */
    void setAdaptee(PresentationModel newAdaptee) {
        if (newAdaptee == null) newAdaptee = defaultPM
        if (newAdaptee == adaptee) return
        adaptee = newAdaptee // don't make this the last statement or bindable will remove it!
        newAdaptee.properties.each {key, attribute ->
            if (key in 'class metaClass id version'.tokenize()) return

            if (attribute in PresentationModelSwitch) { // we have reference, so update it
                def newPM = attribute.adaptee.getClass().newInstance()
                def proxyAttribute = proxyAttributePerName.get(key, attribute)
                if (attribute.model) newPM.model = attribute.model
                proxyAttribute.adaptee = newPM
                println "have set"
                return
            }

            def proxyAttribute = proxyAttributePerName.get(key, new AttributeSwitch())
            attribute = attribute ?: new Attribute([:], key, newAdaptee.getClass().name) // for attributes without model 
            proxyAttribute.attribute = attribute
        }
    }

    def propertyMissing(String propname) {
        proxyAttributePerName[propname]
    }

    void setModel(Object model) {
        throw new UnsupportedOperationException("cannot set model on proxy")
    }

    boolean available() {
        adaptee != defaultPM
    }

    void delete() {
        adaptee.delete()
        setAdaptee null
    }
}