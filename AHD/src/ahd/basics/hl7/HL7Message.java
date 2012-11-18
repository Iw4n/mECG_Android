package ahd.basics.hl7;

import java.util.ArrayList;

public class HL7Message {

	public MSH header;
	public PID patient;
	public OBR report;
	public ArrayList<OBX> result;
	
	public HL7Message (MSH h, PID p, OBR r, ArrayList<OBX> x) {
		header = h;
		patient = p;
		report = r;
		result = x;
	}
	
	public String getHL7Message() {
		String all = header.getMSH()+"\n"+patient.getPID()+"\n"+report.getOBR()+"\n";
		for(int i=0;i<result.size();i++)
			all += result.get(i).getOBX()+"\n";
		
		return all;
	}
	
}
