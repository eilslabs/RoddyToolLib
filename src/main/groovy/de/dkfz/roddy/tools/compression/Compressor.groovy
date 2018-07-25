/*
 * Copyright (c) 2018 German Cancer Research Center (DKFZ).
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */
package de.dkfz.roddy.tools.compression

import groovy.transform.CompileStatic

@CompileStatic
abstract class Compressor {
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
