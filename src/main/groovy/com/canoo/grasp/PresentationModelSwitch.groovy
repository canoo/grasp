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

    PresentationModel getDefaultValue() {
        return defaultPM
    }

    Object clone() {
        def other = getClass().getDeclaredConstructor([PresentationModel]).newInstance([defaultPM] as Object[])
        other.id = id
        other.version = version
        properties.each { key, value ->
            if(value in [Attribute, AttributeSwitch, PresentationModelSwitch]) {
                other[key] = value.clone()
            }
        }
        other.adaptee = adaptee.clone()
        other
    }

    /**
     * @param newAdaptee may be null, in which case the defaultPM is used
     */
    void setAdaptee(PresentationModel newAdaptee) {
        if (newAdaptee == null) newAdaptee = defaultPM
        if (newAdaptee == adaptee) return
        def oldAdaptee = adaptee
        adaptee = newAdaptee // don't make this the last statement or bindable will remove it!
        newAdaptee.properties.each {key, attribute ->
            if (PresentationModel.isTransientProperty(key)) return

            // todo dk: this needs testing!
            if (attribute in PresentationModelSwitch) { // we have reference, so update it
                PresentationModelSwitch reference = attribute
                def switcher = proxyAttributePerName[key]
                if (switcher){
                    switcher.adaptee = reference.adaptee
                } else {
                    proxyAttributePerName[key] = reference
                }
                // println "PresentationModelSwitch.setAdaptee $key = $reference"
                return
            }

            def proxyAttribute = proxyAttributePerName.get(key, new AttributeSwitch())
            attribute = attribute ?: new Attribute([(key) : newAdaptee[key]], key, newAdaptee.getClass().name) // for attributes without model 
            proxyAttribute.attribute = attribute
        }

/*        def moveListeners = null
        moveListeners = {oldA, newA ->
            oldA?.properties.findAll {it.value in PresentationModelSwitch}.each {key, pms ->
                pms.proxyAttributePerName.values().each {att ->
                    println att
                    if (att in AttributeSwitch) {
                        def listeners = att.getPropertyChangeListeners()
                        listeners.each { l ->
                            // att.removePropertyChangeListener(l)
                            // newA[key].proxyAttributePerName[att.attribute.propertyName].addPropertyChangeListener(l)
                        }
                    } else if (att in PresentationModelSwitch) {
                        // moveListeners(att, newA[key])
                    }
                }
            }
            // println "move from $oldAdaptee to $newAdaptee"
        }
        moveListeners(oldAdaptee, newAdaptee)*/
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