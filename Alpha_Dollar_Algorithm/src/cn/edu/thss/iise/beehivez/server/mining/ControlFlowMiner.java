package cn.edu.thss.iise.beehivez.server.mining;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;

public abstract class ControlFlowMiner {

	public abstract PetriNet mine(LogReader logReader);
	public abstract String getName();
	public abstract String getDesription();
}
