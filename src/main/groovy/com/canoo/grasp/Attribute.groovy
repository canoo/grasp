package com.canoo.grasp

import groovy.beans.Bindable
import java.beans.Introspector
import java.beans.PropertyChangeListener

class Attribute implements IAttribute, Cloneable {

    Attribute(model, String propname, String lookupPrefix) {
        this.model = model
        propertyName = propname
        this.lookupPrefix = lookupPrefix
        value = modelValue

        if (model in Map) {
            type = value != null ? value.getClass() : String
        } else if (model) {
            type = Introspector.getBeanInfo(model.class).getPropertyDescriptors().find { it.name == propname }.propertyType
        } else {
            type = String
        }

        GraspLocale.instance.addPropertyChangeListener("locale", listener)
        updateDescription(fetchI18NResource())
        updateLabel(fetchI18NResource('label'))
    }

    private String fetchI18NResource(String key = 'description') {
        String resourceKey = "${lookupPrefix}.${propertyName}.${key}".toString()
        String resource = Grasp.lookup(resourceKey)
        resource != resourceKey ? resource : ""
    }

    private void updateLabel(String lbl) {
        if (!lbl) {
            if(this.label) {
                return
            } else if (propertyName.length() == 1) {
                lbl = propertyName.toUpperCase()
            } else {
                lbl = propertyName[0].toUpperCase() + propertyName[1..-1]
            }
        }
        setLabel(lbl)
    }

    private void updateDescription(String desc){
        if(!desc) {
            if(this.description) {
                return
            } else {
                desc = "${lookupPrefix}.${propertyName}.description".toString()
            }
        }
        setDescription(desc)
    }

    private model
    private String propertyName
    final Class type
    private final String lookupPrefix

    @Bindable String label
    @Bindable String description
    @Bindable def value
    @Bindable boolean readOnly = false

    protected PropertyChangeListener listener = {e ->
        setDescription(fetchI18NResource())
        setLabel(fetchI18NResource('label'))
    } as PropertyChangeListener

    def getModelValue() { model?.getAt(propertyName) }

    Class getValueType() { type }

    Object clone() {
        new Attribute(model, propertyName, lookupPrefix)
    }

    void dispose() {
        GraspLocale.instance.removePropertyChangeListener 'locale', listener
    }
}
