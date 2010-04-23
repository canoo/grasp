package com.canoo.grasp

import spock.lang.Specification
import com.canoo.grasp.swing.AttributeColumn

class AttributeColumnTest extends Specification {

    PresentationModel pm
    Integer propertyValue = 1
    String propertyName = 'attribute'
    AttributeColumn column

    void setup() {
        Grasp.initialize()
        def model = [(propertyName): propertyValue]
        pm = new OneSimpleAttributePM(model: model)
        column = new AttributeColumn(OneSimpleAttributePM)
        column.bind = {pm -> pm.attribute}
    }

    def """The name of an AttributeColumn is the attribute's label, properly capitalized
        if no i18n is available"""() {
        expect:
            column.name() == 'Attribute'
    }

    def """The type of an AttributeColumn is 'String' if the model is a map"""(){
        expect:
            column.type() == String
    }

    def """The type of an AttributeColumn matches the model property type if the model
        is not a map""" (){
        when:
            def owner = new Owner(firstname: "Hamlet", lastname: "D'Arcy")
            def now = new Date()
            pm = new CarPM(model: new Car(wheels: 4, brand: new Brand(name: "Ford"), color: Color.RED, year: now, owner: owner))
            column = new AttributeColumn(CarPM)
            column.bind = {pm -> pm.year}

        then:
            column.type() == Date
    }

    def """AttributeColumns should return the attribute's value"""(){
        expect:
            column.getValue(pm) == propertyValue
    }

    def """AttributeColumns can modify the attribute's value"""(){
        when:
            def otherValue = 2
            column.setValue(pm, otherValue)
        then:
            pm.attribute.value == otherValue
    }

    def """AttributeColumns cannot change the editable state of an attribute if the attribute
        is read-only"""(){
        when:
            pm.attribute.readOnly = true
            column.editable = true

        then:
            !column.isEditable(pm)
    }

    def """AttributeColumns can override the editable property of an attribute if the attribute
        is not read-only"""(){
        when:
            assert pm.attribute.readOnly == false
            assert column.isEditable(pm)
            column.editable = false

        then:
            !column.isEditable(pm)
    }

    def """The read closure of an AttributeColumn can be changed"""(){
        when:
            column.read = {attr -> attr.label}
        then:
            column.getValue(pm) == 'Attribute'
    }

    def """The write closure of an AttributeColumn can be changed"""(){
        when:
            column.write = {attr, value -> attr.label = value}
            assert pm.attribute.label == 'Attribute'
            String someValue = 'someValue'
            column.setValue(pm, someValue)
        then:
            pm.attribute.label = someValue
    }
}
