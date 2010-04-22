package com.canoo.grasp

class PresentationModel implements Cloneable {

    long id
    long version

    private static final String protoPropertyName = '_PM_PROTOYPE_'

    static isTransientProperty(String key) {
        key in ["class", "metaClass", "id", "version", "_PM_PROTOYPE_"]
    }

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
                value.adaptee = newPM
                return
            }
            if (isTransientProperty(key)) return
            this[key] = new Attribute(model, key, this.getClass().name)
        }
    }

    static PresentationModel fetchPrototype(Class clazz) {
        assert clazz in PresentationModel

        MetaClass mc = clazz.metaClass
        MetaProperty protoProperty = mc.getMetaProperty(protoPropertyName)
        if(protoProperty) return protoProperty.getProperty(clazz)
        def proto = mc.respondsTo(clazz, 'prototype') ? clazz.prototype(): initializePrototype(clazz.newInstance())
        mc."$protoPropertyName" = proto
        return proto
    }

    static PresentationModel initializePrototype(PresentationModel pm) {
        def inspectPm = null
        inspectPm = {target ->
            def accum = [:]
            target.properties.inject([:]) { map, entry ->
                if (isTransientProperty(entry.key)) return map
                def type = target.class.getDeclaredField(entry.key).type
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
