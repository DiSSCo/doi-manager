package com.example.handlemanager.testUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import com.example.handlemanager.domain.requests.*;
import com.example.handlemanager.domain.responses.*;
import com.example.handlemanager.model.repositoryObjects.Handles;
import com.example.handlemanager.utils.HandleFactory;
import com.example.handlemanager.utils.Resources;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static com.example.handlemanager.utils.Resources.setLocations;

public class TestUtils {
	
	public static Instant CREATED = Instant.parse("2022-11-01T09:59:24.00Z");
	
	public static String HANDLE = "20.5000.1025/QRS-321-ABC";
	public static String HANDLE_ALT = "20.5000.1025/ABC-123-QRS";
	
	// Request Vars
	// Handles
	public static String PID_ISSUER_PID = "20.5000.1025/PID-ISSUER";
	public static String DIGITAL_OBJECT_TYPE_PID = "20.5000.1025/DIGITAL-SPECIMEN";
	public static String DIGITAL_OBJECT_SUBTYPE_PID = "20.5000.1025/BOTANY-SPECIMEN";
	public static String [] LOCATIONS = {"https://sandbox.dissco.tech/", "https://dissco.eu"};
	//DOIs
	public static String REFERENT_DOI_NAME_PID = "20.5000.1025/OTHER-TRIPLET";
	public static String REFERENT = "";
	//Digital Specimens
	public static String DIGITAL_OR_PHYSICAL = "physical";
	public static String SPECIMEN_HOST_PID = "20.5000.1025/OTHER-TRIPLET";
	public static String IN_COLLECTION_FACILITY = "20.5000.1025/OTHER-TRIPLET";
	//Botany Specimens
	public static String OBJECT_TYPE = "Herbarium Sheet";
	public static String PRESERVED_OR_LIVING = "preserved";
	
	// Pid Type Record vars
	public static String PTR_PID = "http://hdl.handle.net/"+PID_ISSUER_PID;
	public static String PTR_TYPE = "handle";
	public static String PTR_PRIMARY_NAME = "DiSSCo";
	public static String PTR_PID_DOI = "http://doi.org/"+PID_ISSUER_PID;
	public static String PTR_TYPE_DOI = "doi";
	
	public static String PTR_REGISTRATION_DOI_NAME = "Registration Agency";
	
	public static String PTR_HANDLE_RECORD = "{ \n"
			+ "\"pid\": \"" + PTR_PID + "\", \n"
			+ "\"pidType\": \""+PTR_TYPE+"\", \n"
			+ "\"primaryNameFromPid\": \""+ PTR_PRIMARY_NAME +"\" \n"
			+ "}";
	
	public static String PTR_DOI_RECORD = "{ \n"
			+ "\"pid\": \"" + PTR_PID_DOI + "\", \n"
			+ "\"pidType\": \""+PTR_TYPE_DOI+"\", \n"
			+ "\"primaryNameFromPid\": \""+ PTR_PRIMARY_NAME +"\", \n"
			+ "\"registrationAgencyDoiName\": \""+ PTR_REGISTRATION_DOI_NAME +"\" \n"
			+ "}";


	public static List<Handles> generateTestHandleRecord(byte[] handle){

		List<Handles> handleRecord = new ArrayList<Handles>();
		long timestamp =initTime();

		// 100: Admin Handle
		handleRecord.add(Resources.genAdminHandle(handle, timestamp));

		int i = 1;
		// 1: Pid
		handleRecord.add(new Handles(handle, i++, "pid", ("https://hdl.handle.net/" + new String(handle)).getBytes(), timestamp));

		// 2: PidIssuer
		handleRecord.add(new Handles(handle, i++, "pidIssuer", PTR_HANDLE_RECORD, timestamp));

		// 3: Digital Object Type
		handleRecord.add(new Handles(handle, i++, "digitalObjectType", PTR_HANDLE_RECORD, timestamp));

		// 4: Digital Object Subtype
		handleRecord.add(new Handles(handle, i++, "digitalObjectSubtype", PTR_HANDLE_RECORD, timestamp));

		// 5: 10320/loc
		byte[] loc = "".getBytes();
		try {
			loc = setLocations(LOCATIONS);
		} catch (TransformerException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		handleRecord.add(new Handles(handle, i++, "10320/loc", loc, timestamp));

		// 6: Issue Date
		handleRecord.add((new Handles(handle, i++, "issueDate", initDate(), timestamp)));

		// 7: Issue number
		handleRecord.add((new Handles(handle, i++, "issueNumber", "1", timestamp))); // TODO: Will every created handle
		// have a 1 for the issue date?

		// 8: PidStatus
		handleRecord.add((new Handles(handle, i++, "pidStatus", "TEST", timestamp))); // TODO: Can I keep this as test?

		// 9, 10: tombstone text, tombstone pids -> Skip
		i++;
		i++;
		// 11: PidKernelMetadataLicense:
		// https://creativecommons.org/publicdomain/zero/1.0/
		handleRecord.add((new Handles(handle, i++, "pidKernelMetadataLicense",
				"https://creativecommons.org/publicdomain/zero/1.0/", timestamp)));
		return handleRecord;
	}

	public static List<Handles> generateTestDoiRecord(byte[] handle) {
		List<Handles> handleRecord = generateTestHandleRecord(handle);
		long timestamp = initTime();

		int i = 12;
		// 12: Referent DOI Name
		handleRecord.add(new Handles(handle, i++, "referentDoiName", PTR_HANDLE_RECORD, timestamp));
		// 13: Referent
		// it
		handleRecord.add(new Handles(handle, i++, "referent", REFERENT, timestamp));
		return handleRecord;
	}

	public static List<Handles> generateTestDigitalSpecimenRecord(byte [] handle) {
		List<Handles> handleRecord = generateTestDoiRecord(handle);
		long timestamp = initTime();

		int i = 14;

		// 14: digitalOrPhysical
		handleRecord.add(new Handles(handle, i++, "digitalOrPhysical", DIGITAL_OR_PHYSICAL, timestamp));

		// 15: specimenHost
		handleRecord.add(new Handles(handle, i++, "specimenHost", PTR_HANDLE_RECORD, timestamp));

		// 16: In collectionFacillity
		handleRecord.add(new Handles(handle, i++, "inCollectionFacillity", PTR_HANDLE_RECORD, timestamp));
		return handleRecord;
	}


	public static List<Handles> generateTestDigitalSpecimenBotanyRecord(byte[] handle){
		List<Handles> handleRecord = generateTestDigitalSpecimenRecord(handle);
		long timestamp = initTime();

		int i = 17;

		// 17: ObjectType
		handleRecord.add(new Handles(handle, i++, "objectType", OBJECT_TYPE, timestamp));

		// 18: preservedOrLiving
		handleRecord.add(new Handles(handle, i++, "preservedOrLiving", PRESERVED_OR_LIVING, timestamp));

		return handleRecord;
	}

	
	public static HandleRecordRequest generateTestHandleRequest() {
		return new HandleRecordRequest(
				PID_ISSUER_PID,
				DIGITAL_OBJECT_TYPE_PID,
				DIGITAL_OBJECT_SUBTYPE_PID,
				LOCATIONS);
	}
	
	public static HandleRecordResponse generateTestHandleResponse(byte[] handle) {

		return new HandleRecordResponse(generateTestHandleRecord(handle));
	}
	
	public static DoiRecordRequest generateTestDoiRequest() {
		return new DoiRecordRequest(
				PID_ISSUER_PID,
				DIGITAL_OBJECT_TYPE_PID,
				DIGITAL_OBJECT_SUBTYPE_PID,
				LOCATIONS,
				REFERENT_DOI_NAME_PID);				
	}
	
	public static DoiRecordResponse generateTestDoiResponse(byte[] handle){
		return new DoiRecordResponse(generateTestDoiRecord(handle));
	}
	
	public static DigitalSpecimenRequest generateTestDigitalSpecimenRequest() {
		return new DigitalSpecimenRequest(
				PID_ISSUER_PID,
				DIGITAL_OBJECT_TYPE_PID,
				DIGITAL_OBJECT_SUBTYPE_PID,
				LOCATIONS,
				REFERENT_DOI_NAME_PID,
				DIGITAL_OR_PHYSICAL,
				SPECIMEN_HOST_PID,
				IN_COLLECTION_FACILITY);
	}
	
	public static DigitalSpecimenResponse generateTestDigitalSpecimenResponse(byte[] handle) {
		return new DigitalSpecimenResponse(generateTestDigitalSpecimenRecord(handle));
	}
	
	
	public static DigitalSpecimenBotanyRequest generateTestDigitalSpecimenBotanyRequest() {
		return new DigitalSpecimenBotanyRequest(PID_ISSUER_PID,
				DIGITAL_OBJECT_TYPE_PID,
				DIGITAL_OBJECT_SUBTYPE_PID,
				LOCATIONS,
				REFERENT_DOI_NAME_PID,
				DIGITAL_OR_PHYSICAL,
				SPECIMEN_HOST_PID,
				IN_COLLECTION_FACILITY,
				OBJECT_TYPE,
				PRESERVED_OR_LIVING);
	}
	
	public static DigitalSpecimenBotanyResponse generateTestDigitalSpecimenBotanyResponse(byte[] handle) {
		return new DigitalSpecimenBotanyResponse(generateTestDigitalSpecimenBotanyRecord(handle));
	}
	
	public static List<byte[]> generateByteHandleList() {
		List<byte[]> handles = new ArrayList<>();
		handles.add(HANDLE.getBytes());
		handles.add(HANDLE_ALT.getBytes());
		
		return handles;
	}

	public static long initTime(){
		return CREATED.getEpochSecond();
	}

	public static String initDate(){
		DateTimeFormatter dt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
				.withZone(ZoneId.of("UTC"))
				.withLocale(Locale.UK);

		return dt.format(CREATED);
		//return "2022-11-01";
	}

}