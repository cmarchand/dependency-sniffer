/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, 
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can 
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
package top.marchand.maven.dependency.sniffer.builds;

import nu.xom.Document;
import top.marchand.org.basex.api.BaseXClient;

/**
 * Allows to submit a dependency scan task
 * @author cmarchand
 */
public interface DependencyService {
    /**
     * Submits a new dependency task
     * @param r 
     */
    public void submitDependencyTask(Runnable r);
    /**
     * Returns the BaseXClient
     * @return 
     */
    public BaseXClient getDbClient();
    /**
     * This method is never thread safe, and <b>must not</b> be used by multiple 
     * concurrent threads
     * @return The tree document.
     */
    public Document getTreeDocument();
}
