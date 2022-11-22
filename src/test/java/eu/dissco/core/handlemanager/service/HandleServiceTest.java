package eu.dissco.core.handlemanager.service;

import static eu.dissco.core.handlemanager.testUtils.TestUtils.CREATED;
import static eu.dissco.core.handlemanager.testUtils.TestUtils.HANDLE;
import static eu.dissco.core.handlemanager.testUtils.TestUtils.HANDLE_ALT;
import static eu.dissco.core.handlemanager.testUtils.TestUtils.PTR_HANDLE_RECORD;
import static eu.dissco.core.handlemanager.testUtils.TestUtils.generateTestDigitalSpecimenBotanyRecord;
import static eu.dissco.core.handlemanager.testUtils.TestUtils.generateTestDigitalSpecimenBotanyRequest;
import static eu.dissco.core.handlemanager.testUtils.TestUtils.generateTestDigitalSpecimenBotanyResponse;
import static eu.dissco.core.handlemanager.testUtils.TestUtils.generateTestDigitalSpecimenRecord;
import static eu.dissco.core.handlemanager.testUtils.TestUtils.generateTestDigitalSpecimenRequest;
import static eu.dissco.core.handlemanager.testUtils.TestUtils.generateTestDigitalSpecimenResponse;
import static eu.dissco.core.handlemanager.testUtils.TestUtils.generateTestDoiRecord;
import static eu.dissco.core.handlemanager.testUtils.TestUtils.generateTestDoiRequest;
import static eu.dissco.core.handlemanager.testUtils.TestUtils.generateTestDoiResponse;
import static eu.dissco.core.handlemanager.testUtils.TestUtils.generateTestHandleRecord;
import static eu.dissco.core.handlemanager.testUtils.TestUtils.generateTestHandleRequest;
import static eu.dissco.core.handlemanager.testUtils.TestUtils.generateTestHandleResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.core.handlemanager.domain.requests.DigitalSpecimenBotanyRequest;
import eu.dissco.core.handlemanager.domain.requests.DigitalSpecimenRequest;
import eu.dissco.core.handlemanager.domain.requests.DoiRecordRequest;
import eu.dissco.core.handlemanager.domain.requests.HandleRecordRequest;
import eu.dissco.core.handlemanager.domain.responses.DigitalSpecimenBotanyResponse;
import eu.dissco.core.handlemanager.domain.responses.DigitalSpecimenResponse;
import eu.dissco.core.handlemanager.domain.responses.DoiRecordResponse;
import eu.dissco.core.handlemanager.domain.responses.HandleRecordResponse;
import eu.dissco.core.handlemanager.exceptions.PidCreationException;
import eu.dissco.core.handlemanager.exceptions.PidResolutionException;
import eu.dissco.core.handlemanager.repository.HandleRepository;
import eu.dissco.core.handlemanager.repositoryobjects.Handles;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Slf4j
class HandleServiceTest {

  @Mock
  private HandleRepository handleRep;

  @Mock
  private PidTypeService pidTypeService;

  @Mock
  private HandleGeneratorService hgService;

  private DocumentBuilderFactory documentBuilderFactory;
  private TransformerFactory transformerFactory;

  private HandleService service;

  private List<byte[]> handlesList;

  private MockedStatic<Instant> mockedStatic;


  @BeforeEach
  void setup() throws PidResolutionException, JsonProcessingException {
    log.info("tbf is null: " + String.valueOf(documentBuilderFactory == null));
    documentBuilderFactory = DocumentBuilderFactory.newInstance();
    transformerFactory = TransformerFactory.newInstance();

    service = new HandleService(handleRep, pidTypeService, hgService, documentBuilderFactory,
        transformerFactory);
    given(pidTypeService.resolveTypePid(any(String.class))).willReturn(PTR_HANDLE_RECORD);
    initTime();
    initHandleList();
  }

  @AfterEach
  void destroy() {
    mockedStatic.close();
  }

  @Test
  void testCreateHandleRecord()
      throws PidCreationException, PidResolutionException, JsonProcessingException, ParserConfigurationException, TransformerException {
    // Given
    byte[] handle = handlesList.get(0);
    HandleRecordRequest request = generateTestHandleRequest();
    HandleRecordResponse responseExpected = generateTestHandleResponse(handle);
    List<Handles> recordTest = generateTestHandleRecord(handle);

    given(handleRep.saveAll(recordTest)).willReturn(recordTest);
    given(hgService.genHandleList(1)).willReturn(handlesList);

    // When
    HandleRecordResponse responseReceived = service.createHandleRecord(request);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testCreateDoiRecord()
      throws PidCreationException, PidResolutionException, JsonProcessingException, ParserConfigurationException, TransformerException {
    // Given
    byte[] handle = handlesList.get(0);
    DoiRecordRequest request = generateTestDoiRequest();
    DoiRecordResponse responseExpected = generateTestDoiResponse(handle);
    List<Handles> recordTest = generateTestDoiRecord(handle);
    given(handleRep.saveAll(recordTest)).willReturn(recordTest);
    given(hgService.genHandleList(1)).willReturn(handlesList);

    // When
    DoiRecordResponse responseReceived = service.createDoiRecord(request);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testCreateDigitalSpecimen()
      throws PidCreationException, PidResolutionException, JsonProcessingException, ParserConfigurationException, TransformerException {
    // Given
    byte[] handle = handlesList.get(0);
    DigitalSpecimenRequest request = generateTestDigitalSpecimenRequest();
    DigitalSpecimenResponse responseExpected = generateTestDigitalSpecimenResponse(handle);
    List<Handles> recordTest = generateTestDigitalSpecimenRecord(handle);

    given(handleRep.saveAll(recordTest)).willReturn(recordTest);
    given(hgService.genHandleList(1)).willReturn(handlesList);

    // When
    DigitalSpecimenResponse responseReceived = service.createDigitalSpecimenRecord(request);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testCreateDigitalSpecimenBotany()
      throws PidCreationException, PidResolutionException, JsonProcessingException, ParserConfigurationException, TransformerException {
    // Given
    byte[] handle = handlesList.get(0);
    DigitalSpecimenBotanyRequest request = generateTestDigitalSpecimenBotanyRequest();
    DigitalSpecimenBotanyResponse responseExpected = generateTestDigitalSpecimenBotanyResponse(
        handle);
    List<Handles> recordTest = generateTestDigitalSpecimenBotanyRecord(handle);

    given(handleRep.saveAll(recordTest)).willReturn(recordTest);
    given(hgService.genHandleList(1)).willReturn(handlesList);

    // When
    DigitalSpecimenBotanyResponse responseReceived = service.createDigitalSpecimenBotanyRecord(
        request);

    // Then
    assertThat(responseReceived).isEqualTo(responseExpected);
  }

  @Test
  void testCreateBatchHandleRecord()
      throws PidCreationException, PidResolutionException, JsonProcessingException, ParserConfigurationException, TransformerException {
    // Given
    List<HandleRecordRequest> request = generateBatchHandleRequest();
    List<HandleRecordResponse> responseExpected = generateBatchHandleResponse();
    List<Handles> recordTest = generateBatchHandleList();

    given(handleRep.saveAll(recordTest)).willReturn(recordTest);
    given(hgService.genHandleList(2)).willReturn(handlesList);

    // When
    List<HandleRecordResponse> responseReceived = service.createHandleRecordBatch(request);

    // Then
    assertThat(responseExpected).isEqualTo(responseReceived);
  }

  @Test
  void testCreateBatchDoiRecord()
      throws PidCreationException, PidResolutionException, JsonProcessingException, ParserConfigurationException, TransformerException {
    // Given
    List<DoiRecordRequest> request = generateBatchDoiRequest();
    List<DoiRecordResponse> responseExpected = generateBatchDoiResponse();
    List<Handles> recordTest = generateBatchDoiList();

    given(handleRep.saveAll(recordTest)).willReturn(recordTest);
    given(hgService.genHandleList(2)).willReturn(handlesList);

    // When
    List<DoiRecordResponse> responseReceived = service.createDoiRecordBatch(request);

    // Then
    assertThat(responseExpected).isEqualTo(responseReceived);
  }

  @Test
  void testCreateBatchDigitalSpecimen()
      throws PidCreationException, PidResolutionException, JsonProcessingException, ParserConfigurationException, TransformerException {
    // Given
    List<DigitalSpecimenRequest> request = generateBatchDigitalSpecimenRequest();
    List<DigitalSpecimenResponse> responseExpected = generateBatchDigitalSpecimenResponse();
    List<Handles> recordTest = generateBatchDigitalSpecimenList();

    given(handleRep.saveAll(recordTest)).willReturn(recordTest);
    given(hgService.genHandleList(2)).willReturn(handlesList);

    // When
    List<DigitalSpecimenResponse> responseReceived = service.createDigitalSpecimenBatch(request);

    // Then
    assertThat(responseExpected).isEqualTo(responseReceived);
  }

  @Test
  void testCreateBatchDigitalSpecimenBotany()
      throws PidCreationException, PidResolutionException, JsonProcessingException, ParserConfigurationException, TransformerException {
    // Given
    List<DigitalSpecimenBotanyRequest> request = generateBatchDigitalSpecimenBotanyRequest();
    List<DigitalSpecimenBotanyResponse> responseExpected = generateBatchDigitalSpecimenBotanyResponse();
    List<Handles> recordTest = generateBatchDigitalSpecimenBotanyList();

    given(handleRep.saveAll(recordTest)).willReturn(recordTest);
    given(hgService.genHandleList(2)).willReturn(handlesList);

    // When
    List<DigitalSpecimenBotanyResponse> responseReceived = service.createDigitalSpecimenBotanyBatch(
        request);

    // Then
    assertThat(responseExpected).isEqualTo(responseReceived);
  }

  private List<HandleRecordRequest> generateBatchHandleRequest() {
    List<HandleRecordRequest> requestList = new ArrayList<>();
    for (int i = 0; i < handlesList.size(); i++) {
      requestList.add(generateTestHandleRequest());
    }
    return requestList;
  }

  private List<HandleRecordResponse> generateBatchHandleResponse() {
    List<HandleRecordResponse> responseList = new ArrayList<>();
    for (byte[] h : handlesList) {
      responseList.add(generateTestHandleResponse(h));
    }
    return responseList;
  }

  private List<Handles> generateBatchHandleList() {
    List<Handles> handleList = new ArrayList<>();
    for (byte[] h : handlesList) {
      handleList.addAll(generateTestHandleRecord(h));
    }
    return handleList;
  }


  private List<DoiRecordRequest> generateBatchDoiRequest() {
    List<DoiRecordRequest> requestList = new ArrayList<>();
    for (int i = 0; i < handlesList.size(); i++) {
      requestList.add(generateTestDoiRequest());
    }
    return requestList;
  }

  private List<DoiRecordResponse> generateBatchDoiResponse() {
    List<DoiRecordResponse> responseList = new ArrayList<>();
    for (byte[] h : handlesList) {
      responseList.add(generateTestDoiResponse(h));
    }
    return responseList;
  }

  private List<Handles> generateBatchDoiList() {
    List<Handles> handleList = new ArrayList<>();
    for (byte[] h : handlesList) {
      handleList.addAll(generateTestDoiRecord(h));
    }
    return handleList;
  }

  private List<DigitalSpecimenRequest> generateBatchDigitalSpecimenRequest() {
    List<DigitalSpecimenRequest> requestList = new ArrayList<>();
    for (int i = 0; i < handlesList.size(); i++) {
      requestList.add(generateTestDigitalSpecimenRequest());
    }
    return requestList;
  }

  private List<DigitalSpecimenResponse> generateBatchDigitalSpecimenResponse() {
    List<DigitalSpecimenResponse> responseList = new ArrayList<>();
    for (byte[] h : handlesList) {
      responseList.add(generateTestDigitalSpecimenResponse(h));
    }
    return responseList;
  }

  private List<Handles> generateBatchDigitalSpecimenList() {
    List<Handles> handleList = new ArrayList<>();
    for (byte[] h : handlesList) {
      handleList.addAll(generateTestDigitalSpecimenRecord(h));
    }
    return handleList;
  }

  private List<DigitalSpecimenBotanyRequest> generateBatchDigitalSpecimenBotanyRequest() {
    List<DigitalSpecimenBotanyRequest> requestList = new ArrayList<>();
    for (int i = 0; i < handlesList.size(); i++) {
      requestList.add(generateTestDigitalSpecimenBotanyRequest());
    }
    return requestList;
  }

  private List<DigitalSpecimenBotanyResponse> generateBatchDigitalSpecimenBotanyResponse() {
    List<DigitalSpecimenBotanyResponse> responseList = new ArrayList<>();
    for (byte[] h : handlesList) {
      responseList.add(generateTestDigitalSpecimenBotanyResponse(h));
    }
    return responseList;
  }

  private List<Handles> generateBatchDigitalSpecimenBotanyList() {
    List<Handles> handleList = new ArrayList<>();
    for (byte[] h : handlesList) {
      handleList.addAll(generateTestDigitalSpecimenBotanyRecord(h));
    }
    return handleList;
  }


  private void initTime() {
    Clock clock = Clock.fixed(CREATED, ZoneOffset.UTC);
    Instant instant = Instant.now(clock);
    mockedStatic = mockStatic(Instant.class);
    mockedStatic.when(Instant::now).thenReturn(instant);
    mockedStatic.when(() -> Instant.from(any())).thenReturn(instant);
  }

  private void initHandleList() {
    handlesList = new ArrayList<>();
    handlesList.add(HANDLE.getBytes());
    handlesList.add(HANDLE_ALT.getBytes());
  }


}
