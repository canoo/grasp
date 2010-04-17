package com.canoo.grasp

import java.beans.PropertyChangeListener
import spock.lang.Specification

class AttributeSwitchTest extends Specification {

    // todo dk: these tests should all be combined with PCLs on the _attribute_

    AttributeSwitch switcher
    PropertyChangeListener changeListenerMock


    void setup(){
        Map model = [a: 1]
        Attribute attribute = new Attribute(model, 'a', 'prefix')
        switcher = new AttributeSwitch(attribute: attribute)
        changeListenerMock = Mock(PropertyChangeListener)
    }

    Attribute otherDelegateWithNotification() {
        int newValue = switcher.value + 1
        def other = new Attribute([a: newValue], switcher.propertyName, null) // todo dk: shall we test that the propertyName remains constant over switches?
        switcher.addPropertyChangeListener("value", changeListenerMock)
        switcher.attribute = other              // setting new attribute notifies listeners
        return other
    }

    def 'An AttributeSwitch should behave like the Attribute that it delegates to. It is a decorator'() {
        expect:
            switcher.value == switcher.attribute.value
            switcher.description == 'prefix.a.description'
            switcher.propertyName == 'a'
    }

    def '''When an AttributeSwitch points to a new Attribute,
            its listeners must be notified and
            the switch no longer listens to the value changes in the old attribute
            but to changes in the new one.'''() {

        when:
            def initialAttribute = switcher.attribute
            assert initialAttribute.propertyChangeListeners.size() == 1, "only the switcher listens to attribute value changes"
            def other = otherDelegateWithNotification()

        then:
            1 * changeListenerMock.propertyChange(_)
            switcher.value == other.value
            initialAttribute.propertyChangeListeners == []
            other.propertyChangeListeners.size() == 1     // switch now listens to other delegate
            initialAttribute.value == 1                   // old attribute remains unaffected
    }

    def 'Setting a new value through a switch must notify the switch listeners'() {

        when:
            def other = otherDelegateWithNotification()
            def newValue = switcher.value + 1
            switcher.value = newValue                   // set new value through switch
        then:
            2 * changeListenerMock.propertyChange(_)    // attribute change plus value change
            switcher.value == newValue
            other.value == newValue
    }

    void 'Setting a new value on an Attribute must trigger the AttributeSwitch to notify its listeners'(){

        when:
            def other = otherDelegateWithNotification()
            def newValue = switcher.value + 1
            other.value = newValue                      // set new value in attribute
        then:
            2 * changeListenerMock.propertyChange(_)    // attribute change plus value change
            switcher.value == newValue
    }

}
