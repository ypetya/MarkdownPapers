/*
 * Copyright 2011, TAUTUA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tautua.markdownpapers;

import org.custommonkey.xmlunit.HTMLDocumentBuilder;
import org.custommonkey.xmlunit.TolerantSaxDocumentBuilder;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

/**
 * Larry Ruiz
 */
public abstract class BaseTest {

    private static final String DEFAULT_OUTPUT_EXTENSION = ".html";
    private static final String DEFAULT_INPUT_EXTENSION = ".text";
    
    protected String fileName;
    private String inputExtension;
    private String outputExtension; 
    private File inputDirectory;
    private File outputDirectory;

    protected BaseTest(String fileName, File inputDirectory, File outputDirectory) {
        this(fileName, inputDirectory, outputDirectory, DEFAULT_INPUT_EXTENSION, DEFAULT_OUTPUT_EXTENSION);
    }
    
    protected BaseTest(String fileName, File inputDirectory, File outputDirectory, String inputExtension, String outputExtension) {
        this.fileName = fileName;
        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;
        this.inputExtension = inputExtension;
        this.outputExtension = outputExtension;
    }


    protected static void transform(File in, File out) throws Exception {
        Markdown md = new Markdown();
        Reader r = new FileReader(in);
        Writer w = new FileWriter(out);
        md.transform(r, w);
        r.close();
        w.close();
    }

    /**
     * <p>Compare two xml files, whitespace and attribute order are ignored.</p>
     * @param expected
     * @param output
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    protected static void compare(File expected, File output) throws IOException, SAXException, ParserConfigurationException {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        TolerantSaxDocumentBuilder tolerantSaxDocumentBuilder = new TolerantSaxDocumentBuilder(XMLUnit.newTestParser());
        HTMLDocumentBuilder htmlDocumentBuilder = new HTMLDocumentBuilder(tolerantSaxDocumentBuilder);
        org.w3c.dom.Document e = htmlDocumentBuilder.parse(new FileReader(expected));
        org.w3c.dom.Document o = htmlDocumentBuilder.parse(new FileReader(output));
        XMLAssert.assertXMLEqual(e, o);
    }
    
    @Before
    public void setup() {
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
    }

    @Test
    public void execute() throws Exception {
        File input = new File(inputDirectory, fileName + inputExtension);
        File expected = new File(inputDirectory, fileName + outputExtension);
        File output = new File(outputDirectory, fileName + outputExtension);
        transform(input, output);
        if(expected.exists()) {
            compare(expected, output);
        }
    }
}
