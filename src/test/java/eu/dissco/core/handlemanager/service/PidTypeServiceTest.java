package eu.dissco.core.handlemanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.core.handlemanager.exceptions.PidResolutionException;
import eu.dissco.core.handlemanager.repository.HandleRepository;
import eu.dissco.core.handlemanager.repositoryobjects.Handles;
import eu.dissco.core.handlemanager.testUtils.TestUtils;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PidTypeServiceTest {
  // NOTE: Pid Type Record => PTR in naming convention because these
  // PTR refers to the handle record that stores information about a type

  @Mock
  private HandleRepository handleRep;

  @Mock
  private Clock clock;

  @InjectMocks
  private PidTypeService pidTypeService;

  // Time stuff
  private MockedStatic mockedStatic;
  private long timestamp;

  // These are what the values are called in the handle record
  private String pid;
  private String pidType;
  private String primaryNameFromPid;
  private String registrationAgencyDoiName;

  private byte[] recordPid;

  private List<Handles> typeRecord;


  @BeforeEach
  void init() {
    timestamp = initTimestamp();
  }

  @Test
  void testPidTypeRecordResolutionHandle() throws PidResolutionException, JsonProcessingException {
    // Given
    initTestPidTypeRecordHandle();
    String expected = TestUtils.PTR_HANDLE_RECORD;

    given(handleRep.resolveHandle(recordPid)).willReturn(typeRecord);

    // When
    String returned = pidTypeService.resolveTypePid(TestUtils.PID_ISSUER_PID);

    // Then
    assertThat(expected).isEqualTo(returned);
  }

  @Test
  void TestPidTypeRecordResolutionDoi() throws PidResolutionException, JsonProcessingException {
    //Given
    initTestPidTypeRecordDoi();
    String expected = TestUtils.PTR_DOI_RECORD;
    given(handleRep.resolveHandle(recordPid)).willReturn(typeRecord);

    // When
    String returned = pidTypeService.resolveTypePid(TestUtils.PID_ISSUER_PID);

    // Then
    assertThat(expected).isEqualTo(returned);
  }


  private void initTestPidTypeRecordHandle() {
    recordPid = TestUtils.PID_ISSUER_PID.getBytes();

    pid = TestUtils.PTR_PID;
    pidType = TestUtils.PTR_TYPE;
    primaryNameFromPid = TestUtils.PTR_PRIMARY_NAME;

    typeRecord = initTestPidTypeRecord(false);
  }

  private void initTestPidTypeRecordDoi() {
    recordPid = TestUtils.PID_ISSUER_PID.getBytes();

    pid = TestUtils.PTR_PID_DOI;
    pidType = TestUtils.PTR_TYPE_DOI;
    primaryNameFromPid = TestUtils.PTR_PRIMARY_NAME;

    registrationAgencyDoiName = TestUtils.PTR_REGISTRATION_DOI_NAME;

    typeRecord = initTestPidTypeRecord(true);

  }

  private List<Handles> initTestPidTypeRecord(boolean isDoi) {
    List<Handles> record = new ArrayList<>();
    int i = 1;
    record.add(new Handles(recordPid, i++, "pid", pid, timestamp));
    record.add(new Handles(recordPid, i++, "pidType", pidType, timestamp));
    record.add(new Handles(recordPid, i++, "primaryNameFromPid", primaryNameFromPid, timestamp));

    if (isDoi) {
      registrationAgencyDoiName = TestUtils.PTR_REGISTRATION_DOI_NAME;
      record.add(new Handles(recordPid, i++, "registrationAgencyDoiName", registrationAgencyDoiName,
          timestamp));
    }
    return record;
  }

  private long initTimestamp() {
    //CREATED_INSTANT.getEpochSecond();
    return TestUtils.CREATED.getEpochSecond();
  }

}