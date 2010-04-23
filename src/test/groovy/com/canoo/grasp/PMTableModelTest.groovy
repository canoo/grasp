package com.canoo.grasp

import spock.lang.Specification
import com.canoo.grasp.swing.PMTableModel
import groovy.swing.SwingBuilder
import com.canoo.grasp.swing.PMTableFactory
import com.canoo.grasp.swing.AttributeColumnFactory
import groovy.beans.Bindable

class PMTableModelTest extends Specification {
    Store store
    Class type

    void setup() {
        Grasp.initialize()
        store = new Store()
        type = OneSimpleAttributePM
        Grasp.useBinding(store)
        store.mockDomain(type)
    }

    def "The model should be empty if no pms are available in the store"() {
        when:
            def model = new PMTableModel(store, type)
        then:
            !model.rows
    }

    def "The model should NOT be empty if pms are available on the store"() {
        when:
            def pm = new OneSimpleAttributePM()
            store.save pm
            def model = new PMTableModel(store, type)
        then:
            model.rows.size() == 1
            model.rows[0] == pm
    }

    def "Deleting an element from the store should delete it from the model"() {
        when:
            def pm = new OneSimpleAttributePM()
            store.save pm
            def model = new PMTableModel(store, type)
            assert model.rows.size() == 1
            assert model.rows[0] == pm
            pm.delete()
        then:
            !model.rows
    }

    def "A TableModel can listen to different PM updates, not just the selected one"() {
        setup:
            def pm1 = new OneSimpleAttributePM(model: [attribute: 42])
            def pm2 = new OneSimpleAttributePM(model: [attribute: 21])
            store.save pm1
            store.save pm2

            PresentationModelSwitch selection = new PresentationModelSwitch(OneSimpleAttributePM)
            SwingBuilder builder = new SwingBuilder()
            builder.registerFactory("pmTable", new PMTableFactory())
            builder.registerFactory("attributeColumn", new AttributeColumnFactory())
            builder.pmTable(store: store,
                type: OneSimpleAttributePM, selection: selection, id: 'master') {
                attributeColumn bind: {pm -> pm.attribute}
            }

            SimpleEditor editor1 = new SimpleEditor()
            editor1.bind selection.attribute, on: "propertyChange"
            SimpleEditor editor2 = new SimpleEditor()
            editor2.bind pm2.attribute, on: "propertyChange"

        when:
            builder.master.selectionModel.setSelectionInterval(0,0)
            assert editor1.text == "42"
            assert editor2.text == "21"

            editor1.text = "lorem ipsum"
            editor2.text = "sit amet"

        then:
            selection.attribute.value == "lorem ipsum"
            pm1.attribute.value == "lorem ipsum"
            builder.master.model.getValueAt(0, 0) == "lorem ipsum"

            pm2.attribute.value == "sit amet"
            builder.master.model.getValueAt(1, 0) == "sit amet"
    }

    def "A TableModel can listen to different PM updates, including nested models"() {
        setup:

            def pm1 = new CarPM(model: new Car(wheels: 4, brand: new Brand(name: "Ford"),
                      color: Color.RED, year: new Date(), owner: new Owner(firstname: "Hamlet", lastname: "D'Arcy")))
            def pm2 = new CarPM(model: new Car(wheels: 4, brand: new Brand(name: "Kia"),
                      color: Color.BLUE, year: new Date(), owner: new Owner(firstname: "Andres", lastname: "Almiray")))

            store.save pm1
            store.save pm2
            PresentationModelSwitch selection = new PresentationModelSwitch(CarPM)

            SwingBuilder builder = new SwingBuilder()
            builder.registerFactory("pmTable", new PMTableFactory())
            builder.registerFactory("attributeColumn", new AttributeColumnFactory())
            builder.pmTable(store: store,
                type: CarPM, selection: selection, id: 'master') {
                attributeColumn bind: {pm -> pm.wheels}
                attributeColumn bind: {pm -> pm.owner.firstname}
            }

            SimpleEditor editor1 = new SimpleEditor()
            editor1.bind selection.wheels, on: "propertyChange"
            SimpleEditor editor2 = new SimpleEditor()
            editor2.bind pm2.owner.firstname, on: "propertyChange"

        when:
            builder.master.selectionModel.setSelectionInterval(0,0)
            assert editor1.text == "4"
            assert editor2.text == "Andres"

            editor1.text = "6"
            editor2.text = "Dieter"

        then:
            selection.wheels.value == "6"
            pm1.wheels.value == "6"
            builder.master.model.getValueAt(0, 0) == "6"
            builder.master.model.getValueAt(0, 1) == "Hamlet"

            pm2.owner.firstname.value == "Dieter"
            builder.master.model.getValueAt(1, 0) == 4
            builder.master.model.getValueAt(1, 1) == "Dieter"
    }

    static class SimpleEditor {
        @Bindable String text = ""
    }
}
