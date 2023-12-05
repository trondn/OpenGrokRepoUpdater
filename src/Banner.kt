fun banner(message:String, border :String = "*") {
    for (i in 0 until message.length + 4) {
        print(border)
    }
    println()
    println("$border $message $border")
    for (i in 0 until message.length + 4) {
        print(border)
    }
    println()
}
