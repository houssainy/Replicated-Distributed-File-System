package main_package;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;

import rmi_interface_package.MasterServerInterface;

public class DFSMaster implements MasterServerInterface{

	@Override
	public String read(String fileName) throws FileNotFoundException,
			IOException, RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String newTxn(String fileName) throws RemoteException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
