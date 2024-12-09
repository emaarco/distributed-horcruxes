package de.emaarco.example.domain

data class Email(val value: String) {
    init {
        require(value.matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+\$"))) { "Invalid email format" }
    }
}