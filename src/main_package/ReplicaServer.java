package main_package;

import java.io.IOException;
import java.rmi.RemoteException;

import rmi_interface_package.MessageNotFoundException;
import rmi_interface_package.ReplicaServerInterface;

public class ReplicaServer implements ReplicaServerInterface{

	@Override
	public String read(String fileName) throws IOException, RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int write(long txnID, long msgSeqNum, String data)
			throws RemoteException, IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int commit(long txnID, long numOfMsgs)
			throws MessageNotFoundException, RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int abort(long txnID) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

}
