package com.canoo.grasp

import spock.lang.Specification

class StoreTest extends Specification {

    Store store = new Store()
    PresentationModel pm = new OneSimpleAttributePM(model: [attribute: 'value'])

    def "find must work after save performed"() {
        when:
            store.save pm

        then:
            1 == OneSimpleAttributePM.count()
            pm.is(OneSimpleAttributePM.findByAttribute('value'))
    }

    def "listener is notified after a save"() {
        setup:
            def found
            def listener = [added:{found = it}] as IStoreListener

        when:
            store.addStoreListener OneSimpleAttributePM, listener
            store.save pm

        then:
            found == pm
    }

    def "listener is not notified twice after two saves"(){

        setup:
            def found
            def listener = [added:{found = it}] as IStoreListener

        when:
            store.addStoreListener OneSimpleAttributePM, listener
            store.save pm
            found = null
            store.save pm

        then: 
            found == null
    }

    def "listener is notified on delete"(){
        setup:
            def deleted
            def listener = [deleted:{deleted = it}] as IStoreListener

        when:
            store.save pm
            store.addStoreListener OneSimpleAttributePM, listener
            pm.delete()

        then:
            deleted == pm
            OneSimpleAttributePM.list() == []
    }

    def "listener is not updated after being removed"(){
        setup:
            def found
            def listener = [added: { found = it } ] as IStoreListener

        when:
            store.addStoreListener OneSimpleAttributePM, listener
            store.removeStoreListener OneSimpleAttributePM, listener
            store.save pm

        then:
            found == null
    }

}
