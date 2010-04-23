package com.canoo.grasp

import com.canoo.grasp.demo.domain.Errors
import java.beans.PropertyChangeListener

class PresentationModel implements Cloneable {
    // protected static final Log LOG = LogFactory.getLog(PresentationModel)

    long id
    long version
    Attribute errors

    protected final PropertyChangeSupport pcs
    private static final String PROTO_PROPERTY_NAME = '_PM_PROTOYPE_'

    static isTransientProperty(String key) {
        key in ["class", "metaClass", "scaffold", "errors", "constraints", "id", "version", "listener", "_PM_PROTOYPE_"]
    }

    protected final PropertyChangeListener listener = {e ->
        pcs.firePropertyChange e.propertyName, e.oldValue, e.newValue
    } as PropertyChangeListener

    Object clone() {
        def other = getClass().newInstance()
        other.id = id
        other.version = version
        properties.each {key, value ->
            if (value in [Attribute, AttributeSwitch, PresentationModelSwitch]) {
                other[key] = value.clone()
            }
        }
        other
    }

    void dispose() {
        properties.each {key, value ->
            if (value in [Attribute, AttributeSwitch, PresentationModelSwitch]) {
                value.dispose()
            }
        }
    }

    PresentationModel() {
        errors = new Attribute(new Errors(errors: [] as Set), "errors", "grasp.errors", this)

        pcs = new PropertyChangeSupport(this)
        // LOG.trace "BUILDING ${getClass().name}"
        if (properties.containsKey("scaffold")) {
            def emc = new ExpandoMetaClass(this.getClass(), false)
            
            this.scaffold.metaClass.properties.each {MetaBeanProperty property ->
                def fieldname = property.name
                if (!(fieldname in "metaClass class".tokenize())) {
                    try {
                        def pmClassName = GraspContext.instance.resolvePresentationModelClassName(property.type)
                        def pmClass = Class.forName(pmClassName)
                        // LOG.trace "$fieldname is a SWITCH"
                        def modelSwitch = new PresentationModelSwitch(pmClass)
                        emc."$fieldname" = modelSwitch
                        modelSwitch.addPropertyChangeListener listener
                    } catch (ClassNotFoundException e) {
                        def attr = new Attribute([:], fieldname, this.getClass().name, this)
                        // LOG.trace "$fieldname is an ATTRIBUTE"
                        attr.addPropertyChangeListener listener
                        emc."$fieldname" = attr
                    }
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
        properties.each {key, value ->
            if (value in PresentationModelSwitch) { // todo: check (Andres)
                if (model == null) return
                def npm = value.presentationModelClass.newInstance()
                npm.model = model?.getAt(key)
                value.adaptee?.dispose()
                value.adaptee = npm
                return
            }
            if (isTransientProperty(key)) return
            this[key]?.removePropertyChangeListener listener
            this[key]?.dispose()
            this[key] = new Attribute(model, key, this.getClass().name, this)
            this[key].addPropertyChangeListener listener
        }
    }

    static PresentationModel fetchPrototype(Class clazz) {
        assert clazz in PresentationModel

        MetaClass mc = clazz.metaClass
        MetaProperty protoProperty = mc.getMetaProperty(PROTO_PROPERTY_NAME)
        if (protoProperty) return protoProperty.getProperty(clazz)
        def proto = mc.respondsTo(clazz, 'prototype') ? clazz.prototype() : initializePrototype(clazz.newInstance())
        mc."$PROTO_PROPERTY_NAME" = proto
        return proto
    }

    static PresentationModel initializePrototype(PresentationModel pm) {
        def mc = pm.getClass().metaClass
        if (mc.hasProperty(pm, 'scaffold')) {
            pm.model = pm.getClass().scaffold.newInstance()
            return pm
        }

        try {
            String modelClassName = pm.getClass().name - 'PM'
            Class modelClass = Class.forName(modelClassName)
            def model = modelClass.newInstance()
            pm.model = model
            return pm
        } catch (x) {
            // ignore
        }

        pm.model = [:]
        pm
    }

    List fetchModelReferences() {
        properties.inject([]) {list, entry ->
            if (entry.value in PresentationModelSwitch) {
                list << entry.value
            }
            list
        }
    }

}
