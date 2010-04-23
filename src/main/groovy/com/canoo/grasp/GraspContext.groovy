package com.canoo.grasp

// todo: review this decision when multiple contexts
// an the same JVM process are needed
@Singleton
class GraspContext extends ConfigObject {
    String basePackage
    String domainPackage
    String presentationModelPackage

    String getCanonicalDomainPackage() {
        if(domainPackage) return domainPackage
        if(basePackage) return basePackage + '.domain'
        return ''
    }

    String getCanonicalPresentationModelPackage() {
        if(presentationModelPackage) return presentationModelPackage
        if(basePackage) return basePackage + '.pm'
        return ''
    }

    String resolveDomainModelClassName(PresentationModel pm) {
        resolveDomainModelClassName(pm?.getClass())
    }

    String resolveDomainModelClassName(Class clazz) {
        if(!clazz) return ''

        String dpackage = getCanonicalDomainPackage()
        if(!dpackage) dpackage = clazz.getPackage()?.name
        String shortClassName = clazz.simpleName - 'PM'
        (dpackage ? dpackage +'.' : '') + shortClassName
    }

    String resolvePresentationModelClassName(Object domain) {
        Class clazz = domain instanceof Class ? domain : domain?.getClass()
        if(!clazz || domain in Map) return ''

        String pmpackage = getCanonicalPresentationModelPackage()
        if(!pmpackage) pmpackage = clazz.getPackage()?.name
        (pmpackage ? pmpackage + '.' : '') + clazz.simpleName +'PM'
    }
}
