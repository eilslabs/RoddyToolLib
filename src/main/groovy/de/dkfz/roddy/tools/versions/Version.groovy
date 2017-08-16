package de.dkfz.roddy.tools.versions

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import java.text.ParseException
import java.util.regex.Matcher
import java.util.regex.Pattern

class VersionInterval {
    final Version from
    final Version to

    def contains(Version query) {
        return query >= from && query <= to
    }
}


@CompileStatic
class Version implements Comparable<Version> {

    enum VersionLevel {
        MAJOR,
        MINOR,
        PATCH,
        REVISION;

        static VersionLevel fromInteger(int x) {
            switch(x) {
                case 0:
                    return MAJOR;
                case 1:
                    return MINOR;
                case 2:
                    return PATCH;
                case 3:
                    return REVISION;
                default:
                    throw new RuntimeException("Cannot interpret integer ${x} as VersionLevel")
            }
            return null;
        }

        // Taken from https://kousenit.org/2008/03/19/turning-java-enums-into-groovy-ranges
        VersionLevel next() {
            VersionLevel[] vals = VersionLevel.values()
            return vals[(this.ordinal() + 1) % vals.length]
        }

        VersionLevel previous() {
            VersionLevel[] vals = VersionLevel.values()
            return vals[(this.ordinal() - 1 + vals.length) % vals.length]
        }
    }

    final Integer major
    final Integer minor
    final Integer patch
    final Integer revision

    public File buildVersionFile = null

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

    private static final Pattern versionPattern = Pattern.compile(/^(\d+)\.(\d+)\.(\d+)(-(\d+))?$/)

    static Version fromString (String versionString) {
        Matcher matcher = versionPattern.matcher(versionString)
        if (matcher.find()) {
            return new Version (
                    matcher.group(1).toInteger(),
                    matcher.group(2).toInteger(),
                    matcher.group(3).toInteger(),
                    matcher.group(5).toInteger(),
            )
        } else {
            throw new ParseException("Could not parse version string '${versionString}'", 0)
        }
    }


    @CompileDynamic
    Integer getAt (int level) {
        return getAt(VersionLevel.fromInteger(level))
    }

    @CompileDynamic
    Integer getAt (VersionLevel level) {
        if (level == VersionLevel.MAJOR) major
        else if (level == VersionLevel.MINOR) minor
        else if (level == VersionLevel.PATCH) patch
        else if (level == VersionLevel.REVISION) revision
        else throw new IndexOutOfBoundsException("1.2.3-4")
    }

    Boolean equals (Version other) {
        return this.compareTo(other) == 0
    }

    @Override
    int compareTo(Version o) {
        return compareTo(o, VersionLevel.REVISION)
    }

    /** A more generic version of comparison function that allows to compare up to a specific version level. */
    int compareTo(Version o, VersionLevel level) {
        for (l in VersionLevel.MAJOR..level) {
            int result = this[l].compareTo(o[l])
            if (result != 0)
                return result
        }
        return 0
    }


    /** If two versions are compatible, then all intermediate versions are also compatible. Compatibility generates an
     *  equivalence class of versions (reflexive, symmetric, transitive). */

    /** Two versions are compatible, if they have they only differ in the revision, or if the versions are explicitly
     *  marked as being compatible. The explicit marking of revisions as compatible can be done with the
     *  compatibilities parameter. If there is any interval that contains both versions (this and the other) then
     *  the two versions are compatible. Also these checks are up to the VersionLevel.REVISION.
     *
     */
    boolean compatibleTo(Version other, Collection<VersionInterval> compatibilities) {
        Boolean samePatch = this.compareTo(other, VersionLevel.PATCH) == 0
        if (samePatch) {
            return true
        } else {
            VersionInterval sharedInterval = compatibilities.find { interval ->
                interval.contains(this) && interval.contains(other)
            }
            return null != sharedInterval
        }
    }



}

