package ahd.basics.hl7;

public class MSA {
	public String acknowledgement_code = "";
	public String message_control_id = "";
	
	public MSA (String acknowledgement_code, String message_control_id) {
		this.acknowledgement_code = acknowledgement_code;
		this.message_control_id = message_control_id;
	}
	
	public String getMSA() {
		String all = "MSA|"+acknowledgement_code+"|"+message_control_id+"|";
		return all;
	}
}
