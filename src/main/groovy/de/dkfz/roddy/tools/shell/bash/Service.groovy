package de.dkfz.roddy.tools.shell.bash

class Service {

    static Vector<Character> specialCharacters = [
            '#', '$', ';', '&', '\'', '"', '\\', '`', ':', '>', "<", '|', '!', '*', '?',
            '{', '}', '(', ')', '[', ']', '~', " ", "\t", "\n", "\r", "\f", "\b"
    ]


    /** Escape a string value to be used as such in an unquoted expression.
     * @param var
     * @return
     */
    static String escape(String val) {
        return val.collectReplacements { Character c ->
            specialCharacters.contains(c as String) ? "\\${c}" as String : "${c}" as String
        }
    }

}
