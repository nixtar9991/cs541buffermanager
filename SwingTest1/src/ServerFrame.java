import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.*;

public class ServerFrame extends JFrame implements IUserInteractionHandler, ActionListener{
	JPanel _pnlPeerDetails = null;
	JPanel _pnlStatus = null;
	JLabel _lblPeerName = null;
	JTextField _tfPeerName = null; // RMI-registry name of the peer. 
	JLabel _lblPeerHostName = null;
	JTextField _tfPeerHostName = null; // IP-hostname of the peer.
	JLabel _lblPeerPortNumber = null;
	JTextField _tfPeerPortNumber = null;
	JButton _btnAddPeer = null;
	JScrollPane _scpStatus = null;
	JTextArea _txtStatus = null;
	JButton _btnAccountDetails = null;
	
	// Set default size parameters.
	int _windowWidth = 800;
	int _windowHeight = 400;
	
	// Event listeners.
	IPeerEventListener _peerEventListener = null;
	
	public ServerFrame(String name){
		super(name);
		setSize(_windowWidth, _windowHeight);
		
		// Create panel.
		_pnlPeerDetails = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		// Create labels, buttons, textfields.
		_lblPeerName = new JLabel("Peer name:");
		_tfPeerName = new JTextField(15);
		_lblPeerHostName = new JLabel("Peer hostname:");
		_tfPeerHostName = new JTextField(15);
		_lblPeerPortNumber = new JLabel("Peer port:");
		_tfPeerPortNumber = new JTextField(10);
		_btnAddPeer = new JButton("Add Peer");
		_btnAccountDetails = new JButton("Print Accounts");
		
		_pnlPeerDetails.add(_lblPeerName);
		_pnlPeerDetails.add(_tfPeerName);
		_pnlPeerDetails.add(_lblPeerHostName);
		_pnlPeerDetails.add(_tfPeerHostName);
		_pnlPeerDetails.add(_lblPeerPortNumber);
		_pnlPeerDetails.add(_tfPeerPortNumber);
		_pnlPeerDetails.add(_btnAddPeer);
		_pnlPeerDetails.add(_btnAccountDetails);
		
		getContentPane().add(_pnlPeerDetails, BorderLayout.NORTH);
		_pnlPeerDetails.setPreferredSize(new Dimension(_windowWidth, 50));
		
		// Add status text area.
		_pnlStatus = new JPanel();
		_txtStatus = new JTextArea("");
		_scpStatus = new JScrollPane(_txtStatus);
		_scpStatus.setPreferredSize(new Dimension(_windowWidth - 10, _windowHeight - _pnlPeerDetails.getPreferredSize().height - 50));
		
		_pnlStatus.add(_scpStatus);		
		getContentPane().add(_pnlStatus);
		
		// Add window listener.
		addWindowListener(new MyWindowAdapter());
		_btnAddPeer.addActionListener(this);
		_btnAccountDetails.addActionListener(this);
		
		setVisible(true);
	}
	
	class MyWindowAdapter extends WindowAdapter{
		public void windowClosing(WindowEvent event){
			System.exit(0);
		}
	}
	
	@Override
	public void appendLog(String logString) {
		// Add logString to status display. Newline appended automatically.
		_txtStatus.append(logString + "\n");
	}

	@Override
	public void addPeerEventListener(IPeerEventListener peerEventListener) {
		// TODO Auto-generated method stub
		_peerEventListener = peerEventListener;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals(_btnAddPeer.getActionCommand())){
			try{
				// Perform basic validation.
				String peerHostname = _tfPeerHostName.getText().trim();
				String peerName = _tfPeerName.getText().trim();
				int peerPortNumber = Integer.parseInt(_tfPeerPortNumber.getText().trim());
			
				if(!peerHostname.equals("") && !peerName.equals("") && 
						(peerPortNumber > 0 && peerPortNumber < 65536)){
					// Attempt to send this event down.
					PeerEvent peerEvent = new PeerEvent(PeerEvent.PEER_ADDED, peerName, peerHostname, peerPortNumber);
					_peerEventListener.peerAdded(peerEvent);
				}
			}catch(Exception exception){
				// Peer addition failed.
				appendLog("Unable to add peer " + _tfPeerHostName.getText().trim() + ".");
			}
			
			// Clear text box contents.
			_tfPeerHostName.setText("");
			_tfPeerName.setText("");
			_tfPeerPortNumber.setText("");
		}else if(e.getActionCommand().equals("Print Accounts")){
			int peerID = (Integer)GlobalState.get("localPeerID");
			Peer peer;
			
			try {
				peer = PeerIDKeyedMap.getPeer(peerID);
				Registry registry = LocateRegistry.getRegistry(peer.getPeerHostname(), peer.getPeerPortNumber());
				ITransactionManager transactionManagerRemoteObj = (ITransactionManager)registry.lookup(peer.getPeerName() + "_TransactionManager");
				
				Hashtable<Integer, Double> htAccountDetails = transactionManagerRemoteObj.getAccountDetails(peerID); 
				Enumeration<Integer> enumAccountID = htAccountDetails.keys();
				
				appendLog("Account details:");
				
				while(enumAccountID.hasMoreElements()){
					int accountID =  enumAccountID.nextElement();
					
					appendLog("Acc: " + accountID + " ; Balance: " + htAccountDetails.get(accountID));
				}				
			} catch (Exception e1) {
				appendLog("No account information available.");
			}			
		}
	}
	
}
