# Interactive heat map app in Scala

Functional Programming in Scala Capstone

This application shows interactive visualization of climate data. The app allows to trace the evolution of temperatures over time and temperature deviations over time in all over the world.


The data you will see comes from the National Center for Environmental Information of the United States. It covers years from 1975 to 2015 and contains observation from 29444 weather stations from all over the world.

## Technical details:

The inverse distance weighting algorithm (https://en.wikipedia.org/wiki/Inverse_distance_weighting) is used for spatial interpolation of temperatures.

The great-circle distance formula (https://en.wikipedia.org/wiki/Great-circle_distance) is used to approximate the distance between two stations.

Colors are smoothed by applying the linear interpolation algorithm (https://en.wikipedia.org/wiki/Linear_interpolation).

The map is broken down into tiles using the Web Mercator projection (http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames).

In order to make computation of tiles faster, scattered data spatially interpolated into a regular grid.

For generating tiles with temperatures' deviations the bilinear interpolation (https://en.wikipedia.org/wiki/Bilinear_interpolation) is used.
