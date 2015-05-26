package main_package;

import java.io.File;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

public class RMIMasterServer {
	public static void main(String[] args) throws IOException {
		Scanner fileReader = new Scanner(new File("conf/master_ip"));
		String masterIp = fileReader.next();
		fileReader.close();

		System.out.println("Master Ip = " + masterIp);
		System.setProperty("java.rmi.server.hostname", masterIp);

		DfsMaster masterServr = new DfsMaster();

		LocateRegistry.createRegistry(Constants.RMI_REGISTRY_PORT).rebind(
				Constants.RMI_NAME, masterServr);

		System.out.println("DfsMaster Registred to Registry Server...");
	}
}
