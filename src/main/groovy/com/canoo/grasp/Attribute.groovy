package com.canoo.grasp

import groovy.beans.Bindable
import java.beans.Introspector

class Attribute implements IAttribute, Cloneable {

    Attribute(model, String propname, String lookupPrefix) {
        this.model = model
        propertyName = propname
        this.lookupPrefix = lookupPrefix
        description = GraspContext.lookup("${lookupPrefix}.${propertyName}.description".toString())
        value = modelValue

        if(model in Map) {
            type = value != null ? value.getClass() : String
        } else if (model){
            type = Introspector.getBeanInfo(model.class).getPropertyDescriptors().find{ it.name == propname }.propertyType
        } else {
            type = String
        }
    }

    private model
    private String propertyName
    final Class type
    final String description
    private final String lookupPrefix

    @Bindable def value
    @Bindable boolean readOnly = false

    def getModelValue() { model[propertyName] }
    Class getValueType() { type }

    Object clone() {
        new Attribute(model, propertyName, lookupPrefix)
    }
}
