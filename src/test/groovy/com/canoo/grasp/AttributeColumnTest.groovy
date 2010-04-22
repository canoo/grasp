package com.canoo.grasp

import spock.lang.Specification
import com.canoo.grasp.swing.AttributeColumn

class AttributeColumnTest extends Specification {

    // name
    // type
    // get/set value
    // specify which attr from the pm
    // override editable
    // override read/write

    PresentationModel pm
    Integer propertyValue = 1
    String propertyName = 'attribute'
    AttributeColumn column

    void setup() {
        def model = [(propertyName): propertyValue]
        pm = new OneSimpleAttributePM(model: model)
        column = new AttributeColumn(OneSimpleAttributePM)
        column.bind = {pm -> pm.attribute}
    }

    def "AttributeColumns can give you the name of an attribute"() {
        expect:
            column.name() == propertyName
    }
}
