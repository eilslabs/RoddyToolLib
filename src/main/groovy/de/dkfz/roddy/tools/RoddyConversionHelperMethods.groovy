/*
 * Copyright (c) 2017 eilslabs.
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */

package de.dkfz.roddy.tools

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovy.util.slurpersupport.NodeChild
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil

/**
 * Contains methods to convert numbers
 *
 * User: michael
 * Date: 27.11.12
 * Time: 09:09
 */
@groovy.transform.CompileStatic
class RoddyConversionHelperMethods {

    static int toInt(String value, int number = 0) {
        try {
            return Integer.parseInt(value)
        } catch (any) {
            return number
        }
    }

    static float toFloat(String value, float number = 0) {
        try {
            return Float.parseFloat(value)
        } catch (any) {
            return number
        }
    }

    static double toDouble(String value, double number = 0) {
        try {
            return Double.parseDouble(value)
        } catch (any) {
            return number
        }
    }

    static boolean toBoolean(String value, boolean defaultValue) {
        value = value?.toLowerCase()
        if (value == "true" || value == "1" || value == "yes") return true
        if (value == "false" || value == "0" || value == "no") return false
        return defaultValue ?: false
    }

    static boolean safeToBoolean(String value) {
        value = value?.toLowerCase()
        if (value == "true" || value == "1" || value == "yes")
            return true
        else if (value == "false" || value == "0" || value == "no")
            return false
        else
            throw new IllegalArgumentException("Cannot convert '${value}' to boolean")
    }

    static boolean isInteger(String str) {
        return !isNullOrEmpty(str) && str.isInteger()
    }

    static boolean isFloat(String str) {
        return !isNullOrEmpty(str) && str.isFloat() &&
                (!str.contains(".") || str.endsWith("f")) // Expand test to "real" floats ending with "f", if there is a format like 1.0f 1.0e10f
    }

    static boolean isDouble(String str) {
        return !isNullOrEmpty(str) && !str.endsWith("f") && str.isDouble() // In case of double it is easy, they are not allowed to end with f
    }

    static boolean isBoolean(String str) {
        try {
            safeToBoolean(str)
        } catch (IllegalArgumentException e) {
            return false
        }
        return true
    }

    static boolean isNullOrEmpty(String string) {
        return !string
    }

    static boolean isNullOrEmpty(Collection collection) {
        return !collection
    }

    static boolean isDefinedArray(String value) {
        return !isNullOrEmpty(value?.trim()) && ([value.trim()[0], value.trim()[-1]] == ["(", ")"]) // What else to test??
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    static String toFormattedXML(NodeChild nc, String separator = "\n") {
        List<String> resultList = XmlUtil.serialize(new StreamingMarkupBuilder().bind { it -> it.faulty nc }.toString()).readLines()
        if (resultList.size() >= 2)
            return resultList[1 .. -2].join(separator)
        else
            return ""

    }
}
