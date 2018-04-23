/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, 
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can 
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
package top.marchand.maven.dependency.sniffer.builds;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;

/**
 *
 * @author cmarchand
 */
public class DependencyScanner implements Runnable {
    private final String url;
    private final ErrorReporter reporter;
    private final DependencyService service;
    
    public DependencyScanner(final String url, ErrorReporter reporter, final DependencyService service) {
        super();
        this.url=url;
        this.reporter = reporter;
        this.service = service;
    }

    @Override
    public void run() {
        ArrayList<String> lines=new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            String line = br.readLine();
            while(line!=null && !line.isEmpty()) {
                lines.add(line);
                line = br.readLine();
            }
        } catch(Exception ex) {
            reporter.error("while reading "+url, ex);
        }
        Document document = new DependencyDocumentBuilder().buildDocument(lines.toArray(new String[lines.size()]));
        String id = document.getRootElement().getAttributeValue("id");
        String[] its = id.split(":");
        StringBuilder sb = new StringBuilder("/");
        sb.append(its[0].replaceAll("\\.","/")).append("/");
        sb.append(its[1]).append("/");
        sb.append(its[3]).append(".xml");
        String path = sb.toString();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(baos);
        PrintWriter pw = new PrintWriter(osw);
        pw.print(document.toXML());
        pw.flush();
        // tree
        String[] groups = document.getRootElement().getAttributeValue("groupId").split("\\.");
        boolean treeModified = false;
        Element parent = service.getTreeDocument().getRootElement();
        for(String s:groups) {
            Element child = parent.getFirstChildElement(s);
            if(child==null) {
                child = new Element(s);
                parent.appendChild(child);
                treeModified = true;
            }
            parent = child;
        }
        String artifactId = document.getRootElement().getAttributeValue("artifactId");
        Element child = parent.getFirstChildElement(artifactId);
        if(child == null) {
            child = new Element(artifactId);
            parent.appendChild(child);
            parent = child;
            treeModified = true;
        }
        String version = document.getRootElement().getAttributeValue("version");
        child = parent.getFirstChildElement(version);
        if(child == null) {
            child = new Element(version);
            child.addAttribute(new Attribute("id",document.getRootElement().getAttributeValue("id")));
            parent.appendChild(child);
            treeModified = true;
        }
        try {
            service.getDbClient().replace(path, new ByteArrayInputStream(baos.toByteArray()));
            if(treeModified) {
                ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                OutputStreamWriter osw2 = new OutputStreamWriter(baos2);
                PrintWriter pw2 = new PrintWriter(osw2);
                pw.print(document.toXML());
                pw.flush();
                service.getDbClient().replace("/tree.xml", new ByteArrayInputStream(baos2.toByteArray()));
            }
        } catch(IOException ex) {
            reporter.error("Saving "+url, ex);
        }
    }
}
