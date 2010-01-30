package com.canoo.grasp

class StoreTest extends GroovyTestCase {

    void testMocking() {

        PresentationModel pm = new TestPM(model: [attribute:'value'])

        Store store = new Store()
        store.save pm

        assertEquals 1, TestPM.count()
        assertSame pm, TestPM.findByAttribute('value')

    }


}

class TestPM extends PresentationModel {
    Attribute attribute
}
