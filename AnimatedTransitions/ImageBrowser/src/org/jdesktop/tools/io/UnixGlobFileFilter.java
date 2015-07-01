/**
 * Copyright (c) 2006, Sun Microsystems, Inc
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following 
 *     disclaimer in the documentation and/or other materials provided 
 *     with the distribution.
 *   * Neither the name of the Harvester project nor the names of its
 *     contributors may be used to endorse or promote products derived 
 *     from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jdesktop.tools.io;

import java.io.FileFilter;
import java.io.File;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class UnixGlobFileFilter implements FileFilter {
    private Pattern pattern;

    public UnixGlobFileFilter(String filter) {
        pattern = Pattern.compile(globToRegex(filter));
    }

    public boolean accept(File file) {
        String path = file.getName();
        Matcher matcher = pattern.matcher(path);
        return matcher.matches();
    }

    private String globToRegex(String glob) {
        char c = '\0';
        boolean escape = false;
        boolean enclosed = false;
        StringBuffer buffer = new StringBuffer(glob.length());

        for (int i = 0; i < glob.length(); i++) {
            c = glob.charAt(i);

            if (escape) {
                buffer.append('\\');
                buffer.append(c);
                escape = false;
                continue;
            }

            switch (c) {
                case '*':
                    buffer.append('.').append('*');
                    break;
                case '?':
                    buffer.append('.');
                    break;
                case '\\':
                    escape = true;
                    break;
                case '.':
                    buffer.append('\\').append('.');
                    break;
                case '{':
                    buffer.append('(');
                    enclosed = true;
                    break;
                case '}':
                    buffer.append(')');
                    enclosed = false;
                    break;
                case ',':
                    if (enclosed)
                        buffer.append('|');
                    else
                        buffer.append(',');
                    break;
                default:
                    buffer.append(c);
            }
        }
        return buffer.toString();
    }
}
