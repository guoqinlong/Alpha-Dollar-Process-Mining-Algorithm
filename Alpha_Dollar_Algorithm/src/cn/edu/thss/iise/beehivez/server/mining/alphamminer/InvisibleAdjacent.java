package cn.edu.thss.iise.beehivez.server.mining.alphamminer;

import java.util.HashSet;

import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;

class PlaceAdjacent
{
	HashSet<Transition> pre;
	HashSet<Transition> succ;
	
	public PlaceAdjacent(HashSet<Transition> _pre, HashSet<Transition> _succ)
	{
		pre = (HashSet<Transition>) _pre.clone();
		succ = (HashSet<Transition>) _succ.clone();
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result =1;
		result = prime * result + pre.hashCode();
		result = prime * result + succ.hashCode();
		return result;
	}
	
	@Override
	public boolean equals(Object placeAdjacent)
	{
		if (!(placeAdjacent instanceof PlaceAdjacent))
			return false;
		//TODO 严格来说,在这里彻底的排除掉invisible task也是不对的啊，还是应该考虑invisible task的，但是invisible task需要有一个读。。。
		PlaceAdjacent pa = (PlaceAdjacent) placeAdjacent;
		HashSet<Transition> thisPreWithoutInv = removeInv(this.pre);		// remove the invisible tasks....
		HashSet<Transition> thisSuccWithoutInv = removeInv(this.succ);		// remove the invisible tasks....
		HashSet<Transition> paPreWithoutInv = removeInv(pa.pre);		// remove the invisible tasks....
		HashSet<Transition> paSuccWithoutInv = removeInv(pa.succ);		// remove the invisible tasks....
		boolean result;
		result = (thisPreWithoutInv.equals(paPreWithoutInv) || (thisPreWithoutInv.size() == 0 && paPreWithoutInv.size() == 0) ) && (thisSuccWithoutInv.equals(paSuccWithoutInv) ||  (thisSuccWithoutInv.size() == 0 && paSuccWithoutInv.size() == 0));
		return result;
	}
	
	
	public static HashSet<Transition> removeInv(HashSet<Transition> original)
	{
		HashSet<Transition> result = new HashSet<Transition>();
		for (Transition t	:	original)
		{
			if (t.getLogEvent() == null)
				continue;
			result.add(t);
		}
		return result;		
	}

	public static PlaceAdjacent convert(Place place) {
		PlaceAdjacent result;
		HashSet<Transition> _pre = place.getPredecessors();		
		HashSet<Transition> _succ = place.getSuccessors();
		HashSet<Transition> pre = removeInv(_pre);
		HashSet<Transition> succ = removeInv(_succ);
		result = new PlaceAdjacent(pre, succ);
		return result;
	}
	
	
}

public class InvisibleAdjacent {
	
	HashSet<PlaceAdjacent> pred;	//the pred places.		
	HashSet<PlaceAdjacent> succ;	//the succ places.
	
	public InvisibleAdjacent(HashSet<PlaceAdjacent> _pred, HashSet<PlaceAdjacent> _succ)
	{
		pred = _pred;
		succ = _succ;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
	    int result = 1;
		result =  prime * result + pred.hashCode() + succ.hashCode();
		return result;
	}
	
	@Override 
	public boolean equals(Object invisibleAdjacent)
	{
		if (!(invisibleAdjacent instanceof InvisibleAdjacent))
			return false;
		InvisibleAdjacent ia = (InvisibleAdjacent) invisibleAdjacent;
		boolean result;
		result = ia.pred.equals(this.pred) && ia.succ.equals(this.succ);
		return result;
	}

	public static InvisibleAdjacent getInvisibleTask(
			PetriNet petriNet, HashSet<ModelGraphVertex> prePlaces, HashSet<ModelGraphVertex> postPlaces) {
		InvisibleAdjacent result;
		HashSet<PlaceAdjacent> pred = new HashSet<PlaceAdjacent>();
		HashSet<PlaceAdjacent> succ = new HashSet<PlaceAdjacent>();
		for (ModelGraphVertex mgv	:	prePlaces)
		{
			Place place = (Place) mgv;
			PlaceAdjacent pa = PlaceAdjacent.convert(place);
			pred.add(pa);
		}
		for (ModelGraphVertex mgv	:	postPlaces)
		{
			Place place = (Place) mgv;
			PlaceAdjacent pa = PlaceAdjacent.convert(place);
			succ.add(pa);
		}
		result = new InvisibleAdjacent(pred, succ);
		return result;
	}
}
