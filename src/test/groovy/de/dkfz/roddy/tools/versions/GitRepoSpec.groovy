/*
 * Copyright (c) 2017 eilslabs.
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */

package de.dkfz.roddy.tools.versions

import spock.lang.Specification

class GitRepoSpec extends Specification {

    File tmpDir
    File tmpFile

    def setup () {
        tmpDir = File.createTempDir("testRepoDir-", "")
        tmpFile = File.createTempFile("testFile", "", tmpDir)
        tmpDir.deleteOnExit()
    }

    def "check initializing new repo" () {
        when:
        GitRepo repo = new GitRepo(tmpDir)
        repo.initialize()
        then:
        "sync".execute()
        repo.repoDir.exists() && new File (repo.repoDir, ".git").exists()
    }

    def "adding a file" () {
        when:
        GitRepo repo = new GitRepo(tmpDir).initialize()
        then:
        "sync".execute()
        repo.add([tmpFile])
    }

    def "committing a file" () {
        when:
        GitRepo repo = new GitRepo (tmpDir).initialize()
        repo.add([tmpFile])
        then:
        "sync".execute()
        repo.commit("testfile")
    }

    def "checking current commit" () {
        when:
        GitRepo repo = new GitRepo (tmpDir).initialize()
        repo.add([tmpFile])
        repo.commit("testfile")
        then:
        "sync".execute()
        repo.lastCommitHash(true).matches(~ /^[0-9a-f]+$/)
    }

    def "listing modified files" () {
        when:
        GitRepo repo = new GitRepo (tmpDir).initialize()
        repo.add([tmpFile])
        repo.commit("testfile")
        tmpFile.write("hallo")
        then:
        "sync".execute()
        repo.modifiedObjects() == [tmpFile.toString()]
    }

    def "checking current commit date" () {
        when:
        GitRepo repo = new GitRepo (tmpDir).initialize()
        repo.add([tmpFile])
        repo.commit("testfile")
        then:
        "sync".execute()
        (repo.lastCommitDate(true) =~ /^\S{3}\s\S{3}\s\d{1,2}\s\d{2}:\d{2}:\d{2}\s\d{4}\s[+-]\d{4}$/) as Boolean
    }

    def "recognizing dirty repo" () {
        when:
        GitRepo repo = new GitRepo (tmpDir).initialize()
        tmpFile.write("hallo")
        repo.add([tmpFile])
        // This test tends to fail without "sync" of the target filesystem, probably because the git data are not flushed.
        then:
        "sync".execute()
        repo.isDirty()
        when:
        repo.commit("testmessage")
        then:
        "sync".execute()
        !repo.isDirty()
        when:
        tmpFile.write("modified hello")
        then:
        "sync".execute()
        repo.isDirty()
    }

    def "tagging a commit" () {
        when:
        GitRepo repo = new GitRepo (tmpDir).initialize()
        repo.add([tmpFile])
        repo.commit("testmessage")
        then:
        "sync".execute()
        repo.tag("testname", "testmessage", false)
    }

}
