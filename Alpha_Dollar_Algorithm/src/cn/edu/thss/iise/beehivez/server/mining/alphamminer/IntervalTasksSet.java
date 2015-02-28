package cn.edu.thss.iise.beehivez.server.mining.alphamminer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvents;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

class IntervalTasks {
	HashSet<HashSet<Integer>> content;		//each integer means the interval tasks.
	
	HashSet<Integer> mayIn;			//may be in the content
	HashSet<Integer> mustNotIn;		//must not be in the content.
	
	public IntervalTasks(int n)
	{
		content =new HashSet<HashSet<Integer>>();		
	}

	public HashSet<Integer> getIntervalTasks() {
		HashSet<Integer> result = new HashSet<Integer>();
		for (HashSet<Integer> set	:	content)
		{
			result = AlphaMMinerDataUtil.union(result, set);
		}
		return result;
	}
	
	public void beginAdd()
	{
		mayIn = new HashSet<Integer>();
		mustNotIn = new HashSet<Integer>();
	}
	
	public void addIn(int task)
	{
		mayIn.add(task);
	}
	
	public void addNotIn(int task)
	{
		mustNotIn.add(task);
	}
	
	public void endAdd()
	{
		//content = AlphaMMinerDataUtil.except(mayIn, mustNotIn);
		if (mayIn.size() != 0)
			content.add(mayIn);				
	}
	
}

public class IntervalTasksSet
{
	HashMap<Integer, IntervalTasks> content;  //这里的integer是向后的啊
	HashMap<String, Integer> transferTabel;
	int base;
	public IntervalTasksSet(int n)
	{
		base = n+2;
		content = new HashMap<Integer, IntervalTasks>();
		for (int i=0; i<(n-1)*base + (n-1)+1; i++)
		{
			IntervalTasks intervalTasks = new IntervalTasks(n);
			content.put(i,intervalTasks);			
		}
		transferTabel = new HashMap<String, Integer>();
	}
	
	public HashSet<Integer> getBetween(Integer taskY, Integer taskX) 
	{
		HashSet<Integer> result = new HashSet<Integer>();
		IntervalTasks intervalTasks = content.get(taskY*base + taskX);
		if (intervalTasks != null)
			result = intervalTasks.getIntervalTasks();	
		return result;
	}

	/*
	public void addTask(String preTask, String postTask, String nowTask, LogEvents logEvents) {
		int pre = -1, post = -1, now = -1;
		for (int i=0; i<logEvents.size(); i++)
		{
			String name = logEvents.get(i).getModelElementName();
			if (name.equals(preTask))
				pre = i;
			if (name.equals(postTask))
				post = i;
			if (name.equals(nowTask))
				now = i;
		}
		if (pre == -1 || post == -1 || now == -1)
			return;
		IntervalTasks intervalTasks = content.get(pre * base + post);
		intervalTasks.add(now);
	}
	*/

	public void beginAdd()
	{
		for (int i=0; i<content.size(); i++)
			content.get(i).beginAdd();
	}
	
	public void endAdd()
	{
		for (int i=0; i<content.size(); i++)
			content.get(i).endAdd();
	}

	public void addTrace(AuditTrailEntryList ates, LogEvents logEvents) 
	{
		ArrayList<Integer> intTrace = new ArrayList<Integer>();		
		beginAdd();
		//1.get the int-version of trace(intTrace)
		for (int i=0; i<ates.size(); i++)
		{
			try 
			{
				Integer taskInt;
				String taskName = ates.get(i).getName();
				if ((taskInt = transferTabel.get(taskName)) == null)
				{
					for (int j=0; j<logEvents.size(); j++)					
						if (logEvents.get(j).getModelElementName().equals(taskName))
						{
							taskInt = j;
							break;
						}
					transferTabel.put(taskName, taskInt);
				}				
				if (taskInt != null)
					intTrace.add(taskInt);								
			} 
			catch (IndexOutOfBoundsException e) 
			{			
				e.printStackTrace();
			} 
			catch (IOException e) 
			{			
				e.printStackTrace();
			}			
		}
		
		//2.search to add
		for (int i=0; i<intTrace.size(); i++)
			for (int j=i; j<intTrace.size(); j++)
			{
				int pre = intTrace.get(i);
				int post = intTrace.get(j);
				IntervalTasks intervalTasks = content.get(pre*base+post);
				for (int k=0; k<i; k++)
					intervalTasks.addNotIn(intTrace.get(k));
				for (int k=i+1; k<j; k++)
					intervalTasks.addIn(intTrace.get(k));
				for (int k=j+1; k<intTrace.size(); k++)
					intervalTasks.addNotIn(intTrace.get(k));
			}
		endAdd();
	}

	public HashSet<HashSet<Integer>> getContent(int taskY, int taskX) {		
		IntervalTasks intervalTasks = content.get(taskY*base + taskX);
		if (intervalTasks != null)
			return intervalTasks.content;
		else
			return new HashSet<HashSet<Integer>>();
	}

}
