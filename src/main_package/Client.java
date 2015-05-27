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
import rmi_interface_package.MessageNotFoundException;
import rmi_interface_package.ReplicaServerInterface;

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
				try {
					writDataToRemoteFile(filePath, in);
				} catch (MessageNotFoundException e) {
					e.printStackTrace();
				}
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
		return (MasterServerInterface) registry.lookup(Constants.RMI_MASTER_NAME);
	}

	private static void writDataToRemoteFile(String filePath, Scanner in)
			throws NotBoundException, FileNotFoundException, IOException, MessageNotFoundException {
		String[] temp = dfsMaster.newTxn(filePath).split(",");
		long txnID = Long.parseLong(temp[0]);
		String replicaIp = temp[1];
		
		System.out.println("Replica ip "+ replicaIp);
		
		System.setProperty("java.rmi.server.hostname", replicaIp);
		
		System.out.println("Registering...");
		Registry registry = LocateRegistry.getRegistry(replicaIp,
				Constants.RMI_REGISTRY_PORT);
		System.out.println("Registered.");
		
		ReplicaServerInterface replicaServer = (ReplicaServerInterface) registry
				.lookup(Constants.RMI_REPLICA_NAME);
		
		System.out.println("ReplicaServer Object initiated");
		
		// Enter pressed
		in.nextLine();
		
		// TODO(houssainy) add commit and abort requests
		int msgSeqNum = 0;
		int ack;
		String line;
		System.out.println("Enter your message line by line and end it by END_FILE:");
		while (!(line = in.nextLine()).equals("END_FILE")) {
			System.out.println("Sending msg "+ line);
			ack = replicaServer.write(txnID, msgSeqNum++, line);
			System.out.println("Acknowlgdment Received " + ack);
		}
		replicaServer.commit(txnID, msgSeqNum);
	}

	private static void readRemoteFile(String filePath)
			throws NotBoundException, FileNotFoundException, IOException {
		String replicaIp = dfsMaster.read(filePath);
		System.out.println("Primary Replica ip " + replicaIp);
		
		System.setProperty("java.rmi.server.hostname", replicaIp);

		Registry registry = LocateRegistry.getRegistry(replicaIp,
				Constants.RMI_REGISTRY_PORT);
		ReplicaServerInterface replicaServer = (ReplicaServerInterface) registry
				.lookup(Constants.RMI_REPLICA_NAME);

		System.out.println(replicaServer.read(filePath));
	}
}
