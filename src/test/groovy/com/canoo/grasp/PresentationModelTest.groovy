package com.canoo.grasp


class PresentationModelTest extends GroovyTestCase {

    static final int value = 1
    OneSimpleAttributePM simplePM
    Map simpleModel

    void setUp(){
        simpleModel = [attribute: value]
        simplePM = new OneSimpleAttributePM(model:simpleModel)
    }

    void testSetModelLeavesDefaultPropertiesAlone() {
        println '''A PresentationModel that has no attributes can take a null model
        effectively showing that PM default props are ignored since an NPE would be thrown otherwise.'''
        def noAttributePM = new PresentationModel()
        noAttributePM.model = null
    }

    void testSimplePresentationModel() {
        assert simplePM.attribute in Attribute
        assert simplePM.attribute.value == value
    }

    void testPMThatHasAPresentationModelSwitchAttribute() {
        println '''PresentationModels may have attributes that are of type PresentationModelSwitch
        thus referring to some other PresentationModel, e.g. a BookPM to a PublisherPM.'''
        def referringPM = new PMSwitchAttributePM()
        def initialPM = referringPM.presentationModelReference
        assert initialPM, 'attributes of type PresentatioModelSwitch must always have a prototype value'
        assert initialPM.adaptee, 'the adaptee must always be set since it is used for prototyping'

        // Now we set the model, which means the given simpleModel must be wrapped in a OneSimpleAttributePM,
        // which is then set as new adaptee of the switch
        referringPM.model = [presentationModelReference: simpleModel ]

        assert referringPM.presentationModelReference.is(initialPM), 'the switch instance remains'

        def newPM = referringPM.presentationModelReference.adaptee
        assert ! newPM.is(simplePM), 'we have a new PM instance'

        assert newPM in OneSimpleAttributePM
        assert newPM.attribute in Attribute
        assert ! newPM.attribute.is(simplePM.attribute) // remember for binding: we have new Attribute instances!

        assert referringPM.presentationModelReference.attribute.value == value

        // todo dk: in the course of this setting, which listeners have been fired?

    }


}