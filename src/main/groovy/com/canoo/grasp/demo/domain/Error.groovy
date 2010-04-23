package com.canoo.grasp.demo.domain

/**
 * An error.
 */
class Error {

    String id
    Object source


    boolean equals(o) {
        if (this.is(o)) return true;

        if (getClass() != o.class) return false;

        Error error = (Error) o;

        if (id != error.id) return false;
        if (source != error.source) return false;

        return true;
    }

    int hashCode() {
        int result;

        result = (id != null ? id.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        return result;
    }
}
