package com.canoo.grasp.demo

import com.canoo.grasp.Attribute
import com.canoo.grasp.PresentationModel

class PublisherPM extends PresentationModel {
    Attribute name

    String toString() {'PublisherPM ' + name?.value + ' '+ hashCode() }
}