# 5G network optimisation

Energy efficient virtual function placement in 5g networks

## Description

> open `instance-definition.txt` to see how the input file should be structured
> see `instance.txt` and `instance_small.txt` for examples

### Problem

Network is configured with nodes (routers) which are connected via links.
Each server is connected to only one node.

The main problem is to place virtual functions onto servers and find a routing between them with respect to constraints so that used power is minimal.

### Constraints

Server has limited amount of resources (processing power, memory). Every component has certain demands for resources. Because of that, we can't place a component on a server if it doesn't have enough needed resources.

Communication that goes over a link has a certain delay. Every link has its own bandwidth.


### Power usage

Each used node uses constant amount of memory. The same goes for links.
Server uses power proportional to the amount of used resources.

### Service chains

Service chain is a chain of components which communicate with each other. For example, in `1->2->5` chain, `1` communicates with `2`, which communicates with `5`.
Each service chain has maximal allowed delay (sum of delays between components in a chain can't be greater than this).

### Bandwidth demands

Each pair of communicating components require a certain amount of bandwidth.

## Implementation

My implementation is separated into two parts:
1. placing components onto servers
2. finding routes between each pair of components

### Finding valid placement

##### RandomPlacer
Places components in a random order onto server until it finds a valid placement.
##### GreedyPlacer
Places each component on a server for which additional used power would be minimal.

### Finding route between components
Each router goes through pairs of components and finds the _best_ route for that pair. 

##### GreedyRouter
Try to use a link for which additional used power would be minimal until you get to the destination node.
##### AntColonyRouter
Use _Ant Colony Optimization (ACO)_ to find the _best_ route from component A to component B.

### Tying it together
Used _tabu search_ to search neighbor solutions from a given initial solutions.

