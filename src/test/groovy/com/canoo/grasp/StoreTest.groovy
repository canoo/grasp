package com.canoo.grasp

class StoreTest extends GroovyTestCase {

    Store store = new Store()
    PresentationModel pm = new TestPM(model: [attribute: 'value'])

    void testSaveAndFind() {
        store.save pm
        assertEquals 1, TestPM.count()
        assertSame pm, TestPM.findByAttribute('value')
    }

    void testStoreListenerOnSave(){
        def found
        def listener = [added:{found = it}] as IStoreListener
        store.addStoreListener TestPM, listener
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
        store.addStoreListener TestPM, listener
        deleted = null
        //pm.delete() // todo dk: no idea why this doesn't work
        store.delete pm
        assert deleted == pm
        assert TestPM.list() == []
    }

    void testRemoveStoreListener(){
        def found
        def listener = [added:{found = it}] as IStoreListener
        store.addStoreListener TestPM, listener
        store.removeStoreListener TestPM, listener
        store.save pm
        assert found == null
    }

}

class TestPM extends PresentationModel {
    Attribute attribute
}
