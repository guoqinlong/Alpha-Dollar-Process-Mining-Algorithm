package cn.edu.thss.iise.beehivez.server.mining.alphamminer;

import java.util.ArrayList;
import java.util.HashSet;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.mining.logabstraction.LogRelations;
import org.processmining.mining.logabstraction.LogRelationsImpl;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

public class AlphaMMinerDataUtil {


	public static AlphaSharpData convertRelation(AlphaPPData alphaPPData, LogEvents leEvents) {
		// TODO Auto-generated method stub
		LogRelations result;
		RelationMatrix relations = alphaPPData.rmRelation;	
		ArrayList<String> alT_I = alphaPPData.alT_I;
		ArrayList<String> alT_O = alphaPPData.alT_O;
		ArrayList<String> alL1L = alphaPPData.alL1L;
		ArrayList<String> alT_log = alphaPPData.alT_log;
		int size = alT_log.size();		//所有的任务的size;
				
		DoubleMatrix1D dmOneLoop;
		DoubleMatrix1D dmStart;
		DoubleMatrix1D dmEnd;
		DoubleMatrix2D dmParallel;
		DoubleMatrix2D dmCausal;
		
		//1.dmCausal;
		//2.dmParallel;
		dmCausal = DoubleFactory2D.sparse.make(size, size, 0);
		dmParallel = DoubleFactory2D.sparse.make(size, size, 0);		
		for (int i=0; i<size; i++)
			for (int j=0; j<size; j++)
			{
				String task1 = alT_log.get(i);
				String task2 = alT_log.get(j);
				int rela = relations.getRelation(task1, task2);
				if ((rela & Relation.SUCCESSIVE) > 0)
					dmCausal.set(i, j, 1);
				if ((rela & Relation.PARALLEL) > 0)
					dmParallel.set(i, j, 1);				
			}
		

		//3.dmEnd;
		dmEnd = DoubleFactory1D.sparse.make(size,0);
		for (String task	:	alT_O)
		{
			int pos = alT_log.indexOf(task);
			dmEnd.set(pos, 1);
			
		}
		//4.dmStart;
		dmStart = DoubleFactory1D.sparse.make(size,0);
		for (String task	:	alT_I)
		{
			int pos = alT_log.indexOf(task);
			dmStart.set(pos, 1);
			
		}
		//5.dmOneLoop;
		dmOneLoop = DoubleFactory1D.sparse.make(size,0);
		for (String task	:	alL1L)
		{
			int pos = alT_log.indexOf(task);
			dmOneLoop.set(pos, 1);
		}
		//6.leEvents;
		
		AlphaSharpData alphaSharpData = new AlphaSharpData();
		result = new LogRelationsImpl(dmCausal, dmParallel, dmEnd, dmStart, dmOneLoop, leEvents);
		alphaSharpData.theRelations = result;
		alphaSharpData.nme = size;
		
		return alphaSharpData;
	}

	public static void addRelation(AlphaPPData alphaPPData,
			AlphaSharpData alphaSharpData) {
		
		//RelationMatrix _rmRelation = alphaPPData.rmRelation;
		RelationMatrix _rmRelation = new RelationMatrix();
		ArrayList<String> _alL1L;
		ArrayList<String> _alT_I;
		ArrayList<String> _alT_O;
		ArrayList<String> _alT_prime;			
		ArrayList<String> _alT_log;				
		
		LogRelations theRelations = alphaSharpData.theRelations;
		LogEvents logEvents = theRelations.getLogEvents();
		
		//0. get the size of visible task and all task
		
		int size = alphaSharpData.nme;		//number of all tasks.
		int realTaskSize = alphaSharpData.nme_old;					//number of "visible" task.
					
		//0.alT_log		//all the tasks, concludes the new-added invisible task
		_alT_log = new ArrayList<String>();
		for (int i=0; i<size; i++)
		{
			LogEvent logEvent = logEvents.get(i);
			String taskName = logEvent.getModelElementName()+"\0"+logEvent.getEventType();
			_alT_log.add(taskName);
		}
		
		//1._alL1L
		_alL1L = alphaPPData.alL1L;			//the alphasharp doesn't generate the alL1L
		
		//2.end
		_alT_O = new ArrayList<String>();
		DoubleMatrix1D endInfo = theRelations.getEndInfo();
		for (int i=0; i<size; i++)
			if (endInfo.get(i) >0)
			{
				String taskName = _alT_log.get(i);
				_alT_O.add(taskName);
			}		
		
		//3.start
		_alT_I = new ArrayList<String>();
		DoubleMatrix1D startInfo = theRelations.getStartInfo();
		for (int i=0; i<size; i++)
			if (startInfo.get(i)>0)
			{
				String taskName = _alT_log.get(i);
				_alT_I.add(taskName);
			}
		
//		//5.oneloop
//		_alL1L = new ArrayList<String>();
//		DoubleMatrix1D oneLoopInfo = theRelations.getOneLengthLoopsInfo();
//		for (int i=0; i<size; i++)
//			if (oneLoopInfo.get(i)>0)
//			{
//				String taskName = _alT_log.get(i);
//				_alL1L.add(taskName);
//			}
		//6.alT_prime
		_alT_prime = (ArrayList<String>) _alT_log.clone();
		_alT_prime.removeAll(_alL1L);
		
		//7-10: >, ||, , ->,aba,仅考虑添加的部分，其他的不考虑			
		
//		for (int i=0; i<_alT_log.size(); i++)
//		{
//			String taskName = _alT_log.get(i);
//			_rmRelation.addTask(taskName);
//		}
		
		//7.a>b 		
		//7.1 由->推出
		DoubleMatrix2D dmCausal = theRelations.getCausalFollowerMatrix();
		for (int i=0; i<size; i++)
			for (int j=0; j<size; j++)
			{
//				if (i<realTaskSize && j<realTaskSize)
//					continue;
				if (dmCausal.get(i, j) >0)
				{
					String taskNameI = _alT_log.get(i);
					String taskNameJ = _alT_log.get(j);			
					_rmRelation.addRelation(taskNameI, taskNameJ, Relation.BEFORE);
					_rmRelation.addBefore(taskNameJ,taskNameI);					
				}
			}
				
		//7.2由||推出
		DoubleMatrix2D dmParal = theRelations.getParallelMatrix();
		for (int i=0; i<size; i++)
			for (int j=0; j<size; j++)
			{
//				if (i<realTaskSize && j<realTaskSize)
//					continue;
				if(dmParal.get(i, j)>0)
				{
					String taskNameI = _alT_log.get(i);
					String taskNameJ = _alT_log.get(j);			
					_rmRelation.addRelation(taskNameI, taskNameJ, Relation.BEFORE);
					_rmRelation.addBefore(taskNameJ,taskNameI);
					
					_rmRelation.addRelation(taskNameJ, taskNameI, Relation.BEFORE);
					_rmRelation.addBefore(taskNameI,taskNameJ);
				}
			}
		//8.-> 关系						
		for (int i=0; i<size; i++)
			for (int j=0; j<size; j++)
			{
//				if (i<realTaskSize && j<realTaskSize)
//					continue;
				if (dmCausal.get(i, j)>0)
				{
					String taskNameI = _alT_log.get(i);
					String taskNameJ = _alT_log.get(j);
					TaskUnit tuI = (TaskUnit) _rmRelation.htRelations.get(taskNameI);
					tuI.addSucc(taskNameJ);
					tuI.addRela(taskNameJ, Relation.SUCCESSIVE);					
				}
			}
		//9.paral		
		for (int i=0; i<size; i++)
			for (int j=0; j<size; j++)
			{
//				if (i<realTaskSize && j<realTaskSize)
//					continue;
				if (dmParal.get(i, j)>0)		
				{
					String taskNameI = _alT_log.get(i);
					String taskNameJ = _alT_log.get(j);
					TaskUnit tuI = (TaskUnit) _rmRelation.htRelations.get(taskNameI);
					
					tuI.removePred(taskNameJ);		//TODO 没搞明白为啥不需要pred里面.因为不是before关系了哦！但是succ保留下了，因为succ关系当且仅当->才可以
					tuI.addPara(taskNameJ);
					tuI.addRela(taskNameJ, Relation.PARALLEL);
				}
			}
		//10.aba, 没有考虑，因为aba的作用是寻找->和||,但是->和||已经在alpha#中找出来了，所以不需要这个关系了，呵呵,实际上在alpha#中也没法考虑这个关系啊
		//需要考虑，因为alphasharp.invisible_1_49等5个模型则算不出来。aba,bab的关系时a->b,b->a,a\||b
		for (int i=0; i<size; i++)
			for (int j=0; j<size; j++)
			{
				if (i == j)
					continue;
				if (dmCausal.get(i, j) >0 && dmCausal.get(j, i)>0 && dmParal.get(i, j) == 0)
				{
					String taskNameI = _alT_log.get(i);
					String taskNameJ = _alT_log.get(j);
					_rmRelation.addRelation(taskNameI, taskNameJ, Relation.SHORTLOOP);
					_rmRelation.addRelation(taskNameJ, taskNameI, Relation.SHORTLOOP);
				}
			}
		//11.logEvents 不要考虑了
		
		alphaPPData.rmRelation = _rmRelation;
		alphaPPData.alL1L = _alL1L;
		alphaPPData.alT_I = _alT_I;
		alphaPPData.alT_O = _alT_O;		
		alphaPPData.alT_log = _alT_log;
		alphaPPData.alT_prime = _alT_prime;
		
		alphaPPData.allSize = size;
		alphaPPData.allVisibleSize = realTaskSize;		
		alphaPPData.invTasks = alphaSharpData.invTasks;
		alphaPPData.invTaskAXYB = alphaSharpData.invTaskAXYB;
		
	}
	
	
	
	public static <T> HashSet<T> except(HashSet<T> a, HashSet<T> b)
	{
		HashSet<T> result = new HashSet<T>();
		for (T t	:	a)
			if (!b.contains(t))
				result.add(t);
		return result;
	}

	public static <T> HashSet<T> join(HashSet<T> a, HashSet<T> b)
	{
		HashSet<T> result = new HashSet<T>();
		for (T t	:	a)
			if (b.contains(t))
				result.add(t);
		return result;
	}
	
	public static <T> ArrayList<T> join(ArrayList<T> a, ArrayList<T> b)
	{
		ArrayList<T> result = new ArrayList<T>();
		for (T t	:	a)
			if (b.contains(t))
				result.add(t);
		return result;
	}

	public static <T> ArrayList<T> except(ArrayList<T> a, ArrayList<T> b)
	{
		ArrayList<T> result = new ArrayList<T>();
		for (T t	:	a)
			if (!b.contains(t))
				result.add(t);
		return result;
	}
	
	//determine all the elements in a belongs to the set b
	public static boolean belongs(HashSet<Integer> a,
			HashSet<Integer> b) {
		for (Integer i	:	a)
			if (!b.contains(i))
				return false;
		return true;
	}

	public static HashSet<Integer> removeParallel(HashSet<Integer> betweenYX,
			int y, int x, LogRelations relations) {
		HashSet<Integer> result = new HashSet<Integer>();
		for (Integer i	:	betweenYX)
		{
			if (relations.getParallelMatrix().get(y, i) ==1 || relations.getParallelMatrix().get(i,x) == 1)
				continue;
			result.add(i);				
		}
		return result;
	}

	public static <T> ArrayList<T> union(ArrayList<T> x,
			ArrayList<T> y) {
		ArrayList<T> result = new ArrayList<T>();
		for (T t	:	x)			
				result.add(t);
		for (T t	:	y)
			if (!result.contains(t))
				result.add(t);
		return result;
	}

	public static <T> HashSet<T> union(HashSet<T> a,
			HashSet<T> b) {
		HashSet<T> result = (HashSet<T>)a.clone();
		for (T t1	:	b)
			if (!result.contains(t1))
				result.add(t1);
		return result;		
	}

	public static boolean isSame(DoubleSet[] a, DoubleSet[] b) 
	{
		if (a.length != b.length)
			return false;
		for (DoubleSet dsA	:	a)
		{
			boolean isFound = false;
			for (DoubleSet dsB	:	b)
				if (dsA.equals(dsB))
				{
					isFound = true;
					break;
				}
			if (!isFound)
				return false;
		}
		return true;
	}

	public static <T> ArrayList join(T[] before, T[] succ) {
		ArrayList<T> result = new ArrayList<T>();
		for (T t1	:	before)
		{
			boolean isFound = false;
			for (T t2	:	succ)
				if (t1.equals(t2))
				{
					isFound = true;
					break;
				}
			if (isFound)
				result.add(t1);
		}
		return result;
	}	
}

