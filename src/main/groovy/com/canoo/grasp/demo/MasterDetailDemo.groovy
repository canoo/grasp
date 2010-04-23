package com.canoo.grasp.demo

import com.canoo.grasp.Grasp
import com.canoo.grasp.PresentationModelSwitch
import com.canoo.grasp.Store
import com.canoo.grasp.demo.domain.Book
import com.canoo.grasp.demo.domain.Publisher
import groovy.swing.SwingBuilder
import static javax.swing.ListSelectionModel.SINGLE_SELECTION
import static javax.swing.WindowConstants.EXIT_ON_CLOSE

import com.canoo.grasp.swing.PMTableFactory
import com.canoo.grasp.swing.AttributeColumnFactory
import com.canoo.grasp.GraspLocale

Store store = new Store()
Grasp.initialize()
Grasp.useBinding(store)
Grasp.setupI18n()


["Groovy in Action", "Griffon in Action", "Grails in Action"].eachWithIndex { it, idx ->
    final Publisher publisher = new Publisher(name: "Manning $idx")
    Book book = new Book(title: it, isbn: "0123456789-$idx", publisher: publisher)
    BookPM bookPM = new BookPM(model: book)
    store.save bookPM
}

def selectedBook = new PresentationModelSwitch(new BookPM())

SwingBuilder builder = new SwingBuilder()
builder.registerFactory("pmTable", new PMTableFactory())
builder.registerFactory("attributeColumn", new AttributeColumnFactory())
builder.registerExplicitMethod("boundLabel") { Map args = [prop: 'label'], attribute ->
   def aLabel = label()
   aLabel.bind(args, attribute)
   aLabel.bind(attribute, viewProperty: 'toolTipText', prop: 'description')
   aLabel
}

builder.edt {
  frame(pack: true, visible: true, id: "f", defaultCloseOperation: EXIT_ON_CLOSE) {
    vbox {
        scrollPane {
            pmTable(selectionMode: SINGLE_SELECTION, store: store,
                    type: BookPM, selection: selectedBook, id: 'master') {
                attributeColumn bind: {pm -> pm.title}
                attributeColumn bind: {pm -> pm.isbn}, editable: false
                attributeColumn bind: {pm -> pm.author}, editable: false
                attributeColumn bind: {pm -> pm.publisher.name}
            }
        }
        hstrut 20
         boundLabel(selectedBook.title)
        // long way ->
        // label().bind selectedBook.title, prop: "description"
        textField(columns: 20).bind selectedBook.title, on: "keyReleased"
        boundLabel(selectedBook.isbn)
        textField(columns: 20).bind selectedBook.isbn, on: "keyReleased"

        boundLabel(selectedBook.publisher.name)
        textField(columns: 20).bind selectedBook.publisher.name
        boundLabel(selectedBook.publisher.name, prop: 'description')

        textField(columns: 20).bind BookPM.list()[2].title
        def publishers = ["aaa","bbb","ccc"].collect { new PublisherPM(model:new Publisher(name:it)) }
        comboBox(items:publishers) //todo : binden

        hbox {
            button label: "top", actionPerformed: {
                if(master.model.rows) {
                    selectedBook.adaptee = master.model.rows[0]
                } else {
                    selectedBook.adaptee = selectedBook.defaultValue
                }
                def l = GraspLocale.instance.locale
                GraspLocale.instance.locale = l == Locale.US ? Locale.GERMAN : Locale.US
                // selectedBook.title.attribute.description = "FOO!"
            }
            button label: "new", actionPerformed: {
                BookPM newPM = new BookPM(model: new Book(title: 'new', publisher: new Publisher()))
                store.save newPM
                selectedBook.adaptee = newPM
            }
            //todo: FIXME!!!
            button("remove", actionPerformed: { selectedBook.delete() }).onSwitch selectedBook //todo: not reliable

        }
    }
  }
}