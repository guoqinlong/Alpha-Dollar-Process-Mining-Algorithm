package cn.edu.thss.iise.beehivez.server.mining;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.mining.petrinetmining.AlphaProcessMiner;
import org.processmining.mining.petrinetmining.PetriNetResult;

import cn.edu.thss.iise.beehivez.server.mining.alphamminer.AlphaMMiner;

public class AlphaMixMiner extends ControlFlowMiner {

	@Override
	public PetriNet mine(LogReader logReader) {
		if (logReader != null) {
			// Mine the log for a Petri net.
			AlphaMMiner miningPlugin = new AlphaMMiner();
			PetriNetResult result = (PetriNetResult) miningPlugin.mine(logReader);
			return result.getPetriNet();
		} else {
			System.err.println("No log reader could be constructed.");
			return null;
		}		
	}

	@Override
	public String getName() {		
		return "Alpha Mix Miner";
	}

	@Override
	public String getDesription() {
		return "New Under Test Miner";
	}

}
