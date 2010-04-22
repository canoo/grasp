package com.canoo.grasp.demo.components

import com.canoo.grasp.Attribute

/**
 * Created by IntelliJ IDEA.
 * User: johannes
 * Date: Apr 22, 2010
 * Time: 9:41:30 PM
 * To change this template use File | Settings | File Templates.
 */
static class LocaleEditor implements GraspEditor { /**
 * Created by IntelliJ IDEA.
 * User: johannes
 * Date: Apr 22, 2010
 * Time: 9:41:30 PM
 * To change this template use File | Settings | File Templates.
 */
    def boolean canHandle(Object value, Map attributes) {
        (value instanceof Attribute && value.value instanceof Locale)
    }

    def Object newInstance(FactoryBuilderSupport factoryBuilderSupport, Object name, Object attribute, Map map) {
        factoryBuilderSupport.label(attribute.value.toString())
    }
}
