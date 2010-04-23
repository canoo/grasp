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
            listener.equals(listener) >> true   // by default, two listeners are not equal to each other. 
        
        when:
            store.addStoreListener OneSimpleAttributePM, listener
            store.removeStoreListener OneSimpleAttributePM, listener
            store.save pm

        then:
            0 * listener.added(_)
            0 * listener.deleted(_)
    }

    def "transitive model references are saved to the store too"() {
        when:
            def owner = new Owner(firstname: "Hamlet", lastname: "D'Arcy")
            def now = new Date()
            def carPM = new CarPM(model: new Car(wheels: 4, brand: new Brand(name: "Ford"), color: Color.RED, year: now, owner: owner))
            store.save carPM
        then:
            CarPM.list().size() == 1
            OwnerPM.list().size() == 1
    }
}
