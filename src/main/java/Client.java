import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;



public class Client
{
	Socket requestSocket;           //socket connect to the server
	ObjectOutputStream out;         //stream write to the socket
 	ObjectInputStream in;          //stream read from the socket
	String message;                //message send to the server
	String MESSAGE;                //capitalized message read from the server

	public void startConnection(String ip, int port) throws IOException
	{
		try
		{
			requestSocket = new Socket(ip, port);
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			boolean handshakeComplete = false;
			String handshakeMsg = "";
			try
			{
				sendHandshake();
				while (!handshakeComplete)
				{
					handshakeMsg = (String) in.readObject();

					System.out.println(handshakeMsg);

					if (!handshakeMsg.isEmpty())
					{
						handshakeComplete = Objects.equals(handshakeMsg.substring(0, 28), "P2PFILESHARINGPROJ0000000000");
					}
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			} catch (ClassNotFoundException e)
			{
				throw new RuntimeException(e);
			}
		}
		catch (ConnectException e)
		{
			System.err.println("Connection refused. You need to initiate a server first.");
		}
		catch(UnknownHostException unknownHost)
		{
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally
		{
			try
			{
				in.close();
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException)
			{
				ioException.printStackTrace();
			}
		}
	}

	public void Client() throws Exception
	{
		//get info from peer cfg files
		Dictionary<String, String[]> peerInfo = new Hashtable<>();
		loadcfg getcfg = new loadcfg();
		peerInfo = getcfg.readcfgfile(".\\src\\main\\java\\project_config_file_large\\PeerInfo.cfg");
		//add peers to peer list
		peerslist peerobject = new peerslist();
		//create new peers list
		ArrayList<peerslist.peer> peerlistarray = new ArrayList<peerslist.peer>(0);
		for(int i =0; i < peerInfo.size(); i++)
		{
			System.out.println(Arrays.toString(peerInfo.get(i)));
			peerobject.addpeer(peerInfo.get(i));
		}
		peerlistarray = peerobject.getpeerslist();
		//for(int i =0; i < peerlistarray.size(); i++)
		//{
			//open a socket
			try
			{
				//requestSocket = new Socket(peerlistarray.get(i).getpeerhostname(), peerlistarray.get(i).getpeerlisteningport());
				requestSocket = new Socket("localhost", 8000);
			}
			catch(Exception e)
			{
				System.out.println("could not create tcp socket on port: " + 8000);
			}
		//}
	}

	void run()
	{
		try
		{
			requestSocket = new Socket("localhost", 8000);
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			boolean handshakeComplete = false;
			String handshakeMsg = "";
			try
			{
				sendHandshake();
				while (!handshakeComplete)
				{
					handshakeMsg = (String) in.readObject();

					System.out.println(handshakeMsg);

					if (!handshakeMsg.isEmpty())
					{
						handshakeComplete = Objects.equals(handshakeMsg.substring(0, 28), "P2PFILESHARINGPROJ0000000000");
					}
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			} catch (ClassNotFoundException e)
			{
				throw new RuntimeException(e);
			}
		}
		catch (ConnectException e)
		{
			System.err.println("Connection refused. You need to initiate a server first.");
		}
		catch(UnknownHostException unknownHost)
		{
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally
		{
			try
			{
				in.close();
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException)
			{
				ioException.printStackTrace();
			}
		}
	}

	void sendExampleMessage() throws IOException
	{
		// Creating a message (i think this is the right way to do it)
		Message messageToSend = new Message(MessageType.REQUEST, "Requesting piece 5".getBytes());

		byte[] messageBytes = messageToSend.toByteArray();

		out.write(messageBytes);
		out.flush();

	}

    //to receive a message from the client on the server
    //use the fromByteArray() method
    //and then use the getMessage() method to get the message
    //and then use the getContent() method to get the content of the message

	void sendHandshake() throws IOException
	{
		String msg = "P2PFILESHARINGPROJ0000000000";
		out.writeObject(msg);
		out.flush();
	}

	//send a message to the output stream
	void sendMessage(Message message)
	{
		try
		{
			// Serialize and send the message
			out.writeObject(message);
			out.flush();
		} catch (IOException ioException)
		{
			ioException.printStackTrace();
		}
	}

	//main method
	public static void main(String args[]) throws Exception
	{
		Client client = new Client();
		//client.Client();
		client.run();
	}

}
