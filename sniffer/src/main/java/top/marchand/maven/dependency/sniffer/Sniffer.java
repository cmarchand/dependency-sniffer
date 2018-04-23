/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, 
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can 
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
package top.marchand.maven.dependency.sniffer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import top.marchand.maven.dependency.config.Config;
import top.marchand.maven.dependency.sniffer.builds.DependencyDocumentBuilder;
import top.marchand.maven.dependency.sniffer.builds.DependencyService;
import top.marchand.maven.dependency.sniffer.builds.HttpScanner;
import top.marchand.maven.dependency.sniffer.builds.ErrorReporter;
import top.marchand.maven.dependency.sniffer.builds.ScanService;
import top.marchand.org.basex.api.BaseXClient;

/**
 *
 * @author cmarchand
 */
public class Sniffer implements ScanService, DependencyService {
    
    public static void main(String[] args) throws IOException, ParsingException {
        new Sniffer(parseArguments(args)).run();
    }
    private final Config config;
    private final DependencyDocumentBuilder dependencyDocumentBuidler;
    private final ErrorReporter reporter;
    private ExecutorService scannerService;
    private ExecutorService dependencyService;
    private BaseXClient client;
    private long lastSubmit = 0L;
    private Document tree;
    
    public Sniffer(Config config) {
        super();
        this.config=config;
        this.dependencyDocumentBuidler = new DependencyDocumentBuilder();
        this.reporter = new ErrorReporter();
    }
    
    public void run() throws IOException {
        final ScheduledExecutorService surveillor = Executors.newSingleThreadScheduledExecutor();
        scannerService = Executors.newFixedThreadPool(config.getNbThreads());
        dependencyService = Executors.newSingleThreadExecutor();
        client = new BaseXClient(
                config.getBasexHost(), 
                config.getBasexPort(), 
                config.getBasexUser(), 
                config.getBasexPassword());
        // sets the collection to use
        client.execute("OPEN DEPS;");
        // gets the tree.xml document
        BaseXClient.Query query = client.query("xquery version 3.1; db:open('deps.xml','tree.xml')");
        tree = null;
        try {
            tree = new nu.xom.Builder().build(query.execute(), "/tree.xml");
        } catch(ParsingException ex) {
            tree = new Document(new Element("tree"));
        }

        for(String root: config.getNexusRoots()) {
            scannerService.submit(new HttpScanner(root, this, reporter, this));
        }
        surveillor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                if((now-lastSubmit)>10000) {
                    scannerService.shutdown();
                    surveillor.shutdown();
                    dependencyService.shutdown();
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
        try {
            scannerService.awaitTermination(1l, TimeUnit.HOURS);
            dependencyService.awaitTermination(1l, TimeUnit.HOURS);
            client.close();
        } catch(InterruptedException ex) {
            System.err.println("HTTP scanning took more than 1 hour");
        }
    }
    
    @Override
    public void submitScantask(Runnable t) {
        lastSubmit = System.currentTimeMillis();
        scannerService.submit(t);
    }
            
    @Override
    public void submitDependencyTask(Runnable r) {
        lastSubmit=System.currentTimeMillis();
        dependencyService.submit(r);
    }

    @Override
    public BaseXClient getDbClient() {
        return client;
    }
    
    @Override
    public Document getTreeDocument() {
        return tree;
    }

    protected static Config parseArguments(String[] args) throws IOException, ParsingException {
        if(args.length<2) {
            System.err.println(SYNTAX);
            throw new IllegalArgumentException("At least 2 arguments are required");
        }
        boolean isConfigFile = false;
        Config config = null;
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
            }
        }
        return config;
    }
    
    public static final String SYNTAX = "java top.marchand.maven.dependency.sniffer.Sniffer -c config <fileName>";

}
