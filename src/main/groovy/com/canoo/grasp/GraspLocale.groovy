package com.canoo.grasp

import groovy.beans.Bindable

@Singleton
class GraspLocale {
    @Bindable Locale locale = Locale.getDefault()
}
