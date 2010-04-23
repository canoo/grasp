package com.canoo.grasp.demo.components

import com.canoo.grasp.Attribute

/**
 * todo: write javadoc
 */
class StringEditor implements GraspEditor {

    def boolean canHandle(Object attribute, Map attributes) {
        (attribute instanceof Attribute && attribute.value instanceof String)
    }

    def Object newInstance(FactoryBuilderSupport factoryBuilderSupport, Object name, Object attribute, Map map) {
        factoryBuilderSupport.textField(attribute.value)
    }
}
