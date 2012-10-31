package com.example.HL7;

public class MSH {

	public String encoding_characters = "^~\\&amp;";
	public String sending_application = "";
	public String sending_facility = "";
	public String receiving_application = "";
	public String receiving_facility = "";
	public String timestamp = "";
	public String message_type = "ORU^R01^ORU_R01";
	public String message_control_id = "";
	public String processing_id = "P";
	public String version_id = "2.6";
	public String accept_acknowledgemnt_type = "NE";
	public String application_acknowledgement_type = "AL";
	public String message_profile_identifier = "";
	
	
	// constructor for R
	public MSH(String sending_facility, String timestamp, String message_control_id, String message_profile_identifier) {
		this.sending_facility = sending_facility;
		this.timestamp = timestamp;
		this.message_control_id = message_control_id;
		this.message_profile_identifier = message_profile_identifier;
	}//MSH
	
	//constructor for R+RE
	public MSH(String sending_application, String sending_facility, String receiving_application, String receiving_facility, String timestamp, String message_control_id, String message_profile_identifier) {
		this.sending_application = sending_application;
		this.sending_facility = sending_facility;
		this.receiving_application = receiving_application;
		this.receiving_facility = receiving_facility;
		this.timestamp = timestamp;
		this.message_control_id = message_control_id;
		this.message_profile_identifier = message_profile_identifier;
	}//MSH
	
	public String getMSH() {
		String msg = "MSH|"+encoding_characters+"|"+sending_application+"|"+sending_facility+"|"+receiving_application+"|"+receiving_facility+"|"+timestamp+"|"+"|"+message_type+
			"|"+message_control_id+"|"+processing_id+"|"+version_id+"|"+"|"+"|"+accept_acknowledgemnt_type+"|"+application_acknowledgement_type+"|"+"|"+"|"+"|"+"|"+message_profile_identifier;
		return msg;
	}//getMSH
	
}//class
