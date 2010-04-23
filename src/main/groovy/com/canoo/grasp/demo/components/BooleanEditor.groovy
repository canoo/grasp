package com.canoo.grasp.demo.components

import com.canoo.grasp.Attribute

/**
 * Todo: javadoc
 */
class BooleanEditor implements GraspEditor {

    def boolean canHandle(Object attribute, Map map) {
        attribute instanceof Attribute && attribute.value instanceof Boolean
    }

    def Object newInstance(FactoryBuilderSupport factoryBuilderSupport, Object name, Object attribute, Map map) {
        factoryBuilderSupport.checkBox(selected: attribute.value)
    }
}
