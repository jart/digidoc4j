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

import static org.digidoc4j.SignatureProfile.B_BES;
import static org.digidoc4j.SignatureProfile.LT;
import static org.digidoc4j.SignatureProfile.LTA;
import static org.digidoc4j.testutils.TestDataBuilder.createContainerWithFile;
import static org.digidoc4j.testutils.TestDataBuilder.createEmptyBDocContainer;
import static org.digidoc4j.testutils.TestDataBuilder.open;
import static org.digidoc4j.testutils.TestDataBuilder.signContainer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.digidoc4j.Container;
import org.digidoc4j.SignatureProfile;
import org.digidoc4j.exceptions.DigiDoc4JException;
import org.digidoc4j.impl.DigiDoc4JTestHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ExtendingBDocContainerTest extends DigiDoc4JTestHelper {

  String testContainerPath;

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();

  @Before
  public void setUp() throws Exception {
    testContainerPath = testFolder.newFile("testExtendTo.bdoc").getPath();
  }

  @Test
  public void extendFromB_BESToTS() throws Exception {
    Container container = createContainerWithFile("testFiles/test.txt", "text/plain");
    signContainer(container, B_BES);
    container.saveAsFile(testContainerPath);

    assertEquals(1, container.getSignatures().size());
    assertNull(container.getSignatures().get(0).getOCSPCertificate());

    container = open(testContainerPath);
    container.extendSignatureProfile(SignatureProfile.LT);
    container.saveAsFile(testFolder.newFile().getPath());

    assertEquals(1, container.getSignatures().size());
    assertNotNull(container.getSignatures().get(0).getOCSPCertificate());
  }

  @Test
  public void extendFromB_BESToLTA() throws Exception {
    Container container = createContainerWithFile("testFiles/test.txt", "text/plain");
    signContainer(container, B_BES);
    container.saveAsFile(testContainerPath);

    assertEquals(1, container.getSignatures().size());
    assertNull(container.getSignatures().get(0).getOCSPCertificate());

    container = open(testContainerPath);
    container.extendSignatureProfile(SignatureProfile.LTA);
    container.saveAsFile(testFolder.newFile().getPath());

    assertEquals(1, container.getSignatures().size());
    assertNotNull(container.getSignatures().get(0).getOCSPCertificate());
  }

  @Test (expected = DigiDoc4JException.class)
  public void extendFromB_BESToLT_TMThrowsException() throws Exception {
    Container container = createContainerWithFile("testFiles/test.txt", "text/plain");
    signContainer(container, B_BES);
    container.extendSignatureProfile(SignatureProfile.LT_TM);
  }

  @Test (expected = DigiDoc4JException.class)
  public void extendFromLTToLT_TMThrowsException() throws Exception {
    Container container = createContainerWithFile("testFiles/test.txt", "text/plain");
    signContainer(container, LT);
    container.extendSignatureProfile(SignatureProfile.LT_TM);
  }

  @Test (expected = DigiDoc4JException.class)
  public void extendFromLTAToLT_TMThrowsException() throws Exception {
    Container container = createContainerWithFile("testFiles/test.txt", "text/plain");
    signContainer(container, LTA);
    container.extendSignatureProfile(SignatureProfile.LT_TM);
  }

  @Test(expected = DigiDoc4JException.class)
  public void extendToWhenConfirmationAlreadyExists() throws Exception {
    Container container = createContainerWithFile("testFiles/test.txt", "text/plain");
    signContainer(container, B_BES);
    container.saveAsFile(testContainerPath);

    assertEquals(1, container.getSignatures().size());
    assertNull(container.getSignatures().get(0).getOCSPCertificate());

    container = open(testContainerPath);
    container.extendSignatureProfile(LT);
    container.extendSignatureProfile(LT);
  }

  @Test
  public void extendToWithMultipleSignatures() throws Exception {
    Container container = createContainerWithFile("testFiles/test.txt", "text/plain");
    signContainer(container, B_BES);
    signContainer(container, B_BES);
    container.saveAsFile(testContainerPath);

    assertEquals(2, container.getSignatures().size());
    assertNull(container.getSignatures().get(0).getOCSPCertificate());
    assertNull(container.getSignatures().get(1).getOCSPCertificate());

    container = open(testContainerPath);
    container.extendSignatureProfile(LT);
    String containerPath = testFolder.newFile("testExtendToContainsIt.bdoc").getPath();
    container.saveAsFile(containerPath);

    container = open(containerPath);
    assertEquals(2, container.getSignatures().size());
    assertNotNull(container.getSignatures().get(0).getOCSPCertificate());
    assertNotNull(container.getSignatures().get(1).getOCSPCertificate());
  }

  @Test
  public void extendToWithMultipleSignaturesAndMultipleFiles() throws Exception {
    Container container = createContainerWithFile("testFiles/test.txt", "text/plain");
    container.addDataFile("testFiles/test.xml", "text/xml");
    signContainer(container, B_BES);
    signContainer(container, B_BES);
    container.saveAsFile(testContainerPath);

    assertEquals(2, container.getSignatures().size());
    assertEquals(2, container.getDataFiles().size());
    assertNull(container.getSignatures().get(0).getOCSPCertificate());
    assertNull(container.getSignatures().get(1).getOCSPCertificate());

    container = open(testContainerPath);
    container.extendSignatureProfile(LT);
    container.saveAsFile(testFolder.newFile("testAddConfirmationContainsIt.bdoc").getPath());

    assertEquals(2, container.getSignatures().size());
    assertEquals(2, container.getDataFiles().size());
    assertNotNull(container.getSignatures().get(0).getOCSPCertificate());
    assertNotNull(container.getSignatures().get(1).getOCSPCertificate());
  }

  @Test
  public void testContainerExtensionFromLTtoLTA() throws Exception {
    Container container = createContainerWithFile("testFiles/test.txt", "text/plain");
    signContainer(container);

    container.extendSignatureProfile(LTA);
    assertNotNull(container.getSignatures().get(0).getOCSPCertificate());
  }

  @Test(expected = DigiDoc4JException.class)
  public void extensionNotPossibleWhenSignatureLevelIsSame() throws Exception {
    Container container = createEmptyBDocContainer();
    container.addDataFile("testFiles/test.txt", "text/plain");
    signContainer(container, LTA);
    container.extendSignatureProfile(LTA);
  }
}
