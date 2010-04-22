package com.canoo.grasp.demo.components

/**
 * Created by IntelliJ IDEA.
 * User: johannes
 * Date: Apr 22, 2010
 * Time: 8:03:40 PM
 * To change this template use File | Settings | File Templates.
 */

public interface GraspEditor {

    boolean canHandle(Object value, Map attributes)

    Object newInstance(FactoryBuilderSupport factoryBuilderSupport, Object name, Object value, Map map)
}
