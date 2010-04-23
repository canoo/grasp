package com.canoo.grasp

import java.beans.PropertyChangeSupport
import java.beans.PropertyChangeListener

class PresentationModel implements Cloneable {

    long id
    long version

    protected final PropertyChangeSupport pcs
    private static final String PROTO_PROPERTY_NAME = '_PM_PROTOYPE_'

    static isTransientProperty(String key) {
        key in ["class", "metaClass", "scaffold", "constraints", "id", "version", "listener", "_PM_PROTOYPE_"]
    }

    protected final PropertyChangeListener listener = {e ->
        pcs.firePropertyChange(e.propertyName, e.oldValue, e.newValue)
    } as PropertyChangeListener

    Object clone() {
        def other = getClass().newInstance()
        other.id = id
        other.version = version
        properties.each { key, value ->
            if(value in [Attribute, AttributeSwitch, PresentationModelSwitch]) {
                other[key] = value.clone()
            }
        }
        other
    }

    void dispose() {
        properties.each { key, value ->
            if(value in [Attribute, AttributeSwitch, PresentationModelSwitch]) {
                 value.dispose()
            }
        }
    }
    
    PresentationModel() {
        pcs = new PropertyChangeSupport(this)

        if (properties.containsKey("scaffold")) {
            def emc = new ExpandoMetaClass(this.getClass(), false)
            this.scaffold.metaClass.properties.each { MetaBeanProperty property ->
                def fieldname = property.name
                if (!(fieldname in "metaClass class".tokenize())) {
                    try {
                        def pmClassName =  this.getClass().getPackage().getName() + "." + property.type.getSimpleName() + "PM"
                        def pmClass = Class.forName(pmClassName)
                        def instance = fetchPrototype(pmClass).clone()
                        instance.model = [:]
                        def modelSwitch = new PresentationModelSwitch(instance)
                        emc."$fieldname" = modelSwitch
                    } catch (ClassNotFoundException e) {
                        emc."$fieldname" = new Attribute([:], fieldname, this.getClass().name)
                    }
                    // emc."$fieldname".addPropertyChangeListener listener
                }
            }
            emc.initialize()
            this.metaClass = emc
        }
    }

    void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener l
    }

    void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener l
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        pcs.firePropertyChange propertyName, oldValue, newValue
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
                def newPM = PresentationModel.fetchPrototype(value.adaptee.getClass()).clone()
                newPM.model = model[key]
                value.adaptee.dispose()
                value.adaptee = newPM
                return
            }
            if (isTransientProperty(key)) return
            this[key]?.removePropertyChangeListener listener
            this[key]?.dispose()
            this[key] = new Attribute(model, key, this.getClass().name)
            this[key].addPropertyChangeListener listener
        }
    }

    static PresentationModel fetchPrototype(Class clazz) {
        assert clazz in PresentationModel

        MetaClass mc = clazz.metaClass
        MetaProperty protoProperty = mc.getMetaProperty(PROTO_PROPERTY_NAME)
        if(protoProperty) return protoProperty.getProperty(clazz)
        def proto = mc.respondsTo(clazz, 'prototype') ? clazz.prototype(): initializePrototype(clazz.newInstance())
        mc."$PROTO_PROPERTY_NAME" = proto
        return proto
    }

    static PresentationModel initializePrototype(PresentationModel pm) {
        def inspectPm = null
        inspectPm = {target ->
            def accum = [:]
            target.properties.inject([:]) { map, entry ->
                if (isTransientProperty(entry.key)) return map
                MetaProperty mp = target.metaClass.getMetaProperty(entry.key) 
                def type = mp ? mp.getProperty(target) : target.class.getDeclaredField(entry.key).type
                if(type in PresentationModelSwitch) map[(entry.key)] = entry.value.adaptee
                map
            }.each { key, type ->
                accum[key] = inspectPm(type)
            }
            accum
        }
        pm.model = inspectPm(pm)
        pm
    }
}
