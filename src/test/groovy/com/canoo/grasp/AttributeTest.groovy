package com.canoo.grasp

import java.beans.PropertyChangeListener
import spock.lang.Specification

class AttributeTest extends Specification {

    Attribute attribute
    PropertyChangeListener changeListener

    void setup(){
        Grasp.initialize()
        Map model = [a: 1]
        attribute = new Attribute(model, 'a', 'prefix')
        changeListener = Mock(PropertyChangeListener)
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

    void "listener is notified when an attribute value changes"() {
        when:
            attribute.addPropertyChangeListener("value", changeListener)
            def oldValue = attribute.value
            def newValue = oldValue + 1
            attribute.value = newValue
        then:
            1 * changeListener.propertyChange(_)
    }
}