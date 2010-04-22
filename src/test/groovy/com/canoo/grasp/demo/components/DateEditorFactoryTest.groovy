package com.canoo.grasp.demo.components

import spock.lang.Specification
import com.canoo.grasp.demo.BookPM
import org.jdesktop.swingx.JXDatePicker
import com.canoo.grasp.GraspContext
import com.canoo.grasp.demo.domain.Book
import com.canoo.grasp.demo.domain.Publisher
import java.text.DateFormat

/**
 * Unit test for the SwingBuilder support.
 */
class DateEditorFactoryTest extends Specification {

    def dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.ENGLISH)

    def setup() {
        GraspContext.useBinding()
    }
    
    def "the date editor factory should correctly create SwingX DatePicker components"() {
        setup:
        def factory = new DateEditorFactory()
        Date now = new Date()
        Book gina = new Book(title: "gina", isbn: "0123456789", author: null, publishDate: now, publisher: new Publisher(name: "publisher"))
        BookPM bookPM = new BookPM(model: gina)

        when:
        def widget = factory.newInstance(null, null, bookPM.publishDate, null)

        then:
        widget instanceof JXDatePicker
        dateFormat.format(widget.date) == dateFormat.format(now)
    }
}

