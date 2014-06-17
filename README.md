NetworkPacketRouting
====================
Using Dijkstra’s Shortest Paths to Route Packets in a Network Overlay 
VERSION 0.8 
 
DUE DATE: Wednesday February 19th, 2014 @ 5:00 pm 
 
The objective of this assignment is to get you familiar with coding in a distributed setting where you 
need to manage the underlying communications between nodes. Upon completion of this assignment 
you will have a set of reusable classes that you will be able to draw upon. As part of this assignment 
you will be: (1) constructing a logical overlay over a distributed set of nodes, and then (2) computing 
shortest paths using Dijkstra’s algorithm to route packets in the system. 
 
The overlay will contain at least 10 messaging nodes, and each messaging node will be connected to N 
(default of 4) other messaging nodes. Each link that connects two messaging nodes within the overlay 
has a weight associated with it. Links are bidirectional i.e. if messaging node A established a 
connection to messaging node B, then messaging node B must use that link to communicate with A. 
 
Once the overlay has been setup, messaging nodes in the system will select a node at random and 
send that node (also known as the sink node) a message. Rather than send this message directly to 
the sink node, the source node will use the overlay for communications. This is done by computing 
the shortest route (based on the weights assigned during overlay construction) between the source 
node and the sink node. Depending on the overlay and link weights, there may be zero or more 
intermediate messaging nodes that packets between a particular source and sink must pass through. 
Such intermediate nodes are said to relay the message and they do not target either the receipt or 
forwarding of such messages. The assignment requires you to verify correctness of packet 
exchanges between the source and sinks by ensuring that: (1) the number of messages that you send 
and receive within the system match, and (2) these messages have been not corrupted in transit to 
the intended recipient. Message exchanges and connection setups/terminations happen continuously 
in the system. 
 
All communications in this assignment are based on TCP. The assignment must be implemented in 
Java and you cannot use any external jar files. You must develop all functionality yourself. This 
assignment may be modified to clarify any questions (and the version number incremented), but the 
crux of the assignment and the distribution of points will not change. 
 
1 Components 
There are two components that you will be building as part of this assignment: a registry and a 
messaging node. 
 
1.1 Registry: 
There is exactly one registry in the system. The registry provides the following functions: 
A. Allows messaging nodes to register themselves. This is performed when a messaging node 
starts up for the first time. 
B. Allows messaging nodes to deregister themselves. This is performed when a messaging node 
leaves the overlay. 
C. Enables the construction of the overlay by orchestrating connections that a messaging node 
initiates with other messaging nodes in the system. Based on its knowledge of the messaging 
nodes (through function A) the registry informs messaging nodes about the other messaging 
nodes that they should connect to. 
D. Assign and publish weights to the links connecting any two messaging nodes in the overlay. 
The weights these links take will range from 1-10. 
 CS 455: INTRODUCTION TO DISTRIBUTED SYSTEMS 
Department of Computer Science 
Colorado State University 
SPRING 2014 
URL: http://www.cs.colostate.edu/~cs455 
Instructor: Shrideep Pallickara 
 
Page 2 of 11 
The registry maintains information about the registered messaging nodes in a registry; you can use 
any data structure for managing this registry but make sure that your choice can support all the 
operations that you will need. 
 
The registry does not play any role in the routing of data within the overlay. Interactions between the 
messaging nodes and the registry are via request-response messages. For each request that it 
receives from the messaging nodes, the registry will send a response back to the messaging node 
(based on the IP address associated with Socket’s input stream) where the request originated. The 
contents of this response depend on the type of the request and the outcome of processing this 
request. 
 
1.2 The Messaging node 
Unlike the registry, there are multiple messaging nodes (minimum of 10) in the system. A messaging 
node provides two closely related functions: it initiates and accepts both communications and 
messages within the system. 
 
Communications that nodes have with each other are based on TCP. Each messaging node needs to 
automatically configure the ports over which it listens for communications i.e. the port numbers 
should not be hard-coded or specified at the command line. TCPServerSocket is used to accept 
incoming TCP communications. 
 
Once the initialization is complete, the node should send a registration request to the registry. 
 
 
2 Interactions between the components 
This section will describe the interactions between the registry and the messaging nodes. This section 
includes the prescribed wire-formats. You have freedom to construct your wire-formats but you must 
include the fields that have been specified. A good programming practice is to have a separate class 
for each message type so that you can isolate faults better. The Message Types that have been 
specified could be part of an interface, say cs455.overlay.wireformats.Protocol and have values 
specified there. This way you are not hard-coding values in different portions of your code. 
 
Use of Java serialization is not allowed. Your classes for the message types should not implement the 
java.io.Serializable interface. 
 
2.1 Registration: 
Upon starting up, each messaging node should register its IP address, and port number with the 
registry. It should be possible for your system to register messaging nodes that are running on the 
same host but are listening to communications on different ports. There should be 4 fields in this 
registration request: 
 
Message Type (int): REGISTER_REQUEST 
IP address (String) 
Port number (int) 
 
When a registry receives this request, it checks to see if the node had previously registered and 
ensures the IP address in the message matches the address where the request originated. The 
registry issues an error message under two circumstances: 
 If the node had previously registered and has a valid entry in its registry. 
 If there is a mismatch in the address that is specified in the registration request and the IP 
address of the request (the socket’s input stream). 
 
The contents of the response message are depicted below. The success or failure of the registration 
request should be indicated in the status field of the response message. 
 CS 455: INTRODUCTION TO DISTRIBUTED SYSTEMS 
Department of Computer Science 
Colorado State University 
SPRING 2014 
URL: http://www.cs.colostate.edu/~cs455 
Instructor: Shrideep Pallickara 
 
Page 3 of 11 
Message Type (int): REGISTER_RESPONSE 
Status Code (byte): SUCCESS or FAILURE 
Additional Info (String): 
 
In the case of successful registration, the registry should include a message that indicates the number 
of entries currently present in its registry. A sample information string is “Registration request 
successful. The number of messaging nodes currently constituting the overlay is (5)”. If 
the registration was unsuccessful, the message from the registry should indicate why the request was 
unsuccessful. 
 
NOTE: In the rare case that a messaging node fails just after it sends a registration request, the 
registry will not be able to communicate with it. In this case, the entry for the messaging node should 
be removed from the messaging node-registry maintained at the registry. 
 
 
2.2 Deregistration 
When a messaging node exits it should deregister itself. It does so by sending a message to the 
registry. This deregistration request includes the following fields 
 
Message Type: DEREGISTER_REQUEST 
Node IP address: 
Node Port number: 
 
 
The registry should check to see that request is a valid one by checking (1) where the message 
originated and (2) whether this node was previously registered. Error messages should be returned in 
case of a mismatch in the addresses or if the messaging node is not registered with the overlay. You 
should be able to test the error-reporting functionality by de-registering the same messaging node 
twice. 
 
 
2.3 Peer messaging nodes list 
 
Once the setup-overlay command (see section 3) is specified at the registry it must perform a series 
of actions that lead to the creation of the overlay via messaging nodes initiating connections with each 
other. Messaging nodes await instructions from the registry regarding the other messaging nodes that 
they must establish connections to. 
 
The registry must ensure two properties. First, it must ensure that the number of links to/from (the 
links are bidirectional) every messaging node in the overlay is identical; this is configurable metric 
(with a default value of 4) and is specified as part of the setup-overlay command. Second, the 
registry must ensure that there is no partition within the overlay i.e. it should be possible to reach any 
messaging node from any other messaging node in the overlay. 
 
If the connection requirement for the overlay is CR, each messaging node will have CR links to other 
messaging nodes in the overlay. The registry selects these CR messaging nodes that constitute the 
peer-messaging nodes list for a messaging node randomly. However, a check should be performed to 
ensure that the peer-messaging nodes list for a messaging node does not include the targeted 
messaging node i.e. a messaging node should not have to connect to itself. The registry keeps track of 
the connections that are being created; for example, if messaging node A is asked to connect to 
messaging node B, the connection counts for both A and B are incremented. The registry must ensure 
that connection counts are met and not breached. 
 
The registry sends a different list of messaging nodes to each messaging node in the overlay. 
Depending on the connections that were previously set up, the number of peer messaging nodes 
included in messages to different messaging nodes may vary from CR through 1. If a messaging 
node’s connection limit was reached through previous messages sent to other messaging nodes in the CS 455: INTRODUCTION TO DISTRIBUTED SYSTEMS 
Department of Computer Science 
Colorado State University 
SPRING 2014 
URL: http://www.cs.colostate.edu/~cs455 
Instructor: Shrideep Pallickara 
 
Page 4 of 11 
overlay, no message needs to be sent to that messaging node. The peer-list message will have the 
following format 
 
Message Type: MESSAGING_NODES_LIST 
Number of peer messaging nodes: X 
Messaging node1 Info 
Messaging node2 Info 
….. 
Messaging nodeX Info 
 
The information corresponding to a messaging node includes the following: messaging 
node_hostname:portnum. Upon receiving the MESSAGING_NODES_LIST message a messaging node 
should initiate connections to the specified messaging nodes. 
 
2.4 Assign overlay link weights 
The registry is also responsible for assigning weights to connections in the overlay. The weight for 
each link is an integer between 1-10 and is randomly computed by the registry. This information will 
be encoded in the message as follows. 
 
Message Type: Link_Weights 
Number of links: L 
Linkinfo1 
Linkinfo2 
... 
LinkinfoL 
 
A Linkinfo connecting messaging nodes A and B contains the following fields: hostnameA:portnumA 
hostnameB:portnumB weight 
 
 
2.5 Initiate sending messages 
The registry informs nodes in the overlay when they should start sending messages to each other. It 
does so via the TASK_INITIATE control message. 
 
 
Message Type: TASK_INITIATE 
 
 
2.6 Send message 
Data can be fed into the network from any messaging node within the network. Packets are sent from 
a source to a sink; it is possible that there might be zero or more intermediate nodes in the system 
that relay messages en route to the sink. Every node tracks the number of messages that it has 
relayed during communications within the overlay. 
 
When a packet is ready to be sent from a source node to the sink node, the source node computes the 
shortest path to the sink node using Dijkstra’s shortest path algorithm. This path is then used as a 
routing plan that will be included in the packet. The routing plan indicates how the packet must be 
routed; for example, A may have a direct connection to B, but depending on the link weights, the 
routing plan may call for the packet to sent as A  C  E  D  B. 
 
A key requirements for the dissemination of packets within the overlay is that no messaging node 
should receive the same packet more than once. This should be achieved without having to rely on 
duplicate detection and suppression. 
 
 
