package com.canoo.grasp

import java.beans.PropertyChangeListener

class AttributeTest extends GroovyTestCase {

    Attribute attribute
    int callCount = 0           // for use in count listener

    void setUp(){
        Map model = [a: 1]
        attribute = new Attribute(model, 'a', 'prefix')
    }

    def attachCountListener() {
        def update = { callCount++ }
        attribute.addPropertyChangeListener("value", update as PropertyChangeListener)
        assert callCount == 0
    }

    void testAttributeAccess() {
        assert attribute.value == 1
        assert attribute.description == 'prefix.a.description'
        assert attribute.modelValue == 1
    }

    void testValueChangeLetsModelValueUnaffected() {
        def oldValue = attribute.value
        def newValue = oldValue + 1
        attribute.value = newValue
        assert attribute.value == newValue
        assert attribute.modelValue == oldValue
    }

    void testValueChangeNotifiesAttachedListener() {
        attachCountListener()
        testValueChangeLetsModelValueUnaffected()
        assert callCount == 1
    }

}