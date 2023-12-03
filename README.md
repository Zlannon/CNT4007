Project Overview:
-
This project is a peer to peer file sharing software similar to BitTorrent written in java. It uses a TCP protocol with 9 different message types.
The message types are handshake, choke, unchoke, interested, not interested, have, bitfield, request, and piece.

Project Playbook:
-
A) Group Number
  - Group 4

B) Team Member Names and UF Emails
- Steven Miller: millersteven@ufl.edu
- Zachary Lannon: zlannon@ufl.edu
- Esfar Mohammad: immam.m@ufl.edu

C) Contributions of each team member
 - This project was made together over several meetings using discord and code with me.

D) Youtube Video Link:
 - https://youtu.be/K8-Ejcx-2K4

E) What you were able to achieve and what you were not
 - Our group was able to fully complete the project

F) Running the project:
  - unzip the project (unzip projectGroup4.zip)
  - enter the project directory (cd projectGroup4)
  - Update the PeerInfo.cfg and Common.cfg for your file configuration
  - Ensure all peers labeled with a '1' in the PeerInfo.cfg have the file in their respective folders, if the folder does not exist create it with mkdir and place the file in it
  - Run javac peerProcess.java to compile the code
  - Run java peerProcess <peer id> on the all machines in ascending order (1001, 1002, 1003, 1004, 1005, 1006)
