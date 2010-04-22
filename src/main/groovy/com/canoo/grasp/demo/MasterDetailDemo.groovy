package com.canoo.grasp.demo

import com.canoo.grasp.GraspContext
import com.canoo.grasp.PresentationModelSwitch
import com.canoo.grasp.Store
import com.canoo.grasp.demo.domain.Book
import com.canoo.grasp.demo.domain.Publisher
import groovy.swing.SwingBuilder
import static javax.swing.ListSelectionModel.SINGLE_SELECTION
import static javax.swing.WindowConstants.EXIT_ON_CLOSE

import com.canoo.grasp.swing.PMTableFactory
import com.canoo.grasp.swing.AttributeColumnFactory

Store store = new Store()
GraspContext.useBinding(store)

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

builder.edt {
  frame(pack: true, visible: true, defaultCloseOperation: EXIT_ON_CLOSE) {
    vbox {
        scrollPane {
            pmTable(selectionMode: SINGLE_SELECTION, store: store,
                    type: BookPM, selection: selectedBook, id: 'master') {
                attributeColumn bind: {pm -> pm.title}, editable: false
                attributeColumn bind: {pm -> pm.isbn}, editable: false
                attributeColumn bind: {pm -> pm.author}, editable: false
                attributeColumn bind: {pm -> pm.publisher.name}
            }
        }
        hstrut 20
        label selectedBook.title.description
        textField(columns: 20).bind selectedBook.title, on: "keyReleased"
        label selectedBook.isbn.description
        textField(columns: 20).bind selectedBook.isbn, on: "keyReleased"

        label selectedBook.publisher.name.description
        textField(columns: 20).bind selectedBook.publisher, { it.name }
        label selectedBook.publisher.name.description

        textField(columns: 20).bind selectedBook.publisher, { it.name }
        def publishers = ["aaa","bbb","ccc"].collect { new PublisherPM(model:new Publisher(name:it)) }
        comboBox(items:publishers) //todo : binden

        hbox {
            button label: "top", actionPerformed: {
                if(master.model.rows) {
                    selectedBook.adaptee = master.model.rows[0]
                } else {
                    selectedBook.adaptee = selectedBook.defaultValue
                }
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