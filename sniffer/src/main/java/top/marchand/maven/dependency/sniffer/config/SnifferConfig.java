/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, 
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can 
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
package top.marchand.maven.dependency.sniffer.config;

import java.io.File;
import top.marchand.maven.dependency.config.Config;

/**
 * The cofniguration for Sniffer
 * @author cmarchand
 */
public class SnifferConfig {
    private final Config config;
    private final File inputFile;
    
    public SnifferConfig(final Config config, final File inputFile) {
        super();
        this.config=config;
        this.inputFile=inputFile;
    }

    public Config getConfig() {
        return config;
    }

    public File getInputFile() {
        return inputFile;
    }
    
    
    
}
