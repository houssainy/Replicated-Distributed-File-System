package main_package;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class RMIReplicaServer {
	public static void main(String[] args) throws AccessException,
			RemoteException {
		System.setProperty("java.rmi.server.hostname", "localhost");

		String name = "DfsMaster";
		ReplicaServer masterServr = new ReplicaServer();

		LocateRegistry.createRegistry(Constants.RMI_REGISTRY_PORT).rebind(name,
				masterServr);

		System.out.println("ReplicaServer Registred to Registry Server...");
	}
}
