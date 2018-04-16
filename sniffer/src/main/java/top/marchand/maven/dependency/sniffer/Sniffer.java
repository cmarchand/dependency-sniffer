/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, 
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can 
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
package top.marchand.maven.dependency.sniffer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import nu.xom.Document;
import nu.xom.ParsingException;
import top.marchand.maven.dependency.config.Config;
import top.marchand.maven.dependency.sniffer.builds.DependencyDocumentBuilder;
import top.marchand.maven.dependency.sniffer.builds.DependencyScanner;
import top.marchand.maven.dependency.sniffer.builds.ErrorReporter;
import top.marchand.org.basex.api.BaseXClient;

/**
 *
 * @author cmarchand
 */
public class Sniffer {
    
    public static void main(String[] args) throws IOException, ParsingException {
        new Sniffer(parseArguments(args)).run();
    }
    private final Config config;
//    private final File inputFile;
    private final DependencyDocumentBuilder dependencyDocumentBuidler;
    private final ErrorReporter reporter;
    
    public Sniffer(Config config) {
        super();
        this.config=config;
//        this.inputFile=config.getInputFile();
        this.dependencyDocumentBuidler = new DependencyDocumentBuilder();
        this.reporter = new ErrorReporter();
    }
    
    public void run() throws IOException {
//        Document document = constructDocument(inputFile);
//        System.out.println(document.toXML());
//        saveDocument(document);
        ExecutorService service = Executors.newFixedThreadPool(config.getNbThreads());
        for(String root: config.getNexusRoots()) {
            service.submit(new DependencyScanner(root, service, reporter));
        }
        try {
            service.awaitTermination(1l, TimeUnit.HOURS);
        } catch(InterruptedException ex) {
            System.err.println("HTTP scanning took more than 1 hour");
        }
    }
    
    protected void saveDocument(Document document) throws IOException {
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
        
        BaseXClient client = new BaseXClient(config.getBasexHost(), config.getBasexPort(), config.getBasexUser(), config.getBasexPassword());
        client.execute("OPEN DEPS;");
        client.replace(path, new ByteArrayInputStream(baos.toByteArray()));
        client.close();
    }
    /**
     * Constructs a Document from the specified file
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws IOException 
     */
    protected Document constructDocument(File file) throws FileNotFoundException, IOException {
        ArrayList<String> lines;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            lines = new ArrayList<>();
            String line = br.readLine();
            while(line!=null && !line.isEmpty()) {
                lines.add(line);
                line = br.readLine();
            }
        }
        return dependencyDocumentBuidler.buildDocument(lines.toArray(new String[lines.size()]));
    }
    
    protected static Config parseArguments(String[] args) throws IOException, ParsingException {
        if(args.length<2) {
            System.err.println(SYNTAX);
            throw new IllegalArgumentException("At least 2 arguments are required");
        }
        boolean isConfigFile = false;
        Config config = null;
        String inputFileName = null;
        for(String s: args) {
            if("-c".equals(s)) {
                isConfigFile = true; 
                continue;
            }
            if(isConfigFile) {
                try {
                    config = Config.parseConfigFile(new File(s));
                } catch(IOException | ParsingException ex) {
                    System.err.println("while reading config");
                    throw ex;
                }
                isConfigFile = !isConfigFile;
//            } else {
//                inputFileName = s;
            }
        }
        return config;
    }
    
    public static final String SYNTAX = "java top.marchand.maven.dependency.sniffer.Sniffer -c config <fileName>";
    
}
