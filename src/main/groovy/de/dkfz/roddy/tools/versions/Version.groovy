/*
 * Copyright (c) 2016 German Cancer Research Center (DKFZ).
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/TheRoddyWMS/RoddyToolLib/LICENSE.txt).
 */

package de.dkfz.roddy.tools.versions

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import java.text.ParseException
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Version class for semantic versioning. In particular a disciplined way of increasing versions at different semantic
 * versioning levels is supported as well as a compatibility relation as equivalence relation and tolerating differences
 * at any chosen version level. By default revision-level difference of otherwise identical versions are considered
 * compatible.
 *
 * Version is comparable to allow "versionA < versionB" expressions.
 */
@CompileStatic
class Version implements Comparable<Version> {

    final Integer major
    final Integer minor
    final Integer patch
    final Integer revision


    Version (Integer major, Integer minor, Integer patch, Integer revision = 0) {
        this.major = major
        this.minor = minor
        this.patch = patch
        this.revision = revision
    }

    Version increaseMajor() {
        return new Version(major + 1, 0, 0, 0)
    }

    Version increaseMinor() {
        return new Version(major, minor + 1, 0, 0)
    }

    Version increasePatch() {
        return new Version(major, minor, patch + 1, 0)
    }

    Version increaseRevision() {
        return new Version(major, minor, patch, revision + 1)
    }

    String toString() {
        return "${major}.${minor}.${patch}-${revision}"
    }

    String toString(VersionLevel level) {
        String result = getAt(VersionLevel.MAJOR)
        if (level != VersionLevel.MAJOR) {
            for (l in VersionLevel.MINOR..level) {
                if (l != VersionLevel.REVISION)
                    result += '.' + getAt(l)
                else
                    result += '-' + getAt(l)
            }
        }
        return result
    }

    private static final Pattern versionPattern = Pattern.compile(/^(\d+)(?:\.(\d+)(?:\.(\d+)(?:-(\d+))?)?)?$/)

    static Version fromString (String versionString) {
        Matcher matcher = versionPattern.matcher(versionString)
        if (matcher.matches()) {
            return new Version(
                    matcher.group(1).toInteger(),
                    matcher.group(2) ? matcher.group(2).toInteger() : 0,
                    matcher.group(3) ? matcher.group(3).toInteger() : 0,
                    matcher.group(4) ? matcher.group(4).toInteger() : 0,
            )
        } else {
            throw new ParseException("Could not parse version string '${versionString}'", 0)
        }
    }


    /** The indexed access allows selecting the version level of the version. Additionally, getAt() allows a
     *  destructuring bind of the form `def (major, minor, patch, revision) = version`.
     *
     * @param level    VersionLevel to be returned.
     * @return
     */
    @CompileDynamic
    Integer getAt (VersionLevel level) {
        if (level == VersionLevel.MAJOR) major
        else if (level == VersionLevel.MINOR) minor
        else if (level == VersionLevel.PATCH) patch
        else if (level == VersionLevel.REVISION) revision
        else throw new IndexOutOfBoundsException("Use 1-based indices: 1.2.3-4")
    }

    /** Convenience method. */
    @CompileDynamic
    Integer getAt (int level) {
        return getAt(VersionLevel.fromInteger(level))
    }

    /** Note: These equals and compareTo are too important to mess around with compatibility levels. Therefore, for
     *        these methods the most exact comparison is done. E.g. when compatibility intervals are to be sorted
     *        this needs to be done exactly.
     *
     * @param other
     * @return exact semantic equality of Versions
     */
    Boolean equals (Version other) {
        return this.compareTo(other, VersionLevel.REVISION) == 0
    }

    int compareTo(Version o) {
        return compareTo(o, VersionLevel.REVISION)
    }

    /** A more generic version of comparison function that allows to compare up to a specific version level. This is
     *  used to determine compatibility. */
    int compareTo(Version o, VersionLevel level) {
        for (l in VersionLevel.MAJOR..level) {
            int result = this[l].compareTo(o[l])
            if (result != 0)
                return result
        }
        return 0
    }

}


