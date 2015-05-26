package main_package;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import rmi_interface_package.MasterServerInterface;

public class Client {
	private final static int READ = 1;
	private final static int WRITE = 2;
	private final static int CLOSE = 3;

	private static MasterServerInterface dfsMaster;

	public static void main(String[] args) throws NotBoundException,
			IOException {
		Scanner fileReader = new Scanner(new File("conf/master_ip"));
		String masterIp = fileReader.next();
		fileReader.close();

		Scanner in = new Scanner(System.in);
		String filePath;

		dfsMaster = getMaster(masterIp);
		System.out.println("------------");
		System.out.println("Master Object initiated...");
		boolean running = true;
		do {
			System.out.println("Enter your command:\n 1- Read.\n 2- Write.\n 3- Close.");
			switch (in.nextInt()) {
			case READ:
				System.out.println("Enter File Path:");
				filePath = in.next();
				readRemoteFile(filePath);
				break;
			case WRITE:
				System.out.println("Enter File Path:");
				filePath = in.next();
				writDataToRemoteFile(filePath, in);
				break;
			case CLOSE:
				running = false;
				break;
			default:
				System.out.println("Invalid Input.");
			}
		} while (running);
		in.close();
	}

	private static MasterServerInterface getMaster(String masterIp) throws RemoteException,
			NotBoundException {
		System.out.println("Getting Master with ip " + masterIp);
		System.setProperty("java.rmi.server.hostname", masterIp);

		Registry registry = LocateRegistry.getRegistry(masterIp,
				Constants.RMI_REGISTRY_PORT);
		System.out.println("Server Registered");
		return (MasterServerInterface) registry.lookup(Constants.RMI_NAME);
	}

	private static void writDataToRemoteFile(String filePath, Scanner in)
			throws NotBoundException, FileNotFoundException, IOException {
		String[] temp = dfsMaster.newTxn(filePath).split(",");
		String replicaIp = temp[0];
		long txnID = Long.parseLong(temp[1]);

		System.setProperty("java.rmi.server.hostname", replicaIp);

		Registry registry = LocateRegistry.getRegistry(replicaIp,
				Constants.RMI_REGISTRY_PORT);

		ReplicaServer replicaServer = (ReplicaServer) registry
				.lookup(Constants.RMI_NAME);

		// TODO(houssainy) add commit and abort requests
		int msgSeqNum = 0;
		int ack;
		String line;
		while ((line = in.nextLine()) != null) {
			ack = replicaServer.write(txnID, msgSeqNum++, line);
			System.out.println("Acknowlgdment Received " + ack);
		}
	}

	private static void readRemoteFile(String filePath)
			throws NotBoundException, FileNotFoundException, IOException {
		String replicaIp = dfsMaster.read(filePath);
		System.out.println("Primary Replica ip " + replicaIp);
		
		System.setProperty("java.rmi.server.hostname", replicaIp);

		Registry registry = LocateRegistry.getRegistry(replicaIp,
				Constants.RMI_REGISTRY_PORT);
		ReplicaServer replicaServer = (ReplicaServer) registry
				.lookup(Constants.RMI_NAME);

		System.out.println(replicaServer.read(filePath));
	}
}
