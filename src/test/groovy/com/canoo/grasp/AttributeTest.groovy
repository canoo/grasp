package com.canoo.grasp

import java.beans.PropertyChangeListener
import spock.lang.Specification

class AttributeTest extends Specification {

    Attribute attribute
    int callCount = 0           // for use in count listener

    void setup(){
        Map model = [a: 1]
        attribute = new Attribute(model, 'a', 'prefix')
    }

    def attachCountListener() {
        def update = { callCount++ }
        attribute.addPropertyChangeListener("value", update as PropertyChangeListener)
        assert callCount == 0
    }

    def "attributes can be accessed"() {
        expect:
            attribute.value == 1
            attribute.description == 'prefix.a.description'
            attribute.modelValue == 1
    }

    def "model remains unchanged when attribute is updated"() {
        when:
            def oldValue = attribute.value
            def newValue = oldValue + 1
            attribute.value = newValue
        then:
            attribute.value == newValue
            attribute.modelValue == oldValue
    }

    void "testValueChangeNotifiesAttachedListener"() {
        when:
            attachCountListener()
            def oldValue = attribute.value
            def newValue = oldValue + 1
            attribute.value = newValue
        then:
        assert callCount == 1
    }
}