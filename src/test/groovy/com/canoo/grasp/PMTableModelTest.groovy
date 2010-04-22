package com.canoo.grasp

import spock.lang.Specification
import com.canoo.grasp.swing.PMTableModel


class PMTableModelTest extends Specification {
    Store store
    Class type
    
    void setup() {
        store = new Store()
        type = OneSimpleAttributePM
        GraspContext.useBinding(store)
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
}
