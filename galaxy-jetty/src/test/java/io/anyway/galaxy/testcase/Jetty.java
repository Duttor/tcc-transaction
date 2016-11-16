package io.anyway.galaxy.testcase;

import io.anyway.galaxy.infoBoard.TransactionServer;

public class Jetty {

	
    public static void main(String[] args){
    	TransactionServer.instance().start();
    }
}
