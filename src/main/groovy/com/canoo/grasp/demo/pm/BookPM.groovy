package com.canoo.grasp.demo.pm

import com.canoo.grasp.PresentationModel
import com.canoo.grasp.Attribute
import com.canoo.grasp.PresentationModelSwitch

class BookPM extends PresentationModel {
    Attribute title, isbn, author, publishDate, inPrint
    PresentationModelSwitch publisher = new PresentationModelSwitch(PublisherPM)

    // static scaffold = Book

    String toString() {"BookPM " + title?.value + ' '+hashCode()}
}
