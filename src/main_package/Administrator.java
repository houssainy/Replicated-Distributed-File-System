package main_package;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import ssh.Jssh;

public class Administrator {
	private final static String USER_NAME = "houssainy";

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.out.println("Project Path is Missing!");
			return;
		}

		Scanner in = new Scanner(System.in);

		String input = in.nextLine();
		in.close();

		String[] temp = input.split(" ");

		/**
		 * Expected input: server -ip [ip address string] -port [port number]
		 * -dir <directory path>
		 */
		String masterIp = temp[1];
		int port = Integer.parseInt(temp[2]);
		String dir = temp[3];

		// Start Master Server
		String command = "cd " + args[0]
				+ " && javac main_package/DFSMaster.java"
				+ " && java main_package.DFSMaster";
		Jssh ssh = new Jssh();
		ssh.doCommand(USER_NAME, masterIp, port, "mohamed1992", command);

		// Start Replica Servers
		command = "cd " + args[0] + " && javac main_package/ReplicaServer.java"
				+ " && java main_package.ReplicaServer";

		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File("conf/replica_servers"))));
		String line;
		while ((line = br.readLine()) != null) {
			temp = line.split(" ");

			if (!temp[0].equals(masterIp))
				ssh.doCommand(USER_NAME, temp[0], Integer.parseInt(temp[1]),
						temp[2], command);
		}
	}
}
