package com.example.main;

import java.util.List;

import com.example.acqusition.AcquisitionControllerImpl;
import com.example.controller.AcquisitionController;
import com.example.mysqltest.RouteDataExtractor;

public class Main {
	public static void main(String[] args){
		//TODO:ここにスキットを書く
		String start = "清瀬";
		String end = "池袋";
		
		//駅情報の登録
		RouteDataExtractor rde = new RouteDataExtractor();
		rde.initStationIdList(start, end);
		List<Integer> idList = rde.getStationList();
		for(int id:idList){
			System.out.println(id);
		}
		
		//測位制御部分の初期化
		AcquisitionController controller = new AcquisitionControllerImpl(idList, 4);
		boolean canLocate = controller.canAcquisition(true, System.currentTimeMillis());
		
		
		
		
	}
}
