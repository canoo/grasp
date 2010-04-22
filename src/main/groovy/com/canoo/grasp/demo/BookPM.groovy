package com.canoo.grasp.demo

import com.canoo.grasp.PresentationModel
import com.canoo.grasp.demo.domain.Book

class BookPM extends PresentationModel {

    static scaffold = Book

    String toString() {"BookPM " + title?.value + ' '+hashCode()}
}
