package com.canoo.grasp.swing

import javax.swing.table.TableColumn
import com.canoo.grasp.Attribute
import com.canoo.grasp.PresentationModel
import com.canoo.grasp.AttributeSwitch

/**
 * Created by IntelliJ IDEA.
 * User: aalmiray
 * Date: Apr 22, 2010
 * Time: 12:33:47 AM
 * To change this template use File | Settings | File Templates.
 */
class AttributeColumn extends TableColumn {
    private Closure binding
    Closure read = {attr -> attr.value }
    Closure write = {attr, value -> attr.value = value }
    private Boolean editable = null

    private final Class presentationModelClass

    AttributeColumn(Class presentationModelClass){
        this.presentationModelClass = presentationModelClass
    }

    private static Attribute normalize(attr) {
        attr in AttributeSwitch ? attr.attribute : attr
    }

    String name() {
        rebind(PresentationModel.fetchPrototype(presentationModelClass)).propertyName
    }

    Class type() {
        rebind(PresentationModel.fetchPrototype(presentationModelClass)).valueType
    }

    void setBind(Closure binding) {
        this.binding = binding
    }

    void setEditable(boolean b) {
        editable = b
    }

    def getValue(PresentationModel pm) {
        read(rebind(pm))
    }

    void setValue(PresentationModel pm, value) {
        if (isEditable(pm)) write(rebind(pm), value)
    }

    boolean isEditable(PresentationModel pm) {
        boolean readOnly = rebind(pm).readOnly
        if(readOnly) return false
        if(editable != null) return editable
        return true
    }

    private Attribute rebind(PresentationModel pm) {
        normalize(binding(pm))
    }
}
