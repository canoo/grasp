package com.canoo.grasp

import java.beans.PropertyChangeListener

class AttributeSwitchTest extends GroovyTestCase {

    // todo dk: these tests should all be combined with PCLs on the _attribute_

    AttributeSwitch switcher
    int callCount = 0           // for use in count listener

    void setUp(){
        Map model = [a: 1]
        Attribute attribute = new Attribute(model, 'a', 'prefix')
        switcher = new AttributeSwitch(attribute: attribute)
    }

    def attachCountListener() {
        def update = { callCount++ }
        switcher.addPropertyChangeListener("value", update as PropertyChangeListener)
        assertEquals 0, callCount
    }

    Attribute otherDelegateWithNotification() {
        int newValue = switcher.value + 1
        def other = new Attribute([a: newValue], switcher.propertyName, null) // todo dk: shall we test that the propertyName remains constant over switches?
        attachCountListener()
        switcher.attribute = other              // setting new attribute notifies listeners
        return other
    }

    void testSwitchBehavesAsDelegate() {
        assert switcher.value == switcher.attribute.value
        assert switcher.description == 'prefix.a.description'
        assert switcher.propertyName == 'a'
    }

    void testAttributeChangeOnSwitchNotifiesSwitchListeners() {
        def initialAttribute = switcher.attribute
        assert initialAttribute.propertyChangeListeners.size() == 1, "only the switcher listens to attribute value changes"

        def other = otherDelegateWithNotification()

        assert callCount == 1
        assert switcher.value == other.value
        assert initialAttribute.propertyChangeListeners == []
        assert other.propertyChangeListeners.size() == 1     // switch now listens to other delegate
        assert initialAttribute.value == 1                   // old attribute remains unaffected
    }

    void testSettingNewValueOnSwitchNotifiesSwitchListeners() {
        def other = otherDelegateWithNotification()

        def newValue = switcher.value + 1
        switcher.value = newValue                   // set new value through switch
        assert callCount == 2                       // attribute change plus value change
        assert switcher.value == newValue
        assert other.value == newValue
    }

    void testSettingNewValueOnDelegateNotifiesSwitchListeners(){
        def other = otherDelegateWithNotification()

        def newValue = switcher.value + 1
        other.value = newValue                      // set new value in attribute
        assert callCount == 2                       // attribute change plus value change
        assert switcher.value == newValue
    }

}
