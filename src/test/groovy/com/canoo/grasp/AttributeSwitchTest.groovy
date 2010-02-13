package com.canoo.grasp

import java.beans.PropertyChangeListener

class AttributeSwitchTest extends GroovyTestCase {

    AttributeSwitch switcher

    void setUp(){
        Map model = [a: 1]
        Attribute attribute = new Attribute(model, 'a', 'prefix')
        switcher = new AttributeSwitch(attribute: attribute)
    }

    void testSimpleDelegation() {
        assertEquals 1, switcher.value
        assertEquals 'prefix.a.description', switcher.description
    }

    void testPropertyChangeListeners() {
        def initialAttribute = switcher.attribute
        assertEquals 1, initialAttribute.propertyChangeListeners.size()

        int callCount = 0
        def update = { callCount++ }
        switcher.addPropertyChangeListener("value", update as PropertyChangeListener)
        assertEquals 0, callCount

        Attribute newAttribute = new Attribute([a: 2], 'a', 'prefix')
        switcher.attribute = newAttribute
        assertEquals 1, callCount                                       // setting new attribute notifies listeners
        assertEquals 2, switcher.value                                  // new value is returned
        assertEquals 0, initialAttribute.propertyChangeListeners.size() // old listeners are removed
        assertEquals 1, newAttribute.propertyChangeListeners.size()     // new listeners are attached
        assertEquals 1, initialAttribute.value                          // old attribute remains unaffected

        switcher.value = 3                  // set new value through switch
        assertEquals 2, callCount           // notifies listeners
        assertEquals 3, switcher.value      // new value is returned
        assertEquals 3, newAttribute.value  // new value is set

        newAttribute.value = 4              // set new value in attribute
        assertEquals 3, callCount           // notifies listeners
        assertEquals 4, switcher.value      // new value is returned
    }
}
