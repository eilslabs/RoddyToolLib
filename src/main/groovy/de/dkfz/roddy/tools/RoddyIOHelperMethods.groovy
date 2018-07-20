/*
 * Copyright (c) 2017 eilslabs.
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */

package de.dkfz.roddy.tools

import de.dkfz.roddy.StringConstants
import de.dkfz.roddy.execution.io.LocalExecutionHelper
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils

import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermissions

/**
 * Contains methods which print out text on the console, like listworkflows.
 * This is a bit easier in groovy so we'll use it for these tasks.
 *
 * User: michael
 * Date: 27.11.12
 * Time: 09:09
 */
@groovy.transform.CompileStatic
class RoddyIOHelperMethods {


    static abstract class Compressor {

        void compress(File from, File to) {
            if (from.isFile())
                compressFile(from, to)

            else if (from.isDirectory())
                compressDirectory(from, to)
        }

        @Deprecated
        void decompress(File from, File to, File wd) { decompress(from, to) }

        abstract void decompress(File from, File to)

        @Deprecated
        void compressFile(File from, File to, File wd) { compressFile(from, to) }

        abstract void compressFile(File from, File to)

        @Deprecated
        void compressDirectory(File from, File to, File wd) { compressDirectory(from, to) }

        abstract void compressDirectory(File from, File to)

        @Deprecated
        GString getCompressionString(File from, File to, File wd) { getCompressionString(from, to) }

        abstract GString getCompressionString(File from, File to)

        @Deprecated
        GString getDecompressionString(File from, File to, File wd) { getDecompressionString(from, to) }

        abstract GString getDecompressionString(File from, File to)
    }

    /**
     * Produces md5 compatible zipped archives of the input file / folder
     * Also produces a file named [zip.gz].md5
     */
    static class NativeLinuxZipCompressor extends Compressor {

        @Override
        void compressFile(File from, File to) {
            try {
                compressDirectory(from, to)
            } catch (Exception ex) {
                throw new IOException("Could not zip file ${from} to zip archive ${to}", ex)
            }
        }

        @Override
        void compressDirectory(File from, File to) {

            try {
                String result = LocalExecutionHelper.executeSingleCommand(getCompressionString(from, to).toString())
            } catch (Exception ex) {
                throw new IOException("Could not zip folder ${from} to zip archive ${to}", ex)
            }
        }

        @Override
        void decompress(File from, File to) {
            try {
                String result = LocalExecutionHelper.executeSingleCommand(getDecompressionString(from, to))
            } catch (Exception ex) {
                throw new IOException("Could not unzip zip archive ${from} to ${to}", ex)
            }
        }

        @Override
        GString getCompressionString(File from, File to) {
            String _to = to.getAbsolutePath()
            GString zipTarget = "${_to} ${from.getName()}"

            GString gString = "[[ -f \"${_to}\" ]] && rm ${_to}; cd ${from.parent} && zip -r9 ${zipTarget} > /dev/null && md5sum ${_to}"
            return gString
        }

        @Override
        GString getDecompressionString(File from, File to) {
            GString gString = "[[ ! -d ${to} ]] && mkdir -p ${to}; cd ${to} && unzip -o ${from} > /dev/null"
            return gString
        }
    }

    private static LoggerWrapper logger = LoggerWrapper.getLogger(RoddyIOHelperMethods.class.getSimpleName())

    private static Compressor compressor = new NativeLinuxZipCompressor()

    static Compressor getCompressor() { return compressor }

    static String[] loadTextFile(File f) {
        try {
            return f.readLines().toArray(new String[0])
        } catch (Exception ex) {
            return new String[0]
        }
    }

    static String loadTextFileEnblock(File f) {
        try {
            return f.text
        } catch (Exception ex) {
            return StringConstants.EMPTY
        }
    }

    static void writeTextFile(String path, List items) {
        writeTextFile(new File(path), items)
    }

    static void writeTextFile(File path, List items) {
        writeTextFile(path, items.collect { it.toString() }.join("\n") + "\n")
    }

    static void writeTextFile(String path, String text) {
        File f = new File(path)
        writeTextFile(f, text)
    }

    static void writeTextFile(File file, String text) {
        try {
            file.write(text)
        } catch (Exception ex) {
            logger.severe(ex.toString())
        }
    }

    static List<String> readTextFile(String path) {
        return readTextFile(new File(path))
    }

    static List<String> readTextFile(File path) {
        return path.readLines()
    }

    /**
     * This method copies a file and tries to preserve access rights using Java get/set rights methods.
     *
     * Modified after http://docs.oracle.com/javase/8/docs/api/.
     *
     * @param src
     * @param tgt
     */
    static void copyDirectory(File source, File target, CopyOption... options = [StandardCopyOption.COPY_ATTRIBUTES]) {
        copyDirectory(source.toPath(), target.toPath(), options)
    }

    static void copyDirectory(Path source, Path target, CopyOption... options = [StandardCopyOption.COPY_ATTRIBUTES]) {
        try {
            Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                    new SimpleFileVisitor<Path>() {
                        @Override
                        FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                                throws IOException {
                            Path targetdir = target.resolve(source.relativize(dir))
                            // I am not happy with the fuzzy specification in the Java API concerning symlinks.
                            try {
                                Files.copy(dir, targetdir, options)
                            } catch (FileAlreadyExistsException e) {
                                if (!Files.isDirectory(targetdir))
                                    throw e
                            }
                            return FileVisitResult.CONTINUE
                        }

                        @Override
                        FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                throws IOException {
                            Files.copy(file, target.resolve(source.relativize(file)), options)
                            return FileVisitResult.CONTINUE
                        }
                    })
        } catch (Exception ex) {
            logger.severe(ex.toString())
        }
    }

    static void compressFile(File file, File targetFile) {
        compressor.compressFile(file, targetFile)
    }

    static void compressDirectory(File file, File targetFile) {
        compressor.compressDirectory(file, targetFile)
    }

    /**
     *
     * @param file
     * @param targetBaseFolder The target folder to which the archives content will be extracted.
     * @param workingDirectory
     */
    static void decompressFile(File file, File targetBaseFolder) {
        compressor.decompress(file, targetBaseFolder)
    }

    static String getStackTraceAsString(Exception exception) {
        try {
            StackTraceElement[] stackTrace = null
            for (int i = 0; i < 3 && stackTrace == null; i++)
                stackTrace = exception.getStackTrace()
            if (stackTrace != null)
                return joinArray(stackTrace, System.getProperty("line.separator"))
        } catch (Exception ex) {
            logger.info("No stacktrace could be printed for an exception.")
            return ""
        }
    }

    static String join(String separator = "\n", String... entries) {
        return joinArray(entries as String[], separator)
    }

    static String joinArray(Object[] array, String separator) {
        return array.collect { it -> it.toString() }.join(separator)
    }

    static String joinArray(String[] array, String separator) {
        return array.join(separator)
    }

    static String joinTextFileContent(String[] array) {
        return joinArray(array, System.lineSeparator())
    }

    static String getMD5OfText(String text) {
        return DigestUtils.md5Hex(text)
    }

    static String getMD5OfFile(File f) {
        try {
            return DigestUtils.md5Hex(new FileInputStream(f))
        } catch (Exception ex) {
            logger.warning("Could not md5 file ${f.absolutePath} " + ex.toString())
            return ""
        }
    }

    static String getMD5OfPermissions(File f) {
        try {
            PosixFileAttributeView view = Files.getFileAttributeView(f.toPath(), PosixFileAttributeView.class) as PosixFileAttributeView
            return DigestUtils.md5Hex(PosixFilePermissions.toString(view.readAttributes().permissions()))
        } catch (Exception ex) {
            logger.warning("Could not md5 permissions string for file ${f.absolutePath}" + ex.toString())
            return ""
        }
    }

    static String truncateCommand(String inStr, int maxLength = 80) {
        if (maxLength > 0 && inStr?.size() > maxLength) {
            if (maxLength > 4) {
                return inStr[0..(maxLength - 4)] + " ..."
            } else {
                return inStr[0..(maxLength - 1)]
            }
        } else {
            return inStr
        }
    }

    /**
     * Recursively find all files in a directory, create their md5 sum and combine the sums to a single sum
     * (without directory names!!)
     * @param baseDirectory
     * @return
     */
    static String getSingleMD5OfFilesInDirectoryExcludingDirectoryNames(File baseDirectory) {
        List<File> list = []
        baseDirectory.eachFileRecurse(FileType.FILES) { File aFile -> list << aFile }
        getMD5OfText(list.sort().collect { getMD5OfFile(it) }.join(System.getProperty("line.separator")))
    }

    @Deprecated
    static String getSingleMD5OfFiles(File baseDirectory) {
        return getSingleMD5OfFilesInDirectoryIncludingDirectoryNamesAndPermissions(baseDirectory)
    }

    /**
     * The method finds all files in a directory, creates an md5sum of each baseDirectory and md5'es the result text.
     * This is i.e. useful when the folder has to be archived and the archives content should be comparable.
     * @param baseDirectory
     * @return
     */
    static String getSingleMD5OfFilesInDirectoryIncludingDirectoryNamesAndPermissions(File baseDirectory) {
        List<File> list = []
        List<String> md5s = []
        baseDirectory.eachFileRecurse(FileType.FILES) { File aFile -> list << aFile }
        list.sort()
        list.each {
            File file ->
                String md5OfDir = getMD5OfText(baseDirectory.name + file.absolutePath - baseDirectory.absolutePath)
                String md5OfFile = getMD5OfFile(file)
                String md5OfPermissions = getMD5OfPermissions(file)
                md5s << md5OfDir + md5OfFile + md5OfPermissions
        }
        return getMD5OfText(md5s.join(System.getProperty("line.separator")))
    }

    static synchronized void appendLineToFile(File file, String line) {
        file.append(line + System.getProperty("line.separator"))
    }

    static File assembleLocalPath(String rootPath, String... structure) {
        return assembleLocalPath(new File(rootPath), structure)
    }

    static File assembleLocalPath(File rootPath, String... structure) {
        if (!structure)
            return rootPath
        File result = new File(rootPath, structure[0])
        for (int i = 1; i < structure.length; i++) {
            result = new File(result, structure[i])
        }
        return result
    }

    private static final String calculateUMaskFromStringWithBash(String rightsStr, int defaultUserMask) {
        def defaultRights = numericToHashAccessRights(defaultUserMask)
        return LocalExecutionHelper.executeSingleCommand("umask ${defaultRights.values().join("")} && umask ${rightsStr} && umask")
    }

    static final String convertUMaskToAccessRights(String umask) {
        String ars = new String("0")
        for (int i = 1; i < umask.length(); i++) {
            ars += "" + (7 - umask[i].toInteger())
        }
        return ars
    }

    static final int getIntegerValueFromOctalAccessRights(String octalAccessRights) {
        return Integer.decode(octalAccessRights)
    }

    static final int symbolicToIntegerAccessRights(String rightsStr, int defaultUserMask) {
        return getIntegerValueFromOctalAccessRights(symbolicToNumericAccessRights(rightsStr, defaultUserMask))
    }

    /** Convert symbolic to numeric access rights.
     *
     * This method is currently not portable and uses a separate umask process ot properly calculate umasks
     * However, this will on work on systems supporting umask!
     *
     * @param rightsStr string representation of access rights
     * @return numeric access rights
     */
    static String symbolicToNumericAccessRights(String rightsStr, int defaultUserMask) {

        return convertUMaskToAccessRights(calculateUMaskFromStringWithBash(rightsStr, defaultUserMask))

    }

    static LinkedHashMap<String, Integer> numericToHashAccessRights(int rights) {
        assert rights <= 07777  // including suid, sgid, sticky bits.
        return [u: (rights & 0700) >> 6,
                g: (rights & 0070) >> 3,
                o: (rights & 0007)]
    }

    /** Split a pathname string into components (using '/'). Empty path components, as they occur between double
     *  component separators ('//') are omitted.
     *
     * @param pathname
     * @return
     */
    static ArrayList<String> splitPathname(String pathname) {
        pathname.split(StringConstants.SPLIT_SLASH).findAll({ it != "" }) as ArrayList<String>
    }

    /** Note that this method uses splitPathname(), which ignores empty path elements, e.g. the element in leading '/' or between '//'. This means
     *  that if you need both (index and value) you need to take splitPathname().
     *
     * @param path
     * @param component
     * @return
     */
    static Optional<Integer> findComponentIndexInPath(String path, String component) {
        Integer index = splitPathname(path).findIndexOf { it -> it == component }
        if (-1 == index) {
            Optional.empty()
        } else {
            Optional.of(index)
        }
    }

    /** Match a variable, defined by a pattern in a path. This checks that all leading path components
     *  that are not variable definitions in the pattern (i.e. ${someVar}) are identical for both the
     *  pattern and the path. If there is a mismatch, a RuntimeException is raised. Only the first match
     *  is considered. Later path components may diverge.
     *
     * @param pattern Path pattern, e.g. /path/to/${variable}/to/be/matched
     * @param variable Variable to be matched, e.g. pid. ${variable} will be searched for in pattern.
     * @param path Path containing the value. The path value that is matched in here will be returned
     * @return
     */
    static Optional<String> getPatternVariableFromPath(String pattern, String variable, String path) {
        ArrayList<String> patternComponents = splitPathname(pattern)
        ArrayList<String> pathComponents = splitPathname(path)
        Integer index = 0
        while (index < Math.min(patternComponents.size(), pathComponents.size())) {
            String patternC = patternComponents[index]
            String pathC = pathComponents[index]
            if (patternC.startsWith(StringConstants.DOLLAR_LEFTBRACE) && patternC.endsWith(StringConstants.BRACE_RIGHT)) {
                if (patternC == "\${${variable}}") {
                    return Optional.of(pathC)
                }
            } else if (patternC != pathC) {
                throw new RuntimeException("Pattern and path have incompatible prefix path component ${index} before \${${variable}}. Pattern = ${pattern}, Path = ${path}")
            }
            ++index
        }
        return Optional.empty()
    }


    static String printTimingInfo(String info, long t1, long t2) {
        return "Timing " + info + ": " + ((t2 - t1) / 1000000) + " ms"
    }
}
