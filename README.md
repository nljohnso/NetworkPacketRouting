NetworkPacketRouting
====================
Using Dijkstra’s Shortest Paths to Route Packets in a Network Overlay 
 
The overlay will contain at least 10 messaging nodes, and each messaging node will be connected to N 
(default of 4) other messaging nodes. Each link that connects two messaging nodes within the overlay 
has a weight associated with it. Links are bidirectional i.e. if messaging node A established a 
connection to messaging node B, then messaging node B must use that link to communicate with A. 

Fix: (1) Deregister node while packet routing is in process.

(2) Allow for overlay creations other than 10 nodes and 4 connections per node.

(3) Fix statistics that are printed out.
