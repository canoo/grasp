package com.canoo.grasp

import java.beans.Introspector
import java.beans.PropertyChangeListener

class Store {

    {
        ExpandoMetaClass.enableGlobally() // needed to allow change of metaClass on old pm instances
    }

    static final COMPARATORS = Collections.unmodifiableList([
            "IsNull",
            "IsNotNull",
            "LessThan",
            "LessThanEquals",
            "GreaterThan",
            "GreaterThanEquals",
            "NotEqual",
            "Like",
            "Ilike",
            "Between"])
    static final COMPARATORS_RE = COMPARATORS.join("|")
    static final DYNAMIC_FINDER_RE = /(\w+?)(${COMPARATORS_RE})?((And|Or)(\w+?)(${COMPARATORS_RE})?)?/

    Map pmListPerClass = [:] // class to [objects]
    Map listenersPerClass = [:] // class to [Listener]
    private PropertyChangeListener listener = {e ->
        listenersPerClass[e.source.getClass()]*.updated(e.source)
    } as PropertyChangeListener


    void save(PresentationModel pm) {
        Class clazz = pm.class
        List knownPMs = fetchClassList(clazz)
        if (!knownPMs) {
            mockDomain clazz  // add static methods if not already done
        }
        if (!(pm in knownPMs)) {
            addDynamicInstanceMethods pm
            knownPMs.add pm
            if (!pm.id) pm.id = knownPMs.id.max() + 1 // id generator // todo start with hashcode, find next available
            listenersPerClass[clazz]*.added(pm)
            pm.addPropertyChangeListener listener
        }
    }

    void delete(PresentationModel pm) { // todo dk: think about deleting all references to that instance
        fetchClassList(pm.class).remove pm
        pm.removePropertyChangeListener listener
        for (listener in listenersPerClass[pm.class]) listener.deleted pm
    }


    void mockDomain(Class clazz) {
        addDynamicFinders clazz
        addGetMethods clazz
        addCountMethods clazz
        addListMethod clazz
    }


    private void addDynamicFinders(Class clazz) {
        // Implement the dynamic class methods for domain classes.

        clazz.metaClass.static.findAll = {->
            fetchClassList(clazz).clone()
        }

        clazz.metaClass.static.findAllWhere = {args = [:] ->
            fetchClassList(clazz).findAll {instance ->
                args.every {k, v -> instance[k] == v }
            }
        }

        clazz.metaClass.static.methodMissing = {method, args ->
            def m = method =~ /^find(All)?By${DYNAMIC_FINDER_RE}$/
            if (m) {
                def field = Introspector.decapitalize(m[0][2])
                def comparator = m[0][3]

                // How many arguments do we need to pass for the given
                // comparator?
                def numArgs = getArgCountForComparator(comparator)

                // Strip out that number of arguments from the ones
                // we've been passed.
                def subArgs = args[0..<numArgs]
                def result = processInstances(fetchClassList(clazz), field, comparator, subArgs)

                args = args[numArgs..<args.size()]

                // If we have a second clause, evaluate it now.
                def join = m[0][5]
                if (join) {
                    field = Introspector.decapitalize(m[0][6])
                    comparator = m[0][7]
                    numArgs = getArgCountForComparator(comparator)
                    subArgs = args[0..<numArgs]

                    def secondResult = processInstances(fetchClassList(clazz), field, comparator, subArgs)

                    args = args[numArgs..<args.size()]

                    // Combine the first result with the second result
                    // based on the join type.
                    if (join == "And") {
                        result = intersect(result, secondResult)
                    }
                    else if (join == "Or") {
                        result = intersect(fetchClassList(clazz), result + secondResult)
                    }
                    else {
                        throw new RuntimeException("Unrecognised join type: '$join'")
                    }
                }

                if (m[0][1]) {
                    // We're doing a findAllBy* so return a list.
                    return result ?: []
                }
                else {
                    // we're doing a findBy* so just return the first
                    // result (or null if there are none).
                    return result ? result[0] : null
                }
            } else {
                m = method =~ /^countBy${DYNAMIC_FINDER_RE}$/
                if (m) {
                    switch (args.size()) {
                        case 0: return clazz."findAllBy${method[7..-1]}"().size()
                        case 1: return clazz."findAllBy${method[7..-1]}"(args[0]).size()
                        case 2: return clazz."findAllBy${method[7..-1]}"(args[0], args[1]).size()
                        case 3: return clazz."findAllBy${method[7..-1]}"(args[0], args[1], args[2]).size()
                        case 4: return clazz."findAllBy${method[7..-1]}"(args[0], args[1], args[2], args[3]).size()
                    }

                }
                else {
                    throw new MissingMethodException(method, delegate, args)
                }
            }
        }
    }

    private List fetchClassList(Class clazz) {
         pmListPerClass.get(clazz, [])
    }

    /**
     * Adds methods that mock the behavior of the count() methods
     */
    private void addCountMethods(Class clazz) {
        clazz.metaClass.static.count = {->
            return fetchClassList(clazz).size()
        }
    }

    private void addGetMethods(Class clazz) {
        def pms = fetchClassList(clazz)

        // First get()...
        clazz.metaClass.static.get = {id ->
            return pms.find { it.id == id }
        }
    }

    private void addListMethod(Class clazz) {
        clazz.metaClass.static.list = {->
            fetchClassList(clazz).clone()
        }
    }

    private void addDeleteMethod(PresentationModel pm) {
        pm.class.metaClass.delete = {-> owner.delete  pm }
    }

    private void addDynamicInstanceMethods(PresentationModel pm) {
        addDeleteMethod pm
    }

    /**
     * Badly named method that filters a list of objects using the
     * "findBy()" comparators such as "IsNull", "GreaterThan", etc.
     */
    private processInstances(instances, property, comparator, args) {
        def result = []
        instances.each {record ->
            def propValue = record."${property}".value
            switch (comparator) {
                case null:
                    if (propValue == args[0]) result << record
                    break

                case "IsNull":
                    if (propValue == null) result << record
                    break

                case "IsNotNull":
                    if (propValue != null) result << record
                    break

                case "LessThan":
                    if (propValue < args[0]) result << record
                    break

                case "LessThanEquals":
                    if (propValue <= args[0]) result << record
                    break

                case "GreaterThan":
                    if (propValue > args[0]) result << record
                    break

                case "GreaterThanEquals":
                    if (propValue >= args[0]) result << record
                    break

                case "NotEqual":
                    if (propValue != args[0]) result << record
                    break

                case "Like":
                    if (propValue ==~ args[0].replaceAll("%", ".*")) result << record
                    break

                case "Ilike":
                    if (propValue ==~ /(?i)${args[0].replaceAll("%", ".*")}/) result << record
                    break;

                case "Between":
                    if (propValue >= args[0] && propValue <= args[1]) result << record
                    break;

                default:
                    throw new RuntimeException("Unrecognised comparator: ${comparator}")
            }
        }

        return result
    }

    private int getArgCountForComparator(String comparator) {
        if (comparator == "Between") {
            return 2
        }
        else if (["IsNull", "IsNotNull"].contains(comparator)) {
            return 0
        }
        else {
            return 1
        }
    }

    /**
     * Returns a list of all the items that are in both <code>left</code>
     * and <code>right</code>. The items in the returned list have the
     * same order as the items in <code>left</code>.
     */
    private List intersect(List left, List right) {
        def result = new ArrayList(left.size())
        left.each {item ->
            if (right.contains(item)) result << item
        }

        return result
    }

    void addStoreListener(Class pmClass, IStoreListener storeListener) {
        listenersPerClass.get(pmClass, []) << storeListener // allows double entries 
    }

    void removeStoreListener(Class pmClass, IStoreListener storeListener) {
        def object = listenersPerClass[pmClass]
        object?.remove storeListener
    }
}
