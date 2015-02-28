package cn.edu.thss.iise.beehivez.server.mining.alphamminer;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Progress;
import org.processmining.mining.MiningResult;
import org.processmining.mining.logabstraction.LogRelationBasedAlgorithm;
import org.processmining.mining.logabstraction.LogRelations;

import java.util.*;
import java.io.*;




public class AlphaMMiner extends LogRelationBasedAlgorithm
{
	AlphaPPData alphaPPData = new AlphaPPData();
	
	
	@Override
	public String getName() {		
		return "Alpha Mix Miner";
	}

	@Override
	public String getHtmlDescription() {
		return "Alpha Mix Miner";
	}

	@Override
	public MiningResult mine(LogReader log, LogRelations relations, Progress progress)
	{
		ModifiedAlphaPPProcessMiner alphapp = new ModifiedAlphaPPProcessMiner();
		ModifiedAlphaSharpProcessMiner alphasharp = new ModifiedAlphaSharpProcessMiner();
		Date d1 = new Date();
		alphaPPData = alphasharp.mineAlphaSharpInfo(log,relations,progress);
		Date d2 = new Date();
		System.out.println("Alpha Sharp"+(d2.getTime() - d1.getTime()));
		MiningResult miningResult = alphapp.mineAlphPPInfo(log, alphaPPData,progress);
		Date d3 = new Date();		
		System.out.println("Alpha PP"+(d3.getTime() - d2.getTime()));
		return miningResult;
	}
	
	/*
	@Override
	public MiningResult mine(LogReader log, LogRelations relations,
			Progress progress) {
		ModifiedAlphaPPProcessMiner alphapp = new ModifiedAlphaPPProcessMiner();
		ModifiedAlphaSharpProcessMiner alphasharp = new ModifiedAlphaSharpProcessMiner();
		
		//0.discover the L1L
		discoverL1L(log, progress);
		//0.3
		computeL_W();
		//0.5 elminate the l1l
		eliminateL1L();
		//1.mine basic relations 
		mineBasicRelations(log, progress);
		
		//2.mine the special relations 							
		LogEvents leEvents = relations.getLogEvents();
		alphasharp.mineSpecialRelation(log, alphaPPData, progress, leEvents);		//alpha sharp mine the mendacious relations
		MiningResult miningResult = alphapp.mineMiscInfo(log, alphaPPData,progress);
		
		
		//3.mine the alpha++ relation and construct the petrinet.
		
		//alphapp.mineSpecialRelation(log, rmRelation, progress); (construct the process mining)
		//alphasharp.mineSpecialRelation(log, rmRelation, progress);
		//5.construct the process model (withL1L)
		
		return miningResult;
	}
	*/
	private void computeL_W() {
		// TODO Auto-generated method stub
		alphaPPData.lpL_W = new L1LPlaces(alphaPPData.rmRelation, alphaPPData.alL1L);
		alphaPPData.lpL_W.compute();
	}

	/**
	 * discover the l1l of the logs, which are stored in the alL1L/ and also the al_log are discovered
	 * @param log
	 * @param progress
	 */
	private void discoverL1L(LogReader log, Progress progress) 
	{
		  //enumate all process instances
        int np = log.numberOfInstances();
        for(int i=0; i<np; i++)
        {
            ProcessInstance pi = log.getInstance(i);
            AuditTrailEntryList atel = pi.getAuditTrailEntryList();

            //enumerate all events in one process instance
            String last2Task = "";
            String last1Task = "";
            boolean isFirst = true;
            for(int j=0; j<atel.size(); j++)
            {
                try
                {
                    //next event
                    AuditTrailEntry ate = atel.get(j);
                    String task = ate.getElement() + "\0" + ate.getType();

                    if (task.equals(last1Task) && ! alphaPPData.alL1L.contains(task))
                    {
                    	alphaPPData.alL1L.add(task);
                    }
                    
                    //T_log
                    if (!alphaPPData.alT_log.contains(task))
                    {
                    	alphaPPData.alT_log.add(task);
                    }
                
                    //T_I
                    if (isFirst)
                    {
                        if (!alphaPPData.alT_I.contains(task))
                        {
                        	alphaPPData.alT_I.add(task);
                        }
                        isFirst = false;
                    }
                    
                    //go ahead
                    last2Task = last1Task;
                    last1Task = task;
                }
                catch (IOException ex)
                {
                }
                catch (IndexOutOfBoundsException ex)
                {
                }
            }
            //last task
            if (!alphaPPData.alT_O.contains(last1Task))
            {
            	alphaPPData.alT_O.add(last1Task);
            }

        }
        
        //T_prime
        alphaPPData.alT_prime.addAll(alphaPPData.alT_log);
        alphaPPData.alT_prime.removeAll(alphaPPData.alL1L);
	}

	/**
	 * Mine the basic relations between tasks.
	 * basic relations are a>b, aba,->, || (these 4 relations are common in both alpha# and alpha++; NOTE: NO consider #?# = not-> and not <-..)  
	 */
	private void mineBasicRelations(LogReader log, Progress progress) {
		//1.a>b
		//2.aba
		
	    //enumate all process instances
        int np = log.numberOfInstances();
        for(int i=0; i<np; i++)
        {
            ProcessInstance pi = log.getInstance(i);
            AuditTrailEntryList atel = pi.getAuditTrailEntryList();

            //enumerate all events in one process instance
            String last2Task = "";
            String last1Task = "";
            boolean isFirst = true;
            for(int j=0; j<atel.size(); j++)
            {
                try
                {
                    //next event
                    AuditTrailEntry ate = atel.get(j);
                    String task = ate.getElement() + "\0" + ate.getType();                
                    
                    //a>b
                    if (!last1Task.equals("") && !last1Task.equals(task))
                    {
                        //> relation
                    	alphaPPData.rmRelation.addRelation(last1Task, task, Relation.BEFORE);
                        //before task
                    	alphaPPData.rmRelation.addBefore(task, last1Task);
                    }
                    //aba
                    if (last2Task.equals(task) && !task.equals(last1Task))
                    {
                    	alphaPPData.rmRelation.addRelation(last2Task, last1Task,
                                               Relation.SHORTLOOP);
                    }
                    //go ahead
                    last2Task = last1Task;
                    last1Task = task;
                }
                catch (IOException ex)
                {
                }
                catch (IndexOutOfBoundsException ex)
                {
                }
            }
        }	
		//3.->	
        
		//4.||
        

        
        alphaPPData.rmRelation.induceSucParOrder();
        
        
	}
	
    /**
     * eliminate any event who's task is in L1L
     */
    private void eliminateL1L()
    {
        if (alphaPPData.alL1L.size() == 0)
        {
            return;
        }
        ArrayList<String> allTasks = alphaPPData.rmRelation.getAllTasks();
        for (int i = 0; i < allTasks.size(); i++)
        {
            String t = (String) allTasks.get(i);
            alphaPPData.rmRelation.removeBefore(t, alphaPPData.alL1L);
            alphaPPData.rmRelation.removeAfter(t, alphaPPData.alL1L);
        }
    }
	
}


