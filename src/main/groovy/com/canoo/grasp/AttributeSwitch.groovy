package com.canoo.grasp

import groovy.beans.Bindable
import java.beans.PropertyChangeListener

class AttributeSwitch implements IAttribute, Cloneable {

    private Attribute attribute
    private relay = true // do not relay the attribute value change if we triggered it ourselves
    private PropertyChangeListener listener = { event ->
        if (relay) this$propertyChangeSupport.firePropertyChange event
    } as PropertyChangeListener

    void setAttribute(Attribute newAttribute) {
        attribute?.removePropertyChangeListener listener
        def oldValue = value
        attribute = newAttribute
        attribute.addPropertyChangeListener listener
        this$propertyChangeSupport.firePropertyChange('value', oldValue, value)
    }

    def propertyMissing(String name) {
        attribute[name]
    }

    @Bindable def value // notifies listeners even though the setter is implemented

    void setValue(newValue) {
        relay = false
        attribute?.value = newValue
        relay = true
    }

    def getValue() { attribute?.value }

    def getModelValue() { attribute?.modelValue }

    Object clone() {
        def other = new AttributeSwitch()
        other.attribute = attribute?.clone()
        other.value = value
        other
    }
}