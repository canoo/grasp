package com.canoo.grasp.demo

import com.canoo.grasp.PresentationModel
import com.canoo.grasp.Attribute

/**
 * @author: dierk.koenig at canoo.com
 */
class BookPM extends PresentationModel {
    Attribute title, author, isbn
    String toString() {title.value + ' '+hashCode()}
}
