package org.digidoc4j;

import org.digidoc4j.exceptions.ConfigurationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Hashtable;

import static org.digidoc4j.Configuration.*;
import static org.digidoc4j.Configuration.Mode.PROD;
import static org.digidoc4j.Configuration.Mode.TEST;
import static org.junit.Assert.*;

public class ConfigurationTest {
  private Configuration configuration;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() {
    System.clearProperty("digidoc4j.mode");
    configuration = new Configuration(TEST);
  }

  @Test
  public void setTslLocation() throws Exception {
    configuration.setTslLocation("tslLocation");
    assertEquals("tslLocation", configuration.getTslLocation());
  }

  @Test
  public void getTslLocationFromConfigurationFile() throws Exception {
    configuration.loadConfiguration("testFiles/digidoc_test_conf.yaml");
    assertEquals("file:conf/test_TSLLocation", configuration.getTslLocation());
  }

  @Test
  public void setTslLocationOverwritesConfigurationFile() throws Exception {
    configuration.loadConfiguration("testFiles/digidoc_test_conf.yaml");
    configuration.setTslLocation("tslLocation");
    assertEquals("tslLocation", configuration.getTslLocation());
  }

  @Test
  public void setTspSource() throws Exception {
    configuration.setTspSource("tspSource");
    assertEquals("tspSource", configuration.getTspSource());
  }

  @Test
  public void setValidationPolicy() throws Exception {
    configuration.setValidationPolicy("policy");
    assertEquals("policy", configuration.getValidationPolicy());
  }

  @Test
  public void setOcspSource() throws Exception {
    configuration.setOcspSource("ocsp_source");
    assertEquals("ocsp_source", configuration.getOcspSource());
  }

  @Test
  public void defaultOCSPAccessCertificateFile() {
    assertNull(configuration.getOCSPAccessCertificateFileName());
  }

  @Test
  public void getOCSPAccessCertificateFileFromConfigurationFile() {
    configuration.loadConfiguration("testFiles/digidoc_test_conf.yaml");
    assertEquals("conf/OCSP_access_certificate_test_file_name", configuration.getOCSPAccessCertificateFileName());
  }

  @Test
  public void setOCSPAccessCertificateFileNameOverwritesConfigurationFile() {
    configuration.loadConfiguration("testFiles/digidoc_test_conf.yaml");
    configuration.setOCSPAccessCertificateFileName("New File");
    assertEquals("New File", configuration.getOCSPAccessCertificateFileName());
  }

  @Test
  public void defaultOCSPAccessCertificatePassword() {
    assertEquals(0, configuration.getOCSPAccessCertificatePassword().length);
  }

  @Test
  public void getOCSPAccessCertificatePasswordFromConfigurationFile() throws Exception {
    configuration.loadConfiguration("testFiles/digidoc_test_conf.yaml");
    assertArrayEquals("OCSP_test_password".toCharArray(), configuration.getOCSPAccessCertificatePassword());
  }

  @Test
  public void setOCSPAccessCertificatePasswordOverwritesConfigurationFile() {
    configuration.loadConfiguration("testFiles/digidoc_test_conf.yaml");
    char[] newPassword = "New password".toCharArray();
    configuration.setOCSPAccessCertificatePassword(newPassword);
    assertArrayEquals(newPassword, configuration.getOCSPAccessCertificatePassword());
  }

  @Test
  public void defaultOcspSource() throws Exception {
    assertEquals("http://www.openxades.org/cgi-bin/ocsp.cgi", configuration.getOcspSource());
  }

  @Test
  public void defaultProductionConfiguration() throws Exception {
    Configuration configuration = new Configuration(PROD);
    assertEquals("https://ec.europa.eu/information_society/policy/esignature/trusted-list/tl-mp.xml",
        configuration.getTslLocation());
  }

  @Test
  public void defaultConstructorWithSetSystemProperty() throws Exception {
    System.setProperty("digidoc4j.mode", "TEST");
    Configuration configuration = new Configuration();
    assertEquals("http://10.0.25.57/tsl/trusted-test-mp.xml", configuration.getTslLocation());
  }

  @Test
  public void setMaxDataFileCached() throws Exception {
    configuration = new Configuration();
    long maxDataFileCached = 12345;
    configuration.enableBigFilesSupport(maxDataFileCached);
    assertEquals(maxDataFileCached, configuration.getMaxDataFileCachedInMB());
    assertEquals(maxDataFileCached * ONE_MB_IN_BYTES, configuration.getMaxDataFileCachedInBytes());
  }

  @Test
  public void setMaxDataFileCachedToNoCaching() {
    configuration = new Configuration();
    long maxDataFileCached = CACHE_NO_DATA_FILES;
    configuration.enableBigFilesSupport(maxDataFileCached);
    assertEquals(CACHE_NO_DATA_FILES, configuration.getMaxDataFileCachedInMB());
    assertEquals(CACHE_NO_DATA_FILES, configuration.getMaxDataFileCachedInBytes());
  }

  @Test
  public void setMaxDataFileCachedToAllCaching() {
    configuration = new Configuration();
    long maxDataFileCached = CACHE_ALL_DATA_FILES;
    configuration.enableBigFilesSupport(maxDataFileCached);
    assertEquals(CACHE_ALL_DATA_FILES, configuration.getMaxDataFileCachedInMB());
    assertEquals(CACHE_ALL_DATA_FILES, configuration.getMaxDataFileCachedInBytes());
  }

  @Test
  public void maxDataFileCachedNotAllowedValue() {
    configuration = new Configuration();
    long oldValue = 4096;
    configuration.enableBigFilesSupport(oldValue);
    configuration.enableBigFilesSupport(-2);
    assertEquals(oldValue, configuration.getMaxDataFileCachedInMB());
  }

  @Test
  public void maxDataFileCachedNotAllowedValueFromFile() {
    configuration = new Configuration();
    String fileName = "testFiles/digidoc_test_conf_max_datafile_cached_invalid.yaml";

    expectedException.expect(ConfigurationException.class);
    expectedException.expectMessage("Configuration parameter DIGIDOC_MAX_DATAFILE_CACHED should be greater or equal " +
        "-1 but the actual value is: -2.");

    configuration.loadConfiguration(fileName);
  }

  @Test
  public void defaultConstructorWithUnSetSystemProperty() throws Exception {
    Configuration configuration = new Configuration();
    assertEquals("https://ec.europa.eu/information_society/policy/esignature/trusted-list/tl-mp.xml",
        configuration.getTslLocation());
  }

  @Test
  public void testGetPKCS11ModulePath() throws Exception {
    assertEquals("/usr/lib/x86_64-linux-gnu/opensc-pkcs11.so", configuration.getPKCS11ModulePath());
  }

  @Test
  public void generateJDigiDocConfig() throws Exception {
    Hashtable<String, String> jDigiDocConf = configuration.loadConfiguration("digidoc4j.yaml");

    assertEquals("jar://certs/ESTEID-SK.crt", jDigiDocConf.get("DIGIDOC_CA_1_CERT2"));
    assertEquals("jar://certs/KLASS3-SK OCSP 2006.crt", jDigiDocConf.get("DIGIDOC_CA_1_OCSP2_CERT_1"));
    assertEquals("jar://certs/EID-SK OCSP 2006.crt", jDigiDocConf.get("DIGIDOC_CA_1_OCSP13_CERT_1"));
    assertEquals("jar://certs/TEST Juur-SK.crt", jDigiDocConf.get("DIGIDOC_CA_1_CERT17"));
    assertEquals(DEFAULT_LOG4J_CONFIGURATION, jDigiDocConf.get("DIGIDOC_LOG4J_CONFIG"));
    assertEquals(DEFAULT_SECURITY_PROVIDER, jDigiDocConf.get("DIGIDOC_SECURITY_PROVIDER"));
    assertEquals(DEFAULT_SECURITY_PROVIDER_NAME, jDigiDocConf.get("DIGIDOC_SECURITY_PROVIDER_NAME"));
    assertEquals("false", jDigiDocConf.get("DATAFILE_HASHCODE_MODE"));
    assertEquals(DEFAULT_CANONICALIZATION_FACTORY_IMPLEMENTATION, jDigiDocConf.get("CANONICALIZATION_FACTORY_IMPL"));
    assertEquals("-1", jDigiDocConf.get("DIGIDOC_MAX_DATAFILE_CACHED"));
    assertEquals("false", jDigiDocConf.get("SIGN_OCSP_REQUESTS"));
    assertEquals("jar://certs/KLASS3-SK OCSP.crt", jDigiDocConf.get("DIGIDOC_CA_1_OCSP2_CERT"));
  }

  @Test
  public void loadsJDigiDocSecurityProviderFromFile() throws Exception {
    Hashtable<String, String> jDigiDocConf = configuration.loadConfiguration("testFiles/digidoc_test_conf.yaml");
    assertEquals("org.bouncycastle.jce.provider.BouncyCastleProvider1", jDigiDocConf.get("DIGIDOC_SECURITY_PROVIDER"));
  }

  @SuppressWarnings("NumericOverflow")
  @Test
  public void loadsMaxDataFileCachedFromFile() throws Exception {
    Hashtable<String, String> jDigiDocConf = configuration.loadConfiguration("testFiles/digidoc_test_conf.yaml");

    assertEquals("8192", jDigiDocConf.get("DIGIDOC_MAX_DATAFILE_CACHED"));
    assertEquals(8192, configuration.getMaxDataFileCachedInMB());
    assertEquals(8192 * ONE_MB_IN_BYTES, configuration.getMaxDataFileCachedInBytes());
  }

  @Test
  public void settingNonExistingConfigurationFileThrowsError() throws Exception {
    expectedException.expect(ConfigurationException.class);
    expectedException.expectMessage("File testFiles/not_exists.yaml not found in classpath.");

    configuration.loadConfiguration("testFiles/not_exists.yaml");
  }


  @Test
  public void digiDocSecurityProviderDefaultValue() throws Exception {
    Hashtable<String, String> jDigiDocConf = configuration.loadConfiguration("digidoc4j.yaml");
    assertEquals(DEFAULT_SECURITY_PROVIDER, jDigiDocConf.get("DIGIDOC_SECURITY_PROVIDER"));
  }

  @Test
  public void digiDocSecurityProviderDefaultName() throws Exception {
    Hashtable<String, String> jDigiDocConf = configuration.loadConfiguration("digidoc4j.yaml");
    assertEquals(DEFAULT_SECURITY_PROVIDER_NAME, jDigiDocConf.get("DIGIDOC_SECURITY_PROVIDER_NAME"));
  }

  @Test
  public void asksValueOfNonExistingParameter() throws Exception {
    Hashtable<String, String> jDigiDocConf = configuration.loadConfiguration("digidoc4j.yaml");
    assertNull(jDigiDocConf.get("DIGIDOC_PROXY_HOST"));
  }

  @Test
  public void digidocMaxDataFileCachedParameterIsNotANumber() throws Exception {
    String fileName = "testFiles/digidoc_test_conf_invalid_max_data_file_cached.yaml";
    expectedException.expect(ConfigurationException.class);
    expectedException.expectMessage("Configuration parameter DIGIDOC_MAX_DATAFILE_CACHED" +
        " should have an integer value but the actual value is: 8192MB.");

    configuration.loadConfiguration(fileName);
  }

  @Test
  public void digidocSignOcspRequestIsNotABoolean() throws Exception {
    String fileName = "testFiles/digidoc_test_conf_invalid_sign_ocsp_request.yaml";
    expectedException.expect(ConfigurationException.class);
    expectedException.expectMessage("Configuration parameter SIGN_OCSP_REQUESTS should be set to true or false" +
        " but the actual value is: NonBooleanValue.");

    configuration.loadConfiguration(fileName);
  }

  @Test
  public void digidocKeyUsageCheckIsNotABoolean() throws Exception {
    String fileName = "testFiles/digidoc_test_conf_invalid_key_usage.yaml";
    expectedException.expect(ConfigurationException.class);
    expectedException.expectMessage("Configuration parameter KEY_USAGE_CHECK should be set to true or false" +
        " but the actual value is: NonBooleanValue.");

    configuration.loadConfiguration(fileName);
  }

  @Test
  public void digidocUseLocalTslIsNotABoolean() throws Exception {
    String fileName = "testFiles/digidoc_test_conf_invalid_use_local_tsl.yaml";
    expectedException.expect(ConfigurationException.class);
    expectedException.expectMessage("Configuration parameter DIGIDOC_USE_LOCAL_TSL should be set to true or false" +
        " but the actual value is: NonBooleanValue.");

    configuration.loadConfiguration(fileName);
  }

  @Test
  public void digidocDataFileHashcodeModeIsNotABoolean() throws Exception {
    String fileName = "testFiles/digidoc_test_conf_invalid_datafile_hashcode_mode.yaml";
    expectedException.expect(ConfigurationException.class);
    expectedException.expectMessage("Configuration parameter DATAFILE_HASHCODE_MODE should be set to true or false" +
        " but the actual value is: NonBooleanValue.");

    configuration.loadConfiguration(fileName);
  }

  @Test
  public void missingOCSPSEntryThrowsException() throws Exception {
    String fileName = "testFiles/digidoc_test_conf_ocsps_no_entry.yaml";
    expectedException.expect(ConfigurationException.class);
    expectedException.expectMessage("No OCSPS entry found or OCSPS entry is empty. Configuration file: " + fileName);

    configuration.loadConfiguration(fileName);
  }

  @Test
  public void emptyOCSPSEntryThrowsException() throws Exception {
    String fileName = "testFiles/digidoc_test_conf_ocsps_empty.yaml";
    expectedException.expect(ConfigurationException.class);
    expectedException.expectMessage("No OCSPS entry found or OCSPS entry is empty. Configuration file: " + fileName);

    configuration.loadConfiguration(fileName);
  }

  @Test
  public void OCSPWithoutCaCnValueThrowsException() throws Exception {
    String fileName = "testFiles/digidoc_test_conf_ocsps_no_ca_cn.yaml";
    expectedException.expect(ConfigurationException.class);
    expectedException.expectMessage("Configuration file " + fileName + " contains error(s):\n" +
        "OCSPS list entry 2 does not have an entry for CA_CN or the entry is empty\n");

    configuration.loadConfiguration(fileName);
  }

  @Test
  public void OCSPWithEmptySubEntriesThrowsException() throws Exception {
    String fileName = "testFiles/digidoc_test_conf_ocsps_empty_sub_entries.yaml";
    String expectedErrorMessage = "Configuration file " + fileName + " contains error(s):\n" +
        "OCSPS list entry 3 does not have an entry for CA_CN or the entry is empty\n" +
        "OCSPS list entry 4 does not have an entry for CA_CERT or the entry is empty\n" +
        "OCSPS list entry 5 does not have an entry for CN or the entry is empty\n" +
        "OCSPS list entry 8 does not have an entry for URL or the entry is empty\n";
    expectedException.expect(ConfigurationException.class);
    expectedException.expectMessage(expectedErrorMessage);

    configuration.loadConfiguration(fileName);
  }

  @Test
  public void OCSPWithMissingSubEntriesThrowsException() throws Exception {
    String fileName = "testFiles/digidoc_test_conf_ocsps_missing_sub_entries.yaml";
    String expectedErrorMessage = "Configuration file " + fileName + " contains error(s):\n" +
        "OCSPS list entry 3 does not have an entry for CN or the entry is empty\n" +
        "OCSPS list entry 4 does not have an entry for URL or the entry is empty\n" +
        "OCSPS list entry 5 does not have an entry for CA_CERT or the entry is empty\n" +
        "OCSPS list entry 8 does not have an entry for CA_CN or the entry is empty\n";
    expectedException.expect(ConfigurationException.class);
    expectedException.expectMessage(expectedErrorMessage);

    configuration.loadConfiguration(fileName);
  }

  @Test
  public void OCSPWithMissingOcspsCertsEntryThrowsException() throws Exception {
    String fileName = "testFiles/digidoc_test_conf_ocsps_missing_certs_entry.yaml";
    String expectedErrorMessage = "Configuration file " + fileName + " contains error(s):\n" +
        "OCSPS list entry 3 does not have an entry for CERTS or the entry is empty\n";
    expectedException.expect(ConfigurationException.class);
    expectedException.expectMessage(expectedErrorMessage);

    configuration.loadConfiguration(fileName);
  }

  @Test
  public void OCSPWithEmptyOcspsCertsEntryThrowsException() throws Exception {
    String fileName = "testFiles/digidoc_test_conf_ocsps_empty_certs_entry.yaml";
    String expectedErrorMessage = "Configuration file " + fileName + " contains error(s):\n" +
        "OCSPS list entry 2 does not have an entry for CERTS or the entry is empty\n";
    expectedException.expect(ConfigurationException.class);
    expectedException.expectMessage(expectedErrorMessage);

    configuration.loadConfiguration(fileName);
  }

  @Test
  public void configurationFileIsNotYamlFormatThrowsException() throws Exception {
    String fileName = "testFiles/test.txt";
    String expectedErrorMessage = "Configuration file " + fileName + " is not a correctly formatted yaml file";
    expectedException.expect(ConfigurationException.class);
    expectedException.expectMessage(expectedErrorMessage);

    configuration.loadConfiguration(fileName);
  }

  @Test
  public void isOCSPSigningConfigurationAvailableWhenItIsNotAvailable() throws Exception {
    assertFalse(configuration.isOCSPSigningConfigurationAvailable());
  }

  @Test
  public void isOCSPSigningConfigurationAvailableWhenItIsAvailable() throws Exception {
    configuration.setOCSPAccessCertificateFileName("test.p12");
    configuration.setOCSPAccessCertificatePassword("aaa".toCharArray());

    assertTrue(configuration.isOCSPSigningConfigurationAvailable());
  }

  @Test
  public void isOCSPSigningConfigurationAvailableWhenFileIsAvailable() throws Exception {
    configuration.setOCSPAccessCertificateFileName("test.p12");
    assertFalse(configuration.isOCSPSigningConfigurationAvailable());
  }

  @Test
  public void isOCSPSigningConfigurationAvailableWhenPasswordIsAvailable() throws Exception {
    configuration.setOCSPAccessCertificatePassword("aaa".toCharArray());
    assertFalse(configuration.isOCSPSigningConfigurationAvailable());
  }

  @Test
  public void getTspSourceFromConfigurationFile() throws Exception {
    configuration.loadConfiguration("testFiles/digidoc_test_conf.yaml");
    assertEquals("http://tsp.source.test/HttpTspServer", configuration.getTspSource());
  }

  @Test
  public void getValidationPolicyFromConfigurationFile() throws Exception {
    configuration.loadConfiguration("testFiles/digidoc_test_conf.yaml");
    assertEquals("conf/test_validation_policy.xml", configuration.getValidationPolicy());
  }

  @Test
  public void getPKCS11ModulePathFromConfigurationFile() throws Exception {
    configuration.loadConfiguration("testFiles/digidoc_test_conf.yaml");
    assertEquals("/usr/lib/x86_64-linux-gnu/test_pkcs11_module.so", configuration.getPKCS11ModulePath());
  }

  @Test
  public void getOcspSourceFromConfigurationFile() throws Exception {
    configuration.loadConfiguration("testFiles/digidoc_test_conf.yaml");
    assertEquals("http://www.openxades.org/cgi-bin/test_ocsp_source.cgi", configuration.getOcspSource());
  }

  @Test
  public void loadMultipleCAsFromConfigurationFile() throws Exception {
    Hashtable<String, String> jDigiDocConf = configuration.loadConfiguration("testFiles/digidoc_test_conf_two_cas" +
        ".yaml");

    assertEquals("AS Sertifitseerimiskeskus", jDigiDocConf.get("DIGIDOC_CA_1_NAME"));
    assertEquals("jar://certs/ESTEID-SK.crt", jDigiDocConf.get("DIGIDOC_CA_1_CERT2"));
    assertEquals("Second CA", jDigiDocConf.get("DIGIDOC_CA_2_NAME"));
    assertEquals("jar://certs/CA_2_CERT_3.crt", jDigiDocConf.get("DIGIDOC_CA_2_CERT3"));
    assertEquals("jar://certs/CA_2_OCSP_1_SECOND_CERT", jDigiDocConf.get("DIGIDOC_CA_2_OCSP1_CERT_1"));
  }

  @Test
  public void missingCAThrowsException() throws Exception {
    String fileName = "testFiles/digidoc_test_conf_no_ca.yaml";
    String expectedErrorMessage = "Configuration file " + fileName + " contains error(s):\n" +
        "Empty or no DIGIDOC_CAS entry";

    expectedException.expect(ConfigurationException.class);
    expectedException.expectMessage(expectedErrorMessage);

    configuration.loadConfiguration(fileName);
  }

  @Test
  public void emptyCAThrowsException() throws Exception {
    String fileName = "testFiles/digidoc_test_conf_empty_ca.yaml";
    String expectedErrorMessage = "Configuration file " + fileName + " contains error(s):\n" +
        "Empty or no DIGIDOC_CA for entry 1";

    expectedException.expect(ConfigurationException.class);
    expectedException.expectMessage(expectedErrorMessage);

    configuration.loadConfiguration(fileName);
  }

  @Test
  public void isTestMode() throws Exception {
    Configuration configuration = new Configuration(TEST);
    assertTrue(configuration.isTest());
  }

  @Test
  public void isNotTestMode() throws Exception {
    Configuration configuration = new Configuration(PROD);
    assertFalse(configuration.isTest());
  }

  //  // getCACerts is currently only used for testing purposes and not yet updated for multiple CA's
//  @Test
//  public void readConfigurationFromPropertiesFile() throws Exception {
//    configuration.loadConfiguration("digidoc4j.yaml");
//    List<X509Certificate> certificates = configuration.getCACerts();
//    assertEquals(17, certificates.size());
//  }
//
//  @Test
//  public void readConfigurationFromPropertiesFileThrowsException() throws Exception {
//    Configuration configuration = spy(new Configuration(Mode.TEST));
//    doThrow(new CertificateException()).when(configuration).getX509CertificateFromFile(anyString());
//    doCallRealMethod().when(configuration).loadConfiguration(anyString());
//
//    configuration.loadConfiguration("digidoc4j.yaml");
//
//    assertEquals(0, configuration.getCACerts().size());
//  }
//

}