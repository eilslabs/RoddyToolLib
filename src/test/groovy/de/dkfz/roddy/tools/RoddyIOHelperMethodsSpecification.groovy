package de.dkfz.roddy.tools

import spock.lang.Shared
import spock.lang.Specification

import java.awt.image.ImagingOpException

import static de.dkfz.roddy.tools.RoddyIOHelperMethods.NativeLinuxZipCompressor

class RoddyIOHelperMethodsSpecification extends Specification {

    @Shared
    def zipCompressor = new NativeLinuxZipCompressor()

    def "FindComponentIndexInPath"() {
        when:
        def result = RoddyIOHelperMethods.findComponentIndexInPath("/a/b/\${sample}/d", '${sample}')
        then:
        result == Optional.of(2)
    }

    def "test get native linux zip compressor compression string"(String source, String zipFile, String result) {
        expect:
        zipCompressor.getCompressionString(new File(source), new File(zipFile)).toString() == result

        where:
        source        | zipFile            | result
        "/source/abc" | "/target/test.zip" | '[[ -f "/target/test.zip" ]] && rm /target/test.zip; cd /source && zip -r9 /target/test.zip abc > /dev/null && md5sum /target/test.zip'
    }

    def "test get native linux zip compressor decompression string"(String from, String to, String result) {
        expect:
        zipCompressor.getDecompressionString(new File(from), new File(to)).toString() == result

        where:
        from              | to                | result
        "/source/abc.zip" | "/target/results" | "[[ ! -d /target/results ]] && mkdir -p /target/results; cd /target/results && unzip -o /source/abc.zip > /dev/null"
    }

    def "Test correct run of native linux zip compression and decompression on directory. The test shall return without exceptions!"() {
        setup: "First setup input data"

        def rangeOfFiles = ["a", "b", "c"]
        File tempFolder = File.createTempDir()
        tempFolder.deleteOnExit()

        File sourceTempFolder = new File(tempFolder, "src")
        File zipFile = new File(sourceTempFolder, "zipped.zip")
        File dirToZip = new File(sourceTempFolder, "toZip")
        dirToZip.mkdirs()

        and: "Populate src folder with test files"
        rangeOfFiles.each { new File(dirToZip, it) << "testFile" }

        and: "Then setup output data templates"
        File targetTempFolder = new File(tempFolder, "tgt")
        File targetUnzippedFolder = new File(targetTempFolder, "toZip")

        when:
        zipCompressor.compress(dirToZip, zipFile)
        zipCompressor.decompress(zipFile, targetTempFolder)

        then:
        targetUnzippedFolder.exists()
        targetUnzippedFolder.isDirectory()
        rangeOfFiles.every() {
            File f = new File(targetUnzippedFolder, it);
            f.exists() && f.text == "testFile"
        }

        cleanup:
        tempFolder.deleteDir()
    }

    def "Test failed compression with unreadable input data"() {
        setup: "First setup input data"

        def rangeOfFiles = ["a", "b", "c"]
        File tempFolder = File.createTempDir()
        tempFolder.deleteOnExit()

        File sourceTempFolder = new File(tempFolder, "src")
        File zipFile = new File(sourceTempFolder, "zipped.zip")
        File dirToZip = new File(sourceTempFolder, "toZip")

        and: "Populate src folder with test files. Make source files unreadable"
        dirToZip.mkdirs()
        rangeOfFiles.each { (new File(dirToZip, it) << "testFile").setReadable(false) }

        when:
        zipCompressor.compress(dirToZip, zipFile)

        then:
        thrown IOException

        cleanup: "Remove folders and reset file permissions first (they can't be deleted otherwise)"
        rangeOfFiles.each { new File(dirToZip, it).setReadable(true) }
        tempFolder.deleteDir()
    }

    def "Test failed compression with unwritable output folder"() {
        setup: "First setup input data"

        def rangeOfFiles = ["a", "b", "c"]
        File tempFolder = File.createTempDir()
        tempFolder.deleteOnExit()

        File sourceTempFolder = new File(tempFolder, "src")
        File dirToZip = new File(sourceTempFolder, "toZip")
        dirToZip.mkdirs()

        and: "Populate src folder with test files"
        rangeOfFiles.each { new File(dirToZip, it) << "testFile" }

        and: "Then setup output data templates"
        File targetTempFolder = new File(tempFolder, "tgt")
        File zipFile = new File(targetTempFolder, "zipped.zip")
        targetTempFolder.setWritable(false)
        File targetUnzippedFolder = new File(targetTempFolder, "toZip")

        when:
        zipCompressor.compress(dirToZip, zipFile)

        then:
        thrown IOException

        cleanup:
        targetTempFolder.setWritable(true)
        tempFolder.deleteDir()
    }
}
