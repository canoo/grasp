package com.canoo.grasp

import groovy.beans.Bindable

class Attribute implements IAttribute {

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

    def getModelValue() { model[propertyName] }


}
