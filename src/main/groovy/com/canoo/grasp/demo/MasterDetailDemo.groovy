package com.canoo.grasp.demo

import com.canoo.grasp.GraspContext
import com.canoo.grasp.PresentationModelSwitch
import com.canoo.grasp.Store
import com.canoo.grasp.demo.domain.Book
import com.canoo.grasp.demo.domain.Publisher
import groovy.swing.SwingBuilder
import static javax.swing.ListSelectionModel.SINGLE_SELECTION
import static javax.swing.WindowConstants.EXIT_ON_CLOSE

Store store = new Store()
GraspContext.useBinding(store)

["Groovy in Action", "Griffon in Action", "Grails in Action"].each {
    final Publisher publisher = new Publisher(name: "Manning")
    Book book = new Book(title: it, isbn: "0123456789", publisher: publisher)
    BookPM bookPM = new BookPM(model: book)
    store.save bookPM
}

List list = BookPM.list()
def selectedBook = new PresentationModelSwitch(new BookPM())

def master
SwingBuilder builder = new SwingBuilder()
def frame = builder.frame(defaultCloseOperation: EXIT_ON_CLOSE) {
    vbox {
        scrollPane {
            master = table(selectionMode: SINGLE_SELECTION) {
                tableModel(list: list) {
                    closureColumn header: "title",
                            read: {pm -> pm.title.value},
                            write: {pm, value -> pm.title.value = value  }
                    closureColumn header: "isbn",
                            read: {pm -> pm.isbn.value}
                    closureColumn header: "author",
                            read: {pm -> pm.author.value}
                    closureColumn header: "publisher",
                            read: {pm -> pm.publisher.name.value}
                }
            }
            master.syncWith selectedBook
            master.syncList BookPM
        }
        hstrut 20
        label selectedBook.title.description
        textField(columns: 20).bind selectedBook.title, on: "keyReleased"
        label selectedBook.isbn.description
        textField(columns: 20).bind selectedBook.isbn, on: "keyReleased"

        label selectedBook.publisher.name.description
        textField(columns: 20).bind selectedBook.publisher, { it.name }

        def publishers = ["aaa","bbb","ccc"].collect { new PublisherPM(model:new Publisher(name:it)) }
        comboBox(items:publishers)

        hbox {
            button label: "top", actionPerformed: { selectedBook.adaptee = list[0] }
            button label: "new", actionPerformed: {
                BookPM newPM = new BookPM(model: new Book(title: 'new', publisher: new Publisher()))
                store.save newPM
                selectedBook.adaptee = newPM
            }
            button("remove", actionPerformed: { selectedBook.delete() }).onSwitch selectedBook //todo: not reliable

        }
    }
}

frame.pack()
frame.visible = true