package com.canoo.grasp

import spock.lang.Specification

class CircularReferenceTest extends Specification {
    void setup() {
        Grasp.initialize()
        GraspContext.instance.domainPackage = "com.canoo.grasp"
        GraspContext.instance.presentationModelPackage = "com.canoo.grasp"
    }

    void cleanup() {
        GraspContext.instance.domainPackage = ""
        GraspContext.instance.presentationModelPackage = ""     
    }

    def """A PresentationModel can have a reference to its own type (no scaffolding)"""(){
        when:
            def andres = new Person(name: "Andres")
            def dieter = new Person(name: "Dieter", friend: andres)
            def pm = new PersonPM(model: dieter)
        then:
            pm.name.value == "Dieter"
            pm.friend.name.value == "Andres"
    }

    def """A PresentationModel can have a reference to its own type (scaffolding)"""(){
        when:
            def andres = new Friend(name: "Andres")
            def dieter = new Friend(name: "Dieter", friend: andres)
            def pm = new FriendPM(model: dieter)
        then:
            pm.name.value == "Dieter"
            pm.friend.name.value == "Andres"
    }
}

class Person {
    String name
    Person friend

    String toString() { "$name => [$friend]" }
}

class PersonPM extends PresentationModel {
    Attribute name
    PresentationModelSwitch friend = new PresentationModelSwitch(PersonPM)
}

class Friend {
    String name
    Friend friend

    String toString() { "$name => [$friend]" }
}

class FriendPM extends PresentationModel {
    static scaffold = Friend
}