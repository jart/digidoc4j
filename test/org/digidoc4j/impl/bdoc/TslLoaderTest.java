/* DigiDoc4J library
*
* This software is released under either the GNU Library General Public
* License (see LICENSE.LGPL).
*
* Note that the only valid version of the LGPL license as far as this
* project is concerned is the original GNU Library General Public License
* Version 2.1, February 1999
*/

package org.digidoc4j.impl.bdoc;

import java.io.File;
import java.util.List;

import org.digidoc4j.Configuration;
import org.junit.Assert;
import org.junit.Test;

import eu.europa.esig.dss.tsl.TSLValidationSummary;
import eu.europa.esig.dss.tsl.service.TSLRepository;

public class TslLoaderTest {

  @Test
  public void loadAndValidateProdTsl() throws Exception {
    Configuration configuration = new Configuration(Configuration.Mode.PROD);
    TslLoader tslLoader = createTslLoader(configuration);
    tslLoader.setCheckSignature(true);
    tslLoader.createTSL();
    TSLRepository tslRepository = tslLoader.getTslRepository();
    assertTslValid(tslRepository);

  }

  private TslLoader createTslLoader(Configuration configuration) {
    String keystoreLocation = configuration.getTslKeyStoreLocation();
    TslLoader tslLoader = new TslLoader(configuration.getTslLocation(), new File(keystoreLocation), configuration.getTslKeyStorePassword());
    tslLoader.setConnectionTimeout(configuration.getConnectionTimeout());
    tslLoader.setSocketTimeout(configuration.getSocketTimeout());
    tslLoader.setCheckSignature(false);
    return tslLoader;
  }

  private void assertTslValid(TSLRepository tslRepository) {
    List<TSLValidationSummary> summaryList = tslRepository.getSummary();
    for(TSLValidationSummary summary: summaryList) {
      String indication = summary.getIndication();
      String country = summary.getCountry();
      Assert.assertEquals("TSL is not valid for country " + country, "VALID", indication);
    }
  }
}
