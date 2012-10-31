package com.example.HL7;

public class PID {

	public String set_id = "";
	public String patient_identifier_list = "";
	public String patient_name = "";
	public String mothers_maiden_name = "";
	public String date_of_birth = "";
	public String sex = "";

	//constructor with R
	public PID(String set_id, String patient_identifier_list, String patient_name) {
		this.set_id = set_id;
		this.patient_identifier_list = patient_identifier_list;
		this.patient_name = patient_name;
	}//PID
	
	//constructor with R+RE
	public PID(String set_id, String patient_identifier_list, String patient_name, String mothers_maiden_name, String date_of_birth, String sex) {
		this.set_id = set_id;
		this.patient_identifier_list = patient_identifier_list;
		this.patient_name = patient_name;
		this.mothers_maiden_name = mothers_maiden_name;
		this.date_of_birth = date_of_birth;
		this.sex = sex;
	}//PID
	
	
	public String getPID() {
		String all = "PID|"+set_id+"|"+"|"+patient_identifier_list+"|"+"|"+patient_name+"|"+mothers_maiden_name+"|"+date_of_birth+"|"+sex;
		return all;
	}
}
