package main_package;

import java.util.Scanner;

import ssh.Jssh;

public class Administrator {
	
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
	
		String input = in.nextLine();
		in.close();
		
		String[] temp = input.split(" ");
		
		/**
		 * Expected input:
		 * server -ip [ip address string] -port [port number] -dir <directory path>
		 */
		String masterIp = temp[1];
		int port = Integer.parseInt(temp[2]);
		String dir = temp[3];
		
		// TODO
//		Jssh s = new Jssh();
//		s.doCommand("houssainy", masterIp, port, "mohamed1992", "ls");
	}
}
