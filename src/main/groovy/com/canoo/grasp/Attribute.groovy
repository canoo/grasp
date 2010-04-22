package com.canoo.grasp

import groovy.beans.Bindable

class Attribute implements IAttribute, Cloneable {

    Attribute(model, String propname, String lookupPrefix) {
        this.model = model
        propertyName = propname
        description = GraspContext.lookup("${lookupPrefix}.${propertyName}.description".toString())
        value = modelValue
    }

    private model
    private String propertyName

    final String description

    @Bindable def value
    @Bindable boolean readOnly = false

    def getModelValue() { model[propertyName] }

    Object clone() {
        new Attribute(model, propName, lookupPrefix)
    }
}
