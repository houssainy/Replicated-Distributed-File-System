package main_package;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MasterToPrimaryInterface extends Remote {
	public void commitTansaction(long txnID) throws RemoteException;
	public void abortTransaction(long txnID) throws RemoteException;
}
