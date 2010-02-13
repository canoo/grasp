package com.canoo.grasp

class StoreTest extends GroovyTestCase {

    Store store = new Store()
    PresentationModel pm = new OneSimpleAttributePM(model: [attribute: 'value'])

    void testSaveAndFind() {
        store.save pm
        assertEquals 1, OneSimpleAttributePM.count()
        assertSame pm, OneSimpleAttributePM.findByAttribute('value')
    }

    void testStoreListenerOnSave(){
        def found
        def listener = [added:{found = it}] as IStoreListener
        store.addStoreListener OneSimpleAttributePM, listener
        store.save pm
        assert found == pm

        found = null
        store.save pm
        assert found == null, "no added notification when saving known objects"
    }

    void testStoreListenerOnDelete(){
        def deleted
        def listener = [deleted:{deleted = it}] as IStoreListener
        store.save pm
        store.addStoreListener OneSimpleAttributePM, listener
        deleted = null
        pm.delete()
        assert deleted == pm
        assert OneSimpleAttributePM.list() == []
    }

    void testRemoveStoreListener(){
        def found
        def listener = [added:{found = it}] as IStoreListener
        store.addStoreListener OneSimpleAttributePM, listener
        store.removeStoreListener OneSimpleAttributePM, listener
        store.save pm
        assert found == null
    }

}