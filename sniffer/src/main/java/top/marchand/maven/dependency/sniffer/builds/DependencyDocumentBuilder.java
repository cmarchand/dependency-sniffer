/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, 
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can 
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
package top.marchand.maven.dependency.sniffer.builds;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;

/**
 * This class is in charge to build a document from a dependency:tree output.
 * The output must be provided as a String[] where each entry is a line.
 * 
 * This class is thread-safe and me be used by many concurrent threads.
 * @author cmarchand
 */
public class DependencyDocumentBuilder {
    
    public DependencyDocumentBuilder() {
        super();
    }
    
    /**
     * Creates the document, based on dependency:tree output lines.
     * Each <tt>&lt;artifact /&gt;</tt> and <tt>&lt;dependency /&gt;</tt> elements
     * have <tt>@id</tt> attribute, which uniquely defines the artifact.
     * This to be correctly indexed by BaseX
     * @param lines
     * @return A document(artifact)
     */
    public Document buildDocument(String[] lines) {
        StringBuilder sb = new StringBuilder();
        sb.append(lines[0]).append("\n");
        Element artifactElement = createArtifactFromLine(lines[0]);
        for(int i=1;i<lines.length;i++) {
            sb.append(lines[i]).append("\n");
            artifactElement.appendChild(createDependencyFromLine(lines[i]));
        }
        Element tree = createElement("tree", sb.toString());
        artifactElement.appendChild(tree);
        Document document = new Document(artifactElement);
        return document;
    }
    /**
     * Creates the artifact element, without dependencies
     * @param line
     * @return 
     */
    protected Element createArtifactFromLine(final String line) {
        String[] its = line.split(":");
        Element artifact = new Element("artifact");
        artifact.addAttribute(new Attribute("id", line));
        Element designation = new Element("designation");
        designation.appendChild(createElement("groupId", its[0]));
        designation.appendChild(createElement("artifactId", its[1]));
        designation.appendChild(createElement("version", its[3]));
        artifact.appendChild(designation);
        return artifact;
    }
    /**
     * Creates a dependency element
     * @param line
     * @return 
     */
    protected Element createDependencyFromLine(String line) {
        String compacted = line;
        while(" +-\\/|".contains(compacted.substring(0, 1))) compacted=compacted.substring(1);
        String[] its = compacted.split(":");
        Element dependency = new Element("dependency");
        dependency.addAttribute(new Attribute("id",compacted));
        dependency.appendChild(createElement("groupId", its[0]));
        dependency.appendChild(createElement("artifactId", its[1]));
        dependency.appendChild(createElement("version", its[3]));
        dependency.appendChild(createElement("scope", its[4]));
        return dependency;
    }
    protected Element createElement(String name, String content) {
        Element ret = new Element(name);
        ret.appendChild(content);
        return ret;
    }
}
