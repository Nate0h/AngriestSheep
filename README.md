# AngriestSheep
A Sheepdog AI that has to track down a sheep and lead its pen, in a 51x51 grid environment. The goal is surrounded on three sides by blocked cells. And the sheep is really angry! The sheep moves randomly unless the sheepdog bot is in the view of the sheep, that is when the sheep moves closer to the sheepdog bot at every step to ultimately destroy the sheepdog. The aim of this simulation is to avoid the sheep destroying the sheepdog and to safely bring the sheep it its pen.

Utilized Bellman Equation based Policy Iteration to determine what is the best step for sheepdog bot to make in any state of the grid (state taking into account both the positions of the sheep and the sheepdog) to get the sheep into the pen.
