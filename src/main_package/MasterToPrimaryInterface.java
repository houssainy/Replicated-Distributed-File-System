package main_package;

import java.rmi.Remote;

public interface MasterToPrimaryInterface extends Remote{
	
	public void newTransaction(long txnID, String fileName);
	
}
