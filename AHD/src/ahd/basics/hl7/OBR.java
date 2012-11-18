package ahd.basics.hl7;

public class OBR {

	public String set_id = "";
	public String placer_order_number = "";
	public String filler_order_number = "";
	public String universal_service_identifier = "";
	public String observation_timestamp = "";
	
	public OBR (String set_id, String placer_order_number, String filler_order_number, String universal_service_identifier, String observation_timestamp) {
		this.set_id = set_id;
		this.placer_order_number = placer_order_number;
		this.filler_order_number = filler_order_number;
		this.universal_service_identifier = universal_service_identifier;
		this.observation_timestamp = observation_timestamp;
	}
	
	public String getOBR() {
		String all = "OBR|"+set_id+"|"+placer_order_number+"|"+filler_order_number+"|"+universal_service_identifier+"|"+"|"+"|"+observation_timestamp;
		return all;
	}
}
