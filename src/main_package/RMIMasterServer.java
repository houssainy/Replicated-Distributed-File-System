package main_package;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;

import utilities_package.Constants;

public class RMIMasterServer {
	public static void main(String[] args) throws IOException {
		System.setProperty("java.rmi.server.hostname", "localhost");

		DfsMaster masterServr = new DfsMaster();

		LocateRegistry.createRegistry(Constants.RMI_REGISTRY_PORT).rebind(Constants.RMI_NAME,
				masterServr);

		System.out.println("DfsMaster Registred to Registry Server...");
	}
}
