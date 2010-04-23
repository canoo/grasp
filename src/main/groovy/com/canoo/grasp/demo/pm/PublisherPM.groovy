package com.canoo.grasp.demo.pm

import com.canoo.grasp.PresentationModel
import com.canoo.grasp.demo.domain.Publisher

class PublisherPM extends PresentationModel {

    static scaffold = Publisher

    String toString() {'PublisherPM ' + name?.value + ' '+ hashCode() }
}