package com.canoo.grasp

import groovy.beans.Bindable

class Attribute implements IAttribute {

    Attribute(model, String propname, String lookupPrefix) {
        this.model = model
        propertyName = propname
        description = propertyName[0].toUpperCase() + propertyName[1..-1] // call GraspContext lookup
        value = modelValue
    }

    private model
    private String propertyName

    final String description

    @Bindable def value

    def getModelValue() { model[propertyName] }

    boolean isDirty() { value != modelValue }

}
