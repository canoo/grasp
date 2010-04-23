package com.canoo.grasp

import spock.lang.Specification

class PresentationModelTest extends Specification {

    static final int value = 1
    OneSimpleAttributePM simplePM
    Map simpleModel

    void setup(){
        Grasp.initialize()
        simpleModel = [attribute: value]
        simplePM = new OneSimpleAttributePM(model:simpleModel)
    }

    void '''A PresentationModel that has no attributes can take a null model
        effectively showing that PM default props are ignored since an NPE would be thrown otherwise.'''() {

        when:
            def noAttributePM = new PresentationModel()
            noAttributePM.model = null
        then: 
            notThrown(NullPointerException)
    }

    def "a simple presentation model has an attribute value"() {

        expect:
            simplePM.attribute in Attribute
            simplePM.attribute.value == value
    }

    def '''PresentationModel attributes of type "Switch" must always have a prototype value and an adaptee, even
        when a default value is not supplied and it '''() {

        when:
            def model = new PMSwitchAttributePM()

        then:
            assert model.presentationModelReference
            assert model.presentationModelReference.adaptee
    }

    def '''PresentationModels may have attributes that are of type PresentationModelSwitch
        thus referring to some other PresentationModel, e.g. a BookPM to a PublisherPM.'''() {

        when:
            def referringPM = new PMSwitchAttributePM()
            def initialPM = referringPM.presentationModelReference

            // Now we set the model, which means the given simpleModel must be wrapped in a OneSimpleAttributePM,
            // which is then set as new adaptee of the switch
            // println referringPM.model
            referringPM.model = [presentationModelReference: simpleModel ]
            // println referringPM.model

            assert referringPM.presentationModelReference.is(initialPM), 'the switch instance remains'
    
            def newPM = referringPM.presentationModelReference.adaptee

        then:
            assert ! newPM.is(simplePM), 'we have a new PM instance'
            newPM in OneSimpleAttributePM
            newPM.attribute in Attribute
            ! newPM.attribute.is(simplePM.attribute) // remember for binding: we have new Attribute instances!
            referringPM.presentationModelReference.attribute.value == value

            // todo dk: in the course of this setting, which listeners have been notified?

    }

    private static class PrototypedPM extends PresentationModel {
        Attribute someAttribute
        Attribute otherAttribute
        // PresentationModelSwitch someSwitch = new PresentationModelSwitch(new OneSimpleAttributePM())

        static PresentationModel prototype() {
           def proto = new PrototypedPM(model: [someAttribute: "foo"])
            proto
        }
    }

    def '''All PresentationModel classes have a default protoype that is equal to
           a no-args instance of the source class'''() {
        when:
            def proto = PresentationModel.fetchPrototype(OneSimpleAttributePM)
        then:
            proto.attribute
            !proto.attribute.value
    }

    def '''Any PresentationModel class can override its default prototype'''() {
        when:
            def proto = PresentationModel.fetchPrototype(PrototypedPM)
        then:
            proto.someAttribute.value == "foo"
            proto.otherAttribute
            !proto.otherAttribute.value
    }

    def '''Prototype instances are unique per class'''() {
        when:
            def proto1 = PresentationModel.fetchPrototype(OneSimpleAttributePM)
            def proto2 = PresentationModel.fetchPrototype(OneSimpleAttributePM)
            def proto3 = PresentationModel.fetchPrototype(PrototypedPM)
            def proto4 = PresentationModel.fetchPrototype(PrototypedPM)
        then:
            proto1.is(proto2)
            proto3.is(proto4)
    }
}