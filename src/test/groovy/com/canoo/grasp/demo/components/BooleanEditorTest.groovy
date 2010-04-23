package com.canoo.grasp.demo.components

import com.canoo.grasp.PresentationModel
import spock.lang.Specification

class BooleanEditorTest extends Specification {

    def "the editor can handle Boolean proeprties, but not much else"() {

        when:
        PresentationModel pm = new SimplePM(model: new SimpleDomain(string: "a string", bool: true))
        def editor = new BooleanEditor()

        then:
        editor.canHandle(pm.bool, [:])
        !editor.canHandle(pm.string, [:])
    }

    def "the editor creates checkboxes on the builder"() {

        setup:
        PresentationModel pm = new SimplePM(model: new SimpleDomain(string: "a string", bool: true))
        def editor = new BooleanEditor()
        def methodWasCalled = false
        def builder = [checkBox:  {methodWasCalled = true} ] as FactoryBuilderSupport

        when:
        editor.newInstance(builder, null, pm.bool, [:])

        then:
        methodWasCalled
    }
}

class SimpleDomain {
    String string
    boolean bool
}

class SimplePM extends PresentationModel {
    static scaffold = SimpleDomain
}
