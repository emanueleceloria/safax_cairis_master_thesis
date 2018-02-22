package nl.tue.sec.cairis.impl;

import nl.tue.sec.cairis.util.LogUtil;

public class EvaluationMethods {

	public static boolean evaluateThreshold(int risk, int threshold, String transID){
		boolean accepted = false;

		if(risk <= threshold){
//			System.out.println("\n\n\nOk, Risk acceptable!\n\n\n");
			LogUtil.writeLog(transID, "Ok, Risk acceptable!" , 2);
			accepted = true;
		}else{		
//			System.out.println("\n\n\nRisk cannot be accepted, permission has to be denied!\n\n\n");
			LogUtil.writeLog(transID, "Risk cannot be accepted, permission has to be denied!" , 2);
		}

		return accepted;
	}
}
