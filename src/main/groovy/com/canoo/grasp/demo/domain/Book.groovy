package com.canoo.grasp.demo.domain

class Book {

    String title
    Author author
    String isbn
    Publisher publisher
    Date publishDate
    boolean inPrint

    static constraints = {
        title size:3..255
        isbn  match:/\d{10}/
    }
}
