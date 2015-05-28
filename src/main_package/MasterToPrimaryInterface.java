package main_package;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MasterToPrimaryInterface extends Remote {
	public void newTransaction(long txnID, String fileName) throws RemoteException;
	public void newReplicaServer(String replicaIp) throws RemoteException;
}
