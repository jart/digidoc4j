/* DigiDoc4J library
*
* This software is released under either the GNU Library General Public
* License (see LICENSE.LGPL).
*
* Note that the only valid version of the LGPL license as far as this
* project is concerned is the original GNU Library General Public License
* Version 2.1, February 1999
*/

package org.digidoc4j.impl.bdoc.xades;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.digidoc4j.Configuration;
import org.digidoc4j.exceptions.DigiDoc4JException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.DSSException;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.report.Reports;

public class XadesValidationReportGenerator implements Serializable {

  private final static Logger logger = LoggerFactory.getLogger(XadesValidationReportGenerator.class);
  private transient SignedDocumentValidator validator;
  private transient Reports validationReport;
  private DSSDocument signatureDocument;
  private List<DSSDocument> detachedContents;
  private Configuration configuration;

  public XadesValidationReportGenerator(DSSDocument signatureDocument, List<DSSDocument> detachedContents, Configuration configuration) {
    this.signatureDocument = signatureDocument;
    this.detachedContents = detachedContents;
    this.configuration = configuration;
  }

  public Reports openValidationReport() {
    if (validationReport != null) {
      logger.debug("Using existing validation report");
      return validationReport;
    }
    validationReport = createNewValidationReport();
    printReport(validationReport);
    return validationReport;
  }

  public void setValidator(SignedDocumentValidator validator) {
    this.validator = validator;
  }

  private Reports createNewValidationReport() {
    try {
      logger.debug("Creating a new validation report");
      InputStream validationPolicyAsStream = getValidationPolicyAsStream();
      if(validator == null) {
        validator = createXadesValidator();
      }
      return validator.validateDocument(validationPolicyAsStream);
    } catch (DSSException e) {
      logger.error("Error creating a new validation report: " + e.getMessage());
      throw new DigiDoc4JException(e);
    }
  }

  private SignedDocumentValidator createXadesValidator() {
    logger.debug("Creating a new xades validator");
    XadesValidationDssFacade validationFacade = new XadesValidationDssFacade(detachedContents, configuration);
    SignedDocumentValidator validator = validationFacade.openXadesValidator(signatureDocument);
    return validator;
  }

  private InputStream getValidationPolicyAsStream() {
    String policyFile = configuration.getValidationPolicy();
    if (Files.exists(Paths.get(policyFile))) {
      try {
        return new FileInputStream(policyFile);
      } catch (FileNotFoundException ignore) {
        logger.warn(ignore.getMessage());
      }
    }
    return getClass().getClassLoader().getResourceAsStream(policyFile);
  }

  private void printReport(Reports report) {
    if (logger.isTraceEnabled()) {
      Reports currentReports = report;
      do {
        logger.trace("----------------Validation report---------------");
        logger.trace(currentReports.getDetailedReport().toString());

        logger.trace("----------------Simple report-------------------");
        logger.trace(currentReports.getSimpleReport().toString());

        currentReports = currentReports.getNextReports();
      } while (currentReports != null);
    }
  }

}
