package com.canoo.grasp.demo.components

import com.canoo.grasp.Attribute

/**
 * This object displays boolean values as check boxes.  
 */
class BooleanEditor implements GraspEditor {

    def boolean canHandle(Object attribute, Map map) {
        attribute instanceof Attribute && attribute.value instanceof Boolean
    }

    def Object newInstance(FactoryBuilderSupport builder, Object name, Object attribute, Map map) {
        builder.checkBox(selected: attribute.value)
    }
}
