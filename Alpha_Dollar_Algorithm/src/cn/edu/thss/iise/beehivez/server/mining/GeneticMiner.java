package cn.edu.thss.iise.beehivez.server.mining;

import org.processmining.converting.HNetToPetriNetConverter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.mining.geneticmining.GeneticMinerUI;
import org.processmining.mining.geneticmining.RealGeneticMiner;
import org.processmining.mining.geneticmining.duplicates.DTGeneticMinerResult;
import org.processmining.mining.heuristicsmining.HeuristicsNetResult;
import org.processmining.mining.petrinetmining.AlphaProcessMiner;
import org.processmining.mining.petrinetmining.PetriNetResult;

public class GeneticMiner extends ControlFlowMiner{

	@Override
	public String getDesription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "GeneticMiner";
	}

	@Override
	public PetriNet mine(LogReader logReader) {
		if (logReader != null) {
			// Mine the log for a Petri net.
			LogSummary summary = logReader.getLogSummary();		
			RealGeneticMiner miningPlugin = new RealGeneticMiner();
			miningPlugin.ui = new GeneticMinerUI(summary);
			DTGeneticMinerResult resultlist = (DTGeneticMinerResult)miningPlugin.mine(logReader);
			HeuristicsNet HNetResult = resultlist.population[0];
			HNetToPetriNetConverter converter = new HNetToPetriNetConverter();
			PetriNet result = converter.convert(HNetResult);
			return result;			
		} else {
			System.err.println("No log reader could be constructed.");
			return null;
		}
	}

}
