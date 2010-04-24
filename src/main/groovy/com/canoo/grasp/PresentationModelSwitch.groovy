package com.canoo.grasp

import java.beans.PropertyChangeSupport
import java.beans.PropertyChangeListener

class PresentationModelSwitch {

    PresentationModel adaptee
    final Map<String, AttributeSwitch> proxyAttributePerName = [:] // propname to proxyAttribute

    private final PresentationModel defaultPM // in use when no real adaptee is set
    protected final PropertyChangeSupport pcs

    final Class presentationModelClass

    protected final PropertyChangeListener listener = {e ->
        pcs.firePropertyChange e.propertyName, e.oldValue, e.newValue
    } as PropertyChangeListener

    /**
     * @param defaultPM.model may be null
     */
    PresentationModelSwitch(Class clazz) {
        pcs = new PropertyChangeSupport(this)

        presentationModelClass = clazz
        // this.defaultPM = defaultPM
        // setAdaptee defaultPM

        PresentationModelSwitch self = this
        clazz.metaClass.properties.each { property ->
            String fieldname = property.name
            // println ">>> $fieldname"
            if(fieldname == 'scaffold') {
                Class scaffoldedClass = clazz.scaffold
                Map scaffoldMappings = scaffoldMappings(this, clazz, scaffoldedClass)
                scaffoldMappings.each { fname, prop ->
                    if(PresentationModel.isTransientProperty(fname) || fname == 'model') return
                    if(prop instanceof Attribute) {
                        // println "#init $fname $prop.lookupPrefix"
                        def attributeSwitch = new AttributeSwitch()
                        attributeSwitch.attribute = prop
                        attributeSwitch.addPropertyChangeListener listener
                        proxyAttributePerName[fname] = attributeSwitch
                    } else if(prop in LazyPresentationModelSwitch) {
                        // println "ASKING FOR $fname as SWITCH (lazy)"
                        proxyAttributePerName[fname] = prop
                    }
                }
                return
            }
            if(PresentationModel.isTransientProperty(fieldname) || fieldname == 'model') return
            // println "init $fieldname $property.type"
            if(property.type in Attribute) {
                def attributeSwitch = new AttributeSwitch()
                proxyAttributePerName[fieldname] = new AttributeSwitch()
                Attribute attr = new Attribute([(fieldname): null], fieldname, clazz.name)
                attributeSwitch.attribute = attr
                attributeSwitch.addPropertyChangeListener listener
                proxyAttributePerName[fieldname] = attributeSwitch
            } else if(property.type in PresentationModelSwitch) {
                // println "ASKING FOR $fieldname as SWITCH (2)"
                String domainClassName = GraspContext.instance.resolveDomainModelClassName(clazz)
                Class domainClass = Class.forName(domainClassName)
                Class referenceClass = domainClass.metaClass.getMetaProperty(fieldname).type
                String pmClassName = GraspContext.instance.resolvePresentationModelClassName(referenceClass)
                proxyAttributePerName[fieldname] = new LazyPresentationModelSwitch(self, Class.forName(pmClassName), fieldname)
            }
        }
    }

    static Map scaffoldMappings(PresentationModelSwitch ownerSwitch, Class ownerClass, Class scaffoldClass) {
        Map mappings = [:]
        scaffoldClass.metaClass.properties.each {MetaBeanProperty property ->
            def fieldname = property.name
            if (fieldname in ['metaClass', 'class']) return
            try {
                def pmClassName = GraspContext.instance.resolvePresentationModelClassName(property.type)
                def pmClass = Class.forName(pmClassName)
                // println "$fieldname is a SWITCH"
                def modelSwitch = new LazyPresentationModelSwitch(ownerSwitch, pmClass, fieldname)
                mappings[fieldname] = modelSwitch
            } catch (ClassNotFoundException e) {
                def attr = new Attribute([:], fieldname, ownerClass.name)
                // println "$fieldname is an ATTRIBUTE"
                mappings[fieldname] = attr
            }
        }
        mappings
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

    PresentationModel getDefaultValue() {
        return defaultPM
    }

    // todo: REVIEW
    Object clone() {
        def other = getClass().getDeclaredConstructor([Class]).newInstance([presentationModelClass] as Object[])
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
        adaptee?.removePropertyChangeListener listener
        adaptee = newAdaptee // don't make this the last statement or bindable will remove it!
        newAdaptee.properties.each {key, attribute ->
            if (PresentationModel.isTransientProperty(key)) return

            // todo dk: this needs testing!
            if (attribute in PresentationModelSwitch) { // we have reference, so update it
                PresentationModelSwitch reference = attribute
                def switcher = proxyAttributePerName[key]
                if (switcher instanceof PresentationModelSwitch){
                    switcher.adaptee = reference.adaptee
                } else if (switcher instanceof LazyPresentationModelSwitch){
                    switcher.resolve().adaptee = reference.adaptee
                } else {
                    proxyAttributePerName[key] = reference
                }
                // println "PresentationModelSwitch.setAdaptee $key = $reference"
                return
            }

            def proxyAttribute = proxyAttributePerName.get(key, new AttributeSwitch())
            attribute = attribute ?: new Attribute([(key) : newAdaptee[key]], key, newAdaptee.getClass().name, newAdaptee) // for attributes without model 
            proxyAttribute.attribute = attribute
        }
        adaptee.addPropertyChangeListener listener
        oldAdaptee?.dispose()
        firePropertyChange "adaptee", oldAdaptee, newAdaptee
    }

    def propertyMissing(String propname) {
        def v = proxyAttributePerName[propname]
        if(v instanceof LazyPresentationModelSwitch) v = v.resolve()
        v
    }

    boolean available() {
        adaptee != defaultPM
    }

    void delete() {
        adaptee.delete()
        setAdaptee null
    }

    private static class LazyPresentationModelSwitch {
        final Class type
        final String name
        final PresentationModelSwitch owner

        LazyPresentationModelSwitch(PresentationModelSwitch owner, Class type, String name) {
            this.owner = owner
            this.type = type
            this.name = name
        }

        PresentationModelSwitch resolve() {
            PresentationModelSwitch s = new PresentationModelSwitch(type)
            s.addPropertyChangeListener owner.listener
            owner.proxyAttributePerName[name] = s
            s
        }
    }
}