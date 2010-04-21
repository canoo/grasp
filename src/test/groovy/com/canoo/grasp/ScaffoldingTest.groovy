package com.canoo.grasp

import spock.lang.Specification

class ScaffoldingTest extends Specification {

    def "PresentationModel can have attributes for simple types"() {
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
    Attribute wheels, brand, color, year
    PresentationModelSwitch owner = new PresentationModelSwitch(new OwnerPM())

}

class OwnerPM extends PresentationModel {
    Attribute firstname, lastname
}

class Car {
    int wheels
    Color color
    Brand brand
    Date year
    Owner owner
}

class Brand {
    String name
}

class Owner {
    String firstname
    String lastname
}

enum Color {
    RED, BLUE, WHITE
}

