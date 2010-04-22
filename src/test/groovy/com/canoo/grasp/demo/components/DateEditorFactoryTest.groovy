package com.canoo.grasp.demo.components

import spock.lang.Specification
import com.canoo.grasp.demo.BookPM
import org.jdesktop.swingx.JXDatePicker
import com.canoo.grasp.GraspContext
import com.canoo.grasp.demo.domain.Book
import com.canoo.grasp.demo.domain.Publisher
import java.text.DateFormat
import com.canoo.grasp.demo.domain.Environment
import com.canoo.grasp.demo.EnvironmentPM

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
        Date now = new Date()
        def factory = new DateEditorFactory()
        Book gina = new Book(title: "gina", isbn: "0123456789", author: null, publishDate: now, publisher: new Publisher(name: "publisher"))
        BookPM bookPM = new BookPM(model: gina)

        when:
        def widget = factory.newInstance(null, null, bookPM.publishDate, null)

        then:
        widget instanceof JXDatePicker
        dateFormat.format(widget.date) == dateFormat.format(now)
    }

    def "the date editor factory should update the presentation model with changed values"() {
        setup:
        def factory = new DateEditorFactory()
        Date oldValue = new Date()
        Book gina = new Book(title: "gina", isbn: "0123456789", author: null, publishDate: oldValue, publisher: new Publisher(name: "publisher"))
        BookPM bookPM = new BookPM(model: gina)
        def widget = factory.newInstance(null, null, bookPM.publishDate, null)

        when:
        def newValue = new Date(101, 0, 1)
        widget.date = newValue
        widget.firePropertyChange "date", oldValue, newValue

        then:

        dateFormat.format(bookPM.publishDate.value) == dateFormat.format(newValue)
    }

    def "changing the local of the environment will change the datepicker locale"() {
        setup:
        Book gina = new Book(title: "gina", isbn: "0123456789", author: null, publishDate: new Date(), publisher: new Publisher(name: "publisher"))
        BookPM bookPM = new BookPM(model: gina)
        def env = new Environment(locale: Locale.CHINA)
        def envPM = new EnvironmentPM(model: env)
        def factory = new DateEditorFactory()
        def widget = factory.newInstance(null, null, bookPM.publishDate, [locale: envPM.locale])

        when:
        envPM.locale.value = Locale.TAIWAN

        then:
        widget.locale == Locale.TAIWAN

    }
}

