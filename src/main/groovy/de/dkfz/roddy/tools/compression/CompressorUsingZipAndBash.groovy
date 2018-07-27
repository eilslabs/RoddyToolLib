/*
 * Copyright (c) 2018 German Cancer Research Center (DKFZ).
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */
package de.dkfz.roddy.tools.compression

import de.dkfz.roddy.execution.io.LocalExecutionHelper
import groovy.transform.CompileStatic

import static de.dkfz.roddy.tools.shell.bash.Service.escape

/**
 * Produces md5 compatible zipped archives of the input file / folder
 * Also produces a file named [zip.gz].md5
 */
@CompileStatic
class CompressorUsingZipAndBash extends Compressor {

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
            LocalExecutionHelper.executeSingleCommand(getCompressionString(from, to).toString())
        } catch (Exception ex) {
            throw new IOException("Could not zip folder ${from} to zip archive ${to}", ex)
        }
    }

    @Override
    void decompress(File from, File to) {
        try {
            LocalExecutionHelper.executeSingleCommand(getDecompressionString(from, to))
        } catch (Exception ex) {
            throw new IOException("Could not unzip zip archive ${from} to ${to}", ex)
        }
    }

    @Override
    GString getCompressionString(File from, File to) {
        String _to = escape(to.getAbsolutePath())
        String _fromPath = escape(from.parent)
        String _fromName = escape(from.name)

        GString gString = "[[ -f ${_to} ]] && rm ${_to}; cd ${_fromPath} && zip -r9 ${_to} ${_fromName} > /dev/null && md5sum ${_to}"
        return gString
    }

    @Override
    GString getDecompressionString(File from, File to) {
        String _to = escape(to.absolutePath)
        String _from = escape(from.absolutePath)

        GString gString = "[[ ! -d ${_to} ]] && mkdir -p ${_to}; cd ${_to} && unzip -o ${_from} > /dev/null"
        return gString
    }
}