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
import nu.xom.Nodes;

/**
 *
 * @author cmarchand
 */
public class DependencyScanner implements Runnable {
    private final String url;
    private final ErrorReporter reporter;
    private final DependencyService service;
    private static final String NAME = "node";
    
    public DependencyScanner(final String url, ErrorReporter reporter, final DependencyService service) {
        super();
        this.url=url;
        this.reporter = reporter;
        this.service = service;
    }

    @Override
    public void run() {
        try {
            reporter.info("reading dependency "+url);
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
            reporter.info("\tpath is "+path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(baos);
            PrintWriter pw = new PrintWriter(osw);
            pw.print(document.toXML());
            pw.flush();

            // tree
            Element designation = document.getRootElement().getFirstChildElement("designation");
            reporter.info(designation.toXML());
            String groupId = designation.getFirstChildElement("groupId").getValue();
            reporter.debug("groupId: "+groupId);
            String[] groups = groupId.split("\\.");
            boolean treeModified = false;
            Document tree = service.getTreeDocument();
            Element parent = tree.getRootElement();
            for(String s:groups) {
                reporter.debug("looking for "+s);
                Nodes nodes = parent.query(NAME+"[@name='"+s+"']");
                if(nodes.size()==0) {
                    reporter.debug("creating "+s);
                    Element child = new Element(NAME);
                    child.addAttribute(new Attribute("name", s));
                    parent.appendChild(child);
                    treeModified = true;
                    parent = child;
                } else {
                    parent = (Element)nodes.get(0);
                }
            }
            String artifactId = designation.getFirstChildElement("artifactId").getValue();
            reporter.debug("artifactId: "+artifactId);
            Nodes nodes = parent.query(NAME+"[@name='"+artifactId+"']");
            if(nodes.size()==0) {
                reporter.debug("creating "+artifactId);
                Element child = new Element(NAME);
                child.addAttribute(new Attribute("name", artifactId));
                parent.appendChild(child);
                parent = child;
                treeModified = true;
            } else {
                parent = (Element)nodes.get(0);
            }
            String version = designation.getFirstChildElement("version").getValue();
            reporter.debug("version: "+version);
            nodes = parent.query(NAME+"[@name='"+version+"']");
            if(nodes.size()==0) {
                reporter.debug("creating "+version);
                Element child = new Element(NAME);
                child.addAttribute(new Attribute("name", version));
                child.addAttribute(new Attribute("id",document.getRootElement().getAttributeValue("id")));
                parent.appendChild(child);
                treeModified = true;
            }
            try {
                reporter.debug("saving dependency to "+path);
                service.getDbClient().replace(path, new ByteArrayInputStream(baos.toByteArray()));
                if(treeModified) {
                    reporter.debug("saving tree");
                    ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                    OutputStreamWriter osw2 = new OutputStreamWriter(baos2);
                    PrintWriter pw2 = new PrintWriter(osw2);
                    pw2.print(tree.toXML());
                    pw2.flush();
                    service.getDbClient().replace("/tree.xml", new ByteArrayInputStream(baos2.toByteArray()));
                    reporter.info("tree is now\n"+new String(baos2.toByteArray()));
                }
            } catch(IOException ex) {
                reporter.error("Saving "+url, ex);
            }
        } catch(Throwable t) {
            reporter.error("in dependency scanner<"+url+">", t);
        } finally {
            service.dependencyTaskCompleted();
        }
    }
}
