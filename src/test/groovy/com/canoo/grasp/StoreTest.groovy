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
            IStoreListener listener = Mock(IStoreListener)

        when:
            store.addStoreListener OneSimpleAttributePM, listener
            store.save pm

        then:
            1 * listener.added(pm)
            0 * listener.deleted(_)
    }

    def "listener is not notified twice after two saves"(){

        setup:
            IStoreListener listener = Mock(IStoreListener)

        when:
            store.addStoreListener OneSimpleAttributePM, listener
            store.save pm
            store.save pm

        then: 
            1 * listener.added(_)
            0 * listener.deleted(_)
    }

    def "listener is notified on delete"(){
        setup:
            IStoreListener listener = Mock(IStoreListener)

        when:
            store.save pm
            store.addStoreListener OneSimpleAttributePM, listener
            pm.delete()

        then:
            0 * listener.added(_)
            1 * listener.deleted(pm)
            OneSimpleAttributePM.list() == []
    }

    def "listener is not updated after being removed"(){
        setup:
            IStoreListener listener = Mock(IStoreListener)
            listener.equals(listener) >> true
        
        when:
            store.addStoreListener OneSimpleAttributePM, listener
            store.removeStoreListener OneSimpleAttributePM, listener
            store.save pm

        then:
            0 * listener.added(_)
            0 * listener.deleted(_)
    }
}
