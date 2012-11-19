package ahd.basics.hl7;

public class OBX {

	public String set_id = "";
	public String value_type = "";
	public String observation_identifier = "";
	public String observation_sub_id = "";
	public String observation_value = "";
	public String units = "";
	public String observation_result_status = "";
	public String observation_timestamp = "";
	public String organization_name = ""; //? unclear position in sequence -> 2.7 required!
	
	public OBX (String set_id, String value_type, String observation_identifier, String observation_sub_id, String observation_value,
			String units, String observation_result_status, String observation_timestamp, String organization_name) {
		this.set_id = set_id;
		this.value_type = value_type;
		this.observation_identifier = observation_identifier;
		this.observation_sub_id = observation_sub_id;
		this.observation_value = observation_value;
		this.units = units;
		this.observation_result_status = observation_result_status;
		this.observation_timestamp = observation_timestamp;
		this.organization_name = organization_name;
	}
	
	public String getOBX() {
		String all = "OBX|"+set_id+"|"+value_type+"|"+observation_identifier+"|"+observation_sub_id+"|"+observation_value+"|"+units+"|"+"|"+"|"+"|"+"|"+
				observation_result_status+"|"+"|"+"|"+observation_timestamp+"|"+"|"+"|"+"|"+organization_name;
		return all;
	}
}
