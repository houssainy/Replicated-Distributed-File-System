package main_package;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PrimaryToMasterInterface extends Remote {
	public void commitTansaction(long txnID) throws RemoteException, IOException;
	public void abortTransaction(long txnID) throws RemoteException;
	public void initiateReplicaServerObject(String ip) throws RemoteException, NotBoundException;
}
