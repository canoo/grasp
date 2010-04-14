package com.canoo.grasp

import spock.lang.Specification

/**
 * Test to make sure Spock is working correctly. 
 */
class HelloSpock extends Specification {

    def "can you figure out what I'm up to?"() {
        expect:
        name.size() == size

        where:
        name << ["Kirk", "Spock", "Scotty"]
        size << [4, 5, 6]
    }

}
