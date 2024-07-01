package com.appsqueeze.librarymanagement

import java.util.Date

class MyBook {
    var bid: Int = 0
    var title: String? = null
    var type: String? = null
    var idate: Date? = null
    var ddate: Date? = null


    constructor(bid: Int, title: String?, type: String?, idate: Date?, ddate: Date?) {
        this.bid = bid
        this.title = title
        this.type = type
        this.idate = idate
        this.ddate = ddate
    }

    constructor()
}
