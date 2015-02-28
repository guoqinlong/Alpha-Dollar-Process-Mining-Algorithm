package cn.edu.thss.iise.beehivez.server.mining;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.mining.petrinetmining.AlphaPPProcessMiner;
import org.processmining.mining.petrinetmining.AlphaProcessMiner;
import org.processmining.mining.petrinetmining.PetriNetResult;

public class AlphaPlusPlusMiner extends ControlFlowMiner {

	@Override
	public String getDesription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "AlphaPlusPlusMiner";
	}

	@Override
	public PetriNet mine(LogReader logReader) {
		if (logReader != null) {
			// Mine the log for a Petri net.
			AlphaPPProcessMiner miningPlugin = new AlphaPPProcessMiner();
			PetriNetResult result = (PetriNetResult) miningPlugin.mine(logReader);
			return result.getPetriNet();
		} else {
			System.err.println("No log reader could be constructed.");
			return null;
		}

	}

}
