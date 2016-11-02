# SpaceSim

SpaceSim simulates gravity for planets and suns, with a full suite of features to explore the physics behind gravity.

The simulation begins with a sun at the origin, and more planets are added by the user. Planets are spawned at a random position and with a random velocity, mass, radius, and name. Planets are shown as dots on the screen. To track the path of the planets, either lines or shadows can be used. All bodies exist in three dimensions, so to indicate position into the screen the planet appears larger or smaller. 

Planets with custom parameters can also be added, and while paused the parameters of selected bodies can be changed. To select a planet or a sun, click on it in the simulation.

The force law governing gravity can also be changed. It can be shown that only the inverse square and spring (linear) force laws can result in stable orbits, and this can be seen in the simulation. 


The jar file can be found in dist/SpaceSim.jar. Java JRE 1.8 is required, and it has been tested with 1.8.0_72 on Linux Mint 17.3 The code is developed with Java JDK 1.8.

