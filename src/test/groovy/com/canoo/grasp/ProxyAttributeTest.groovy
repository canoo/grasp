package com.canoo.grasp

import java.beans.PropertyChangeListener

class ProxyAttributeTest extends GroovyTestCase {

    void testDelegation() {
        Attribute attribute = new Attribute([a: 1], 'a', 'prefix')
        AttributeSwitch proxy = new AttributeSwitch(attribute: attribute)
        assertEquals 1, attribute.propertyChangeListeners.size()

        int callCount = 0
        def update = { callCount++ }
        proxy.addPropertyChangeListener("value", update as PropertyChangeListener)

        assertEquals 1, proxy.value
        assertEquals 'prefix.a.description', proxy.description

        assertEquals 0, callCount
        Attribute newAttribute = new Attribute([a: 2], 'a', 'prefix')
        proxy.attribute = newAttribute
        assertEquals 1, callCount
        assertEquals 2, proxy.value
        assertEquals 0, attribute.propertyChangeListeners.size()
        assertEquals 1, newAttribute.propertyChangeListeners.size()
        assertEquals 1, attribute.value

        proxy.value = 3
        assertEquals 2, callCount
        assertEquals 3, proxy.value
        assertEquals 3, newAttribute.value

        newAttribute.value = 4
        assertEquals 3, callCount
        assertEquals 4, proxy.value
    }
}
