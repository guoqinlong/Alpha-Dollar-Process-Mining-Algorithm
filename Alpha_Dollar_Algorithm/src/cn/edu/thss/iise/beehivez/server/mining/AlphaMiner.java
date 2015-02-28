package cn.edu.thss.iise.beehivez.server.mining;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.mining.petrinetmining.AlphaProcessMiner;
import org.processmining.mining.petrinetmining.PetriNetResult;

public class AlphaMiner extends  ControlFlowMiner{

	@Override
	public String getDesription() {
		// TODO Auto-generated method stub
		return "AlphaMiner";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "AlphaMiner";
	}

	@Override
	public PetriNet mine(LogReader logReader) {
		if (logReader != null) {
			// Mine the log for a Petri net.
			AlphaProcessMiner miningPlugin = new AlphaProcessMiner();
			PetriNetResult result = (PetriNetResult) miningPlugin.mine(logReader);
			return result.getPetriNet();
		} else {
			System.err.println("No log reader could be constructed.");
			return null;
		}
		
	}

}
