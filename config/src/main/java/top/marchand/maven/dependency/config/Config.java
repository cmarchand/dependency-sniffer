/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, 
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can 
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
package top.marchand.maven.dependency.config;

import java.io.File;
import java.io.IOException;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cmarchand
 */
public class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
    private String basexHost;
    private int basexPort = 1984;
    private String basexUser = "admin";
    private String basexPassword = "admin";
    private String[] nexusRoots;
    private int nbThreads = 4;
    
    public static Config parseConfigFile(File configFile) throws ParsingException, ValidityException, IOException {
        System.out.println(configFile.getAbsolutePath());
        Document documentConfig = new Builder().build(configFile);
        return new Config(documentConfig);
    }
    
    private Config(Document documentConfig) {
        Element basexElement = (Element)(documentConfig.query("/config/basex").get(0));
        Elements basexChilds = basexElement.getChildElements();
        for(int i=0;i<basexChilds.size();i++) {
            Element el = basexChilds.get(i);
            switch(el.getLocalName()) {
                case "host": { basexHost = el.getValue(); break ;}
                case "port": { 
                    try { 
                        basexPort = Integer.parseInt(el.getValue());
                    } catch(NumberFormatException ex) {
                        System.err.println("/config/basex/port: "+ex.getMessage()); 
                    } break ; 
                }
                case "user": { basexUser = el.getValue(); break; }
                case "password": { basexPassword = el.getValue(); break; }
                default: System.err.println("unexpected element : /config/basex/"+el.getLocalName());
            }
        }
        LOGGER.info("Connectiong to basex "+basexUser+"@"+basexHost);
        Element nexusElement = (Element)(documentConfig.query("/config/nexus").get(0));
        Elements nexusChilds = nexusElement.getChildElements();
        nexusRoots = new String[nexusChilds.size()];
        for(int i=0; i<nexusChilds.size();i++) {
            nexusRoots[i] = nexusChilds.get(i).getValue();
        }
        Element runtimeElement = documentConfig.getRootElement().getFirstChildElement("runtime");
        try {
            nbThreads = Integer.parseInt(runtimeElement.getFirstChildElement("nbThreads").getValue());
        } catch(NumberFormatException ex) {
            // ignore...
        }
    }

    public String getBasexHost() {
        return basexHost;
    }

    public int getBasexPort() {
        return basexPort;
    }

    public String getBasexUser() {
        return basexUser;
    }

    public String getBasexPassword() {
        return basexPassword;
    }

    public String[] getNexusRoots() {
        return nexusRoots;
    }

    public int getNbThreads() {
        return nbThreads;
    }
    
}
