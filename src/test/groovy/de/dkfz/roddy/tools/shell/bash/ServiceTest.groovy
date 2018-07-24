package de.dkfz.roddy.tools.shell.bash

import spock.lang.Specification

class ServiceTest extends Specification {

    def "Escape"() {
        expect:
        Service.escape(unescaped) == escaped
        where:
        unescaped  | escaped
        ""         | ""
        "a"        | "a"
        "("        | "\\("
        "run &"    | "run\\ \\&"
        "{ a; b }" | "\\{\\ a\\;\\ b\\ \\}"
        """if (( a -eq b )); then
   echo "hola"
fi"""   | """if\\ \\(\\(\\ a\\ -eq\\ b\\ \\)\\)\\;\\ then\\
\\ \\ \\ echo\\ \\"hola\\"\\
fi"""

    }
}
