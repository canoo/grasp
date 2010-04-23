package com.canoo.grasp

import spock.lang.Specification

class GraspContextTest extends Specification {
    def "An Owner model class should resolve to an OwnerPM className"(){
        expect:
            GraspContext.instance.resolvePresentationModelClassName(new Owner()) == OwnerPM.name
            GraspContext.instance.resolvePresentationModelClassName(Owner) == OwnerPM.name
    }

    def "A Map model should resolve to an empty presentation model className"() {
        expect:
            !GraspContext.instance.resolvePresentationModelClassName([:])
    }

    def "An OwnerPM class should resolve to an Owner model className"() {
        expect:
            GraspContext.instance.resolveDomainModelClassName(new OwnerPM()) == Owner.name
            GraspContext.instance.resolveDomainModelClassName(OwnerPM) == Owner.name
    }

    def """Setting a basePackage name should result in the same package for both
        model and pm classes"""(){
        when:
            GraspContext.instance.basePackage = "com.acme"
        then:
            GraspContext.instance.resolvePresentationModelClassName(Owner) == "com.acme.pm.OwnerPM"
            GraspContext.instance.resolveDomainModelClassName(OwnerPM) == "com.acme.domain.Owner"
        cleanup:
            GraspContext.instance.basePackage = ""
    }

    def """Setting a domainPackage name should not influence the presentationPackage name"""(){
        when:
            GraspContext.instance.domainPackage = "com.acme.model"
        then:
            GraspContext.instance.resolveDomainModelClassName(OwnerPM) == "com.acme.model.Owner"
            GraspContext.instance.resolvePresentationModelClassName(Owner) == "com.canoo.grasp.OwnerPM"
        cleanup:
            GraspContext.instance.domainPackage = ""
    }

    def """Setting a presentationPackage name should not influence the domainPackage name"""(){
        when:
            GraspContext.instance.presentationModelPackage = "com.acme.model"
        then:
            GraspContext.instance.resolveDomainModelClassName(OwnerPM) == "com.canoo.grasp.Owner"
            GraspContext.instance.resolvePresentationModelClassName(Owner) == "com.acme.model.OwnerPM"
        cleanup:
            GraspContext.instance.presentationModelPackage = ""
    }

    def """Both presentationPackage and domainPackage have presecende over basePackage"""(){
        when:
            GraspContext.instance.presentationModelPackage = "com.acme.model.pm"
            GraspContext.instance.domainPackage = "com.acme.model.domain"
            GraspContext.instance.basePackage = "com.acme"
        then:
            GraspContext.instance.resolveDomainModelClassName(OwnerPM) == "com.acme.model.domain.Owner"
            GraspContext.instance.resolvePresentationModelClassName(Owner) == "com.acme.model.pm.OwnerPM"
        cleanup:
            GraspContext.instance.presentationModelPackage = ""
            GraspContext.instance.domainPackage = ""
            GraspContext.instance.basePackage = ""
    }
}
