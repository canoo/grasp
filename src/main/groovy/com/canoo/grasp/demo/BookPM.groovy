package com.canoo.grasp.demo

import com.canoo.grasp.PresentationModel
import com.canoo.grasp.Attribute
import com.canoo.grasp.PresentationModelSwitch

class BookPM extends PresentationModel {
    Attribute title, author, isbn
    final PresentationModelSwitch publisher = new PresentationModelSwitch(new PublisherPM())

    String toString() {"BookPM " + title?.value + ' '+hashCode()}
}
