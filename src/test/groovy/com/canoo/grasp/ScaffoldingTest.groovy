package com.canoo.grasp

import spock.lang.Specification

class ScaffoldingTest extends Specification {
    def setup() {
        Grasp.initialize()
    }

    def "PresentationModel can have attributes for simple types"() {
        when:
        def ownerPM = new OwnerPM(model: new Owner(firstname: "Hamlet", lastname: "D'Arcy"))
        then:
        ownerPM.firstname.value == "Hamlet"
        ownerPM.lastname.value == "D'Arcy"
    }

    def "PresentationModel can have attributes for simple types two levels deep"() {
        when:
        def owner = new Owner(firstname: "Hamlet", lastname: "D'Arcy")
        def now = new Date()
        def carPM = new CarPM(model: new Car(wheels: 4, brand: new Brand(name: "Ford"), color: Color.RED, year: now, owner: owner))
        then:
        carPM.wheels.value == 4
        carPM.brand.value.name == "Ford"
        carPM.color.value == Color.RED
        carPM.year.value == now
        carPM.owner.getAdaptee().firstname.value == "Hamlet"
        carPM.owner.getAdaptee().lastname.value == "D'Arcy"
    }
}

class CarPM extends PresentationModel {
    static scaffold = Car
}

class OwnerPM extends PresentationModel {
    static scaffold = Owner
}

class Car {
    int wheels
    Color color
    Brand brand
    Date year
    Owner owner

    String toString() { "Car[wheels: $wheels, color: $color, brand: $brand, year: $year, owner: $owner]" }
}

class Brand {
    String name

    String toString() { "Brand[name: $name]" }
}

class Owner {
    String firstname
    String lastname

    String toString() { "Owner[firstname: $firstname, lastname: $lastname]"}
}

enum Color {
    RED, BLUE, WHITE
}

