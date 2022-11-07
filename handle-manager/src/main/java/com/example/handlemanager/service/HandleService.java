package com.example.handlemanager.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import com.example.handlemanager.HandleFactory;
import com.example.handlemanager.domain.requests.*;
import com.example.handlemanager.exceptions.PidResolutionException;
import com.example.handlemanager.model.HandleRecordSpecimen.HandleRecord;
import com.example.handlemanager.model.HandleRecordSpecimen.HandleRecordReserve;
import com.example.handlemanager.model.HandleRecordSpecimen.HandleRecordSpecimen;
import com.example.handlemanager.model.HandleRecordSpecimen.HandleRecordSpecimenMerged;
import com.example.handlemanager.model.HandleRecordSpecimen.HandleRecordSpecimenSplit;
import com.example.handlemanager.model.HandleRecordSpecimen.HandleRecordTombstone;
import com.example.handlemanager.model.recordMetadataObjects.MaterialSampleName;
import com.example.handlemanager.model.recordMetadataObjects.NameIdTypeTriplet;
import com.example.handlemanager.model.recordMetadataObjects.Referent;
import com.example.handlemanager.model.repositoryObjects.Handles;
import com.example.handlemanager.repository.HandleRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.logging.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

//import net.handle.hdllib.HandleException;
//import net.handle.hdllib.HandleValue;
import lombok.RequiredArgsConstructor;

// Postgres value in (value1, value2)..
// Generate all handles before posting...

@Service
@RequiredArgsConstructor
public class HandleService {
	
	@Autowired
	public HandleRepository handleRep;
	private HandleFactory hf = new HandleFactory();
	Logger logger =  Logger.getLogger(HandleService.class.getName());	
	ObjectMapper mapper = new ObjectMapper();
	
	private String admin = "0fff000000153330303a302e4e412f32302e353030302e31303235000000c8";
	

	// Return all handle identifiers
	
	public List<String> getHandles(){
		return getStrList(handleRep.getHandles());
	}
	
	public List<String> getHandles(String pidStatus){
		return getStrList(handleRep.getHandles(pidStatus.getBytes()));
	}
	
	
	// * old Method with verbose request objects *
	public HandleRecordSpecimen createHandleSpecimen(HandleRecordSpecimen specimen) throws JsonMappingException, JsonProcessingException {
		byte[] h = genHandleList(1).get(0); 
		specimen.setDigitalSpecimenRecord(h);
		logger.info("Referent String: " + specimen.getReferentStr());
		logger.info("Material Sample: " + specimen.getMaterialSampleName().toString());
		HandleRecordSpecimen postedRecord = new HandleRecordSpecimen(h, handleRep.saveAll(specimen.sortByIdx().getEntries()));
		logger.info("New Handle Posted: " + postedRecord.getHandleStr());
		
		return postedRecord;
	}
	
	
	// ** New way **
	public List<Handles> createHandleRecord(HandleRecordRequest request, String recordType) {
		byte[] handle = genHandleList(1).get(0);
		long timestamp = Instant.now().getEpochSecond();
		List<Handles> handleRecord;
		switch (recordType) {
			case "hdl":
				handleRecord = prepareHandleRecord(request, handle, timestamp);
				break;
			case "doi":
				handleRecord = prepareDoiRecord((DoiRecordRequest)request, handle, timestamp);
				break;
			case "ds":
				handleRecord = prepareDigitalSpecimenRecord((DigitalSpecimenRequest)request, handle, timestamp);
				break;
			case "dsB":
				handleRecord = prepareDigitalSpecimenBotanyRecord((DigitalSpecimenBotanyRequest)request, handle, timestamp);
				break;
			default:
				handleRecord = new ArrayList<>(); // TODO should throw an error here
		}
		
		return handleRep.saveAll(handleRecord);
	}
	
	
	// Prepare Record Lists
	
	private List<Handles> prepareHandleRecord(HandleRecordRequest request, byte[] handle, long timestamp){
		List<Handles> handleRecord = new ArrayList<Handles>();
		
		// 100: Admin Handle
		handleRecord.add(genAdminHandle(handle, timestamp));
		
		int i = 1;
		// 1: Pid
		byte [] pid = null;
		pid = concatBytes("https://hdl.handle.net/".getBytes(), handle); // TODO this should check if it's a DOI?
		handleRecord.add(new Handles(handle, i++, "pid", pid, timestamp));		
		
		//2: PidIssuer
		String pidIssuer = resolveTypePid(request.getPidIssuerPid());
		handleRecord.add(new Handles(handle, i++, "pidIssuer", pidIssuer, timestamp));
		
		// 3: Digital Object Type
		String digitalObjectType = resolveTypePid(request.getDigitalObjectTypePid());
		handleRecord.add(new Handles(handle, i++, "digitalObjectType", digitalObjectType, timestamp));
		
		// 4: Digital Object Subtype
		String digitalObjectSubtype = resolveTypePid(request.getDigitalObjectSubtypePid());
		handleRecord.add(new Handles(handle, i++, "digitalObjectSubtype", digitalObjectSubtype, timestamp));
		
		// 5: 10320/loc
		byte [] loc = "".getBytes();
		try {
			loc = setLocations(request.getLocations());
		} catch (TransformerException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		handleRecord.add(new Handles(handle, i++, "10320/loc", loc, timestamp));
		
		// 6: Issue Date
		handleRecord.add((new Handles(handle, i++, "issueDate", getDate(), timestamp)));
		
		// 7: Issue number
		handleRecord.add((new Handles(handle, i++, "issueNumber", "1", timestamp))); // TODO: Will every created handle have a 1 for the issue date?
		
		// 8: PidStatus
		handleRecord.add((new Handles(handle, i++, "pidStatus", "TEST",timestamp))); //TODO: Can I keep this as test?	
		
		// 9, 10: tombstone text, tombstone pids -> Skip
		i++;
		i++;
		// 11: PidKernelMetadataLicense: https://creativecommons.org/publicdomain/zero/1.0/
		handleRecord.add((new Handles(handle, i++, "PidKernelMetadataLicense", "https://creativecommons.org/publicdomain/zero/1.0/", timestamp)));
			
		return handleRecord;
	}
	
	private List<Handles> prepareDoiRecord(DoiRecordRequest request, byte[] handle, long timestamp){
		List<Handles> handleRecord = prepareHandleRecord(request.getHandleRecordRequest(), handle, timestamp);
		int i = 12;
		
		// 12: Referent DOI Name
		String referentDoiName = resolveTypePid(request.getReferentDoiNamePid());		
		handleRecord.add(new Handles (handle, i++, "referentDoiName", referentDoiName, timestamp));
		
		// 13: Referent -> NOTE: Referent is blank currently until we have a model for it
		handleRecord.add(new Handles (handle, i++, "referent", request.getReferent(), timestamp));
		
		return handleRecord;
	}
		
	
	private List<Handles> prepareDigitalSpecimenRecord(DigitalSpecimenRequest request, byte [] handle, long timestamp){
		List<Handles> handleRecord = prepareDoiRecord(request.getDoiRecordRequest(), handle, timestamp);
		
		int i = 14;
		
		// 14: digitalOrPhysical
		handleRecord.add(new Handles (handle, i++, "digitalOrPhysical", request.getDigitalOrPhysical(), timestamp));
		
		// 15: specimenHost
		String specimenHost = resolveTypePid(request.getSpecimenHostPid());
		handleRecord.add(new Handles (handle, i++, "specimenHost", specimenHost, timestamp));
		
		// 16: In collectionFacillity
		String inCollectionFacillity = resolveTypePid(request.getInCollectionFacillityPid());
		handleRecord.add(new Handles (handle, i++, "inCollectionFacillity", inCollectionFacillity, timestamp));
		
		return handleRecord;
	}
	
	private List<Handles> prepareDigitalSpecimenBotanyRecord(DigitalSpecimenBotanyRequest request, byte[] handle, long timestamp){
		List<Handles> handleRecord = prepareDigitalSpecimenRecord(request.getDigitalSpecimenRequest(), handle, timestamp);
		
		int i = 17;
		
		// 17: ObjectType
		handleRecord.add(new Handles (handle, i++, "objectType", request.getObjectType(), timestamp));
		
		//18: preservedOrLiving
		handleRecord.add(new Handles (handle, i++, "preservedOrLiving", request.getPreservedOrLiving(), timestamp));
		
		return handleRecord;
	}
	
	
	
	private byte[] concatBytes(byte [] a, byte[] b) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			outputStream.write(a);
			outputStream.write(b);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outputStream.toByteArray();
	}
	
	private String resolveTypePid(String typePid) {
		String pidData = "";
		try{
			pidData = resolveTypePidVal(typePid);
		} catch (PidResolutionException e){
			e.printStackTrace();
		}
		return pidData;
	}
	
	
	// TODO: Make this cacheable
	private String resolveTypePidVal(String typePid) throws PidResolutionException {
		
		List<Handles> typeRecord = handleRep.resolveHandle(typePid.getBytes());
		if (typeRecord.isEmpty()){
			throw new PidResolutionException("Unable to resolve type PID");
		}
		
		String pid = getDataFromType("pid", typeRecord);
		String primaryNameFromPid = getDataFromType("primaryNameFromPID", typeRecord); // TODO this should be lower case 		
		String pidType;
		String registrationAgencyDoiName = "";
		String typeJson = "";
		
		if (pid.contains("doi")) {
			pidType = "doi";
			registrationAgencyDoiName = getDataFromType("registrationAgencyDoiName", typeRecord);
			
			typeJson = "{ \n"
			+ "\"pid\": \"" + pid + "\", \n"
			+ "\"pidType\": \"" + pidType + "\", \n"
			+ "\"primaryNameFromPid\": \"" + primaryNameFromPid + "\", \n"
			+ "\"registrationAgencyDoiName\": \"" + registrationAgencyDoiName + "\" \n"
			+ "}";
			
			
		}
		else if (pid.contains("handle")) {
			pidType = "handle";
			typeJson =  "{ \n"
			+ "\"pid\": \"" + pid + "\", \n"
			+ "\"pidType\": \"" + pidType + "\", \n"
			+ "\"primaryNameFromPid\": \"" + primaryNameFromPid + "\" \n"
			+ "}";
		}
		
		else {
			throw new PidResolutionException("One of the type PIDs provided resolves to an invalid record. Check handle "+typePid+" and try again");
		}
		
		if (pidType == ""|| primaryNameFromPid == ""){ // If one of these were not resolvable
			throw new PidResolutionException("One of the type PIDs provided resolves to an invalid record. Check handle "+typePid+" and try again");
		} 
		
		return typeJson;
	}
	
	private byte[] setLocations(String[] objectLocations)  throws TransformerException, ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
		
		
		var doc = documentBuilder.newDocument();
		var locations = doc.createElement("locations");
		doc.appendChild(locations);
		for (int i = 0; i < objectLocations.length; i++) {
			
			var locs = doc.createElement("location");  //Todo: can i add multiple elements with the same name
			locs.setAttribute("id", String.valueOf(i));
			locs.setAttribute("href", objectLocations[i]);
			locs.setAttribute("weight", "0");
			locations.appendChild(locs);
		}
		
		return documentToString(doc).getBytes(StandardCharsets.UTF_8);
	}
	
	
	private String documentToString(Document document) throws TransformerException {
	    var tf = TransformerFactory.newInstance();
	    var transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	    StringWriter writer = new StringWriter();
	    transformer.transform(new DOMSource(document), new StreamResult(writer));
	    return writer.getBuffer().toString();
	  }
	
	private String getDate() {
		DateTimeFormatter dt = DateTimeFormatter.ofPattern("yyyy-mm-dd");
		LocalDateTime now = LocalDateTime.now();
		return dt.format(now);
	}
	
	

	
	
	// Create Handle
	/*Handl
	public HandleRecord createHandleSpecimen(String url, String digType, String institute) throws JsonProcessingException {
		byte[] h = genHandleList(1).get(0); // TODO fix this? Make an individual function for single handles?
		
		//byte[] h = "20.5000.1025/123-abc".getBytes();
		HandleRecordSpecimen newRecord = new HandleRecordSpecimen(h, url, digType, institute);
		
		// add new handle record to local list of handles
		HandleRecordSpecimen postedRecord = new HandleRecordSpecimen(handleRep.saveAll(newRecord.sortByIdx().getEntries()), h);
		logger.info("New Handle Posted: " + postedRecord.getHandleStr());
		
		return postedRecord;
	} */
	
	/*
	public HandleRecord createHandleSpecimen(DigitalSpecimenInput ds) throws JsonMappingException, JsonProcessingException {
		byte[] h = genHandleList(1).get(0); 
		HandleRecordSpecimen newRecord = new HandleRecordSpecimen(ds, h);
		HandleRecordSpecimen postedRecord = new HandleRecordSpecimen(handleRep.saveAll(newRecord.sortByIdx().getEntries()), h);
		logger.info("New Handle Posted: " + postedRecord.getHandleStr());
		
		return postedRecord;
		
	}*/

	
	
	/**
	public List<Handles> createTestHandle(String handle) throws JsonProcessingException {
		/*String handle = "20.5000.1025/123-abc";
		byte[] h = handle.getBytes(); 
		handle = handle.toUpperCase();
		deleteHandleSafe(handle);
		
		String url = hf.newHandle(); // this is to show things change
		url = url.substring(url.length()-9, url.length());
		
		HandleRecord testRecord = new HandleRecordSpecimen(handle.getBytes(), url, "AstronomySpecimen", "NASA");
		handleRep.saveAll(testRecord.getEntries());
		return resolveHandle(handle);
		
		//return resolveHandle(handle);		
	} */
	
	
	// Reserve handles
	
	public HashSet<String>reserveHandle(int reserves) {
		List<Handles> reservedRecords = new ArrayList<Handles>();  
		List<byte[]> handleList = genHandleList(reserves); // Mint new handles
		
		// Create list of records to be posted
		for (byte[] h : handleList) {
			reservedRecords.addAll(newReservedHandle(h));
		}
		
		// post the list of handle entries
		handleRep.saveAll(reservedRecords);
		HashSet<String> savedStr = new HashSet<String>();
		for (byte[] h : handleList) {
			savedStr.add(byteToString(h));
		}
		
		return savedStr;
	}
	
	private List<Handles> newReservedHandle(byte[] h) {
		HandleRecord reserveRecord = new HandleRecordReserve(h);		
		//Add new handle string to our record	
		return reserveRecord.sortByIdx().getEntries();
	}

	// Resolve handle

	public HandleRecord resolveHandleRecord(String handle) throws JsonMappingException, JsonProcessingException {
		List<Handles> hList = resolveHandle(handle); // Get list of handle records from repository
		if (hList.isEmpty()) {
			return new HandleRecord(handle.getBytes()); // Return empty HandleRecord if handle record does not exist
		}

		return getRecord(handle.getBytes(), hList); // Determines type of handleRecord and returns appropriate data
	}
	
	private List<Handles> resolveHandle(String handle){					
		return handleRep.resolveHandle(handle.getBytes()); 
	}
	
	// Given a list of Handles (of unknown pidStatus), return HandleRecord
	
	private HandleRecord getRecord(byte[] handle, List<Handles> hList) throws JsonMappingException, JsonProcessingException {
		
		// Get PID status and relation status
		
		String pidStatus = getDataFromType("pidStatus", hList);
		String relationStatus = getDataFromType("pidRelation", hList);
		
		// Use pid Status and relation to extrapolate 
		
		if (pidStatus.equals("RESERVED")) {
			return new HandleRecordReserve(handle);
		}
		if (pidStatus.equals("OBSOLETE")) {
			return new HandleRecordTombstone(handle, hList);
		}
		if ((pidStatus.equals("TEST") | pidStatus.equals("ACTIVE")) && relationStatus.equals("")){
			return new HandleRecordSpecimen(handle, hList);
		}
		if ((pidStatus.equals("TEST") | pidStatus.equals("ACTIVE")) && relationStatus.equals("MERGED")){
			return new HandleRecordSpecimenMerged(handle, hList);
		}
		if ((pidStatus.equals("TEST") | pidStatus.equals("ACTIVE")) && relationStatus.equals("SPLIT")){
			return new HandleRecordSpecimenSplit(handle, hList);
		}
		
		logger.warning("Handle pidStatus not been set. Returning best-guess");
		return new HandleRecord(handle, hList); // In case no PID status has been set
	}
	
	// Search list of handle records for the appropriate value
	private String getDataFromType(String type, List<Handles> hList) {
		for (Handles h: hList) {
			if (h.getType().equals(type)) {
				return h.getData();
			}
		}
		
		return ""; // This should maybe return a warning? 
	}
	
	
	//Update Handle
	
	public void updateHandle(String handle, int[] idxs, String[] newData) {
		long timestamp = Instant.now().getEpochSecond();
		
		for (int i = 0; i<idxs.length; i++) {
			handleRep.updateHandleRecordData(newData[i].getBytes(), timestamp, handle.getBytes(), idxs[i]);
		}
	}
	
	
	public void updateHandle(String handle, HandleRecordSpecimen updates) throws JsonMappingException, JsonProcessingException, JSONException {
		
		byte[] handleBytes = handle.getBytes();
		
		// Timestamp to be used in subsequent DB operations
		Long timestamp = Instant.now().getEpochSecond();
		
		// Get original record
		List<Handles> original = resolveHandle(handle);
		
		//Increment issueNumber field
		incrementIssueNumber(handleBytes, original, timestamp);
		
		// Set up json parsing
		JSONObject jsonObj = new JSONObject(mapper.writeValueAsString(updates));
		Iterator<String> keys = jsonObj.keys();
		
		Map<String, String> referentParams = new HashMap<>();
				
		int idx;
		
		String type;
		String data;
		
		while(keys.hasNext()) {
			type = keys.next();
			data = jsonObj.getString(type);
			
			//logger.info(type + " (type) :"+ data+ " (data)");
			
			if (type.equals("materialSampleName") |
					type.equals("identifier") |
					type.equals("principalAgent") |
					type.equals("structuralType")) {
				referentParams.put(type, data);
			}
			
			else if (!type.equals("issueNumber")) { // Protection against updating issueNum? (Wouldn't that be something...)
				idx = getIdxFromType(original, type);
				handleRep.updateHandleRecordData(data.getBytes(),timestamp, handleBytes, idx);
			}
			
		}
		
		if (!referentParams.isEmpty()) {
			updateReferent(handleBytes, referentParams, original, timestamp);
		}
	}
	
	
	private void incrementIssueNumber(byte[] handleBytes, List<Handles> original, long timestamp) {
		// Increase issueNumber by 1
		
		String issueNum = String.valueOf(Integer.parseInt(getDataFromType("issueNumber", original)) + 1);
		
		int idx = getIdxFromType(original, "issueNumber");
		
		handleRep.updateHandleRecordData(issueNum.getBytes(),timestamp, handleBytes, idx);
	}
	
	private void updateReferent(byte[] handle, Map<String, String> referentParams, List<Handles> original, long timestamp) throws JsonMappingException, JsonProcessingException {
		String originalReferentStr = getDataFromType("referent", original);
		Referent originalReferent = mapper.readValue(originalReferentStr, Referent.class);
		int idx = getIdxFromType(original, "referent");
		
		MaterialSampleName msn;
		List<NameIdTypeTriplet> ids;
		NameIdTypeTriplet pa;
		String struct;
		
		if (referentParams.containsKey("materialSampleName")) {
		msn = mapper.readValue(referentParams.get("materialSampleName"), MaterialSampleName.class);
		}
		else {
			msn = originalReferent.getMaterialSampleName();
		}
		if (referentParams.containsKey("identifier")) {
			ids = mapper.readValue(referentParams.get("identifier"), new TypeReference<List<NameIdTypeTriplet>>(){});
			}
		else {
				ids = originalReferent.getIdentifier();
		}
		if (referentParams.containsKey("principalAgent")) {
			pa = mapper.readValue(referentParams.get("principalAgent"), NameIdTypeTriplet.class);
		}
		else {
			pa = originalReferent.getPrincipalAgent();
		}
		if (referentParams.containsKey("structuralType")) {
			struct = referentParams.get("structuralType");
		}
		else {
			struct = originalReferent.getStructuralType();
		}
		
		Referent newReferent = new Referent(msn, struct, pa, ids);
		
		handleRep.updateHandleRecordData(newReferent.toString().getBytes(),timestamp, handle, idx);		
	}
	
	
	private int getIdxFromType(List<Handles> handleRecords, String type) {
		for (Handles h: handleRecords) {
			if (type.equalsIgnoreCase(h.getType())) {
				return h.getIdx();
			}
		}
		return -1;
	}
	
	
	// Delete Handle	
	public void deleteHandleSafe(String handle) {
		handleRep.deleteAll(resolveHandle(handle));
	}
	
	public HandleRecordTombstone createTombstone(byte[] handleB, String tombstone) {
		//handleRep.flush();
		HandleRecordTombstone tombstoneRecord = new HandleRecordTombstone(handleB, tombstone);
		handleRep.saveAll(tombstoneRecord.sortByIdx().getEntries());		
		return tombstoneRecord;
	}
	
	public HandleRecordTombstone createTombstoneMerged(byte[] handleB, String tombstone, String siblingPids, String childPid) {
		HandleRecordTombstone tombstoneRecord = new HandleRecordTombstone(handleB, tombstone);
		tombstoneRecord.setRelationStatusMerged(siblingPids, childPid);
		List<Handles> ts = handleRep.saveAll(tombstoneRecord.sortByIdx().getEntries());
		logger.info("TS is empty: " +  String.valueOf(ts.isEmpty()));
		
		for (Handles h : ts) {
			logger.info(h.getType() + ": " + h.getData());
		}
		
		return tombstoneRecord;
	}
	
	private List<String> getStrList(List<byte[]> byteList){
		// It's crazy that ArrayList doesn't have a native type converter (especially from byte[] to str?
		// If there's a better way to do this that would be cool
		
		int l = byteList.size();
		List<String> strList = new ArrayList<>(l);
		String s;
		for(byte[] b : byteList){
			s = byteToString(b);
			strList.add(s);
		}
		return strList;
	}
	
	private String byteToString(byte[] b) {
		String str = new String(b, Charset.forName("UTF-8"));
		return str;
	}
	
	// Merge Handle
	public HandleRecordSpecimenMerged mergeHandle(HandleRecordSpecimenMerged mergeRecord) throws JsonProcessingException {
		byte[] handle = genHandleList(1).get(0);
		mergeRecord.setDigitalSpecimenRecordMerged(handle);
		logger.info(byteToString(handle));
		logger.info(mergeRecord.getHandleStr());
		String tombstoneStr = "This handle was merged";		
		
		List<HandleRecordSpecimen> legacyRecord = new ArrayList<HandleRecordSpecimen>();
		
		for (String h : mergeRecord.getLegacyHandles()) {
			try {
				legacyRecord.add((HandleRecordSpecimen) resolveHandleRecord(h));
			}
			catch(java.lang.ClassCastException e) {
				logger.severe("One of the digital objects to be merged is not viable");
				return null;
			}
			deleteHandleSafe(h);
			createTombstoneMerged(h.getBytes(), tombstoneStr, mergeRecord.getLegacyHandleString(), byteToString(handle));
		}
		
		return new HandleRecordSpecimenMerged(handle, handleRep.saveAll(mergeRecord.sortByIdx().getEntries()));		
	}
	
	/*
	public HandleRecordSpecimenMerged mergeHandle(HandleRecordSpecimenMerged mergeRecord) throws JsonProcessingException {
		
		
		List<HandleRecordSpecimen> legacyRecord = new ArrayList<HandleRecordSpecimen>();
		String mergedTombstoneText = "This record was merged with other records.";
		
		byte[] handle = genHandleList(1).get(0);

		
		// Resolve handles to be merged, create tombstone records for them. 
		for (String h : handles) {
			try {
				legacyRecord.add((HandleRecordSpecimen) resolveHandleRecord(h));
			}
			catch(java.lang.ClassCastException e) {
				logger.severe("One of the digital objects to be merged is not viable");
				return null;
			}

			deleteHandleSafe(h);
			createTombstoneMerged(h.getBytes(), mergedTombstoneText, byteToString(handle)); // this might be a problem...
		}
		
		HandleRecordSpecimenMerged mergedRecord = new HandleRecordSpecimenMerged(handle, url, digitalObjectType, institute, legacyRecord);
		handleRep.saveAll(mergedRecord.sortByIdx().getEntries());
		return mergedRecord;
		
	}*/
	
	// Split Handle
	
	public List<HandleRecordSpecimenSplit> splitHandleRecord(List<HandleRecordSpecimenSplit> newRecords, String parent) throws JsonProcessingException{
		
		List<byte[]> siblingHandleBytes = genHandleList(newRecords.size());
		String siblingHandleStrings= genSiblingString(getStrList(siblingHandleBytes));
		
		List<HandleRecordSpecimenSplit> postedRecords = new ArrayList<>();
		byte[] h;
		
		for (HandleRecordSpecimenSplit record : newRecords) {
			h = siblingHandleBytes.remove(0);
			record.setDigitalSpecimenRecordSplit(h, parent, siblingHandleStrings);
			postedRecords.add(new HandleRecordSpecimenSplit(h, handleRep.saveAll(record.sortByIdx().getEntries())));
		}
		String tombstone = "this handle record was split";
		createTombstoneSplit(parent.getBytes(), tombstone, siblingHandleStrings);
		return postedRecords;
	}
	
	public HandleRecordTombstone createTombstoneSplit(byte[] handleB, String tombstone, String childPid) {
		HandleRecordTombstone tombstoneRecord = new HandleRecordTombstone(handleB, tombstone);
		tombstoneRecord.setRelationStatusSplit(childPid);
		deleteHandleSafe(byteToString(handleB));
		
		handleRep.saveAll(tombstoneRecord.sortByIdx().getEntries());
		return tombstoneRecord;
	}
	
	private String genSiblingString (List<String> siblingPidsList){
		String siblingPids = "{\n";
		for (String sib : siblingPidsList) {
			siblingPids += " \"" + sib + "\",\n";
		}
		siblingPids = siblingPids.substring(0, siblingPids.length()-2) + "\n}";
		
		return siblingPids;
	}
	
	/*
	public List<HandleRecordSpecimenSplit> splitHandle(String handle, 
			String urlA, String urlB,
			String digTypeA, String digTypeB) throws JsonProcessingException{
		HandleRecordSpecimen parent = (HandleRecordSpecimen) resolveHandleRecord(handle);
		logger.info("Service started");
		String parentHandle = parent.getHandleStr();
		List<byte[]> handleList = genHandleList(2);
		byte[] childHandleA = handleList.get(0);
		byte[] childHandleB = handleList.get(1);
		
		List<String> siblingHandles = getStrList(handleList);
		
		
		digTypeA = setDigType(parent, digTypeA);
		digTypeB = setDigType(parent, digTypeB);
		
		HandleRecordSpecimenSplit childA = new HandleRecordSpecimenSplit(
				childHandleA,
				urlA,
				digTypeA,
				parent.getInstitute(),
				siblingHandles);
		
		HandleRecordSpecimenSplit childB = new HandleRecordSpecimenSplit(
				childHandleB,
				urlB,
				digTypeB,
				parent.getInstitute(),
				siblingHandles);
		
		List<HandleRecordSpecimenSplit> splitHandles = new ArrayList<HandleRecordSpecimenSplit>();
		splitHandles.add(childA);
		splitHandles.add(childB);
		
		saveChildren(splitHandles);
		
		
		// Kill the parent (...That's kind of dark)
		deleteHandleSafe(parentHandle);
		HandleRecordTombstone parentTombstone = createTombstone(parentHandle.getBytes(), "The handle was split.");
		parentTombstone.setRelationStatusSplit(siblingHandles);
		
		return splitHandles;
	} */
	
	private void saveChildren(List<HandleRecordSpecimenSplit> children) {
		List<Handles> recordsToSave = new ArrayList<>();
		for (HandleRecordSpecimenSplit c : children) {
			recordsToSave.addAll(c.getEntries());
		}
		
		handleRep.saveAll(recordsToSave);
	}
	
	/*
	private String setDigType(HandleRecordSpecimen parent, String digType) {
		if (digType.equals("")) return parent.getDigitalObjectSubtype();
		if (!(parent.getDigTypeList().contains(digType))) {
			logger.warning("Invalid digital object type provided. Using parent digital object type");
			return parent.getDigitalObjectSubtype();
		}
		
		return digType;
	} */
	
	
	
	
	// Minting Handles
	
	// Mint a list of handles
	private List<byte[]> genHandleList(int h){
		return unwrapBytes(genHandleHash(h));
	}
	
	
	private HashSet<ByteBuffer> genHandleHash(int h){	
		/* Generates a HashSet of minted handles of size h
		 * Calls the handlefactory object for random strings (8 alphanum characters with a dash in the middle)
		 * Checks list of random strings against database, replaces duplicates with new strings
		 * Finally, checks for collisions within the list 
		 * */
		
		// Generate h number of bytes and wrap it into a HashSet<ByteBuffer>
		List<byte[]> handleList = hf.newHandle(h); 
		HashSet<ByteBuffer> handleHash = wrapBytes(handleList); 		
		
		// Check for duplicates from repository and wrap the duplicates
		HashSet<ByteBuffer> duplicates = wrapBytes(handleRep.checkDuplicateHandles(handleList)); 		
		
		// If a duplicate was found, recursively call this function
		// Generate new handles for every duplicate found and add it to our hash list
		if (!duplicates.isEmpty()) { 
			handleHash.removeAll(duplicates);						
			handleHash.addAll(genHandleHash(duplicates.size())); 
		}
		
		/* It's possible we have a collision within our list now
		 * i.e. on two different recursive cal)ls to this function, we generate the same
		 * If this occurs, we will not have our expected number of handles 
		 * */
		while (h>handleHash.size()) {
			handleHash.addAll(genHandleHash(h-handleHash.size()));
		}
	
		return handleHash;
	}	
	
	
	/* List<byte[]> <----> HashSet<ByteBuffer>
	 * HashSets are useful for preventing collisions within the list
	 * List<byte[]> is used to interface with repository layer
	*/
	
	//Converts List<byte[]> --> HashSet<ByteBuffer>
	private HashSet<ByteBuffer> wrapBytes(List<byte[]> byteList){
		HashSet<ByteBuffer> byteHash = new HashSet<ByteBuffer>();
		for(Iterator<byte[]> itr = byteList.iterator(); itr.hasNext();) {
			byteHash.add(ByteBuffer.wrap(itr.next()));
		}	
		return byteHash;
	}
	
	//HashSet<ByteBuffer> --> List<byte[]>
	 private List<byte[]> unwrapBytes(HashSet<ByteBuffer> handleHash){
		 List<byte[]> handleList = new ArrayList<byte[]>();
		 Iterator<ByteBuffer> itr = handleHash.iterator();
		 while(itr.hasNext()) {
			 handleList.add(itr.next().array());	 
		 }
		 return handleList;
		}
	 
	 
	 // Admin Handle Generation
	 
	 private Handles genAdminHandle(byte [] handle, long timestamp) {
			return new Handles(handle, 100, "HS_ADMIN".getBytes(), decodeAdmin(), timestamp);
		}
	 
	 private byte[] decodeAdmin() {
			byte[] adminByte = new byte[admin.length()/2];
			for (int i = 0; i < admin.length(); i += 2) {
				adminByte[i / 2] = hexToByte(admin.substring(i, i + 2));
		    }
			return adminByte;		
		}
		
		private byte hexToByte(String hexString) {
		    int firstDigit = toDigit(hexString.charAt(0));
		    int secondDigit = toDigit(hexString.charAt(1));
		    return (byte) ((firstDigit << 4) + secondDigit);
		}
		

		private int toDigit(char hexChar) {
			int digit = Character.digit(hexChar, 16);
			return digit;
		}
	 
}
