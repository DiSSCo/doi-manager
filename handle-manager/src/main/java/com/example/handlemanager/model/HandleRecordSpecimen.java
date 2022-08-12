package com.example.handlemanager.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.example.handlemanager.HandleFactory;
import com.example.handlemanager.model.recordMetadataObjects.DigitalObjectSubtype;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
public class HandleRecordSpecimen extends HandleRecord {
	
	// DiSSCo PID Kernel
	protected String url;
	protected String pidIssuer;
	protected String digitalObjectSubtype;
	protected String digitalObjectSubtypeString;
	protected String digitalObjectTypeString = setDigitalObjectTypeString();
	protected String issueDate;
	protected String institute;
	protected String issueNumber = "1";
	protected  final String pidKernelMetadataLicense = "Creative Commons Zero";
	protected String referent;
	protected final String digitalOrPhysical = "physical";
	
	
	@JsonIgnore
	ObjectMapper objectMapper = new ObjectMapper();

	// Other
	@JsonIgnore
	private Logger logger = Logger.getLogger(HandleFactory.class.getName());
	

	@JsonIgnore
	public final List<String> digSubtypes = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;

		{
			add("AnthropologySpecimen");
			add("MedicalHumanTissueSpecimen");
			add("BotanySpecimen");
			add("MycologySpecimen");
			add("GeneticResourcePlantSpecimen");
			add("GeneticResourceAnimalSpecimen");
			add("MicrobiologyuMicroOrganismSpecimen");
			add("ZoologyVertebrateSpecimen");
			add("ZoologyInvertebrateSpecimen");
			add("PaleontologySpecimen");
			add("GeologyRockSpecimen");
			add("GeologyMineralSpecimen");
			add("GeologyMixedSolidMatterSpecimen");
			add("GeologyWaterIceSpecimen");
			add("GeologyLiquidOrGaseousMatterSpecimen");
			add("AstronomySpecimen");
			add("ExperimentalMaterialSpecimen");
		}
	};
	
	public HandleRecordSpecimen(byte[] handle, String url, String subtype, String institute) throws JsonProcessingException {
		super(handle);
		this.pidStatus = "TEST";
		this.url = url;
		this.institute = institute;
		
		setDigitalObjectSubtype(subtype);
		setDigSubtypeStr();
		setDate();
		setReferent();
		setPidIssuer(); 
		setEntries();
	}	
	
	public HandleRecordSpecimen(List<Handles> entries, byte[] handle) throws JsonMappingException, JsonProcessingException {
		super(handle);
		this.entries = entries;
		
		String type;
		
		for (Handles h: entries) {
			type = h.getType();
			switch(type) {
				case "URL":
					this.url = h.getData();
					break;
				case "pidIssuer":
					this.pidIssuer = h.getData();
					break;
				case "digitalObjectType":
					this.digitalObjectTypeString = h.getData();
					break;
				case "digitalObjectSubtype":
					this.digitalObjectSubtypeString = h.getData();
					parseDigitalObjectSubtypeStr(digitalObjectSubtypeString);
				case "issueDate":
					this.issueDate = h.getData();
					break;
				case "issueNumber":
					this.issueNumber = h.getData();
					break;
				case "pidStatus":
					this.pidStatus = h.getData();
					break;
				case "referent":
					this.referent = h.getData();
					break;				
				default:
					
				}	
		}
	}
	
	@Override
	protected void setEntries() {
		entries = new ArrayList<Handles>();
		int i = 0;
		
		entries.add(new Handles(handle, i++, "URL", url, timestamp));
		entries.add(new Handles(handle, i++, "pidIssuer", pidIssuer, timestamp)); 
		entries.add(new Handles(handle, i++, "digitalObjectType", digitalObjectTypeString, timestamp)); 
		entries.add(new Handles(handle, i++, "digitalObjectSubtype", digitalObjectSubtypeString, timestamp)); 
		entries.add(new Handles(handle, i++, "issueDate", issueDate, timestamp));
		entries.add(new Handles(handle, i++, "issueNumber", "1", timestamp));
		entries.add(new Handles(handle, i++, "pidStatus", "TEST", timestamp));
		entries.add(new Handles(handle, i++, "pidKernelMetadataLicense", pidKernelMetadataLicense, timestamp));
		entries.add(new Handles(handle, i++, "referent", referent, timestamp));		
		setAdminHandle();
	}
	
	
	private void parseDigitalObjectSubtypeStr(String subtypeStr) throws JsonMappingException, JsonProcessingException {
		DigitalObjectSubtype subtype = objectMapper.readValue(subtypeStr, DigitalObjectSubtype.class);
		logger.info("subtype String: " + subtypeStr + " \t subtype: " + subtype.getPrimaryNameFromPid());
		
		setDigitalObjectSubtype(subtype.getPrimaryNameFromPid());
	}
	
	private void setDigitalObjectSubtype(String subtype) throws JsonProcessingException {
		logger.info("Subtype = " + subtype);
		if (this.digSubtypes.contains(subtype)) {
			this.digitalObjectSubtype = subtype;
		}
		else {
			this.digitalObjectSubtype="";
			logger.warning("Digital object type invalid");
		}
	}
	
	private void setDigSubtypeStr() throws JsonProcessingException {
		DigitalObjectSubtype digSubType = new DigitalObjectSubtype(); 
		digSubType.setPrimaryNameFromPid(digitalObjectSubtype);
		
		switch (digitalObjectSubtype) {
		case "ZoologyVertebrateSpecimen":
			digSubType.setPid("21.T11148/ccdb8cda8a785230257e");
		case "AstronomySpecimen":
			digSubType.setPid("21.T11148/894b1e6cad57e921764e");
		default:
			digSubType.setPid("");
		}
		
		digitalObjectSubtypeString = objectMapper.writeValueAsString(digSubType);
	}
	
	private String setDigitalObjectTypeString() {
		return "{\n"
				+ "  \"pid\": \"21.T11148/894b1e6cad57e921764e \",  \n"
				+ "  \"pidType\": \"Handle\",  \n"
				+ "  \"primaryNameFromPid\": \"Digital Specimen\",\n"
				+ "}";		
	}
	
	
	private void setDate() {
		Date d = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-YYYY");
		issueDate = dateFormat.format(d);
		
	}
	
	private void setReferent() {
		// This is going to change when we develop a new DOI schema!!!
		referent = "{\"referentIdentifiers\": \"\", "
				+ "\"primaryReferentType\": \"creation\", "
				+ "\"creationName\": \"\", "
				+ "\"structuralType\": \"\", "
				+ "\"mode\": \"tangible\", "
				+ "\"character\": \"other\", "
				+ "\"principalAgent\": \"principalAgent\": "
					+ "{ \"name\": { "
						+ "\"value\": \"" + institute + "\","
						+ "\"type\": \"PrincipalName\"}"
					+ "\"identifier\": "
						+ "[{\"value\": \""+ getRor() + "\", \"type\": \"ROR\"}],"
						+ "\"role\": \"Creator\" }";		
	}
	
	private String getRor() {
		// TODO: Lookup ror
		// This might be obtained through Keycloak
		return "";
	}
	
	
	
	public String getDigSubtype(){
		return digitalObjectSubtype;
	}
	
	
	private void setPidIssuer() {
		this.pidIssuer = "{\"pid\":\"20.5000.1025/\", \"nameFromPid\":\"DiSSCo\" }";
	}
	
	// For future use?
	private void setPidIssuer(String pid, String name) {
		this.pidIssuer = "{\"pid\":\""+ pid +"\", \"nameFromPid\":\""+name+"\" }";
	}
	
	@JsonIgnore
	public String getInstitute() {
		return this.institute;
	}
	
	@JsonIgnore
	public List<String> getDigTypeList(){
		return digSubtypes;
	}
	
/*
 * private String url;
	private String pidIssuer;
	private String digitalObjectType;
	private String loc = "";
	private String issueDate;
	private String issueNumber = "1";
	private  String pidStatus = "TEST"; 
	private final String pidKernelMetadataLicense = "Creative Commons Zero";
	private String referent;
 */
	
	public boolean equals(HandleRecordSpecimen spec2) {
		// Not sure if I should also test times
		
		return (getHandleStr().equals(spec2.getHandleStr()) &&
				url.equals(spec2.getUrl()) &&
				pidIssuer.equals(spec2.getPidIssuer()) &&
				digitalObjectSubtype.equals(spec2.getDigitalObjectSubtype())&&
				issueDate.equals(spec2.getIssueDate())&&
				issueNumber.equals(spec2.getIssueNumber())&&
				pidStatus.equals(spec2.getPidStatus())&&
				referent.equals(spec2.getReferent()));
	}
}